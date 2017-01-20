/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.query;

import java.util.Arrays;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.FilterDimension;
import kn.uni.hamborg.adv.topic.MalletTopicExtractor;
import kn.uni.hamborg.adv.topic.TopicExtractor;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.knowledge.CountryNames;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import kn.uni.hamborg.utils.DateTimeUtils;
import kn.uni.hamborg.utils.QueryUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Describes information about a data query that can be executed by the system.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class QueryCommand {

    private static final Logger LOG = Logger.getLogger(QueryCommand.class.getSimpleName());

    private static final QueryParser QUERY_PARSER = QueryParserFactory.createQueryParser();

    private final FilterDimension rows;
    private final FilterDimension columns;
    /**
     * This specifies a filter query, e.g., a date range.
     */
    private final Query filterQuery;
    /**
     * The interval from when until when.
     */
    private final Interval fromTo;
    private final int numberOfTopicsPerCell;
    //  private final boolean onlyTopTopicForSummarizationQuery;
    private final boolean summarization_OrderSentencesByFirstOccurenceInDoc;

    // Lin2002Single
    private final boolean summarization_Lin2002Single_FirstSentencesOnly;

    // field on which we calculate the topics
    private final String topicField;

    private final String topicCellDocumentMergeType;

    public QueryCommand(FilterDimension rows, FilterDimension columns,
            String dateFrom, String dateTo, int numberOfTopicsPerCell,
            // boolean onlyTopTopicForSummarizationQuery
            boolean summarization_OrderSentencesByFirstOccurenceInDoc,
            boolean summarization_Lin2002Single_FirstSentencesOnly,
            String userFilterFewRequiredTerms, String userFilterAdditionalTerms,
            String topicField, String topicCellDocumentMergeType,
            IndexReader indexReader, Analyzer analyzer
    ) {
        try {
            this.rows = rows;
            this.columns = columns;
            this.fromTo = new Interval(
                    DateTimeUtils.getStartOfDay(DateTimeUtils.simpleDateTimeFormatter.parseDateTime(dateFrom)),
                    DateTimeUtils.getEndOfDay(DateTimeUtils.simpleDateTimeFormatter.parseDateTime(dateTo)));
            this.filterQuery = buildQuery(fromTo, userFilterFewRequiredTerms,
                    userFilterAdditionalTerms, rows, columns, indexReader, analyzer, topicField);
            this.numberOfTopicsPerCell = numberOfTopicsPerCell;
            // this.onlyTopTopicForSummarizationQuery = onlyTopTopicForSummarizationQuery;
            this.summarization_OrderSentencesByFirstOccurenceInDoc = summarization_OrderSentencesByFirstOccurenceInDoc;
            this.summarization_Lin2002Single_FirstSentencesOnly = summarization_Lin2002Single_FirstSentencesOnly;
            this.topicField = topicField;
            this.topicCellDocumentMergeType = topicCellDocumentMergeType;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Interval getFromTo() {
        return fromTo;
    }

    public static Query buildQuery(Interval fromTo, String userFilterFewRequiredTerms,
            String userFilterAdditionalTerms, FilterDimension rows, FilterDimension columns,
            IndexReader indexReader, Analyzer analyzer, String topicField) {
        final Query dateAndUserFilter = buildQuery(fromTo, userFilterFewRequiredTerms,
                userFilterAdditionalTerms, indexReader, analyzer, topicField);

        BooleanQuery bq = new BooleanQuery();
        bq.add(dateAndUserFilter, BooleanClause.Occur.MUST);

        if (rows != null) {
            bq.add(rows.getAnyQuery(), BooleanClause.Occur.MUST);
        }
        if (columns != null) {
            bq.add(columns.getAnyQuery(), BooleanClause.Occur.MUST);
        }

        return bq;
    }

    public static Query buildDateQuery(Interval fromTo) {
        return QueryUtils.createNumericRangeQueryForDate(
                LightDoc.PUB_DATE, fromTo.getStart(), fromTo.getEnd(), true, true);
    }

    public static Query buildQuery(Interval fromTo, String userFilterFewRequiredTerms,
            String userFilterAdditionalTerms, IndexReader indexReader, Analyzer analyzer, String topicField) {
        try {
            final Query dateQuery = buildDateQuery(fromTo);

            Query resultingQuery = dateQuery;

            // we change this so that the user query is now only one field and also it is more prominent to the user, i.e., the user
            // shall be able to enter his own query. at least one document is required!
            //Query topicQuery = TopicExtractor.buildTopicQuery(userFilterFewRequiredTerms, userFilterAdditionalTerms, analyzer, topicField);
            if (!userFilterAdditionalTerms.isEmpty()) {
                Query topicQuery = TopicExtractor.createFewRequiredTerms(analyzer, userFilterAdditionalTerms, topicField, 1);
                resultingQuery = QueryUtils.addQueryToQuery(resultingQuery, topicQuery);
            }

            return resultingQuery;

            // few minimum required terms of top terms
            /*BooleanQuery bq = new BooleanQuery();
             List<String> parsedTerms = LuceneUtils.parseKeywords(analyzer, LightDoc.CONTENT_STEMMED, userFilter);
             for (String parsedTerm : parsedTerms) {
             TermQuery tq = new TermQuery(new Term(LightDoc.DESCRIPTION_STEMMED, parsedTerm));
             bq.add(tq, BooleanClause.Occur.SHOULD);
             }
             bq.setMinimumNumberShouldMatch(2);
             System.out.println(bq);
             return QueryUtils.addQueryToQuery(bq, dateQuery);
             */
            // create userFilter query
            /*MoreLikeThis mlt = new MoreLikeThis(indexReader);
             mlt.setMinTermFreq(0);
             mlt.setMinDocFreq(0);
             mlt.setAnalyzer(analyzer);
             mlt.setFieldNames(new String[]{LightDoc.CONTENT_STEMMED, LightDoc.DESCRIPTION_STEMMED, LightDoc.TITLE_STEMMED});
             Reader reader = new StringReader(userFilter);
             Query qq = mlt.like(null, reader);
             System.out.println("userfilter query");
             System.out.println(qq.toString());
             return QueryUtils.addQueryToQuery(qq, dateQuery);*/
            // my query (must be enabled on client side)
            //return QueryUtils.addQueryToQuery(dateQuery, QUERY_PARSER.parse(userFilter));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getTopicField() {
        return topicField;
    }

    public FilterDimension getRows() {
        return rows;
    }

    public FilterDimension getColumns() {
        return columns;
    }

    public Query getFilterQuery() {
        return filterQuery;
    }

    public int getNumberOfTopicsPerCell() {
        return numberOfTopicsPerCell;
    }

    public MalletTopicExtractor.CellDocumentMergeType getTopicCellDocumentMergeType() {
        return MalletTopicExtractor.CellDocumentMergeType.valueOf(topicCellDocumentMergeType);
    }

    public boolean isSummarization_OrderSentencesByFirstOccurenceInDoc() {
        return summarization_OrderSentencesByFirstOccurenceInDoc;
    }

    public boolean isSummarization_Lin2002Single_FirstSentencesOnly() {
        return summarization_Lin2002Single_FirstSentencesOnly;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private static void translateUIPublisherCountryNames(String[] publisherNames) {
        for (int i = 0; i < publisherNames.length; i++) {
            String publisherName = publisherNames[i];
            publisherNames[i] = publisherName.toUpperCase();
        }
    }

    private static void translateUIMentionedCountryNames(String[] mentionedCountryNames) {
        for (int i = 0; i < mentionedCountryNames.length; i++) {
            String mentionedCountryName = mentionedCountryNames[i].toUpperCase();
            switch (mentionedCountryName) {
                case "GB":
                    mentionedCountryName = "britain|england";
                    break;
                case "US":
                    mentionedCountryName = "usa \"united states\"";
                    break;
                default:
                    mentionedCountryName = CountryNames.getCountryNameFromUCCode(mentionedCountryName);
            }
            mentionedCountryNames[i] = mentionedCountryName;
        }
    }

    /**
     * Creates a {@link QueryCommand} instance from the JSON wrapper instance.
     *
     * @param w
     * @param queryParser
     * @param indexReader
     * @param analyzer
     * @return
     */
    public static QueryCommand fromWrapper(QueryCommandWrapper w, QueryParser queryParser, IndexReader indexReader,
            Analyzer analyzer) {
        String[] colDimValues = w.colDimSelectedValues.split(",");
        String[] rowDimValues = w.rowDimSelectedValues.split(",");
        translateUIPublisherCountryNames(rowDimValues);
        translateUIMentionedCountryNames(colDimValues);

        //FilterDimension columns = FilterDimension.createFromSimpleClassName(w.colDimTypeSelect, colDimValues, queryParser);
        FilterDimension columns = FilterDimension.createForTextContains(LightDoc.DESCRIPTION_STEMMED, Arrays.asList(colDimValues));
        //FilterDimension rows = FilterDimension.createFromSimpleClassName(w.rowDimTypeSelect, rowDimValues, queryParser);
        FilterDimension rows = FilterDimension.createForCountryCodes(Arrays.asList(rowDimValues));

        if (w.dateFromIncl.equals("")) {
            w.dateFromIncl = DateTimeUtils.simpleDateTimeFormatter.print(DateTime.now().withTimeAtStartOfDay());
        }
        if (w.dateToIncl.equals("")) {
            w.dateToIncl = DateTimeUtils.simpleDateTimeFormatter.print(DateTimeUtils.getEndOfDay(DateTime.now()));
        }

        return new QueryCommand(rows,
                columns,
                w.dateFromIncl,
                w.dateToIncl,
                Integer.valueOf(w.numberOfTopicsPerCell),
                //  w.getOnlyTopTopicForSummarizationQuery().equals("true"));
                w.getSummarization_OrderSentencesByFirstOccurenceInDoc().equals("true"),
                w.getSummarization_Lin2002Single_FirstSentencesOnly().equals("true"),
                w.userFilterFewRequiredTerms,
                w.userFilterAdditionalTerms,
                w.topicField,
                w.topicCellDocumentMergeType,
                indexReader,
                analyzer
        );
    }

    public static class QueryCommandWrapper {

        private String colDimSelectedValues;
        private String colDimTypeSelect;
        private String dateFromIncl;
        private String dateToIncl;
        private String rowDimSelectedValues;
        private String rowDimTypeSelect;
        private String numberOfTopicsPerCell;
        private String onlyTopTopicForSummarizationQuery;
        private String summarization_OrderSentencesByFirstOccurenceInDoc;
        private String summarization_Lin2002Single_FirstSentencesOnly;
        private String userFilterFewRequiredTerms;
        private String userFilterAdditionalTerms;
        private String topicField;
        private String topicCellDocumentMergeType;

        public String getTopicCellDocumentMergeType() {
            return topicCellDocumentMergeType;
        }

        public void setTopicCellDocumentMergeType(String topicCellDocumentMergeType) {
            this.topicCellDocumentMergeType = topicCellDocumentMergeType;
        }

        public String getUserFilterFewRequiredTerms() {
            return userFilterFewRequiredTerms;
        }

        public String getUserFilterAdditionalTerms() {
            return userFilterAdditionalTerms;
        }

        public void setUserFilter(String userFilterFewRequiredTerms, String userFilterAdditionalTerms) {
            this.userFilterFewRequiredTerms = userFilterFewRequiredTerms;
            this.userFilterAdditionalTerms = userFilterAdditionalTerms;
        }

        public String getTopicField() {
            return topicField;
        }

        public void setTopicField(String topicField) {
            this.topicField = topicField;
        }

        public String getOnlyTopTopicForSummarizationQuery() {
            return onlyTopTopicForSummarizationQuery;
        }

        public String getSummarization_OrderSentencesByFirstOccurenceInDoc() {
            return summarization_OrderSentencesByFirstOccurenceInDoc;
        }

        public void setSummarization_OrderSentencesByFirstOccurenceInDoc(String summarization_OrderSentencesByFirstOccurenceInDoc) {
            this.summarization_OrderSentencesByFirstOccurenceInDoc = summarization_OrderSentencesByFirstOccurenceInDoc;
        }

        public void setOnlyTopTopicForSummarizationQuery(String onlyTopTopicForSummarizationQuery) {
            this.onlyTopTopicForSummarizationQuery = onlyTopTopicForSummarizationQuery;
        }

        public String getColDimSelectedValues() {
            return colDimSelectedValues;
        }

        public void setColDimSelectedValues(String colDimSelectedValues) {
            this.colDimSelectedValues = colDimSelectedValues;
        }

        public String getColDimTypeSelect() {
            return colDimTypeSelect;
        }

        public void setColDimTypeSelect(String colDimTypeSelect) {
            this.colDimTypeSelect = colDimTypeSelect;
        }

        public String getDateFromIncl() {
            return dateFromIncl;
        }

        public void setDateFromIncl(String dateFromIncl) {
            this.dateFromIncl = dateFromIncl;
        }

        public String getDateToIncl() {
            return dateToIncl;
        }

        public void setDateToIncl(String dateToIncl) {
            this.dateToIncl = dateToIncl;
        }

        public String getRowDimSelectedValues() {
            return rowDimSelectedValues;
        }

        public void setRowDimSelectedValues(String rowDimSelectedValues) {
            this.rowDimSelectedValues = rowDimSelectedValues;
        }

        public String getRowDimTypeSelect() {
            return rowDimTypeSelect;
        }

        public void setRowDimTypeSelect(String rowDimTypeSelect) {
            this.rowDimTypeSelect = rowDimTypeSelect;
        }

        public String getNumberOfTopicsPerCell() {
            return numberOfTopicsPerCell;
        }

        public void setNumberOfTopicsPerCell(String numberOfTopicsPerCell) {
            this.numberOfTopicsPerCell = numberOfTopicsPerCell;
        }

        public String getSummarization_Lin2002Single_FirstSentencesOnly() {
            return summarization_Lin2002Single_FirstSentencesOnly;
        }

        public void setSummarization_Lin2002Single_FirstSentencesOnly(String summarization_Lin2002Single_FirstSentencesOnly) {
            this.summarization_Lin2002Single_FirstSentencesOnly = summarization_Lin2002Single_FirstSentencesOnly;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }

}
