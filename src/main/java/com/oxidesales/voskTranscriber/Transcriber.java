package com.oxidesales.voskTranscriber;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Transcriber {
    public static final int SAMPLE_RATE = 16000;
    public static final int SAMPLE_SIZE_BITS = 16;
    public static final int CHANNELS = 1;
    public static final int CHUNK_SIZE = 4096;
    public static final boolean BIG_ENDIAN = false;

    // Processing configuration
    public static final int BUFFER_SIZE = 4096;

    // Vosk components
    private final Model voskModel;
    private final Recognizer recognizer;

    // Audio components
    private TargetDataLine microphone;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final ExecutorService executorService;

    private TranscriptionRecognizerResultHandler transcriptionRecognizerResultHandler;

    public Transcriber(String modelPath) {
        try {
            // Set Vosk log level (optional)
            LibVosk.setLogLevel(LogLevel.INFO);

            // Verify model directory exists
            Path modelDir = Paths.get(modelPath);
            if (!Files.exists(modelDir) || !Files.isDirectory(modelDir)) {
                throw new RuntimeException("Vosk model directory not found: " + modelPath);
            }


            // Load Vosk model
            this.voskModel = new Model(modelPath);
            this.recognizer = new Recognizer(voskModel, SAMPLE_RATE);

            // Initialize thread pool
            this.executorService = Executors.newFixedThreadPool(2);
            ModelLanguageDetector languageDetector = new ModelLanguageDetector();
            String modelLanguage = languageDetector.getModelLanguage(modelPath);

            System.out.println("🎙️  Vosk Real-time Speech Transcriber");
            System.out.println("=====================================");
            System.out.println("Model: " + modelPath);
            System.out.println("Sample Rate: " + SAMPLE_RATE + " Hz");
            System.out.println("Language: " + modelLanguage);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Vosk transcriber", e);
        }
    }

    public void startRealtime() {
        try {
            // Set up audio format
            AudioFormat audioFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE,
                    SAMPLE_SIZE_BITS,
                    CHANNELS,
                    (SAMPLE_SIZE_BITS / 8) * CHANNELS,
                    SAMPLE_RATE,
                    BIG_ENDIAN
            );

            // Get microphone
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

            if (!AudioSystem.isLineSupported(info)) {
                throw new RuntimeException("Audio format not supported by system");
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(audioFormat, BUFFER_SIZE);
            microphone.start();

            System.out.println("\n🎤 Microphone started. Begin speaking...");
            System.out.println("📝 Real-time transcription:");
            System.out.println("⏹️  Press Ctrl+C to stop");
            System.out.println("----------------------------------------");

            isRecording.set(true);

            this.transcriptionRecognizerResultHandler = new TranscriptionRecognizerResultHandler();
            MicrophoneStreamProcessor microphoneStreamProcessor = new MicrophoneStreamProcessor(
                    this.recognizer,
                    this.transcriptionRecognizerResultHandler,
                    microphone,
                    isRecording
            );

            // Start audio processing thread
            executorService.submit(microphoneStreamProcessor::processAudioStream);

            // Wait for interruption
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        } catch (Exception e) {
            System.err.println("❌ Error starting real-time transcription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopRealtime() {
        System.out.println("\n🛑 Stopping transcription...");

        isRecording.set(false);

        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Get final result
        if (recognizer != null) {
            try {
                String finalResult = recognizer.getFinalResult();
                transcriptionRecognizerResultHandler.handleFinalResult(finalResult);
            } catch (Exception e) {
                // Ignore errors during shutdown
            }
        }

        System.out.println("\n📋 Complete transcription:");
        System.out.println("----------------------------------------");
        System.out.println(transcriptionRecognizerResultHandler.getCompleteTranscription());
        System.out.println("----------------------------------------");
        System.out.println("✅ Transcription stopped");
    }

    public void cleanupRealtime() {
        stopRealtime();

        // Clean up Vosk resources
        if (recognizer != null) {
            try {
                recognizer.close();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }

        if (voskModel != null) {
            try {
                voskModel.close();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    // Transcribe audio file (non-realtime)
    public String transcribeAudioFile(String audioFilePath) throws IOException {
        AudioFileTranscriber audioFileTranscriber = new AudioFileTranscriber(
            voskModel
        );
        return audioFileTranscriber.transcribeFile(audioFilePath);
    }

    // Test audio system
    public void testAudioSystem() {
        System.out.println("🔊 Testing audio system...");

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (AudioSystem.isLineSupported(info)) {
            System.out.println("✅ Audio format supported");

            try {
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                System.out.println("✅ Microphone access successful");
                line.close();
            } catch (LineUnavailableException e) {
                System.out.println("❌ Microphone access failed: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Audio format not supported");
        }
    }
}

