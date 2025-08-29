package ocrTextModules;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

public class Ocr_Text_Extraction {

    public void extractPdfTextFromPdf(ExtentTest testCaseName) throws IOException {

   
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            prop.load(input);
        }

        String filePath = prop.getProperty("pdfText"+ ".path");
    	
    	
    	
    	 Map<Integer, List<String>> pageContentMap = new LinkedHashMap<>();

    	  Map<Integer, String> chartNumbers = new LinkedHashMap<>();

    	  Map<Integer, String> patientNames = new LinkedHashMap<>();

         Pattern pageHeaderPattern = Pattern.compile("\\*+\\s*Result for Image/Page (\\d+)\\s*\\*+");

         Pattern chartPattern = Pattern.compile(

        		    "CHART\\s*(?:#|\\*)\\s*:\\s*(\\d+)",

        		    Pattern.CASE_INSENSITIVE

        		);

         int newPage =67;

        int currentPage = -1;

        int pageOffset = -1; // Will be calculated dynamically

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;

            while ((line = br.readLine()) != null) {

                Matcher matcher = pageHeaderPattern.matcher(line);

                if (matcher.find()) {

                    int detectedPage = Integer.parseInt(matcher.group(1));

                    // Calculate the offset only for the first page detection

                    if (pageOffset == -1) {

                        pageOffset = 1 - detectedPage; // Example: if first page is 3, offset = 64

                    }

                    // Apply offset

                    currentPage = detectedPage + pageOffset;

                    pageContentMap.put(currentPage, new ArrayList<>());

                } else if (currentPage != -1) {

                    pageContentMap.get(currentPage).add(line.trim());

                    // Look for CHART #:

                    Matcher chartMatcher = chartPattern.matcher(line);

                    if (chartMatcher.find()) {

                        chartNumbers.put(currentPage, chartMatcher.group(1));

                    }

                }

            }

        }


          catch (IOException e) {

             e.printStackTrace();

         }

         for (Map.Entry<Integer, List<String>> entry : pageContentMap.entrySet()) {

             int page =  entry.getKey();

             System.out.println(page);

             List<String> lines = entry.getValue();

             System.out.println(chartNumbers);

             String chart = chartNumbers.getOrDefault(page, "");

             ExtentTest testCaseSceanrio = testCaseName.createNode("📄 Page: " + page) ;

             testCaseSceanrio.log(Status.INFO, "chart" +chart);

             System.out.println(chart);
 
             String patientName = patientNames.getOrDefault(page, "Not found");

             System.out.println("👤 Patient Name: " + patientName);

             System.out.println("📄 Page: " + page);

               System.out.println("🔖 CHART #: " + chart);

             testCaseSceanrio.log(Status.INFO, "The following📄 Page is : " + page + " and the 🔖 CHART is #: " + chart);

             System.out.println("Content:");

             for (String dataLine : lines) {

            	    System.out.println("dataLine "+dataLine);


            	    if (dataLine.toLowerCase().contains("name")) {
 


            	    	// Case 1: Labeled reverse pattern (e.g., "CLARK, ERIN	Patient Name & Address")

//            	    	Matcher reverseTab = Pattern.compile("^(.*?)\\t+Patient Name(?:\\s*&.*)?\\s*$", Pattern.CASE_INSENSITIVE).matcher(dataLine);

//            	    	Matcher reverse = Pattern.compile("^(.*?)\\s+Patient Name(?:\\s*&.*)?\\s*$", Pattern.CASE_INSENSITIVE).matcher(dataLine);

//

//            	    	// Case 2: Normal labeled pattern (e.g., "Patient Name: CLARK, ERIN")

//            	    	Matcher normal = Pattern.compile("Patient Name[:\\t ]+(.+)", Pattern.CASE_INSENSITIVE).matcher(dataLine);

//

//            	    	// Case 3: Fallback – match LAST uppercase name format near the end

//            	    	Matcher fallback = Pattern.compile("([A-Z]+,\\s+[A-Z]+)\\s*$").matcher(dataLine);

//            	    	if (reverseTab.find()) {

//            	    	    patientName = reverseTab.group(1).trim();

//            	    	} else if (reverse.find()) {

//            	    	    patientName = reverse.group(1).trim();

//            	    	} else if (normal.find()) {

//            	    	    patientName = normal.group(1).trim();

//            	    	} else if (fallback.find()) {

//            	    	    patientName = fallback.group(1).trim();

//            	    	}

            	    	Pattern namePattern = Pattern.compile("\\b((?!NAME|N|\\s+TRI|FEDERAL|TAX|ID)[A-Z']+\\s+[A-Z']+(?:\\s+[A-Z']+)?)\\b");

                    	
            	    	Matcher context = Pattern.compile(
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


            

            	    		if (context.find()) {

            	    		    // One of the two groups will be non-null

            	    			if(context.group(1) != null) {
                	    		    patientName = context.group(1) ;
            	    				
            	    			}
            	    			else if(context.group(1) == null) {
                	    		    patientName = context.group() ;
            	    				
            	    			}

            	    			

            	    		       testCaseSceanrio.log(Status.INFO, "The following📄 Page is : " + page + " and the 👤 Patient Name: context.group(1) is " + patientName);
            	    		       testCaseSceanrio.log(Status.INFO, "The following📄 Page is : " + page + " and the 👤 Patient Name: context.group(0) is " + context.group(0));
            	    		}

            	    	    if(page == 38) {

            	    	    System.out.println(page == 38);	

            	    	    	  System.out.println("🔖 CHARTs #: " + chart);

                    	     	System.out.println("👤 Patient patientName: " + patientName);

                    	    }

            	    	Matcher matcher = namePattern.matcher(dataLine);

            	    	String lastNameFound = "";

            	    	while (matcher.find()) {

            	    	    lastNameFound = matcher.group(1).trim().replace("Patient", ""); // Keep overwriting — last match wins

            	    	}

            	    	System.out.println("👤 Patient Name: " + lastNameFound);

            	        testCaseSceanrio.log(Status.INFO, "The following📄 Page is : " + page + " and the 👤 Patient lastNameFound: " + lastNameFound);

            	        System.out.println("The following📄 Page is : " + page + " and the 👤 Patient Name: " + patientName);

 
 


            	        Ocr_Text_CreateAndWriteExcel.ocrWriteExcelData(page, chart, patientName,testCaseSceanrio);
 
 
            	    }

             }}

     }

}
 