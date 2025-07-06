package com.oxidesales.voskTranscriber;

import org.vosk.Recognizer;

import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicrophoneStreamProcessor {

    private final TargetDataLine microphone;

    private final Recognizer recognizer;

    private final TranscriptionRecognizerResultHandler transcriptionRecognizerResultHandler;

    private final AtomicBoolean isRecording;

    public MicrophoneStreamProcessor(
            Recognizer recognizer,
            TranscriptionRecognizerResultHandler transcriptionRecognizerResultHandler,
            TargetDataLine microphone,
            AtomicBoolean isRecording
    ) {
        this.recognizer = recognizer;
        this.transcriptionRecognizerResultHandler = transcriptionRecognizerResultHandler;
        this.microphone = microphone;
        this.isRecording = isRecording;
    }

    public void processAudioStream() {
        byte[] buffer = new byte[Transcriber.CHUNK_SIZE];

        while (isRecording.get() && microphone != null) {
            try {
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                if (bytesRead > 0) {
                    // Process audio chunk with Vosk
                    processAudioChunk(buffer, bytesRead);
                }

            } catch (Exception e) {
                if (isRecording.get()) {
                    System.err.println("❌ Error processing audio stream: " + e.getMessage());
                }
                break;
            }
        }
    }

    public void processAudioChunk(byte[] audioData, int length) {
        try {
            // Feed audio data to recognizer
            if (recognizer.acceptWaveForm(audioData, length)) {
                // Final result available
                String result = recognizer.getResult();
                transcriptionRecognizerResultHandler.handleFinalResult(result);
            } else {
                // Partial result available
                String partialResult = recognizer.getPartialResult();
                transcriptionRecognizerResultHandler.handlePartialResult(partialResult);
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing audio chunk: " + e.getMessage());
        }
    }
}
