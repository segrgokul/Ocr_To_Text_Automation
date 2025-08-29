package ocrText_runner_Execution;

import java.awt.AWTException;
import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import ocrTextModules.Ocr_Text_Extraction;
import ocrTextModules.Ocr_Text_Extraction_Using_API_Key;


public class OCR_Text_Execution  {

	 ExtentReports extentReport ;
	 ExtentSparkReporter report ;
	 ExtentTest testCaseName;

	 Ocr_Text_Extraction OcrText_Pdf_Without_APi = new Ocr_Text_Extraction();
	 Ocr_Text_Extraction_Using_API_Key OcrText_Pdf_With_APi = new Ocr_Text_Extraction_Using_API_Key();

	//For OCR_Text project
	 @Test(priority = 1,enabled=false)
	 public void Ocr_Text_Extraction_Without_Api() throws Exception {
	     testCaseName = extentReport.createTest("OCR_Text_Extraction_Without_Api");
	     OcrText_Pdf_Without_APi.extractPdfTextFromPdf(testCaseName);
	 }

	 @Test(priority = 2,enabled=true)

	 public void Ocr_Text_Extraction_With_Api() throws Exception {
	     testCaseName = extentReport.createTest("OCR_Text_Extraction_With_Api");

	     Ocr_Text_Extraction_Using_API_Key ocr = new Ocr_Text_Extraction_Using_API_Key();
	     ocr.extractPdfTextFromFolder(testCaseName);  // ✅ processes all PDFs in folder
	 }


@BeforeMethod
public void beforeMethod() throws IOException, InterruptedException {
	System.out.println("This will execute foruth before every Method and after the before class");
//	ReadExcelData.ExcelReader(C:\\Users\\User\\Downloads\\DumpScore.xlsx,"mds");
//	ReadExcelData.getColumnData()
	

}

@AfterMethod
public void afterMethod() {
	System.out.println("This will execute after every Method");
	  Properties prop = new Properties();
        try {
            // Load the properties file
      	  FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
            prop.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Flush Extent Report
        extentReport.flush();
}

@BeforeClass
public void beforeClass() {
	System.out.println("This will execute third before the Class and after the befortest");
	  // Set up ExtentReports
 
}

@AfterClass
public void afterClass() {
	System.out.println("This will execute after the Class");

}

@BeforeTest
public void beforeTest() {
	System.out.println("This will execute second before the Test and after the before test suite");
  // Check if the driver is not already initialized
		
	
}

@AfterTest
public void afterTest() {
	System.out.println("This will execute after the Test");
	 Properties prop = new Properties();

     try {
          // Load the properties file
    	  FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
        
          prop.load(fileInputStream);
      } catch (IOException e) {
          e.printStackTrace();
      }
	 
  // Read properties
     String reportPath = System.getenv("Report_Path");
   		  
     	if((reportPath == null)||reportPath.isEmpty()) {
   		  
     		reportPath =	  prop.getProperty("report.path"); // Convert to valid URI format
     	}
     	
	

	 try {
            Desktop.getDesktop().browse(new URI("file:///" + reportPath.replace("\\", "/")));
        } catch (Exception e) {
            e.printStackTrace();
        }	  // Ensure the browser is closed after the test
	
}
@BeforeSuite
public void beforeSuite() throws InterruptedException, FileNotFoundException {
	System.out.println("This will execute first before the Test Suite");
//	report = new 




  Properties prop = new Properties();
  try {
      // Load the properties file
	  FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties");
    
      prop.load(fileInputStream);
  } catch (IOException e) {
      e.printStackTrace();
  }

  // Read properties
  String reportPath = System.getenv("Report_Path");
		  
  	if((reportPath == null)||reportPath.isEmpty()) {
		  
  		reportPath =prop.getProperty("report.path").replace("\\", "/"); // Convert to valid URI format
  	}
  
  String theme = prop.getProperty("report.theme");
  String documentTitle = prop.getProperty("report.documentTitle");
  String reportName = prop.getProperty("report.reportName");

  // Set up Extent Reports
  ExtentSparkReporter report = new ExtentSparkReporter(reportPath);
  report.config().setDocumentTitle(documentTitle);
  report.config().setReportName(reportName);

  // Set Theme based on config
  if ("DARK".equalsIgnoreCase(theme)) {
      report.config().setTheme(Theme.DARK);
  } else {
      report.config().setTheme(Theme.STANDARD);
  }

	  extentReport =new ExtentReports(); 
  extentReport.attachReporter(report);
 

  System.out.println("Extent Report generated successfully at: " + reportPath);


  

}


@AfterSuite
public void afterSuite() throws IOException, URISyntaxException {
 
	

   // String reportPath = prop.getProperty("report.path").replace("\\", "/"); // Convert to valid URI format
	
	System.out.println("This will execute after the Test Suite");

 
}
}
