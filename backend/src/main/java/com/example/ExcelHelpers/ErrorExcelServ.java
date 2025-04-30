// src/main/java/com/example/service/ErrorExcelService.java

package com.example.ExcelHelpers;

import com.example.dto.FailedRowInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface ErrorExcelServ {
    ByteArrayInputStream generateErrorExcel(List<FailedRowInfo> failedRows) throws IOException;
}