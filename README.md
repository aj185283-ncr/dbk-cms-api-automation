# dbk-cms-api-automation

JSON Comparison Tool – 
API automation with json differences report

Git repo : https://github.com/aj185283-ncr/dbk-cms-api-automation



---

Prerequisites

Ensure Java 8+ is installed on your system.

Verify using:


java -version

---
Step-by-Step Instructions

Step 1: 

Download the required executable JAR file from the below link:
https://github.com/aj185283-ncr/dbk-cms-api-automation/blob/main/target/dbk-cms-api-automation-1.0-SNAPSHOT-jar-with-dependencies.jar

---
Step 2: 

Prepare Your JSON Files

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
**Step 3:** 

Note : keep all the files inside templates folder.

Run the Comparison Command

Open a terminal or command prompt in the folder where the .jar is downloaded and run the below command:



To run specific file , Run :

java -jar dbk-cms-api-automation-1.0-SNAPSHOT-jar-with-dependencies.jar {file path}

To run all list of json files , Run :

java -jar dbk-cms-api-automation-1.0-SNAPSHOT-jar-with-dependencies.jar {folder path}

---


Output:

The tool generates a file at:

target/report.txt

---
Example 

Output (report.txt)



=========testcase2.json========

Operation: replace | Path :/transfer/loanOverpayment/enabled


=====testcase2.json =====

No differences found.
