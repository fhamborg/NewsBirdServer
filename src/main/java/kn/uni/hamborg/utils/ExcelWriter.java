/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.io.File;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Provides functionality to write a simple Excel document. Most importantly
 * intended to write a simple table, nothing fancy.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ExcelWriter {

    private static final Logger LOG = Logger.getLogger(ExcelWriter.class.getSimpleName());

    private final WritableWorkbook workbook;
    private final WritableSheet sheet;
    private int nextRow = 1;

    public ExcelWriter(File out, String[] colNames) throws Exception {
        workbook = Workbook.createWorkbook(out);
        sheet = workbook.createSheet("sheet0", 0);
        for (int i = 0; i < colNames.length; i++) {
            String colName = colNames[i];
            sheet.addCell(new Label(i, 0, colName));
        }
    }

    /**
     * If one of the given cell values is of type or subtype Number we write
     * that to the Excel file as a Number. Otherwise String.
     *
     * @param cells
     * @throws WriteException
     */
    public synchronized void addRow(Object[] cells) throws WriteException {
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] instanceof Number) {
                sheet.addCell(new jxl.write.Number(i, nextRow, ((Number) cells[i]).doubleValue()));
            } else {
                sheet.addCell(new Label(i, nextRow, cells[i].toString()));
            }
        }
        nextRow++;
    }

    public void close() throws Exception {
        System.out.println("write");
        workbook.write();
        System.out.println("close");
        workbook.close();
    }

    public static void main(String[] args) throws Exception {
        ExcelWriter w = new ExcelWriter(new File("testexcel.xls"), new String[]{"a", "b"});
        for (int i = 0; i < 10; i++) {
            w.addRow(new Object[]{"felix", new Double(3.333 + i)});
        }
        w.close();
    }
}
