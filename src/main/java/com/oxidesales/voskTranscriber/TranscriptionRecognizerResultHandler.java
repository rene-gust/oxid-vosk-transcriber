package com.oxidesales.voskTranscriber;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class TranscriptionRecognizerResultHandler {

    // Transcription state
    private final StringBuilder currentTranscription = new StringBuilder();

    private final ObjectMapper objectMapper;

    private String lastPartialResult = "";

    private TranscriptionListener listener;

    public TranscriptionRecognizerResultHandler() {
        this.objectMapper = new ObjectMapper();
    }

    public void setTranscriptionListener(TranscriptionListener listener) {
        this.listener = listener;
    }

    public void handleFinalResult(String jsonResult) {
        try {
            JsonNode resultNode = objectMapper.readTree(jsonResult);
            String text = resultNode.get("text").asText().trim();

            if (!text.isEmpty()) {
                // Clear partial result line and show final result
                clearPartialResult();
                System.out.println("✅ " + text);

                // Add to complete transcription
                currentTranscription.append(text).append(" ");

                if (listener != null) {
                    listener.onTranscription(text);
                }

                // Clear partial result
                lastPartialResult = "";
            }

        } catch (Exception e) {
            System.err.println("❌ Error parsing final result: " + e.getMessage());
        }
    }

    public void handlePartialResult(String jsonResult) {
        try {
            JsonNode resultNode = objectMapper.readTree(jsonResult);
            String partialText = resultNode.get("partial").asText().trim();

            if (!partialText.isEmpty() && !partialText.equals(lastPartialResult)) {
                // Clear previous partial result
                clearPartialResult();

                // Show new partial result
                System.out.print("🔄 " + partialText);
                System.out.flush();

                lastPartialResult = partialText;
            }

        } catch (Exception e) {
            System.err.println("❌ Error parsing partial result: " + e.getMessage());
        }
    }

    public String getCompleteTranscription() {
        return currentTranscription.toString().trim();
    }

    private void clearPartialResult() {
        if (!lastPartialResult.isEmpty()) {
            // Move cursor to beginning of line and clear it
            System.out.print("\r" + " ".repeat(lastPartialResult.length() + 10) + "\r");
            System.out.flush();
        }
    }
}
