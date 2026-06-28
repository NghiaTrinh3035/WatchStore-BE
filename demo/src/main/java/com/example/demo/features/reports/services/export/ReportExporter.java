package com.example.demo.features.reports.services.export;

import com.example.demo.features.reports.dtos.response.DashboardSummaryResponse;

/**
 * Lớp trừu tượng định nghĩa Template Method cho việc xuất báo cáo.
 */
public abstract class ReportExporter {

    /**
     * TEMPLATE METHOD: Đóng gói quy trình (được đánh dấu là final để các lớp con không thể thay đổi thuật toán cốt lõi).
     */
    public final byte[] export(DashboardSummaryResponse summaryData) {
        initializeDocument();
        writeHeader(summaryData);
        writeData(summaryData);
        writeFooter();
        return finalizeDocument();
    }

    // Các phương thức mà lớp con (Concrete Class) BẮT BUỘC phải thực thi
    protected abstract void initializeDocument();
    
    protected abstract void writeHeader(DashboardSummaryResponse data);
    
    protected abstract void writeData(DashboardSummaryResponse data);
    
    // Hook Method: Có implementation mặc định, lớp con CÓ THỂ ghi đè hoặc không
    protected void writeFooter() {
        // Mặc định không làm gì. Ví dụ: Có thể in dòng bản quyền hoặc ngày tháng xuất báo cáo.
    }
    
    protected abstract byte[] finalizeDocument();
}
