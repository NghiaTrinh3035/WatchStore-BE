package com.example.demo.features.reports.services.export;

import com.example.demo.features.reports.dtos.response.DashboardSummaryResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

/**
 * Concrete Class thực thi Template Method để xuất báo cáo ra định dạng PDF.
 */
@Component("pdfExporter")
public class PdfReportExporter extends ReportExporter {

    private Document document;
    private ByteArrayOutputStream out;

    @Override
    protected void initializeDocument() {
        document = new Document(PageSize.A4);
        out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();
    }

    @Override
    protected void writeHeader(DashboardSummaryResponse data) {
        try {
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Paragraph title = new Paragraph("BAO CAO TONG QUAN DOANH THU CUA HANG", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi khi ghi tiêu đề PDF: " + e.getMessage(), e);
        }
    }

    @Override
    protected void writeData(DashboardSummaryResponse data) {
        try {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{6f, 4f});

            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

            PdfPCell hcell;
            hcell = new PdfPCell(new Phrase("Ten Chi So", headFont));
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hcell.setBackgroundColor(Color.LIGHT_GRAY);
            hcell.setPadding(8);
            table.addCell(hcell);

            hcell = new PdfPCell(new Phrase("Gia Tri Dat Duoc", headFont));
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hcell.setBackgroundColor(Color.LIGHT_GRAY);
            hcell.setPadding(8);
            table.addCell(hcell);

            Font textFont = FontFactory.getFont(FontFactory.HELVETICA);

            writeRow(table, "Tong Doanh Thu (VND)", String.valueOf(data.getTotalRevenue()), textFont);
            writeRow(table, "Tong So Don Hang Da Xu Ly", String.valueOf(data.getTotalOrders()), textFont);
            writeRow(table, "Khach Hang Moi Trong Ky", String.valueOf(data.getNewCustomers()), textFont);
            writeRow(table, "San Pham Da Ban Duoc", String.valueOf(data.getTotalProductsSold()), textFont);

            document.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi khi ghi dữ liệu PDF: " + e.getMessage(), e);
        }
    }

    private void writeRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell cell;
        
        cell = new PdfPCell(new Phrase(label, font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(8);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(value, font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(8);
        table.addCell(cell);
    }

    @Override
    protected void writeFooter() {
        try {
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY);
            Paragraph footer = new Paragraph("Tao tu dong boi He thong Quan Ly Watch Store", footerFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(30);
            document.add(footer);
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi khi ghi footer PDF: " + e.getMessage(), e);
        }
    }

    @Override
    protected byte[] finalizeDocument() {
        document.close();
        return out.toByteArray();
    }
}
