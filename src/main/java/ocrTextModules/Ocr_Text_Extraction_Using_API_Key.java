package ocrTextModules;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

public class Ocr_Text_Extraction_Using_API_Key {
    final String PROCESSED_FILES = "src/main/resources/processedFiles.txt";

    // --- Extract text from single PDF file
    public void extractPdfTextFromPdf(File pdfFile, ExtentTest testCaseName) throws Exception {
        // Load config
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            prop.load(input);
        }
        ExtentTest testCaseScenario = testCaseName.createNode("Reading pdfFile: " + pdfFile);
        String apiKey = prop.getProperty("ocr.api.key");
       
        String outputFolder = System.getenv("Excel_Output_Folder");
        
        if(((outputFolder==null))||(outputFolder.isEmpty())){
        
            outputFolder = prop.getProperty("excelOutput.folder"); // just to create folder if needed
            
            
        }
        
        
        
        
        if (outputFolder == null) {
            throw new IllegalArgumentException("excelOutput.folder is not defined in config.properties");
        }

        File outFolderFile = new File(outputFolder);
        if (!outFolderFile.exists()) outFolderFile.mkdirs(); // create folder if missing

        // 🔹 API POST request setup
        String url = "https://api.ocr.space/parse/image";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("apikey", apiKey);
        con.setDoOutput(true);

        String postData =
                "language=eng" +
                "&isOverlayRequired=true" +
                "&isTable=true" +
                "&scale=true" +
                "&detectOrientation=true" +
                "&isCreateSearchablePdf=false" +
                "&isSearchablePdfHideTextLayer=false" +
                "&OCREngine=2";

        byte[] fileData = Files.readAllBytes(pdfFile.toPath());
        String boundary = "*****" + System.currentTimeMillis() + "*****";
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes("--" + boundary + "\r\n");
            wr.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + pdfFile.getName() + "\"\r\n");
            wr.writeBytes("Content-Type: application/pdf\r\n\r\n");
            wr.write(fileData);
            wr.writeBytes("\r\n");

            for (String param : postData.split("&")) {
                String[] keyValue = param.split("=");
                wr.writeBytes("--" + boundary + "\r\n");
                wr.writeBytes("Content-Disposition: form-data; name=\"" + keyValue[0] + "\"\r\n\r\n" + keyValue[1] + "\r\n");
            }
            wr.writeBytes("--" + boundary + "--\r\n");
            wr.flush();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();

        JSONObject json = new JSONObject(response.toString());
        JSONArray results = json.getJSONArray("ParsedResults");

        Pattern chartPattern = Pattern.compile("CHART\\s*(?:#|\\*)\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

        // Create writer for this PDF using output folder
        Ocr_Text_CreateAndWriteExcelInFolder writer = new Ocr_Text_CreateAndWriteExcelInFolder(pdfFile.getName());

        for (int i = 0; i < results.length(); i++) {
            JSONObject pageObj = results.getJSONObject(i);
            int page = pageObj.optInt("Page", i + 1);
            String pageText = pageObj.optString("ParsedText", "").trim();

            ExtentTest testCaseScenario1 = testCaseScenario.createNode("Reading pageNo: " + page);
            testCaseScenario1.log(Status.INFO, "Extracted Text:<br>" + pageText.replace("\n", "<br>"));

            String chart = "";
            String patientName = "";
            String providerName = "";

            for (String dataLine : pageText.split("\n")) {
                dataLine = dataLine.trim();

                Matcher chartMatcher = chartPattern.matcher(dataLine);
                if (chartMatcher.find()) chart = chartMatcher.group(1);

                if (dataLine.toLowerCase().contains("name")) {
           	    	Matcher namePattern = Pattern.compile(
        	    		    "(?i)" +

        	    		    // Before label - With comma version
        	    		    "((?!\\b(?:STREET|ST|ROAD|RD|AVENUE|AVE|DRIVE|FEDERAL|NAME|TAX|ID|DR|LANE|LN|BOULEVARD|BLVD|ISLAND|PARK|CITY|STATE|COUNTY)\\b|\\bN\\s+TRI\\b)"

        	    		    + "[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*"
        	    		    + "(?:,\\s*[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*"
        	    		    + "(?:\\s+(?!Patient\\b)[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*)*)*" // <-- added negative lookahead here
        	    		    + ")"
        	    		    + "(?=(?:\\s*Patient Name|\\s*DOB:|\\s*Patrent Name))" +

        	    		    "|" +

        	    		    // Before label - Without comma version
        	    		    "((?!\\b(STREET|ST|ROAD|RD|AVENUE|AVE|DRIVE|DR|LANE|LN|BOULEVARD|BLVD|ISLAND|PARK|CITY|STATE|COUNTY)\\b)"
        	    		    + "[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*\\s+[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*"
        	    		    + "(?:\\s+(?!Patient\\b)[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*)*)" // <-- added negative lookahead here
        	    		    + "(?=(?:\\s*Patient Name|\\s*DOB:|\\s*Patrent Name))" +

        	    		    "|" +

        	    		    // After label - With comma version
        	    		    "(?<=Patient Name[:\\t ]+)"
        	    		    + "((?!\\b(STREET|ST|ROAD|RD|AVENUE|AVE|DRIVE|DR|LANE|LN|BOULEVARD|BLVD|ISLAND|PARK|CITY|STATE|COUNTY)\\b)"
        	    		    + "[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*"
        	    		    + "(?:,\\s*[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*"
        	    		    + "(?:\\s+(?!Patient\\b)[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*)*)*" // <-- added negative lookahead here
        	    		    + ")" +

        	    		    "|" +

        	    		    // After label - Without comma version
        	    		    "(?<=Patient Name[:\\t ]+)"
        	    		    + "((?!\\b(STREET|ST|ROAD|RD|AVENUE|AVE|DRIVE|DR|LANE|LN|BOULEVARD|BLVD|ISLAND|PARK|CITY|STATE|COUNTY)\\b)"
        	    		    + "[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*\\s+[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*"
        	    		    + "(?:\\s+(?!Patient\\b)[A-Za-z'\\.-]+(?:\\*|-|\\*-|\\*\\-)*)*)" // <-- added negative lookahead here
        	    		).matcher(dataLine);


                    if (namePattern.find()) patientName = namePattern.group();
                }

                Matcher providerMatcher = Pattern.compile("(?i)\\*?\\(\\s*ACH\\s*\\)\\s*[A-Z]+(?:\\s+[A-Z]+)*\\s+PA").matcher(dataLine);
                if (providerMatcher.find()) providerName = providerMatcher.group();
            }

            testCaseScenario1.log(Status.INFO, "CHART #: " + chart);
            testCaseScenario1.log(Status.INFO, "Patient Name: " + (patientName.isEmpty() ? "Not found" : patientName));
            testCaseScenario1.log(Status.INFO, "Provider Name: " + providerName);

            // Write the page to Excel in the output folder
            writer.ocrWriteExcelData(page, chart, patientName, testCaseScenario1);
        }
    }

    // --- Extract text from all PDFs in folder
    public void extractPdfTextFromFolder(ExtentTest testCaseName) throws Exception {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            prop.load(input);
        }

        String inputFolderPath = System.getenv("Pdf_Input_Folder");
        
        if(((inputFolderPath==null))||(inputFolderPath.isEmpty())){
        
        inputFolderPath = prop.getProperty("pdfInput.folder");
        }
        
      
        String outputFolder = System.getenv("Excel_Output_Folder");
        
        if(((outputFolder==null))||(outputFolder.isEmpty())){
        
            outputFolder = prop.getProperty("excelOutput.folder"); // just to create folder if needed
            
            
        }
        
        
        
        
        
        
        
        
        
        
        if (outputFolder != null) new File(outputFolder).mkdirs();

        System.out.println("inputFolderPath: "+inputFolderPath);
        
        File inputFolder = new File(inputFolderPath);

        Set<String> processedFiles = new HashSet<>();
      
        
        File processedFileRecord = new File(PROCESSED_FILES);
        if (processedFileRecord.exists()) processedFiles.addAll(Files.readAllLines(processedFileRecord.toPath()));

        File[] pdfFiles = inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null || pdfFiles.length == 0) {
            System.out.println("No PDF files found in the folder.");
            testCaseName.log(Status.WARNING, "No PDF files found in the folder.");
            return;
        }

        for (File pdfFile : pdfFiles) {
            if (processedFiles.contains(pdfFile.getName())) {
                System.out.println("Skipping already processed file: " + pdfFile.getName());
                testCaseName.log(Status.SKIP ,"Skipping already processed file: " + pdfFile.getName());
           
                continue;
            }

            long fileSizeMB = pdfFile.length() / (1024 * 1024);
            if (fileSizeMB > 5) {
                System.out.println("Skipping file larger than 5MB: " + pdfFile.getName() + " (" + fileSizeMB + " MB)");
                testCaseName.log(Status.SKIP ,"Skipping file larger than 5MB: " + pdfFile.getName() + " (" + fileSizeMB + " MB)");
                continue;
            }

            System.out.println("Processing file: " + pdfFile.getName());

            extractPdfTextFromPdf(pdfFile, testCaseName);

            processedFiles.add(pdfFile.getName());
   
            Files.write(processedFileRecord.toPath(), processedFiles);
        }
    }
}
