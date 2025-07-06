package com.oxidesales.voskTranscriber;

public class CLIMain {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java CLITranscriber <model-path>");
            System.err.println("Example: java CLITranscriber ./vosk-model-en-us-0.22");
            System.err.println("");
            System.err.println("Download models from: https://alphacephei.com/vosk/models");
            System.err.println("Popular models:");
            System.err.println("  - vosk-model-en-us-0.22 (English, 1.8GB)");
            System.err.println("  - vosk-model-en-us-0.22-lgraph (English, 128MB)");
            System.err.println("  - vosk-model-small-en-us-0.15 (English, 40MB)");
            System.exit(1);
        }

        String modelPath = args[0];

        Transcriber transcriber = new Transcriber(modelPath);

        // Test audio system first
        transcriber.testAudioSystem();

        // Handle shutdown gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(transcriber::cleanupRealtime));

        try {
            transcriber.startRealtime();
        } catch (Exception e) {
            System.err.println("❌ Application error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
