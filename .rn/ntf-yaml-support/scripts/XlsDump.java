// XLS/XLSX ダンプツール（Task #3 のサンプリング比較用）
//
// 目的: nablarch-testing-converter が生成した YAML と元 Excel の内容一致を
//       独立に検証するため、Apache POI(3.8) で元 Excel の全シート・全セルを
//       「シート名 / 行 / 列 / セル型 / 値」のプレーンテキストに書き出す。
//       変換器とは別経路（POI 直読み）で Excel を読むことで、変換の取りこぼし・
//       値化けを検出できるようにする。
//
// 実行: java -cp "<poi-classpath>" XlsDump.java <excelファイル>
//   （JDK 11+ のソースランチャで単一ファイル実行）
//
// 出力: 空でないセルを `R<row>C<col>\t<型>\t<値>` で列挙する。
//       数値セルは POI の生値（getNumericCellValue）を出力し、NTF の
//       QuotationTrimmer 等の解釈は行わない（生の値を見る）。

import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;

public class XlsDump {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) { System.err.println("usage: XlsDump <excel>"); System.exit(2); }
        try (FileInputStream fis = new FileInputStream(args[0])) {
            Workbook wb = WorkbookFactory.create(fis);
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                System.out.println("### SHEET[" + s + "] name=" + sheet.getSheetName()
                        + " rows=" + (sheet.getLastRowNum() + 1));
                for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        if (cell == null) continue;
                        String type;
                        String v;
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_NUMERIC: type = "NUM"; v = String.valueOf(cell.getNumericCellValue()); break;
                            case Cell.CELL_TYPE_STRING: type = "STR"; v = cell.getStringCellValue(); break;
                            case Cell.CELL_TYPE_BOOLEAN: type = "BOOL"; v = String.valueOf(cell.getBooleanCellValue()); break;
                            case Cell.CELL_TYPE_FORMULA: type = "FORMULA"; v = cell.getCellFormula(); break;
                            default: continue;
                        }
                        if (v.isEmpty()) continue;
                        System.out.println("R" + r + "C" + c + "\t" + type + "\t" + v.replace("\n", "\\n"));
                    }
                }
            }
        }
    }
}
