/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import com.google.common.collect.TreeBasedTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.utils.QueryUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

/**
 * This provides functionality to manage a table spanned by {@link FilterValue}s
 * of two {@link FilterDimension}s.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TableManager {

    private static final Logger LOG = Logger.getLogger(TableManager.class.getSimpleName());

    private final IndexReader indexReader;
    private final IndexSearcher indexSearcher;
    private final FilterDimension dimensionA;
    private final FilterDimension dimensionB;
    private final Query filterQuery;
    private final Map<String, FilterCell> reverseCellsById;
    private final Map<String, FilterCell> reverseCellsByHumanReadableId;

    private final TreeBasedTable<FilterValue, FilterValue, FilterCell> table;
    private final Set<FilterCell> cells;

    /**
     * Constructs a {@code TableManager} instance. Thereby a table is created,
     * which has {@code dimensionA} as rows and {@code dimensionB} as columns.
     *
     * @param indexReader
     * @param indexSearcher
     * @param queryParser
     * @param dimensionA
     * @param dimensionB
     * @param filterQuery, can be null. If null, then a MatchAllDocsQuery is
     * used instead.
     */
    public TableManager(IndexReader indexReader, IndexSearcher indexSearcher,
            QueryParser queryParser, FilterDimension dimensionA, FilterDimension dimensionB,
            Query filterQuery) {
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
        this.dimensionA = dimensionA;
        this.dimensionB = dimensionB;
        this.filterQuery = filterQuery == null ? new MatchAllDocsQuery() : filterQuery;

        this.reverseCellsById = new HashMap<>();
        this.reverseCellsByHumanReadableId = new HashMap<>();
        /**
         * We create a {@link TreeBasedTable} which sorts the elements according
         * to their natural order. In order to provide such order, the
         * {@link FilterValue}'s implementation classes, e.g.,
         * {@link CountryCodeFilterDimension}, implement the {@link Comparable}
         * interface.
         */
        this.table = TreeBasedTable.create();
        this.cells = new HashSet<>();

        buildTable();
    }

    /**
     * Computes table related information.
     */
    private void computeTableInfo() {
        try {
            for (FilterValue val : dimensionA) {
                val.setNumberOfDocs(indexSearcher.search(val.getFilterQuery(), Integer.MAX_VALUE).totalHits);
            }
            for (FilterValue val : dimensionB) {
                val.setNumberOfDocs(indexSearcher.search(val.getFilterQuery(), Integer.MAX_VALUE).totalHits);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void buildTable() {
        try {
            for (FilterValue valueA : dimensionA) {
                for (FilterValue valueB : dimensionB) {
                    final FilterCell cell = createCell(valueA, valueB);
                    LOG.log(Level.INFO, "cell created: {0}", cell.toString());
                    table.put(valueA, valueB, cell);
                    reverseCellsById.put(cell.getId(), cell);
                    reverseCellsByHumanReadableId.put(cell.getHumanReadableId(), cell);
                    cells.add(cell);
                }
            }

            computeTableInfo();

            LOG.log(Level.INFO, "built table with rows x cols: {0} x {1}", new Object[]{table.rowKeySet().size(), table.columnKeySet().size()});
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Creates a {@link Query} that leads to the {@link Document}s of the
     * {@link FilterCell} defined by the two {@link FilterValue} parameters.
     *
     * @param valueA
     * @param valueB
     * @return
     */
    private Query getFilterQueryOfCell(FilterValue valueA, FilterValue valueB) {
        return QueryUtils.addQueryToQuery(valueA.getFilterQuery(), valueB.getFilterQuery());
    }

    /**
     * Creates a {@link FilterCell} for the given parameters. Also computes the
     * total number of documents belonging to that cell (and stores it as an
     * attribute within the cell).
     *
     * @param valueA
     * @param valueB
     * @return
     * @throws IOException
     */
    private FilterCell createCell(FilterValue valueA, FilterValue valueB) throws IOException {
        final Query query = QueryUtils.addQueryToQuery(filterQuery,
                getFilterQueryOfCell(valueA, valueB));
        LOG.info(query.toString());

        //final int countDocsMatchingQuery = QueryUtils.getCountDocsMatchingQuery(query, indexSearcher);
        final int countDocsMatchingQuery = indexSearcher.search(query, Integer.MAX_VALUE).scoreDocs.length;

        final ScoreDoc[] scoreDocs = indexSearcher.search(query, FilterCell.MAX_NUMBER_OF_CELL_DOCS).scoreDocs;
        Document[] docs = QueryUtils.scoreDocsToDocuments(scoreDocs, indexReader);

        /* clone documents. if there are less cell documents than specified by FilterCell.MAX_NUMBER_OF_CELL_DOCS, 
         we clone documents so that each cell has the same amount of documents contributing equally to topic modelling.        
         */
        List<Document> tmpDocs = new ArrayList<>();
        if (docs.length > 0) {
            int i = 0;
            while (tmpDocs.size() < docs.length) {//FilterCell.MAX_NUMBER_OF_CELL_DOCS) {
                tmpDocs.add(docs[i % docs.length]);
                i++;
            }
        }

        return new FilterCell(tmpDocs, query, valueA, valueB, countDocsMatchingQuery);
    }

    /**
     * Returns the rows of this instance, i.e., {@code dimensionA}.
     *
     * @return
     */
    public FilterDimension getRowDimension() {
        return dimensionA;
    }

    /**
     * Returns the columns of this instance, i.e., {@code dimensionB}.
     *
     * @return
     */
    public FilterDimension getColDimension() {
        return dimensionB;
    }

    /**
     * Returns the cell specified by both parameters.
     *
     * @param row, a
     * @param col, b
     * @return
     */
    public FilterCell getCell(FilterValue row, FilterValue col) {
        return table.get(row, col);
    }

    /**
     * Returns the cell specified by its ID.
     *
     * @param id
     * @return
     */
    public FilterCell getCell(String id) {
        return reverseCellsById.get(id);
    }

    /**
     * Returns the cell specified by its human readable ID.
     *
     * @param id
     * @return
     */
    public FilterCell getCellByHumanReadableId(String id) {
        return reverseCellsByHumanReadableId.get(id);
    }

    /**
     * Returns the total number of cells.
     *
     * @return
     */
    public int getCellCount() {
        return table.columnKeySet().size() * table.rowKeySet().size();
    }

    /**
     * Returns a set of all cells in no specific order.
     *
     * @return
     */
    public Set<FilterCell> getCells() {
        return cells;
    }

    /**
     * If one of both dimensions of this {@code cell} is a
     * {@link CountryCodeFilterDimension} the value of that is returned. If none
     * is, {@code null} is returned.
     *
     * @param cell
     * @return
     */
    public String getCountryCode(FilterCell cell) {
        if (getColDimension() instanceof CountryCodeFilterDimension) {
            return cell.getColumnValue();
        } else if (getRowDimension() instanceof CountryCodeFilterDimension) {
            return cell.getRowValue();
        }

        return null;
    }

    /**
     * Returns the fundamental query which defines the overall analysis scope
     * (of the matrix).
     *
     * @return
     */
    public Query getFilterQuery() {
        return filterQuery;
    }

}
