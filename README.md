# dbk-cms-api-automation

JSON Comparison Tool – How to Use
This tool helps compare two JSON files and generates a human-readable difference report. The output is saved in target/report.txt.
---
Prerequisites
Ensure Java 17+ is installed on your system.
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
Notes:

Operation Meaning

Example

add :
A value is present in the actual response but missing in expected.
Actual has a new field like "age": 30 but expected does not.

remove:
A value is present in the expected response but missing in actual.
Expected has "name": "John" but actual doesn't.

replace:
The value exists in both expected and actual, but values are different.
Expected has "status": "success", actual has "status": "failed".

copy:
Rarely used; it means a value was copied from one path to another. This usually appears if copy handling is enabled (you won't see this unless you explicitly support it).

move:
A value was moved from one path to another (also rare unless enabled).

test
Used for validation-only (not seen in diffs — more for PATCH operations).

 

