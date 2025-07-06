package com.oxidesales.voskTranscriber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class WindowAppMain {
    private JTextArea textArea;
    private JButton transcribeButton;
    private final Transcriber transcriber;

    public WindowAppMain(Transcriber transcriber) {
        this.transcriber = transcriber;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java WindowAppMain <model-path>");
            System.err.println("Example: java WindowAppMain ./vosk-model-en-us-0.22");
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

        Runtime.getRuntime().addShutdownHook(new Thread(transcriber::cleanupRealtime));

        SwingUtilities.invokeLater(() -> new WindowAppMain(transcriber).createAndShowGUI());
    }

    private void createAndShowGUI() {
        // Create main window
        JFrame frame = new JFrame("Simple Transcriber");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // Create text area
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Create button
        transcribeButton = new JButton("Transcribe");
        transcribeButton.addActionListener(this::onTranscribeClicked);

        // Layout components
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(transcribeButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private void onTranscribeClicked(ActionEvent event) {
        transcribeButton.setEnabled(false);
        textArea.setText("Listening...");

        // Run transcription in background to avoid UI freezing
        new Thread(() -> {
            try {
                try {
                    transcriber.setTranscriptionListener(text -> {
                        SwingUtilities.invokeLater(() -> textArea.append(text + "\n"));
                    });
                    transcriber.startRealtime();
                } catch (Exception e) {
                    System.err.println("❌ Application error: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
                String result = startTranscription(); // Call your existing logic
                SwingUtilities.invokeLater(() -> textArea.setText(result));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> textArea.setText("Error: " + e.getMessage()));
            } finally {
                SwingUtilities.invokeLater(() -> transcribeButton.setEnabled(true));
            }
        }).start();
    }

    // Replace this stub with your real transcription logic
    private String startTranscription() {
        // Simulate delay
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        return "Simulated transcription result from microphone.";
    }
}
