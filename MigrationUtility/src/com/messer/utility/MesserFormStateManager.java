package com.messer.utility;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class MesserFormStateManager {
    private static final String FORM_STATE_FILE = "./Config/messerformstate.properties";
    private Properties properties;

    public MesserFormStateManager() {
        properties = new Properties();
        loadFormState();
    }
    public void set(String key, String value) {
        properties.setProperty(key, value);
        saveFormState();
    }
    public String get(String key) {
        return properties.getProperty(key);
    }

    public List<String> getSelectedColumns(String currentfile) {
    	currentfile = currentfile.replace(" ", "");
        String selectedColumnsProperty = properties.getProperty("selectedColumns"+currentfile);
        if (selectedColumnsProperty != null) {
            String[] columnsArray = selectedColumnsProperty.split(",");
            List<String> selectedColumns = new ArrayList<>();
            for (String column : columnsArray) {
                selectedColumns.add(column);
            }
            return selectedColumns;
        }
        return new ArrayList<>();
    }

    public void addSelectedColumn(String columnName,String currentfile) {
    	currentfile = currentfile.replace(" ", "");
        List<String> selectedColumns = getSelectedColumns(currentfile);
        selectedColumns.add(columnName);

        // Convert the list to a comma-separated string
        String selectedColumnsString = String.join(",", selectedColumns);

        properties.setProperty("selectedColumns"+currentfile, selectedColumnsString);
        saveFormState();
    }

    private void loadFormState() {
        try (FileInputStream fis = new FileInputStream(FORM_STATE_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            // Handle the exception or simply ignore if the file doesn't exist
        }
    }

    private void saveFormState() {
        try (FileOutputStream fos = new FileOutputStream(FORM_STATE_FILE)) {
            properties.store(fos, "Form State");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void deleteSelectedColumn(String selectedColumnName,String currentfile) {
    	currentfile = currentfile.replace(" ", "");
        List<String> selectedColumns = getSelectedColumns(currentfile);

        // Create a new list without the specified column name
        List<String> updatedSelectedColumns = selectedColumns.stream()
                .filter(column -> !column.equals(selectedColumnName))
                .collect(Collectors.toList());

        // Convert the updated list back to a comma-separated string
        String updatedSelectedColumnsString = String.join(",", updatedSelectedColumns);

        properties.setProperty("selectedColumns"+currentfile, updatedSelectedColumnsString);
        saveFormState();
    }
}

