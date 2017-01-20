/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.gui.era;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.language.analyzers.AggregatedSubjectivity;
import kn.uni.hamborg.language.analyzers.MPQASubjectivityAnalyzer;
import kn.uni.hamborg.utils.DateTimeUtils;
import kn.uni.hamborg.utils.RegionUtils;
import kn.uni.hamborg.language.analyzers.TopicAnalyzerMallet;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import kn.uni.hamborg.utils.QueryUtils;
import kn.uni.hamborg.utils.IndexUtils;
import kn.uni.hamborg.utils.DocumentUtils;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.lucene.summarizer.Summarizer;
import kn.uni.hamborg.lucene.summarizer.TtfidfSummarizer;
import kn.uni.hamborg.utils.NumberUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.joda.time.DateTime;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CountrySummaryFrame extends javax.swing.JFrame {

    private static final Logger LOG = Logger.getLogger(CountrySummaryFrame.class.getSimpleName());

    private String[] ignoreTerms;

    public enum Mode {
        date, country, manual
    }

    private final Mode mode;

    /**
     * Creates new form CountrySummaryFrame
     */
    public CountrySummaryFrame(Mode mode, String title) {
        initComponents();

        setTitle(title);

        tmCountryInfos = createTableModelCountryInfos();
        jTableCountryInfos.setModel(tmCountryInfos);
        jTableCountryInfos.getColumnModel().getColumn(0).setMaxWidth(50);
        jTableCountryInfos.getColumnModel().getColumn(1).setPreferredWidth(100);
        jTableCountryInfos.getColumnModel().getColumn(2).setMaxWidth(40);
        this.mode = mode;
        subjectivityAnalyzer = new MPQASubjectivityAnalyzer();
    }

    private final DefaultTableModel tmCountryInfos;
    private Map<String, List<Integer>> filteredDocumentsByCountry;
    private Analyzer analyzer;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private String[] countryCodes;
    private String[] countryNames;
    private int[] countryCounts;
    private Query originalQuery;
    private DateTime selectionStartDate;
    private DateTime selectionEndDate;
    private final MPQASubjectivityAnalyzer subjectivityAnalyzer;

    private DefaultTableModel createTableModelCountryInfos() {
        return new DefaultTableModel(new String[]{"Code", "Name", "#", "Terms"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };
    }

    /**
     * refreshes this frame
     *
     * @param indexReader
     * @param analyzer
     * @param indexSearcher
     * @param originalQuery
     * @param filteredDocumentsByCountry
     * @param ignoreTerms Can be null, otherwise these terms will be removed
     * from the docs' content.
     * @param selectionStartDate
     * @param selectionEndDate
     */
    public void refresh(final IndexReader indexReader,
            final Analyzer analyzer,
            final IndexSearcher indexSearcher,
            final Query originalQuery,
            final Map<String, List<Integer>> filteredDocumentsByCountry,
            final String[] ignoreTerms,
            final DateTime selectionStartDate,
            final DateTime selectionEndDate
    ) {
        setVisible(true);

        this.filteredDocumentsByCountry = filteredDocumentsByCountry;
        this.analyzer = analyzer;
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
        this.ignoreTerms = ignoreTerms;
        this.originalQuery = originalQuery;
        this.selectionStartDate = selectionStartDate;
        this.selectionEndDate = selectionEndDate;

        guirefresh();
    }

    private void guirefreshtopics() {
        final int maxDocsPerCountry = Integer.valueOf(jTextField1.getText());
        //  tmCountryInfos.setRowCount(0);

        try {
            int size = filteredDocumentsByCountry.size();
            final String[] contents = new String[size];
            int i = 0;
            for (Map.Entry<String, List<Integer>> entrySet : filteredDocumentsByCountry.entrySet()) {
                String countryCode = entrySet.getKey();
                List<Integer> countryIds = entrySet.getValue();
                int origCount = countryIds.size();
                // as they are already sorted by score, we can just select the top k
                countryIds = countryIds.subList(0, Math.min(maxDocsPerCountry, countryIds.size()));

                String concatContent = DocumentUtils.getConcatenatedContent(indexReader, countryIds, LightDoc.TITLE_STEMMED);
                if (ignoreTerms != null
                        && ignoreTerms.length > 0) {
                    // remove ignore terms
                    for (String ignoreTerm : ignoreTerms) {
                        concatContent = concatContent.toLowerCase().replaceAll(ignoreTerm, " ");
                    }
                }
                contents[i++] = concatContent;
            }
            TopicAnalyzerMallet topicAnalyzer = new TopicAnalyzerMallet()
                    .addContents(contents)
                    .runTopicAnalysis(size)
                    .printMostImportantTermsPerTopic();
            String[] summaryPerCountry = topicAnalyzer.getMostImportantTopicPerContentReadable();
            String[] subjectivityPerCountry = new String[size];
            final QueryParser queryParser = QueryParserFactory.createQueryParser();

            /**
             * Calculate top sentences
             */
            for (int j = 0; j < summaryPerCountry.length; j++) {
                /**
                 * This gives us a lucene friendly search string consisting of
                 * all the top terms for this topic
                 */

                int[] topicIndexes = topicAnalyzer.getMostImportantTopicIndexesForContentReadable(j);
                final StringBuilder sbTopTerms = new StringBuilder();
                for (int topicIndex : topicIndexes) {
                    sbTopTerms
                            .append(topicAnalyzer.getTopTermsForTopicIndex(topicIndex, true))
                            .append(", ");
                }

                // now add the selected date to the query and retrieve best (in terms of summarization) sentences to display them
                final Query topTermsQuery = queryParser.parse(sbTopTerms.toString());

                Query finalQuery = null;
                switch (mode) {
                    case country:
                        final Query topTermsQueryWithDateRange = QueryUtils.addDateRangeToQuery(topTermsQuery,
                                selectionStartDate, selectionEndDate);
                        finalQuery = QueryUtils.addPubCountryToQuery(topTermsQueryWithDateRange, countryCodes[j]);
                        break;
                    case date:
                        // in countryCodes we actually have the date string
                        DateTime startOfCurDay = DateTimeUtils.simpleDateTimeFormatter.parseDateTime(this.countryCodes[j]);
                        finalQuery = QueryUtils.addDateRangeToQuery(topTermsQuery, startOfCurDay, DateTimeUtils.getEndOfDay(startOfCurDay));
                        break;
                    case manual:
                        finalQuery = topTermsQuery;
                        break;
                    default:
                        throw new IllegalArgumentException();
                }

                finalQuery = QueryUtils.addQueryToQuery(finalQuery, originalQuery);

                LOG.log(Level.INFO, "created topic summarization query ({0}): {1}", new Object[]{countryCodes[j], finalQuery});
                final ScoreDoc[] scoreDocs = indexSearcher.search(finalQuery, 10).scoreDocs;
                final Document[] docs = QueryUtils.scoreDocsToDocuments(scoreDocs, indexReader);
                StringBuilder tableInfo = new StringBuilder();

                /**
                 * Calculate the subjectivity
                 */
                final String totalContent = DocumentUtils.getConcatenatedContent(docs, LightDoc.CONTENT_STEMMED);
                final AggregatedSubjectivity totalSubjectivityForThisCountryOrDate = subjectivityAnalyzer.calcSubjectivityForSentences(totalContent);
                final double relativeSubjForCountryOrDate = totalSubjectivityForThisCountryOrDate.getRelativeTotalSubjectivity();

                AggregatedSubjectivity aggrSubjOfCountry = null;
                switch (mode) {
                    case date:
                        aggrSubjOfCountry = subjectivityAnalyzer.getSubjectivityOfDayDate(countryCodes[j]);
                        break;
                    case country:
                        aggrSubjOfCountry = subjectivityAnalyzer.getSubjectivityOfCountry(countryCodes[j]);
                        break;
                    case manual:
                        break;
                }

                final double relativeSubjBaselineForCountry = mode != Mode.manual ? aggrSubjOfCountry.getRelativeTotalSubjectivity() : -1.0;

                subjectivityPerCountry[j] = NumberUtils.detailedDecimalFormat.format(relativeSubjForCountryOrDate)
                        + " | " + NumberUtils.detailedDecimalFormat.format(relativeSubjBaselineForCountry);

                /**
                 * Calculate the best sentences (with respect to TF IDF
                 * summarization score)
                 */
                final Summarizer titleSummarizer = new TtfidfSummarizer(docs, analyzer);

                // get the top k sentences from the title
                StringScore[] scoredTitles = titleSummarizer.getTopKSentences(LightDoc.CONTENT_STEMMED, 2);
                for (StringScore scoredTitle : scoredTitles) {
                    tableInfo.append(NumberUtils.defaultDecimalFormat.format(scoredTitle.getScore()))
                            .append(": ")
                            .append(scoredTitle.getValue())
                            .append(". ");
                }

                summaryPerCountry[j] = tableInfo.toString() + "; " + summaryPerCountry[j];
            }

            tmCountryInfos.addColumn("Subj.", subjectivityPerCountry);
            tmCountryInfos.addColumn("Topics", summaryPerCountry);
        } catch (IOException | ParseException ioe) {
            Logger.getLogger(CountrySummaryFrame.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

    private void guirefresh() {
        final int maxDocsPerCountry = Integer.valueOf(jTextField1.getText());
        tmCountryInfos.setRowCount(0);
        List<String> tmpcountryNames = new ArrayList<>();
        int i = 0;
        countryCounts = new int[filteredDocumentsByCountry.size()];
        countryCodes = new String[filteredDocumentsByCountry.size()];
        try {
            for (Map.Entry<String, List<Integer>> entrySet : filteredDocumentsByCountry.entrySet()) {
                String countryCode = entrySet.getKey();
                countryCodes[i] = countryCode;
                List<Integer> countryIds = entrySet.getValue();
                int origCount = countryIds.size();
                // as they are already sorted by score, we can just select the top k
                countryIds = countryIds.subList(0, Math.min(maxDocsPerCountry, countryIds.size()));
                Document[] countryDocs = IndexUtils.getDocsByDocId(indexReader, countryIds);

                StringBuilder info = new StringBuilder();
                StringScore[] topkTerms = new TtfidfSummarizer(countryDocs, analyzer).getTopKTokens(LightDoc.CONTENT_STEMMED, 20);

                for (StringScore topkTerm : topkTerms) {
                    info.append(topkTerm.getValue());
                    info.append(", ");
                }
                countryCounts[i++] = countryIds.size();
                tmCountryInfos.addRow(new Object[]{countryCode,
                    RegionUtils.getCountryName(countryCode),
                    origCount,
                    info.toString()});
                tmpcountryNames.add(RegionUtils.getCountryName(countryCode));
            }

            countryNames = tmpcountryNames.toArray(new String[0]);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTableCountryInfos = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        jButtonRefresh = new javax.swing.JButton();
        jButtonTopics = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTableCountryInfos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTableCountryInfos);

        jTextField1.setText("100");

        jButtonRefresh.setText("Refresh");
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshActionPerformed(evt);
            }
        });

        jButtonTopics.setText("Topics");
        jButtonTopics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTopicsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 783, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(53, 53, 53)
                .addComponent(jButtonRefresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonTopics)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonRefresh)
                    .addComponent(jButtonTopics))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshActionPerformed
        guirefresh();
    }//GEN-LAST:event_jButtonRefreshActionPerformed

    private void jButtonTopicsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTopicsActionPerformed
        guirefreshtopics();
    }//GEN-LAST:event_jButtonTopicsActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CountrySummaryFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CountrySummaryFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CountrySummaryFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CountrySummaryFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CountrySummaryFrame(Mode.country, "").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JButton jButtonTopics;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableCountryInfos;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
