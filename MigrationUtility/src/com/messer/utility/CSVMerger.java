package com.messer.utility;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.csv.*;

public class CSVMerger {

    
    
    public void mergepart2(String filepath) throws IOException {
    	
    	//String inputFilePath = "C:/Users/akshay.kk/Desktop/export/Export 03-Oct-2023/test/2023-10-03 DALLAS COUNTY TAX ASSESSOR CO 128129.csv";
        //String outputFilePath = "C:/Users/akshay.kk/Desktop/export/Export 03-Oct-2023/test/2023-10-03 DALLAS COUNTY TAX ASSESSOR CO 128129.csv";

        // Read the original CSV file
        List<String> lines = Files.readAllLines(Paths.get(filepath), StandardCharsets.UTF_8);

        // Create a set to keep track of unique descriptions
        Set<String> uniqueDescriptions = new HashSet<>();
        List<String> modifiedLines = new ArrayList<>();
        List<String> Ac1= new ArrayList<>();
        // Iterate through the lines and process the data
        for (String line : lines) {
            String[] parts = line.split(",");
            String description = parts[3]; // Assuming Description is in the 4th column

            // Check if the description is unique
            if (!uniqueDescriptions.contains(description)) {
                // If unique, add it to the modified lines
            	Ac1.clear();
                modifiedLines.add(line);
                uniqueDescriptions.add(description);
                Ac1.add(parts[6]);
             
            } else {
            	
            	
            	Ac1.add(parts[6]);
            	
                // If not unique, update the appropriate values
                int index = modifiedLines.size() - 1;
                String[] existingParts = modifiedLines.get(index).split(",");
                //System.out.println(existingParts[4]+"  price   "+parts[4]);
                if(!existingParts[4].isEmpty()|| ! parts[4].isEmpty()) {
                BigDecimal totalPrice = new BigDecimal(parts[4]);
                if(!existingParts[4].isEmpty()) {
                BigDecimal existingPrice = new BigDecimal(existingParts[4]);
                BigDecimal updatedPrice = existingPrice.add(totalPrice);
                existingParts[4] = updatedPrice.toString();
                }else {
                	existingParts[4] = totalPrice.toString();
                }
                
               
                }

				
                
                // Update the total price
                
                
                
                if(!parts[6].equals("0000502501")) {
                	//AccountSegment3.add(parts[7]);
                	existingParts[7]= parts[7];
                	if(parts[7].isEmpty()) {
                		 existingParts[5]="Z";
                	}else {
                		existingParts[5]="K";
                	}
                	
                	
                	
                }
                if(!parts[6].equals("0000502501")) {
                	//AccountSegment3.add(parts[7]);
                	existingParts[8]= parts[8];
                	if(parts[8].isEmpty()) {
                		 existingParts[5]="Z";
                	}else {
                		existingParts[5]="H";
                	}
                	
                	
                	
                }
                
				
               
                
                if (Ac1.contains("0000502501")) {
                    for (String value : Ac1) {
                        if (!value.equals("0000502501")) {
                        	existingParts[6] = value;
                            break;
                        }
                    }
                    if (existingParts[6] == null) {
                    	existingParts[6] = "0000502501";
                    }
                } else if (!Ac1.isEmpty()) {
                	existingParts[6] = Ac1.get(Ac1.size() - 1);
                }

                
              //  System.out.println(AccountSegment3+"  AccountSegment3  "+description);

                // Join the updated parts and replace the existing line
                
                modifiedLines.set(index, String.join(",", existingParts));
            }
            
        }
        
       

        // Write the modified lines back to the original file
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filepath))) {
        	int lineCount = 0;

            for (String modifiedLine : modifiedLines) {
                if (lineCount >= 3) {
                    // Split the line into parts
                    String[] parts = modifiedLine.split(",");

                    if (parts.length >= 5) {
                        if(!parts[4].isEmpty())
                    	
                    	parts[4] = "$"+ parts[4];
                        
                    }

                    // Join the modified parts back into a single line
                    modifiedLine = String.join(",", parts);
                }

                System.out.println(modifiedLine);

                writer.write(modifiedLine);
                writer.newLine();

                lineCount++;
            }
        
        }

        System.out.println("File processing complete.");
	}
}


