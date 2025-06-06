package org.example;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class ApiRunner {

    public static void main(String[] args) throws Exception {

        if(args.length < 1) {
            System.out.println("Please provide complete JSON file or directory.");
            System.exit(1);
        }

        File input = new File(args[0]);
        List<File> jsonFiles = new ArrayList<>();

        // validate input type . It should json file or complete directory with list of json files
        if(input.isFile() && input.getName().endsWith(".json")) {
            jsonFiles.add(input);
        } else if(input.isDirectory()) {
            File[] files = input.listFiles((dir, name) -> name.endsWith(".json"));
            if(files !=null){
                jsonFiles.addAll(Arrays.asList(files));
            }
        } else {
            System.out.println("Invalid input :" +args[0]);
            System.exit(1);
        }
        jsonComparison(jsonFiles);

    }

    private static void jsonComparison(List<File> jsonFiles) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();

        for(File jsonFile : jsonFiles) {
            System.out.println("Processing file - " + jsonFile.getName() +" ...");
            InputStream inputStream = new FileInputStream(jsonFile);
            JsonNode root = mapper.readTree(inputStream);

            // Extract request details
            JsonNode requestNode = root.path("request");
            String method = requestNode.path("method").asText();
            String url = requestNode.path("url").asText();
            JsonNode headersNode = requestNode.path("headers");

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            Iterator<Map.Entry<String, JsonNode>> fields = headersNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                headers.add(entry.getKey(), entry.getValue().asText());
            }

            // Send request based on GET and POST
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = null;

            // Check request method type
            if("GET".equalsIgnoreCase(method)) {
                responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            }
            if("POST".equalsIgnoreCase(method)) {
                responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            }

            JsonNode actualResponse = mapper.readTree(responseEntity.getBody());
            JsonNode expectedResponse = root.path("testcase").path("expected");

            // Compare with zjsonpatch
            JsonNode patch = JsonDiff.asJson(expectedResponse, actualResponse);

            String testCaseName = jsonFile.getName();
            StringBuilder reportBuilder = new StringBuilder();
            reportBuilder.append("=========").append(testCaseName).append("========\n");

            if (patch.isEmpty()) {
                reportBuilder.append("no Difference Found.\n");
            } else {
                for(JsonNode diff : patch) {
                    String op = diff.get("op").asText();
                    String path = diff.get("path").asText();
                    reportBuilder.append("Operation: ").append(op).append(" | Path :").append(path);

                    if(diff.has("from")) {
                        reportBuilder.append(" | From ").append(diff.get("from").asText());
                    }
                    reportBuilder.append("\n");
                }
            }
            reportBuilder.append("\n");
            Files.createDirectories(Paths.get("target"));
            Files.write(Paths.get("target/report.txt"),
            reportBuilder.toString().getBytes(), StandardOpenOption.CREATE,StandardOpenOption.APPEND);

        }
    }
}

