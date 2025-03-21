package de.aichat;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static de.aichat.OpenAIAPI.extractContentFromResponse;

public class OpenAIImageAPI {
    private final AIChatConfig config;

    public OpenAIImageAPI(AIChatConfig config) {
        this.config = config;
    }

    public String analyzeImage(String imagePath, String question) throws Exception {

        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String mimeType = getMimeType(imagePath);
                if (mimeType == null) {
                    throw new Exception("Ungültiger Bildtyp. Erlaubt sind: PNG, JPEG, WEBP, GIF (non-animated).");
                }

        String imageDataUrl = "data:" + mimeType + ",base64," + base64Image;
        String url = "https://api.openai.com/v1/chat/completions";

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            con.setRequestProperty("Content-Type", "application/json");

            String body = "{"
                    + "\"model\": \"" + config.getModel() + "\","
                    + "\"messages\": ["
                    + "    {"
                    + "        \"role\": \"user\","
                    + "        \"content\": ["
                    + "            {\"type\": \"text\", \"text\": \"" + config.getContent() + question + "\"},"
                    + "            {\"type\": \"image_url\", \"image_url\": {\"url\": \"" + imageDataUrl + "\"}}"
                    + "        ]"
                    + "    }"
                    + "]"
                    + "}";
            con.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return extractContentFromResponse(response.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getMimeType(String imagePath) {
        String lower = imagePath.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".jpeg") || lower.endsWith(".jpg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else {
            return null;
        }
    }
}
