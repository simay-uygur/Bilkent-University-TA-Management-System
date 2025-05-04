package com.example.ExcelHelpers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.General.ClassRoom;

public class ClassRoomExcelHelper {
    // Check if the file is an Excel file based on content type.
    public static boolean hasExcelFormat(MultipartFile file) {
        String type = file.getContentType();
        return type != null && (type.equals("application/vnd.ms-excel") ||
                                type.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    public static List<ClassRoom> excelToClassrooms(InputStream is) {
        try {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<ClassRoom> classrooms = new ArrayList<>();
            int rowNumber = 0;

            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header row
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();
                ClassRoom classroom = new ClassRoom();

                int cellIndex = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIndex) {
                        case 0 -> {
                            // Column for Room Code (as String)
                            DataFormatter formatter = new DataFormatter();
                            classroom.setClassroomId(formatter.formatCellValue(currentCell));
                        }
                        case 1 -> // Column for Capacity (as numeric value; cast to integer)
                            classroom.setClassCapacity((int) currentCell.getNumericCellValue());
                        default -> {
                        }
                    }
                    cellIndex++;
                }
                classrooms.add(classroom);
            }
            workbook.close();
            return classrooms;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }
    }
}
