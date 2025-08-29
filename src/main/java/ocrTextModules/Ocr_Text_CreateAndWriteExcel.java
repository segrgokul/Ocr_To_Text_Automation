package ocrTextModules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
public class Ocr_Text_CreateAndWriteExcel {

	    static XSSFWorkbook workbook;
	    static XSSFSheet sheet;
	    static String filePath;
	    static {
	        try {
	            // Load config file
	            Properties prop = new Properties();
	            try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
	                prop.load(input);
	            }
	            filePath = prop.getProperty("excel.file.path3");
	            if (filePath == null) {
	                throw new IllegalArgumentException("Excel file path is not set in config.properties");
	            }
	            File file = new File(filePath);
	            if (file.exists()) {
	                try (FileInputStream fis = new FileInputStream(file)) {
	                    workbook = new XSSFWorkbook(fis); // Load existing
	                }
	            } else {
	                workbook = new XSSFWorkbook(); // Create new
	            }
	            // Get or create sheet
	            sheet = workbook.getSheet("Data");
	            if (sheet == null) {
	                sheet = workbook.createSheet("Data");
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    public static void ocrWriteExcelData(int pageno, String chart, String name,ExtentTest testCaseSceanrio) throws IOException {
	        // Create header only once


	    	XSSFRow firstRow = sheet.getRow(0);
	        if (firstRow == null) {
	            firstRow = sheet.createRow(0);
	            firstRow.createCell(0).setCellValue("Page No");
	            firstRow.createCell(1).setCellValue("Patient Name");
	            firstRow.createCell(2).setCellValue("Chart ID");
	        }
	        // Find the next row index for appending
	        int newRowNum = sheet.getLastRowNum() + 1;
	        // If there is a gap between last row and this pageno, fill empty rows
	        while (newRowNum < pageno) {
	            sheet.createRow(newRowNum);
	            newRowNum++;
	        }
	        System.out.println("Reading: " + name + " (Page: " + pageno + ", Chart: " + chart + ")");
	        testCaseSceanrio.log(Status.WARNING, "Reading: " + name + " Page: " + pageno + ", Chart: " + chart );
    	    if(pageno == 38) {
    	    System.out.println(pageno == 38);	
    	    	  System.out.println("🔖 CHARTs #: " + chart);
    	     	System.out.println("👤 Patient patientName1: " + name);
    	    }
	        // Create the actual row for data
	        Row row = sheet.createRow(newRowNum);
	        row.createCell(0).setCellValue(pageno);
	        row.createCell(1).setCellValue(name != null ? name : "");
	        row.createCell(2).setCellValue(chart != null ? chart : "");
	        System.out.println("Written: " + name + " (Page: " + pageno + ", Chart: " + chart + ")");
	        testCaseSceanrio.log(Status.WARNING, "Written: " + name + " Page: " + pageno + ", Chart: " + chart );
	        // Save file
	        saveWorkbook();
	    }
	    private static void saveWorkbook() throws IOException {
	        try (FileOutputStream fos = new FileOutputStream(filePath)) {
	            workbook.write(fos);
	        }
	    }
		
	}