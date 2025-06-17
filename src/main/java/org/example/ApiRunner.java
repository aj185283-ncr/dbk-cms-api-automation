package org.example;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ApiRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("❌ Please provide a JSON file or directory path.");
            System.exit(1);
        }

        var input = Path.of(args[0]);
        List<Path> jsonFiles = new ArrayList<>();

        if (Files.isRegularFile(input) && input.toString().endsWith(".json")) {
            jsonFiles.add(input);
        } else if (Files.isDirectory(input)) {
            try (var files = Files.list(input)) {
                files.filter(p -> p.toString().endsWith(".json"))
                        .forEach(jsonFiles::add);
            }
        } else {
            System.out.println("❌ Invalid input: " + input);
            System.exit(1);
        }

        jsonComparison(jsonFiles);
    }

    private static void jsonComparison(List<Path> jsonFiles) throws IOException {
        var mapper = new ObjectMapper();
        var restTemplate = new RestTemplate();
        var htmlBuilder = new StringBuilder();

        htmlBuilder.append("""
                <html><head><title>API Test Report</title>
                <style>
                body { font-family: Arial; margin: 20px; }
                .test-case { border: 1px solid #ccc; padding: 15px; margin-bottom: 20px; }
                .pass { color: green; } .fail { color: red; }
                table { border-collapse: collapse; width: 100%; }
                th, td { border: 1px solid #ddd; padding: 8px; }
                th { background-color: #f2f2f2; }
                pre { background: #f9f9f9; padding: 10px; border: 1px solid #ccc; overflow: auto; }
                details summary { cursor: pointer; font-weight: bold; margin-top: 10px; }
                </style></head><body>
                <h1>API Test Report</h1>
                """);

        for (var jsonPath : jsonFiles) {
            System.out.println("🔍 Processing file - " + jsonPath.getFileName() + " ...");

            JsonNode root;
            try (var inputStream = Files.newInputStream(jsonPath)) {
                root = mapper.readTree(inputStream);
            }

            var requestNode = root.path("request");
            var method = requestNode.path("method").asText();
            var url = requestNode.path("url").asText();
            var headersNode = requestNode.path("headers");

            var headers = new HttpHeaders();
            headersNode.fields().forEachRemaining(e -> headers.add(e.getKey(), e.getValue().asText()));
            var entity = new HttpEntity<String>(headers);
            ResponseEntity<String> responseEntity;

            try {
                responseEntity = switch (method.toUpperCase()) {
                    case "GET" -> restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                    case "POST" -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                    default -> throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
                };
            } catch (Exception e) {
                htmlBuilder.append("<div class='test-case'>")
                        .append("<h2>").append(jsonPath.getFileName()).append("</h2>")
                        .append("<p class='fail'>❌ Request failed: ").append(e.getMessage()).append("</p></div>");
                continue;
            }

            var actualResponse = mapper.readTree(responseEntity.getBody());
            var expectedResponse = root.path("testcase").path("expected");
            var patch = JsonDiff.asJson(expectedResponse, actualResponse);

            htmlBuilder.append("<div class='test-case'>")
                    .append("<h2>").append(jsonPath.getFileName()).append("</h2>");

            if (patch.isEmpty()) {
                htmlBuilder.append("<p class='pass'>✅ No differences found. Test Passed.</p>");
            } else {
                htmlBuilder.append("<p class='fail'>❌ Differences found:</p>")
                        .append("<table><tr><th>Operation</th><th>Path</th><th>Expected</th><th>Actual</th></tr>");

                for (var diff : patch) {
                    var op = diff.path("op").asText();
                    var path = diff.path("path").asText();
                    var expected = "";
                    var actual = "";

                    switch (op) {
                        case "add" -> actual = diff.path("value").toString();
                        case "remove" -> expected = diff.path("value").toString();
                        case "replace" -> {
                            actual = diff.path("value").toString();
                            expected = "N/A"; // zjsonpatch doesn’t provide old value
                        }
                        default -> actual = diff.path("value").toString();
                    }

                    htmlBuilder.append("<tr>")
                            .append("<td>").append(op).append("</td>")
                            .append("<td>").append(path).append("</td>")
                            .append("<td>").append(escapeHtml(expected)).append("</td>")
                            .append("<td>").append(escapeHtml(actual)).append("</td>")
                            .append("</tr>");
                }

                htmlBuilder.append("</table>");
            }

            htmlBuilder.append("<details><summary>Expected JSON</summary><pre>")
                    .append(escapeHtml(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedResponse)))
                    .append("</pre></details>");

            htmlBuilder.append("<details><summary>Actual JSON</summary><pre>")
                    .append(escapeHtml(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualResponse)))
                    .append("</pre></details>")
                    .append("</div>");
        }

        htmlBuilder.append("</body></html>");

        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target", "report.html"), htmlBuilder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("✅ HTML report generated at: target/report.html");
    }

    private static String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
