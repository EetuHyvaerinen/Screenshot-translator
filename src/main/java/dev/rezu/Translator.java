package dev.rezu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Translator {
    private final HttpClient httpClient;

    public Translator(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String translateToEnglish(String text) {
        if (text == null || text.isBlank()) return "";

        try {
            // Google translate HTTP endpoint call
            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&q="
                    + URLEncoder.encode(text, StandardCharsets.UTF_8);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10)) // Prevent infinite hanging
                    .header("User-Agent", "Mozilla/5.0") // Faking a browser user-agent is safer
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                System.err.println("Google Translate API Error: HTTP " + resp.statusCode());
                return "(translation error)";
            }

            // Parse the JSON array using Gson instead of regex
            String body = resp.body();
            JsonArray jsonArray = JsonParser.parseString(body).getAsJsonArray();
            JsonArray translationChunks = jsonArray.get(0).getAsJsonArray();

            StringBuilder result = new StringBuilder();
            for (JsonElement chunk : translationChunks) {
                // The actual translated sentence is the first string in each chunk's array
                result.append(chunk.getAsJsonArray().get(0).getAsString());
            }

            return result.toString();

        } catch (Exception e) {
            System.err.println("Translation Request Failed:");
            e.printStackTrace();
            return "(translation error)";
        }
    }
}