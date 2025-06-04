package org.example;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

public class ApiRunner {

    public static void main(String[] args) throws Exception {

        //pass "templates" in the first argument
        String path = args[0];

        URI uri = ApiRunner.class.getClassLoader().getResource(path).toURI();

        File folder = new File(uri);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if(files == null || files.length ==0) {
            System.out.println("No Json Files found in resources folder");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();

        for(File file : files) {
            System.out.println("\n Processing file - " + file.getName() +" ...");
            InputStream input = new FileInputStream(file);
            JsonNode root = mapper.readTree(input);

            // Extract request details
            JsonNode requestNode = root.path("request");
            String method = requestNode.path("method").asText();
            String url = requestNode.path("url").asText();
            JsonNode headersNode = requestNode.path("headers");

       /* if (!"GET".equalsIgnoreCase(method)) {
            throw new UnsupportedOperationException("Only GET method supported in this example.");
        }*/
            // in of multiple HTTP methods , we can add case here

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            Iterator<Map.Entry<String, JsonNode>> fields = headersNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                headers.add(entry.getKey(), entry.getValue().asText());
            }

            // Send request
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode actualResponse = mapper.readTree(responseEntity.getBody());
            JsonNode expectedResponse = root.path("testcase").path("expected");

            // Compare with zjsonpatch
            JsonNode diff = JsonDiff.asJson(expectedResponse, actualResponse);

            if (diff.isEmpty()) {
                System.out.println(" API response matches expected data.");
            } else {
                System.out.println(" Differences found:");
                System.out.println(diff.toPrettyString());
            }
        }
    }
}

