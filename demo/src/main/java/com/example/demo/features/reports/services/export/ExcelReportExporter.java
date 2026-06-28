package com.example.demo.features.reports.services.export;

import com.example.demo.features.reports.dtos.response.DashboardSummaryResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Concrete Class thực thi Template Method để xuất báo cáo ra định dạng Excel.
 */
@Component("excelExporter")
public class ExcelReportExporter extends ReportExporter {

    private Workbook workbook;
    private Sheet sheet;
    private int currentRowNum;

    @Override
    protected void initializeDocument() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Báo Cáo Doanh Thu");
        currentRowNum = 0;
    }

    @Override
    protected void writeHeader(DashboardSummaryResponse data) {
        Row headerRow = sheet.createRow(currentRowNum++);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("BÁO CÁO TỔNG QUAN DOANH THU CỬA HÀNG");
        
        // In đậm cho tiêu đề
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        headerCell.setCellStyle(style);
        
        currentRowNum++; // Để trống một dòng cho đẹp
    }

    @Override
    protected void writeData(DashboardSummaryResponse data) {
        // Style cho dòng tiêu đề cột
        CellStyle headerStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        headerStyle.setFont(boldFont);

        // Tạo dòng tiêu đề cột
        Row rowTitle = sheet.createRow(currentRowNum++);
        Cell c0 = rowTitle.createCell(0);
        c0.setCellValue("Tên Chỉ Số");
        c0.setCellStyle(headerStyle);
        
        Cell c1 = rowTitle.createCell(1);
        c1.setCellValue("Giá Trị Đạt Được");
        c1.setCellStyle(headerStyle);

        // Đổ dữ liệu
        writeRow("Tổng Doanh Thu (VNĐ)", String.valueOf(data.getTotalRevenue()));
        writeRow("Tổng Số Đơn Hàng Đã Xử Lý", String.valueOf(data.getTotalOrders()));
        writeRow("Khách Hàng Mới Trong Kỳ", String.valueOf(data.getNewCustomers()));
        writeRow("Sản Phẩm Đã Bán Được", String.valueOf(data.getTotalProductsSold()));
        
        // Auto size cho các cột
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void writeRow(String label, String value) {
        Row row = sheet.createRow(currentRowNum++);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    @Override
    protected void writeFooter() {
        currentRowNum++; // Cách 1 dòng
        Row footerRow = sheet.createRow(currentRowNum++);
        footerRow.createCell(0).setCellValue("Tạo tự động bởi Hệ thống Quản Lý Watch Store");
    }

    @Override
    protected byte[] finalizeDocument() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            workbook.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Có lỗi xảy ra trong quá trình tạo file Excel: " + e.getMessage(), e);
        }
    }
}
