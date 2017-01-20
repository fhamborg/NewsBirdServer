/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.AnalysisWorkflow;
import kn.uni.hamborg.adv.summary.AdvSummarizer;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.FilterDimension;
import kn.uni.hamborg.adv.table.FilterValue;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.adv.topic.MalletParallelTopicExtractor;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.web.cell.SummaryField;

/**
 * This provides functionality to extract relevant information from a
 * {@link TableManager}.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TableToWebConverter {

    private static final Logger LOG = Logger.getLogger(TableToWebConverter.class.getSimpleName());

    private final TableManager tableManager;
    /**
     * A Gson instance which excludes all fields annotated with Exposed.
     */
    private final Gson gson;

    public TableToWebConverter(TableManager tableManager) {
        this.tableManager = tableManager;
        this.gson = new Gson();
    }

    public String[] getRows() {
        String[] values = new String[tableManager.getRowDimension().size()];
        int i = 0;
        for (FilterValue filterValue : tableManager.getRowDimension()) {
            values[i++] = filterValue.getDescriptor();
        }
        return values;
    }

    public String[] getColumns() {
        String[] values = new String[tableManager.getColDimension().size()];
        int i = 0;
        for (FilterValue filterValue : tableManager.getColDimension()) {
            values[i++] = filterValue.getDescriptor();
        }
        return values;
    }

    MatrixEntry[] getMatrixEntries() {
        final MatrixEntry[] matrixEntries = new MatrixEntry[tableManager.getCellCount()];
        int i = 0;
        for (FilterValue row : tableManager.getRowDimension()) {
            for (FilterValue col : tableManager.getColDimension()) {
                final FilterCell filterCell = tableManager.getCell(row, col);
                matrixEntries[i++] = new MatrixEntry(row.getDescriptor(), col.getDescriptor(), filterCell);
            }
        }
        return matrixEntries;
    }

    public String getFullMatrixInformation() {
        // xlabels, xlabels ( both are dimension values)
        // table col row as key and some object as value, which contains summarization sentences
        FullMatrixInformation fullMatrixInformation = new FullMatrixInformation(getRows(), getColumns(), getMatrixEntries());

        return new Gson().toJson(fullMatrixInformation);
    }

    static class FullMatrixInformation {

        private final String[] rows;
        private final String[] columns;
        private final MatrixEntry[] matrixEntries;

        public FullMatrixInformation(String[] dimensionAValues, String[] dimensionBValues, MatrixEntry[] matrixEntries) {
            this.rows = dimensionAValues;
            this.columns = dimensionBValues;
            this.matrixEntries = matrixEntries;
        }
    }

    static class MatrixEntry {

        private final String row;
        private final String column;
        private final FilterCell cell;

        public MatrixEntry(String dimensionAValue, String dimensionBValue, FilterCell cell) {
            this.row = dimensionAValue;
            this.column = dimensionBValue;
            this.cell = cell;
        }

    }

}
