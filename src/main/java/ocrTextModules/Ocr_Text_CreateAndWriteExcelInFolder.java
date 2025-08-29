package ocrTextModules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

public class Ocr_Text_CreateAndWriteExcelInFolder {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private String excelFilePath;

    // Constructor: accept PDF file name, output folder is read from config
    public Ocr_Text_CreateAndWriteExcelInFolder(String pdfFileName) throws IOException {
        // Load output folder from config.properties
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            prop.load(input);
        }
        String outputFolder = prop.getProperty("excelOutput.folder");
        if (outputFolder == null) {
            throw new IllegalArgumentException("excelOutput.folder is not defined in config.properties");
        }

        File outFolderFile = new File(outputFolder);
        if (!outFolderFile.exists()) outFolderFile.mkdirs();

        // Replace .pdf with .xlsx
        String excelName = pdfFileName.replaceAll("\\.pdf$", ".xlsx");
        File outFile = new File(outFolderFile, excelName);
        this.excelFilePath = outFile.getAbsolutePath();

        // Create workbook and sheet
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Data");

        // Create header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Page No");
        header.createCell(1).setCellValue("Patient Name");
        header.createCell(2).setCellValue("Chart ID");
    //    header.createCell(3).setCellValue("Provider Name"); // Optional
    }

    // Write data for a page
    public void ocrWriteExcelData(int pageNo, String chart, String patientName, ExtentTest testCaseScenario)
            throws IOException {
        int newRowNum = sheet.getLastRowNum() + 1;

        // Fill empty rows if needed
        while (newRowNum < pageNo) {
            sheet.createRow(newRowNum);
            newRowNum++;
        }

        Row row = sheet.createRow(newRowNum);
        row.createCell(0).setCellValue(pageNo);
        row.createCell(1).setCellValue(patientName != null ? patientName : "");
        row.createCell(2).setCellValue(chart != null ? chart : "");
   //     row.createCell(3).setCellValue(providerName != null ? providerName : "");

        System.out.println("Written: " + patientName + " (Page: " + pageNo + ", Chart: " + chart + ")");
        testCaseScenario.log(Status.INFO, "Written: " + patientName + " Page: " + pageNo + ", Chart: " + chart);

        saveWorkbook();
    }

    private void saveWorkbook() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
            workbook.write(fos);
        }
    }

    // ✅ Static helper method to simplify calling without passing folder
    public static void ocrWriteExcelDataStatic(String pdfFileName,
            int pageNo, String chart, String patientName,
            String providerName, ExtentTest testCaseScenario) throws IOException {

        Ocr_Text_CreateAndWriteExcelInFolder writer = new Ocr_Text_CreateAndWriteExcelInFolder(pdfFileName);
        writer.ocrWriteExcelData(pageNo, chart, patientName, testCaseScenario);
    }
}
