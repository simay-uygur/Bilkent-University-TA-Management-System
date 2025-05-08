package com.example.ExcelHelpers;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/errors")
public class ErrorExcelController {

    private final ErrorExcelServ ErrorExcelServ;

    @Autowired
    public ErrorExcelController(ErrorExcelServ ErrorExcelServ) {
        this.ErrorExcelServ = ErrorExcelServ;
    }

    @PostMapping("/export")
    public ResponseEntity<InputStreamResource> exportFailedRows(@RequestBody List<FailedRowInfo> failedRows) {
        try {
            ByteArrayInputStream in = ErrorExcelServ.generateErrorExcel(failedRows);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=failed_rows.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}