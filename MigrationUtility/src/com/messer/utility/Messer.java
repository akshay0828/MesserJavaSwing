package com.messer.utility;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import com.toedter.calendar.JDateChooser;

public class Messer {
	
	static JPanel panel = new JPanel(new GridBagLayout());
	static JPanel orderbyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	static JFrame frame = new JFrame(" Messer Tax Department ");
	static String CurrentTable=null;
	 private static volatile Thread backgroundThread; // Reference to the background database operation thread
	 private static Timer timer; // Declare a Timer as a class variable
	 static JButton addButton = new JButton("Add Field");
	 static JButton displayButton = new JButton("Click here to Generate the report");
	 static JComboBox<String> tableDropdown = new JComboBox<>();
	 static JProgressBar progressBar = new JProgressBar(0, 100);
    public static void main(String[] args) {
    	
    	MesserFormStateManager formStateManager = new MesserFormStateManager();
    	final Properties mainProperties = new Properties();
    	 Connection connection = null;
    	 
    	 
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
    		JOptionPane.showMessageDialog(frame, "Data Base Properties file Missig.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
            
    		//e.printStackTrace();
		}
    	
    	
        final String dbpath = mainProperties.getProperty("messer.exportcsv.jdbc.path");
        
    	
        String url = "jdbc:ucanaccess://"+dbpath;
      
        
        // Create a JFrame (the main window)
        JFrame frame = new JFrame(" Messer Tax Department ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        JScrollPane scrollPane = new JScrollPane(panel);
        
     // Set the preferred size of the scroll pane (if needed)
        scrollPane.setPreferredSize(new Dimension(780, 780));
     // Set the background color to white
        // panel.setBackground(Color.WHITE);
        // Add the JPanel to the content pane of the frame
        // frame.getContentPane().add(panel);
        //frame.setVisible(true);

        // Create a JPanel to hold the form components
        
        
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 2); // Add some spacing
        
     // Create a label and a dropdown box for table selection
        JButton selectTableLabel = new JButton("Report Selection:");
        selectTableLabel.setPreferredSize(new Dimension(150, selectTableLabel.getPreferredSize().height));
        selectTableLabel.setBorder(BorderFactory.createEmptyBorder(0, -45, 0, 0));
        selectTableLabel.setBackground(Color.decode("#eeeeee"));
        selectTableLabel.setBorderPainted(false);
        Dimension dropdownSize = new Dimension(200, 25); // Set your desired width and height
        
        
        tableDropdown.setPreferredSize(dropdownSize);
        tableDropdown.setPrototypeDisplayValue("Select the value:"); // Display the message
        tableDropdown.addItem("Select a report");
		/*
		 * JButton loadTableButton = new JButton("Load Data"); // Button to load the
		 * selected table
		 */
        // Create a label and a dropdown box
        JButton selectColumnLabel  = new JButton("Input Parameter:");
        selectColumnLabel.setPreferredSize(new Dimension(150, selectColumnLabel.getPreferredSize().height));
        selectColumnLabel.setBorder(BorderFactory.createEmptyBorder(0, -45, 0, 0));
       selectColumnLabel.setBackground(Color.decode("#eeeeee"));
        selectColumnLabel.setBorderPainted(false);
        
        JComboBox<String> columnDropdown = new JComboBox<>();
        
        
        columnDropdown.setPreferredSize(dropdownSize);
        
        JButton OrderByLabel = new JButton("  Sorting Parameter:");
        OrderByLabel.setPreferredSize(new Dimension(150, OrderByLabel.getPreferredSize().height));
        OrderByLabel.setBorder(BorderFactory.createEmptyBorder(0, -45, 0, 0));
       OrderByLabel.setBackground(Color.decode("#eeeeee"));
       OrderByLabel.setBorderPainted(false);
      

       
     // Set the preferred width of the button, adjust the width as needed
        int buttonWidth = 125; // Change this value to your desired width
        int buttonHeight = addButton.getPreferredSize().height; // Maintain the original height

        addButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        


        JComboBox<String> OrderDropdown = new JComboBox<>();
        OrderDropdown.setPreferredSize(dropdownSize);
        JButton addOrderButton  = new JButton("Add"); // Button to load the selected table
        JButton ClearorderButton  = new JButton("Clear"); 
        
     // Create a DefaultListModel to hold the data for orderbylist
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> orderbylist = new JList<>(model); // Use the model to create the list
        

        // Create a HashMap to store dynamic text fields and delete buttons
        HashMap<String, JTextField> dynamicTextFields = new HashMap<>();
        HashMap<String, JButton> deleteButtons = new HashMap<>();
        HashMap<String, Component> dynamicComponents = new HashMap<>();
        
        
       
        Dimension progressBarSize = new Dimension(100, 24); // Set your desired width and height
        progressBar.setPreferredSize(progressBarSize);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        ImageIcon loadingIcon = new ImageIcon(Messer.class.getResource("loading.gif"));
        loadingIcon.setImage(loadingIcon.getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT)); // Adjust the width and height as needed
        JLabel loadingLabel = new JLabel(loadingIcon);
        
     // Create a button to display the values of the dynamic fields and perform the query
        
        displayButton.setBackground(Color.decode("#4cfcb3"));
        displayButton.setEnabled(false);
        
   
         
       
        // Add components to the panel using GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(selectTableLabel, gbc);
        // Add a button to load the selected table
        
        gbc.gridx = 1;
        gbc.gridy = 0;
       //gbc.gridwidth =20;
        panel.add(tableDropdown, gbc);
		/*
		 * gbc.gridx = 2; gbc.gridy = 0; panel.add(loadTableButton, gbc);
		 */
  
        
        // Add a label and dropdown for column selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(selectColumnLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(columnDropdown, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        panel.add(addButton, gbc);

        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(OrderByLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(OrderDropdown, gbc);
        gbc.gridx = 2;
        gbc.gridy = 2;
      
        orderbyPanel.add(addOrderButton, gbc);
        orderbyPanel.add(ClearorderButton, gbc);
        panel.add(orderbyPanel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(orderbylist, gbc);
        // Populate the dropdown box with column names
        
     final String export = mainProperties.getProperty("messer.exportcsv.csv.export");
		String[] exportValues = export.split(",");

		// Add each value to the tableDropdown
		for (String value : exportValues) {
			
		    tableDropdown.addItem(value.trim()); // Trim to remove leading/trailing spaces
		}
        
     // Load the last selected table and columns from the form state
        String lastSelectedTable = formStateManager.get("selectedTable");
        List<String> lastSelectedColumns = formStateManager.getSelectedColumns("test");

		
		// if (lastSelectedTable != null) {
		//  tableDropdown.setSelectedItem(lastSelectedTable); 
		//  }
		 
        if (!lastSelectedColumns.isEmpty()) {
            // Clear the existing items in the columnDropdown
            columnDropdown.removeAllItems();
            
            // Add the last selected columns to the columnDropdown
            /*for (String column : lastSelectedColumns) {
                columnDropdown.addItem(column);
            }*/
        }
        
        
        ClearorderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.removeAllElements(); // Clear the items in the DefaultListModel
            }
        });

        
        addOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the selected item from OrderDropdown
                String selectedItem = (String) OrderDropdown.getSelectedItem();
                // Check if the selected item is not null and not already in the list
                if (selectedItem != null && !model.contains(selectedItem)) {
                    model.addElement(selectedItem); // Add the selected item to the list
                }
            }
        });

        

     // Inside the "Load Table" button action listener
        tableDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	String selectedTableName = (String) tableDropdown.getSelectedItem();
            	//gbc.gridx = 2;
                //gbc.gridy = 0;
               //gbc.gridwidth =20;
            	 if (selectedTableName != "Select a report") {
                panel.add(loadingLabel);
            	 }
            	
            	
            	displayButton.setEnabled(true);
            	// Set the preferred width of the JComboBox
                Dimension dropdownSize = new Dimension(200, 25); // Set your desired width and height
                columnDropdown.setPreferredSize(dropdownSize);
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Surround with a border
            	formStateManager.set("selectedTable", (String) tableDropdown.getSelectedItem());
            	 model.removeAllElements(); // Clear the items in the DefaultListModel
            	 String lastSelectedTable = formStateManager.get("selectedTable");
            	 tableDropdown.setSelectedItem(lastSelectedTable); 
            	
            	for (Map.Entry<String, Component> entry : dynamicComponents.entrySet()) {
                    String columnName = entry.getKey();
                    Component component = entry.getValue();
                 // Check if components exist at LINE_START and LINE_END
                    Component lineStartComponent = getComponentAtLineStart(columnName);
                    Component lineEndComponent = getComponentAtLineEnd(columnName);
                    if (lineStartComponent != null) {
             
                        panel.remove(lineStartComponent);
                    }
                    else{
                    	
                    }
                    
                    if (lineEndComponent != null) {
                        panel.remove(lineEndComponent);
                    }
                    panel.remove(component);
                    
                    
                }

                dynamicComponents.clear();
             // Revalidate and repaint the panel to update the layout
                panel.revalidate();
                panel.repaint();

                
            	
            	ArrayList<String> CurrentcolumnNames = new ArrayList<>();
                
                
                if (selectedTableName != "Select a report") {
                	// Create an ExecutorService with a single thread
                    ExecutorService executorService = Executors.newSingleThreadExecutor();

                    Future<Void> future = executorService.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                    try {
                        // Update the selected table name in your existing logic
                        String tableName = selectedTableName;
                        CurrentTable = selectedTableName;
                        Connection connection = DriverManager.getConnection(url);
                        Statement statement = connection.createStatement();

                        // Populate the column selection dropdown with column names from the selected table
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM `CURRENT YR MESSER PAYMENT TABLE`");
                        ArrayList<String> columnNames = new ArrayList<>();
                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                            columnNames.add(resultSet.getMetaData().getColumnName(i));
                        }
                        CurrentcolumnNames.addAll(columnNames);
                        
                        resultSet.close();
                        statement.close();
                        connection.close();

                        // Clear the existing items in the columnDropdown
                        columnDropdown.removeAllItems();
                        OrderDropdown.removeAllItems();
                     // Set a placeholder for the dropdown
                        
panel.remove(loadingLabel);
panel.repaint();
panel.revalidate();
                        for (String columnName : columnNames) {
                            columnDropdown.addItem(columnName);
                            OrderDropdown.addItem(columnName);
                        }

                        // Do not clear the dynamicComponents map
                        String selectedColumnName = (String) columnDropdown.getSelectedItem();
                        
                        if (selectedColumnName != null) {
                            //formStateManager.set("selectedTable", selectedTableName);
                           // formStateManager.addSelectedColumn(selectedColumnName);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error\n Check the Database path or Filename", "Error", JOptionPane.ERROR_MESSAGE);
                        //return;
                    }
                    return null;
                        }
                    });
                    
                
               
                List<String> lastSelectedColumns = formStateManager.getSelectedColumns(selectedTableName);
                
            	if (lastSelectedTable != null) {
            	    tableDropdown.setSelectedItem(lastSelectedTable);
            	}
             // Add the last selected columns to the dynamicComponents map
            	
            	
            	if(!lastSelectedColumns.isEmpty()){
            	
                for (String column : lastSelectedColumns) {
                	if(!column.isEmpty()){
                    if (!dynamicComponents.containsKey(column)) {
                    	
                    	
                    	gbc.gridx = 0;
                        gbc.gridy++;
                        gbc.anchor = GridBagConstraints.WEST;
                     // Create a Font object with regular style
                        Font labelFont = new Font("Arial", Font.PLAIN, 12);
                        JLabel label = new JLabel(column + ":");
                        label.setFont(labelFont);
                        panel.add(label, gbc);

                        gbc.gridx = 1;
                        gbc.anchor = GridBagConstraints.LINE_START;

                        // Check if the column name contains "date" and create a JDateChooser
                        if (column.toLowerCase().contains("date")) {
                            JDateChooser dateChooser = new JDateChooser();
                            dateChooser.setDateFormatString("yyyy/MM/dd");
                         // Create a Dimension object with the desired width and height
                            Dimension preferredSize = new Dimension(225, 20); // Adjust the width and height as needed

                            // Set the preferred size of the dateChooser
                            dateChooser.setPreferredSize(preferredSize);
                            panel.add(dateChooser, gbc);
                            dynamicComponents.put(column, dateChooser);
                        } else {
                            JTextField textField = new JTextField(22); // Set a preferred width
                            panel.add(textField, gbc);
                            dynamicComponents.put(column, textField);
                        }

                        gbc.gridx = 2;
                        gbc.anchor = GridBagConstraints.LINE_END;
                        JButton deleteButton = new JButton("Delete");
                        panel.add(deleteButton, gbc);

                        frame.pack(); // Adjust the frame size
                        frame.setSize(800, 800);
                        deleteButtons.put(column, deleteButton);

                    // Add an action listener to the "Delete" button
                    deleteButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Remove the associated components
                           panel.remove(label);
                            panel.remove(dynamicComponents.get(column));
                            panel.remove(deleteButton);
                            dynamicComponents.remove(column);
                            deleteButtons.remove(column);

                            // Revalidate and repaint the panel to update the layout
                            panel.revalidate();
                            panel.repaint();
                            formStateManager.deleteSelectedColumn(column,selectedTableName);
                        }
                    });
                }
                	}else{
                		
                	
                	}		
            }
                }
               // dynamicComponents.clear();
               // lastSelectedColumns.clear();
            }
                else {
                	progressBar.setVisible(false);
                  	 stopProgress();
                  	 
                  	 displayButton.setEnabled(false);
                	
                	// Clear the existing items in the columnDropdown
                    columnDropdown.removeAllItems();
                    OrderDropdown.removeAllItems();
                 // Revalidate and repaint the panel to update the layout
                    panel.revalidate();
                    panel.repaint();
                }
            }
        });


     // Add an action listener to the "Add Field" button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedColumnName = (String) columnDropdown.getSelectedItem();
if(selectedColumnName == null ) {
	JOptionPane.showMessageDialog(frame, "Please Select the Report.", "Error", JOptionPane.ERROR_MESSAGE);
    // Exit the loop if any field is empty
	return;
}
                // Inside the "Add Field" button action listener
                if (!dynamicComponents.containsKey(selectedColumnName)) {
                    gbc.gridx = 0;
                    gbc.gridy++;
                    gbc.anchor = GridBagConstraints.WEST;
                    
                    Font labelFont = new Font("Arial", Font.PLAIN, 12);
                    JLabel label = new JLabel(selectedColumnName + ":");
                    label.setFont(labelFont);
                    panel.add(label, gbc);
                   

                    gbc.gridx = 1;
                    gbc.anchor = GridBagConstraints.LINE_START;

                    // Check if the column name contains "date" and create a JDateChooser
                    if (selectedColumnName.toLowerCase().contains("date")) {
                        JDateChooser dateChooser = new JDateChooser();
                        dateChooser.setDateFormatString("yyyy/MM/dd");
                     // Create a Dimension object with the desired width and height
                        Dimension preferredSize = new Dimension(225, 20); // Adjust the width and height as needed

                        // Set the preferred size of the dateChooser
                        dateChooser.setPreferredSize(preferredSize);
                        panel.add(dateChooser, gbc);
                        dynamicComponents.put(selectedColumnName, dateChooser);
                    } else {
                        JTextField textField = new JTextField(22); // Set a preferred width
                        panel.add(textField, gbc);
                        dynamicComponents.put(selectedColumnName, textField);
                    }

                    gbc.gridx = 2;
                    gbc.anchor = GridBagConstraints.LINE_END;
                    JButton deleteButton = new JButton("Delete");
                    panel.add(deleteButton, gbc);

                    frame.pack(); // Adjust the frame size
                    frame.setSize(900, 900);
                    deleteButtons.put(selectedColumnName, deleteButton);

                    // Save the selected column to the form state
                    String lastSelectedTable = formStateManager.get("selectedTable");
                    
                    formStateManager.addSelectedColumn(selectedColumnName,lastSelectedTable);

                    // Add an action listener to the "Delete" button
                    deleteButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Remove the associated components
                            panel.remove(label);
                            panel.remove(dynamicComponents.get(selectedColumnName));
                            panel.remove(deleteButton);
                            dynamicComponents.remove(selectedColumnName);
                            deleteButtons.remove(selectedColumnName);

                            // Revalidate and repaint the panel to update the layout
                            panel.revalidate();
                            panel.repaint();
                            
                            formStateManager.deleteSelectedColumn(selectedColumnName,lastSelectedTable);
                        }
                    });
                }
            }
        });


     
     // Create a rounded border
       
        gbc.gridx = 1;
        gbc.gridy++;
        gbc.gridwidth = 3; // Span three columns
        gbc.anchor = GridBagConstraints.LINE_START; // Center-align button
        panel.add(displayButton, gbc);
        
        // Add the progress bar
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
         panel.add(progressBar, gbc);

        displayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
            	// displayButton.setEnabled(false);
            	 
                

                 progressBar.setValue(0);
                 
                 progressBar.setStringPainted(true);
                 progressBar.setVisible(true);
                 startProgress(progressBar, displayButton);
                 
              // Start the database operation in a separate thread
                 backgroundThread = new Thread(new Runnable() {
                     @Override
                     public void run() {
            	
            	JPanel loadPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            	
            	
            	
                Map<String, String> columnValueMap = new HashMap<>();
                boolean allFieldsFilled = true; // Flag to check if all fields are filled

                for (String columnName : dynamicComponents.keySet()) {
                    Component component = dynamicComponents.get(columnName);
                    String value = "";

                    if (component instanceof JTextField) {
                        JTextField textField = (JTextField) component;
                        value = textField.getText().trim();
                    } else if (component instanceof JDateChooser) {
                        JDateChooser dateChooser = (JDateChooser) component;
                        if (dateChooser.getDate() != null) {
                            // Format the date to yyyy-MM-dd
                            java.util.Date selectedDate = dateChooser.getDate();
                            java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());
                            value = sqlDate.toString();
                        }
                    }

                    // Check if the field is empty
                    if (value.isEmpty()) {
                        allFieldsFilled = false;
                        progressBar.setVisible(false);
                   	 stopProgress();
                    	// Revalidate and repaint the panel to update the layout
                         panel.revalidate();
                         panel.repaint();
                        JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        break; // Exit the loop if any field is empty
                    }

                    columnValueMap.put(columnName, value);
                }

                // Proceed only if all fields are filled
                if (allFieldsFilled) {
                	
                	String queryFromMesser = null;
                	
                	String StartPartQuery = null;
                	String endPartQuery =null;
                	 
                      try {
                    	  final Properties queries = new Properties();
                          final String queriesPath = "./Config/messerqueries.properties";
						final FileInputStream fisQuries = new FileInputStream(queriesPath);
						if (fisQuries == null) {
			                System.out.println("The queries.properties file is missing...");
			                
			                System.exit(0);
			            }
			            queries.load(fisQuries);
			            final Set querySet = queries.keySet();
			            for (final Object object : querySet) {
			                final String fileName = object.toString();
			                final String splitFileName = fileName.split("<")[0];
			               
			              
			                queryFromMesser =queries.get(fileName).toString();
			                
			        
			               
			            }

						
					} catch (FileNotFoundException e2) {
						stopProgress();
						progressBar.setVisible(false);
						//e2.printStackTrace();
						JOptionPane.showMessageDialog(frame, "Query file Missig.", "Error", JOptionPane.ERROR_MESSAGE);
						
						return;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
                	
                      String sqlQuery = queryFromMesser;
                      // Find the index of "WHERE"
                      int startIndex = sqlQuery.indexOf("WHERE");
                      
                      // Find the index of "ORDER BY"
                      int endIndex = sqlQuery.indexOf("ORDER BY");
                      
                      if (startIndex != -1 && endIndex != -1) {
                          // Extract substring 1 (before "WHERE")
                           StartPartQuery = sqlQuery.substring(0, startIndex).trim();
                          
                          // Extract substring 2 (from "ORDER BY" to the end)
                           endPartQuery = sqlQuery.substring(endIndex).trim();
                          
                      } else {
                          System.out.println("Both 'WHERE' and 'ORDER BY' not found in the SQL query.");
                      }
                	
                	
                	
                	
                	
                	
					// Create a StringBuilder to build the SQL query
                    StringBuilder queryBuilder = new StringBuilder(StartPartQuery +" WHERE (");

                    for (Map.Entry<String, String> entry : columnValueMap.entrySet()) {
                        String columnName = entry.getKey();
                        String columnValue = entry.getValue();

                        // Append the column name and filter condition to the query
                        if(columnName.equals("Price")){
                        	if(!columnValue.matches(".*\\d.*")){
                        		progressBar.setVisible(false);
                           	 stopProgress();
                            	// Revalidate and repaint the panel to update the layout
                                 panel.revalidate();
                                 panel.repaint();
                        		JOptionPane.showMessageDialog(frame, "Enter valid Price value", "Error", JOptionPane.ERROR_MESSAGE);
                        		return;
                        	}
                        
                        queryBuilder.append("(([CURRENT YR MESSER PAYMENT TABLE].[").append(columnName).append("])>").append(columnValue).append(") AND ");
                        }else if(columnName.contains("Date")){
                        	 // Create a SimpleDateFormat for the input format
                            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");

                            // Create a SimpleDateFormat for the desired output format
                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String formattedDate = null;
                        	try {
								Date date = inputFormat.parse(columnValue);
								 formattedDate = outputFormat.format(date);
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
                        	queryBuilder.append("(([CURRENT YR MESSER PAYMENT TABLE].[").append(columnName).append("])=#").append(formattedDate).append("#) AND ");
                        }
                        else{
                        	queryBuilder.append("(([CURRENT YR MESSER PAYMENT TABLE].[").append(columnName).append("])='").append(columnValue).append("') AND ");
                        }
                    }

                    // Remove the trailing "AND" from the query
                    if (queryBuilder.toString().endsWith("AND ")) {
                        queryBuilder.delete(queryBuilder.length() - 4, queryBuilder.length());
                    }

                    // Get the final SQL query
                    String finalQuery = queryBuilder.toString()+")";
                
                	
                 // Get the data from the DefaultListModel and print to the console
                    StringBuilder endPartQueryBuilder = new StringBuilder(endPartQuery+" ");
                    
                    DefaultListModel<String> model = (DefaultListModel<String>) orderbylist.getModel();
                    int size = model.getSize();
                    
                    if(size!=0){
                    	endPartQueryBuilder.append("[CURRENT YR MESSER PAYMENT TABLE].[PAY TO],[CURRENT YR MESSER PAYMENT TABLE].[Description]");
                    for (int i = 0; i < size; i++) {
                        String item = model.getElementAt(i);
                        endPartQueryBuilder.append(",[CURRENT YR MESSER PAYMENT TABLE].[").append(item).append("]");
                    }
                    if (endPartQueryBuilder.toString().endsWith("],")) {
                    	endPartQueryBuilder.delete(endPartQueryBuilder.length() - 1, endPartQueryBuilder.length());
                    }
                    finalQuery= finalQuery+endPartQueryBuilder;
                    }else{
                    	finalQuery= finalQuery+"ORDER BY [CURRENT YR MESSER PAYMENT TABLE].[PAY TO],[CURRENT YR MESSER PAYMENT TABLE].[Description]";
                    }
                    
                    
                    
                    /*String end ="SELECT [CURRENT YR MESSER PAYMENT TABLE].ID, [CURRENT YR MESSER PAYMENT TABLE].[Initiated By], [CURRENT YR MESSER PAYMENT TABLE].[Site State], [CURRENT YR MESSER PAYMENT TABLE].[Vendor Number], [DEFAULT COLUMNS FOR COUPA].Invoice, [CURRENT YR MESSER PAYMENT TABLE].[Invoice Date], [DEFAULT COLUMNS FOR COUPA].[Submit for Approval?], [DEFAULT COLUMNS FOR COUPA].[Handling Amount], [DEFAULT COLUMNS FOR COUPA].[Misc Amount], [DEFAULT COLUMNS FOR COUPA].[Shipping Amount], [DEFAULT COLUMNS FOR COUPA].[Line Level Taxation], [DEFAULT COLUMNS FOR COUPA].[Requester Email], [CURRENT YR MESSER PAYMENT TABLE].[Chart of Accounts], [DEFAULT COLUMNS FOR COUPA].Currency, [CURRENT YR MESSER PAYMENT TABLE].[Image Scan Filename], [DEFAULT COLUMNS FOR COUPA].[Type of Document], [DEFAULT COLUMNS FOR COUPA].[Purchasing Organization], [CURRENT YR MESSER PAYMENT TABLE].[Remit To Code], [DEFAULT COLUMNS FOR COUPA].[Type of Invoice], [DEFAULT COLUMNS FOR COUPA].[invoice Line], [CURRENT YR MESSER PAYMENT TABLE].[Invoice Number], [CURRENT YR MESSER PAYMENT TABLE].[Supplier Number], [CURRENT YR MESSER PAYMENT TABLE].Description, [CURRENT YR MESSER PAYMENT TABLE].Price, [CURRENT YR MESSER PAYMENT TABLE].[Account Segment 1], [CURRENT YR MESSER PAYMENT TABLE].[Account Segment 2], [CURRENT YR MESSER PAYMENT TABLE].[Account Segment 3], [CURRENT YR MESSER PAYMENT TABLE].[Account Segment 4], [CURRENT YR MESSER PAYMENT TABLE].[Account Segment 5], [DEFAULT COLUMNS FOR COUPA].[Account Segment 6], [DEFAULT COLUMNS FOR COUPA].[Account Segment 7], [DEFAULT COLUMNS FOR COUPA].[Account Segment 8], [DEFAULT COLUMNS FOR COUPA].[Account Segment 9], [DEFAULT COLUMNS FOR COUPA].[Commodity Name], [CURRENT YR MESSER PAYMENT TABLE].[PAY TO], [CURRENT YR MESSER PAYMENT TABLE].[PAY TO 2], [CURRENT YR MESSER PAYMENT TABLE].STREET, [CURRENT YR MESSER PAYMENT TABLE].[STREET 2], [CURRENT YR MESSER PAYMENT TABLE].CITY, [CURRENT YR MESSER PAYMENT TABLE].STATE, [CURRENT YR MESSER PAYMENT TABLE].ZIP, [CURRENT YR MESSER PAYMENT TABLE].[PTMS Co Code], [CURRENT YR MESSER PAYMENT TABLE].[Account / Parcel], [CURRENT YR MESSER PAYMENT TABLE].[Customer Code], [CURRENT YR MESSER PAYMENT TABLE].[Billing Jurisdiction (PTMS)], [CURRENT YR MESSER PAYMENT TABLE].[Account AV], [CURRENT YR MESSER PAYMENT TABLE].[Assessed Value], [CURRENT YR MESSER PAYMENT TABLE].[Tax Bill Amt], [CURRENT YR MESSER PAYMENT TABLE].[ALLOC %], [CURRENT YR MESSER PAYMENT TABLE].[Site Name], [CURRENT YR MESSER PAYMENT TABLE].[Site Address 1], [CURRENT YR MESSER PAYMENT TABLE].[DO NOT USE 1], [CURRENT YR MESSER PAYMENT TABLE].[DO NOT USE 2], [CURRENT YR MESSER PAYMENT TABLE].[DO NOT USE 3], [CURRENT YR MESSER PAYMENT TABLE].[DO NOT USE 4], [CURRENT YR MESSER PAYMENT TABLE].[ALLOC TAX AMT]"+
                    		"FROM [CURRENT YR MESSER PAYMENT TABLE] INNER JOIN [DEFAULT COLUMNS FOR COUPA] ON [CURRENT YR MESSER PAYMENT TABLE].ID = [DEFAULT COLUMNS FOR COUPA].[Row Number]"+
                    		"WHERE ((([CURRENT YR MESSER PAYMENT TABLE].[Initiated By])='AA') AND (([CURRENT YR MESSER PAYMENT TABLE].[Site State])='TX') AND (([CURRENT YR MESSER PAYMENT TABLE].[Invoice Date])=#1/6/2023#) AND (([CURRENT YR MESSER PAYMENT TABLE].[Price])>0))"+
                    		"ORDER BY [CURRENT YR MESSER PAYMENT TABLE].[Vendor Number], [CURRENT YR MESSER PAYMENT TABLE].Description;";
*/
                    try (Connection connection = DriverManager.getConnection(url)) {
                        PreparedStatement statement = connection.prepareStatement(finalQuery);
                       final ResultSet resultSet = statement.executeQuery();
                       ResultSet dataAvailable=statement.executeQuery();;
                       
                        if (!dataAvailable.next()) { // Check if there is no data in the result set
                            allFieldsFilled = false;
                            progressBar.setVisible(false);
                       	 stopProgress();
                        	// Revalidate and repaint the panel to update the layout
                             panel.revalidate();
                             panel.repaint();
                            JOptionPane.showMessageDialog(frame, "No data Available for the given Input parameters", "Error", JOptionPane.ERROR_MESSAGE);
                            // No need for break here as it's not in a loop
                            return;
                        }


                        MesserCSVWriter csv = new MesserCSVWriter();

                        try {
                        	String selectedTableName = (String) tableDropdown.getSelectedItem();
                        	int selectedIndex= tableDropdown.getSelectedIndex();
                        	
                        	csv.sortDataPart1(resultSet,selectedIndex);
							/*
							 * if(selectedTableName.equals("Part 1")) {
							 * csv.sortDataPart1(resultSet,selectedTableName); selectedTableName =null; }
							 * else { csv.sortDataPart2(resultSet); selectedTableName=null; }
							 */
                        } catch (IOException e1) {
                        	progressBar.setVisible(false);
                       	 stopProgress();
                        	// Revalidate and repaint the panel to update the layout
                             panel.revalidate();
                             panel.repaint();
                        	 JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                           return;
                        	 // e1.printStackTrace();
                        }

                    } catch (SQLException z) {
                    	progressBar.setVisible(false);
                   	 stopProgress();
                    	// Revalidate and repaint the panel to update the layout
                         panel.revalidate();
                         panel.repaint();
                    	 JOptionPane.showMessageDialog(frame, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
                    	
                         return;
                    }
                    if(!columnValueMap.keySet().isEmpty()){
                    	stopProgress();
                    	progressBar.setValue(100);
                    	
                      	 
                    	 StringBuilder message = new StringBuilder("The CSV files are exported Successfully \n for the input Parameters:\n");
                         for (String columnName : columnValueMap.keySet()) {
                             String value = columnValueMap.get(columnName);
                             message.append(columnName).append(": ").append(value).append("\n");
                         }
                         
                         Icon successIcon;
						JOptionPane.showMessageDialog(frame, message.toString(),"Success", 1);
						progressBar.setVisible(false);
                       	// Revalidate and repaint the panel to update the layout
                            panel.revalidate();
                            panel.repaint();
						 
                    }
                    else {
                    	progressBar.setVisible(false);
                   	 stopProgress();
                    	// Revalidate and repaint the panel to update the layout
                         panel.revalidate();
                         panel.repaint();
                    	JOptionPane.showMessageDialog(frame, "Please Select the column", "Error", JOptionPane.ERROR_MESSAGE);
                    	 
                    }

                   
                }
                
                     }
                 });
                 backgroundThread.start();
            }
        });


        // Add the panel to the frame
        frame.add(panel);

        // Make the frame visible
        frame.setVisible(true);
        
        
    }
    private static void startProgress(JProgressBar progressBar, JButton displayButton) {
    	
    	displayButton.setEnabled(false);
    	addButton.setEnabled(false);
    	tableDropdown.setEnabled(false);
         timer = new Timer(600, new ActionListener() {
            private int progressValue = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (progressValue < 100) {
                    progressValue++;
                    progressBar.setValue(progressValue);
                } else {
                    ((Timer) e.getSource()).stop();
                    displayButton.setEnabled(true);
                }
            }
        });
        timer.start();
    }
    private static void stopProgress() {
    	displayButton.setEnabled(true);
    	addButton.setEnabled(true);
    	tableDropdown.setEnabled(true);
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

 // Get the component at LINE_START
    private static Component getComponentAtLineStart(String columnName) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JLabel) {
                GridBagConstraints gbc = ((GridBagLayout) panel.getLayout()).getConstraints(component);
                //if (gbc.anchor == GridBagConstraints.LINE_START) {
                    String labelName = ((JLabel) component).getText();
                    
                   // if (labelName.equals(columnName)) {
                        return component;
                   // }
               // }
            }
        }
        
        return null;
    }

    // Get the component at LINE_END
    private static Component getComponentAtLineEnd(String columnName) {
        for (Component component : panel.getComponents()) {
            GridBagConstraints gbc = ((GridBagLayout) panel.getLayout()).getConstraints(component);
            if (gbc.anchor == GridBagConstraints.LINE_END) {
                return component;
            }
        }
        return null;
    }
}
