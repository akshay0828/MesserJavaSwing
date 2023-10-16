package com.messer.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.math.Fraction;

public class MesserCSVWriter {
	
	
	public void sortDataPart1(ResultSet resultSet,int selectedValue) throws IOException, SQLException {
		final Properties mainProperties = new Properties();
		 
    	try{
    
    	
        final String dbPropertiesPath = "./Config/messerdatabase.properties";
        
        final FileInputStream file = new FileInputStream(dbPropertiesPath);
        if (file == null) {
            System.out.println("Please sourcedatabase.properties file with source database configurations is missing.....");
            System.exit(0);
        }
        else {
            mainProperties.load(file);
        }
    	}catch (Exception e) {
    		e.printStackTrace();
		}
	    // Create a map to store CSV writers based on "PAY TO" values
	    Map<String, FileWriter> csvWriters = new HashMap<>();
	    String folderPath = null;
	    ResultSetMetaData metaData = resultSet.getMetaData();
	    int columnCount = metaData.getColumnCount();
	 // Get the current date
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        String folderDate = currentDate.format(dateFormatter);
        // Get the current time
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH mm ss");
        String formattedTime = currentTime.format(timeFormatter);
	    // Find the index of the "PAY TO" column
	    int payToColumnIndex = -1;
	    int supplierNumberColumnIndex =-1;
	    int initiatedByColumnIndex= -1;
	    int siteStateColumnIndex =-1;
	    for (int i = 1; i <= columnCount; i++) {
	        if ("PAY TO".equals(metaData.getColumnName(i))) {
	            payToColumnIndex = i;
	            //break;
	        }
	        if ("Supplier Number".equals(metaData.getColumnName(i))) {
	        	supplierNumberColumnIndex = i;
	            //break;
	        }
	        if ("Initiated By".equals(metaData.getColumnName(i))) {
	        	initiatedByColumnIndex = i;
	            //break;
	        }
	        if("Site State".equals(metaData.getColumnName(i))) {
	        	siteStateColumnIndex=i;
	        	
	        }
	    }

	    if (payToColumnIndex == -1) {
	        // "PAY TO" column not found, handle this case as needed
	        return;
	    }

	    // Define the list of headers you want to include in the CSV
	    List<String> desiredHeaders = Arrays.asList(
	        "Invoice", "Invoice Number", "Supplier Number", "Invoice Date", "Submit for Approval?",
	        "Handling Amount", "Misc Amount", "Shipping Amount", "Line Level Taxation", "Requester Email",
	        "Chart of Accounts", "Currency", "Image Scan Filename", "Type of Document", "Purchasing Organization",
	        "Remit To Code", "Type of Invoice", "invoice Line", "Invoice Number", "Supplier Number",
	        "Description", "Price", "Account Segment 1", "Account Segment 2", "Account Segment 3",
	        "Account Segment 4", "Account Segment 5", "Account Segment 6", "Account Segment 7", "Account Segment 8",
	        "Account Segment 9", "Commodity Name"
	    );
	 // Define a flag to check if it's the first iteration
        boolean isFirstIteration = true;

        // Define a flag to skip the first 17 cells and append data for the rest
        boolean skipFirst17Data = true;
        int count= 0;
        String previouspayToValue;
        
        List<String> paytovalues = new ArrayList();

	    // Iterate through the resultSet and create CSV writers based on "PAY TO" value
	    while (resultSet.next()) {
	    	
	    	
	        String payToValue = resultSet.getString(payToColumnIndex);
	        String supplierNumberValue = resultSet.getString(supplierNumberColumnIndex);
	        String initiatedByValue = resultSet.getString(initiatedByColumnIndex);
	        String siteStateByValue = resultSet.getString(siteStateColumnIndex);
	     // Remove leading zeros
	        supplierNumberValue = supplierNumberValue.replaceFirst("^0+", "");
	     
	        
	        
	        if (payToValue == null) {
	            payToValue = "UNKNOWN"; // Use "unknown" if the value is null
	        }
	        isFirstIteration= !paytovalues.contains(payToValue);
	        paytovalues.add(payToValue);
	        final String exportPath = mainProperties.getProperty("messer.exportcsv.csv.export.path");
	     // Create a folder with the current date
	         folderPath = "./"+exportPath+"/" + "Export " + folderDate+"/"+initiatedByValue+" "+formattedTime;
	        File folder = new File(folderPath);
	        folder.mkdirs();

	        // Your code to work with payToValue and CSV files...
	        
	        if (!csvWriters.containsKey(payToValue)) {
	            String filename = folderPath + "/" +currentDate+ " " +siteStateByValue+" " +payToValue + " " + supplierNumberValue + ".csv";
	            FileWriter csvWriter = null;
	            
	            try {
	                csvWriter = new FileWriter(filename);
	                csvWriters.put(payToValue, csvWriter);

	                // Write headers to the new CSV file
	                for (String header : desiredHeaders) {
	                    if ("Type of Invoice".equals(header)) {
	                        csvWriter.append(header);
	                        csvWriter.append("\n");
	                    } else {
	                        csvWriter.append(header);
	                        csvWriter.append(",");
	                    }
	                }
	                csvWriter.append("\n");
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	     
	        }
	        
	        // Write data to the corresponding CSV file
	        FileWriter csvWriter = csvWriters.get(payToValue);
	        for (String header : desiredHeaders) {
	            String data;
	           
	            if (isFirstIteration) {
	            	
	            	
	                // In the first iteration, print headers as usual
	                if ("Type of Invoice".equals(header)) {
	                    Integer columnIndex = getColumnIndex(metaData, header);
	                    if (columnIndex != null) {
	                        data = resultSet.getString(columnIndex);
	                        if (data != null) {
	                            csvWriter.append(data);
	                        }
	                        csvWriter.append("\n");
	                    }
	                } else {
	                    Integer columnIndex = getColumnIndex(metaData, header);
	                    if (columnIndex != null) {
	                        data = resultSet.getString(columnIndex);
	                        if (data != null) {
	                        	if("Invoice Date".equals(header)){
	                        		// Split the input string by space to separate date and time
	                                String[] parts = data.split(" ");

	                                // Extract the date portion (the first part)
	                                String formattedDate = parts[0];
	                                csvWriter.append(formattedDate);
	                        	}else{
	                        		
	                        		if("Price".equals(header)&& selectedValue== 1){
		                        		csvWriter.append("$"+data);
		                        	}else if("Description".equals(header)) {
		                        	
		                        		
		                                csvWriter.append(data);
		                        	}
		                        	else {
		                        		csvWriter.append(data);
		                        	}
	                        		
	                        	}
	                        }
	                        csvWriter.append(",");
	                    }
	                }
	            } else {
	                // If it's not the first iteration, skip the first 17 cells and append data for the rest
	                if (skipFirst17Data) {
	                    count++;
	                }
	                if (count == 18) {
	                    csvWriter.append("\n");
	                }
	                if (count < 18) {
	                    csvWriter.append("");
	                } else {
	                    Integer columnIndex = getColumnIndex(metaData, header);
	                    if (columnIndex != null) {
	                        data = resultSet.getString(columnIndex);
	                        if (data != null) {
	                        	if("Price".equals(header)&& selectedValue== 1){
	                        		csvWriter.append("$"+data);
	                        	}else if("Description".equals(header)) {
	                        	
	                        		 csvWriter.append(data);
	                        	}
	                        	else {
	                        		csvWriter.append(data);
	                        	}
	                        }
	                        csvWriter.append(",");
	                    }
	                }
	            }
	            
	        }
	     // After the first iteration, set the flag to false and enable skipFirst17Data
	        count=0;
	        previouspayToValue=payToValue;
	        isFirstIteration = false;
	        skipFirst17Data = true;
	    }
	 
	    // Close all CSV writers
	    for (FileWriter writer : csvWriters.values()) {
	        writer.close();
	    }
	    
	    if(selectedValue== 2) {
	    	sortDataPart2(folderPath);
	    }
	    
	}




	

    private static Integer getColumnIndex(ResultSetMetaData metaData, String columnName) throws SQLException {
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (columnName.equals(metaData.getColumnName(i))) {
                return i;
            }
        }
        return null;
    }




	public void sortDataPart2(String directoryPath ) throws IOException  {
		//String directoryPath = "C:/Users/akshay.kk/Desktop/export/Export 03-Oct-2023/test";
        File directory = new File(directoryPath);
CSVMerger csvMerger = new CSVMerger();
        // Check if the directory exists
        if (directory.exists() && directory.isDirectory()) {
            // List all files in the directory
            File[] files = directory.listFiles();

            // Iterate through the files and print the paths of CSV files
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".csv")) {
                    String filePath = file.getAbsolutePath();
                    System.out.println(filePath);
                    csvMerger.mergepart2(filePath);
                }
            }
        } else {
            System.err.println("Directory does not exist or is not a directory.");
        }
    
		
	}

	/*
	 * public static void main(String[] args) throws IOException { MesserCSVWriter
	 * csvWriter = new MesserCSVWriter(); csvWriter.
	 * sortDataPart2("C:/Users/akshay.kk/Desktop/export/Export 03-Oct-2023/test"); }
	 */
	
	
	
   
}
