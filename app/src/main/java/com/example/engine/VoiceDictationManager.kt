package com.example.engine

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface VoiceDictationState {
    data object Idle : VoiceDictationState
    data object Listening : VoiceDictationState
    data class Partial(val text: String) : VoiceDictationState
    data class Error(val message: String) : VoiceDictationState
}

/**
 * Manages real Android SpeechRecognizer for the Soft Microphone feature.
 * Zero background battery drain: recognizer is initialized lazily and destroyed on stop.
 */
class VoiceDictationManager(private val context: Context) {

    private val _state = MutableStateFlow<VoiceDictationState>(VoiceDictationState.Idle)
    val state: StateFlow<VoiceDictationState> = _state.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var onFinalTextCallback: ((String) -> Unit)? = null

    fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(
        localeTag: String = "en-US",
        onFinalResult: (String) -> Unit
    ) {
        if (!hasMicPermission()) {
            _state.value = VoiceDictationState.Error("Microphone permission required")
            return
        }
        if (!isRecognitionAvailable()) {
            _state.value = VoiceDictationState.Error("Speech service unavailable on this device")
            return
        }

        onFinalTextCallback = onFinalResult
        stopInternal(resetState = false)

        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            speechRecognizer = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = VoiceDictationState.Listening
                }

                override fun onBeginningOfSpeech() {
                    _state.value = VoiceDictationState.Listening
                }

                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                    _rmsLevel.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _rmsLevel.value = 0f
                }

                override fun onError(error: Int) {
                    _rmsLevel.value = 0f
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mic permission denied"
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                            "Network needed for online speech"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected, try again"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
                        else -> "Speech recognizer paused"
                    }
                    _state.value = VoiceDictationState.Error(msg)
                    destroyRecognizer()
                }

                override fun onResults(results: Bundle?) {
                    _rmsLevel.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()?.trim().orEmpty()
                    if (spoken.isNotEmpty()) {
                        onFinalTextCallback?.invoke("$spoken ")
                    }
                    _state.value = VoiceDictationState.Idle
                    destroyRecognizer()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull()?.trim().orEmpty()
                    if (partial.isNotEmpty()) {
                        _state.value = VoiceDictationState.Partial(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            _state.value = VoiceDictationState.Listening
            recognizer.startListening(intent)
        } catch (t: Throwable) {
            _state.value = VoiceDictationState.Error(t.localizedMessage ?: "Voice mic error")
            destroyRecognizer()
        }
    }

    fun stopListening() {
        stopInternal(resetState = true)
    }

    fun clearError() {
        if (_state.value is VoiceDictationState.Error) {
            _state.value = VoiceDictationState.Idle
        }
    }

    private fun stopInternal(resetState: Boolean) {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Throwable) {
        }
        destroyRecognizer()
        _rmsLevel.value = 0f
        if (resetState) {
            _state.value = VoiceDictationState.Idle
        }
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Throwable) {
        }
        speechRecognizer = null
    }
}
