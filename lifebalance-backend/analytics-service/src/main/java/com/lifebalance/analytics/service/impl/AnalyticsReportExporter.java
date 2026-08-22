package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.domain.ReportExportFormat;
import com.lifebalance.analytics.dto.AnalyticsReportExport;
import com.lifebalance.analytics.dto.AnalyticsReportResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Component;

@Component
class AnalyticsReportExporter {

    AnalyticsReportExport export(AnalyticsReportResponse report, ReportExportFormat format) {
        List<FieldValue> fields = fields(report);
        byte[] content = switch (format) {
            case CSV -> csv(fields);
            case EXCEL -> xlsx(fields);
            case PDF -> pdf(fields);
        };
        return new AnalyticsReportExport(filename(report.id(), format), format.contentType(), content);
    }

    private static List<FieldValue> fields(AnalyticsReportResponse report) {
        return List.of(
                new FieldValue("Report ID", text(report.id())),
                new FieldValue("Owner ID", text(report.ownerId())),
                new FieldValue("Actor ID", text(report.actorId())),
                new FieldValue("Report Type", text(report.reportType())),
                new FieldValue("Status", text(report.status())),
                new FieldValue("Dimension", text(report.dimension())),
                new FieldValue("Period Start", text(report.periodStart())),
                new FieldValue("Period End", text(report.periodEnd())),
                new FieldValue("Task Count", text(report.taskCount())),
                new FieldValue("Actual Record Count", text(report.actualRecordCount())),
                new FieldValue("Total Actual Minutes", text(report.totalActualMinutes())),
                new FieldValue("Total Actual Cost", decimal(report.totalActualCost())),
                new FieldValue("Currency Code", text(report.currencyCode())),
                new FieldValue("Average Efficiency Percent", decimal(report.averageEfficiencyPercent())),
                new FieldValue("Variance Summary", text(report.varianceSummary())),
                new FieldValue("Generated At", timestamp(report.generatedAt())),
                new FieldValue("Archived At", timestamp(report.archivedAt())),
                new FieldValue("Reason", text(report.reason()))
        );
    }

    private static byte[] csv(List<FieldValue> fields) {
        StringBuilder builder = new StringBuilder("Field,Value\r\n");
        for (FieldValue field : fields) {
            builder.append(csvValue(field.name()))
                    .append(',')
                    .append(csvValue(field.value()))
                    .append("\r\n");
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] xlsx(List<FieldValue> fields) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            zip(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                      <Default Extension="xml" ContentType="application/xml"/>
                      <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                      <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    </Types>
                    """);
            zip(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>
                    """);
            zip(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                              xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                      <sheets>
                        <sheet name="Report" sheetId="1" r:id="rId1"/>
                      </sheets>
                    </workbook>
                    """);
            zip(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                    </Relationships>
                    """);
            zip(zip, "xl/worksheets/sheet1.xml", worksheet(fields));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate Excel export.", exception);
        }
        return output.toByteArray();
    }

    private static byte[] pdf(List<FieldValue> fields) {
        List<String> lines = new ArrayList<>();
        lines.add("LifeBalance Analytics Report");
        lines.add("");
        for (FieldValue field : fields) {
            lines.addAll(wrap(field.name() + ": " + field.value(), 88));
        }

        StringBuilder stream = new StringBuilder();
        stream.append("BT\n/F1 11 Tf\n50 760 Td\n");
        for (String line : lines) {
            stream.append('(').append(pdfEscape(ascii(line))).append(") Tj\n0 -16 Td\n");
        }
        stream.append("ET\n");

        byte[] content = stream.toString().getBytes(StandardCharsets.ISO_8859_1);
        List<byte[]> objects = List.of(
                asciiBytes("<< /Type /Catalog /Pages 2 0 R >>"),
                asciiBytes("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"),
                asciiBytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
                        + "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>"),
                asciiBytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"),
                contentObject(content)
        );
        return pdfDocument(objects);
    }

    private static String worksheet(List<FieldValue> fields) {
        StringBuilder rows = new StringBuilder();
        row(rows, 1, "Field", "Value");
        int index = 2;
        for (FieldValue field : fields) {
            row(rows, index++, field.name(), field.value());
        }
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                  <sheetViews><sheetView workbookViewId="0"/></sheetViews>
                  <sheetFormatPr defaultRowHeight="15"/>
                  <cols><col min="1" max="1" width="28" customWidth="1"/><col min="2" max="2" width="80" customWidth="1"/></cols>
                  <sheetData>
                %s
                  </sheetData>
                </worksheet>
                """.formatted(rows);
    }

    private static void row(StringBuilder builder, int index, String first, String second) {
        builder.append("    <row r=\"").append(index).append("\">");
        cell(builder, "A", index, first);
        cell(builder, "B", index, second);
        builder.append("</row>\n");
    }

    private static void cell(StringBuilder builder, String column, int row, String value) {
        builder.append("<c r=\"")
                .append(column)
                .append(row)
                .append("\" t=\"inlineStr\"><is><t>")
                .append(xml(value))
                .append("</t></is></c>");
    }

    private static void zip(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static byte[] contentObject(byte[] content) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeAscii(output, "<< /Length " + content.length + " >>\nstream\n");
        output.writeBytes(content);
        writeAscii(output, "endstream");
        return output.toByteArray();
    }

    private static byte[] pdfDocument(List<byte[]> objects) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        writeAscii(output, "%PDF-1.4\n");
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(output.size());
            writeAscii(output, (i + 1) + " 0 obj\n");
            output.writeBytes(objects.get(i));
            writeAscii(output, "\nendobj\n");
        }
        int xref = output.size();
        writeAscii(output, "xref\n0 " + (objects.size() + 1) + "\n");
        writeAscii(output, "0000000000 65535 f \n");
        for (Integer offset : offsets) {
            writeAscii(output, "%010d 00000 n \n".formatted(offset));
        }
        writeAscii(output, "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n");
        writeAscii(output, "startxref\n" + xref + "\n%%EOF\n");
        return output.toByteArray();
    }

    private static void writeAscii(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }

    private static byte[] asciiBytes(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static String csvValue(String value) {
        String normalized = escapeSpreadsheetFormula(value == null ? "" : value);
        return '"' + normalized.replace("\"", "\"\"") + '"';
    }

    private static String escapeSpreadsheetFormula(String value) {
        String trimmed = value.stripLeading();
        if (trimmed.isEmpty()) {
            return value;
        }
        char first = trimmed.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@') {
            return "'" + value;
        }
        return value;
    }

    private static String xml(String value) {
        return (value == null ? "" : value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String pdfEscape(String value) {
        return value.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private static String ascii(String value) {
        StringBuilder builder = new StringBuilder();
        for (char character : value.toCharArray()) {
            builder.append(character >= 32 && character <= 126 ? character : '?');
        }
        return builder.toString();
    }

    private static List<String> wrap(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return List.of(value);
        }
        List<String> lines = new ArrayList<>();
        String remaining = value;
        while (remaining.length() > maxLength) {
            int split = remaining.lastIndexOf(' ', maxLength);
            if (split <= 0) {
                split = maxLength;
            }
            lines.add(remaining.substring(0, split));
            remaining = remaining.substring(split).trim();
        }
        if (!remaining.isBlank()) {
            lines.add(remaining);
        }
        return lines;
    }

    private static String filename(UUID reportId, ReportExportFormat format) {
        return "lifebalance-report-" + reportId + "." + format.extension();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String timestamp(OffsetDateTime value) {
        return value == null ? "" : value.toString();
    }

    private static String decimal(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private record FieldValue(String name, String value) {
    }
}
