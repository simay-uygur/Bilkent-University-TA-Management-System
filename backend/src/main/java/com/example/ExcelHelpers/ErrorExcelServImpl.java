package com.example.ExcelHelpers;

import com.example.dto.FailedRowInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ErrorExcelServImpl implements ErrorExcelServ {

    @Override
    public ByteArrayInputStream generateErrorExcel(List<FailedRowInfo> failedRows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Failed Rows");

            // Header
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Row Number");
            headerRow.createCell(1).setCellValue("Error Message");

            // Data
            int rowIdx = 1;
            for (FailedRowInfo fail : failedRows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(fail.getRowNumber());
                row.createCell(1).setCellValue(fail.getErrorMessage());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}