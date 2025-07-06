package com.oxidesales.voskTranscriber;

public class ModelLanguageDetector {
    public String getModelLanguage(String modelPath) {
        String path = modelPath.toLowerCase();
        if (path.contains("en")) return "English";
        if (path.contains("de")) return "German";
        if (path.contains("fr")) return "French";
        if (path.contains("es")) return "Spanish";
        if (path.contains("ru")) return "Russian";
        if (path.contains("zh")) return "Chinese";
        return "Unknown";
    }
}
