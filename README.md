# dbk-cms-api-automation

JSON Comparison Tool – How to Use
This tool helps compare two JSON files and generates a human-readable difference report. The output is saved in target/report.txt.
---
Prerequisites
Ensure Java 11+ is installed on your system.
Verify using:
java -version
---
Step-by-Step Instructions
Step 1: Download the JAR
Download the required executable JAR file from the below link:
https://github.com/aj185283-ncr/dbk-cms-api-automation/blob/main/target/dbk-cms-api-automation-1.0-SNAPSHOT-jar-with-dependencies.jar

---
Step 2: Prepare Your JSON Files
Json format :
{
"request": {
"method": "GET",
"url": "",
"headers": {
"accept": "application/json",
"accept-language": "en"
}
},
"testcase": {
"expected": {
}
}
}

---
Step 3: Run the Comparison Command
Open a terminal or command prompt in the folder where the .jar is downloaded and run the below command:
Note : keep all the files inside templates folder.

To run specific file , Run :
java -jar dbk-cms-api-automation-1.0-SNAPSHOT-jar-with-dependencies.jar /Users/aj185283/IdeaProjects/dbk-cms-api-automation/src/main/resources/templates/testcase1.json

To run all list of json files , Run :
java -jar dbk-cms-api-automation-1.0-SNAPSHOT-jar-with-dependencies.jar /Users/aj185283/IdeaProjects/dbk-cms-api-automation/src/main/resources/templates/

---
Output
The tool generates a file at:
target/report.txt
This report includes a detailed comparison of each test case, separated by headers.
---
Example Output (report.txt)
===== TestCase: testcase1.json =====
Operation: replace | Path: /name | Value: "John"
===== TestCase: testcase2.json =====
No differences found.
