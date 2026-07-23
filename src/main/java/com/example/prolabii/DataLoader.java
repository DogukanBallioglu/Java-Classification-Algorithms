package com.example.prolabii;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public static List<UserRecord> loadData(String filePath) {
        List<UserRecord> recordList = new ArrayList<>();

        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int errorCount = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Başlığı atla

                try {
                    String clientCodeStr = formatter.formatCellValue(row.getCell(9)).trim();
                    String lineNetTotalStr = formatter.formatCellValue(row.getCell(7)).trim();
                    String brandCodeStr = formatter.formatCellValue(row.getCell(10)).trim();
                    String category = formatter.formatCellValue(row.getCell(12)).trim();
                    String genderStr = formatter.formatCellValue(row.getCell(17)).trim();

                    if (clientCodeStr.isEmpty() || lineNetTotalStr.isEmpty() || category.isEmpty() || genderStr.isEmpty()) {
                        continue;
                    }

                    clientCodeStr = clientCodeStr.replaceAll("[^\\d.,-]", "").replace(",", ".");
                    lineNetTotalStr = lineNetTotalStr.replaceAll("[^\\d.,-]", "").replace(",", ".");
                    brandCodeStr = brandCodeStr.replaceAll("[^\\d.,-]", "").replace(",", ".");

                    int clientCode = (int) Double.parseDouble(clientCodeStr);
                    double lineNetTotal = Double.parseDouble(lineNetTotalStr);

                    int brandCode = 0;
                    if (!brandCodeStr.isEmpty()) {
                        brandCode = (int) Double.parseDouble(brandCodeStr);
                    }

                    double genderEncoded = PreProcessor.encodeGender(genderStr);

                    if (genderEncoded == -1.0) {
                        System.out.println(row.getRowNum() + ". Satır çöpe gitti çünkü Cinsiyet formatı bozuk: '" + genderStr + "'");
                        continue;
                    }

                    UserRecord record = new UserRecord(clientCode, genderEncoded, lineNetTotal, brandCode, category);
                    recordList.add(record);

                } catch (Exception e) {
                    errorCount++;
                    if (errorCount <= 10) {
                        System.out.println(row.getRowNum() + ". Satır Atlandı. Sebebi: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Excel dosyası okunurken hata oluştu: " + e.getMessage());
        }

        return recordList;
    }
}