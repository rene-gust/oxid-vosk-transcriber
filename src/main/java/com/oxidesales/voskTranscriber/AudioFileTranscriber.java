package com.oxidesales.voskTranscriber;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class AudioFileTranscriber {
    private final Model voskModel;

    public AudioFileTranscriber (Model voskModel) {
        this.voskModel = voskModel;
    }

    public String transcribeFile(String audioFilePath) throws IOException {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(audioFilePath))) {

            ObjectMapper objectMapper = new ObjectMapper();
            // Convert to required format if needed
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    Transcriber.SAMPLE_RATE,
                    Transcriber.SAMPLE_SIZE_BITS,
                    Transcriber.CHANNELS,
                    (Transcriber.SAMPLE_SIZE_BITS / 8) * Transcriber.CHANNELS,
                    Transcriber.SAMPLE_RATE,
                    Transcriber.BIG_ENDIAN
            );

            AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);

            // Create new recognizer for file processing
            Recognizer fileRecognizer = new Recognizer(voskModel, Transcriber.SAMPLE_RATE);
            StringBuilder transcription = new StringBuilder();

            byte[] buffer = new byte[Transcriber.CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = convertedStream.read(buffer)) != -1) {
                if (fileRecognizer.acceptWaveForm(buffer, bytesRead)) {
                    String result = fileRecognizer.getResult();
                    JsonNode resultNode = objectMapper.readTree(result);
                    String text = resultNode.get("text").asText().trim();

                    if (!text.isEmpty()) {
                        transcription.append(text).append(" ");
                    }
                }
            }

            // Get final result
            String finalResult = fileRecognizer.getFinalResult();
            JsonNode finalNode = objectMapper.readTree(finalResult);
            String finalText = finalNode.get("text").asText().trim();

            if (!finalText.isEmpty()) {
                transcription.append(finalText);
            }

            fileRecognizer.close();
            return transcription.toString().trim();

        } catch (UnsupportedAudioFileException e) {
            throw new IOException("Unsupported audio file format", e);
        }
    }
}
