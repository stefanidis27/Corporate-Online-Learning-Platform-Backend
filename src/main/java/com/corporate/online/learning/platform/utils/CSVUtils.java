package com.corporate.online.learning.platform.utils;

import com.corporate.online.learning.platform.exception.report.ReportCreateTemporaryFileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class CSVUtils {

    public static File createCSVFile(List<List<String>> dataLines, String fileName) {
        File csvOutputFile = new File(fileName);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(CSVUtils::convertToCSV)
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            throw new ReportCreateTemporaryFileException("[Report Creation Error] CSV file creation failed.");
        }

        return csvOutputFile;
    }

    private static String convertToCSV(List<String> data) {
        return data.stream()
                .map(CSVUtils::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }

        return escapedData;
    }
}
