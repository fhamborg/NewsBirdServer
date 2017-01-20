/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.gui.era;

import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import kn.uni.hamborg.config.Events;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.utils.ThreadUtils;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.utils.LightDocUtils;
import kn.uni.hamborg.utils.DateTimeUtils;
import kn.uni.hamborg.utils.RegionUtils;
import kn.uni.hamborg.language.Language;
import kn.uni.hamborg.language.analyzers.DocumentSentiment;
import kn.uni.hamborg.language.analyzers.DocumentSentimentWorker;
import kn.uni.hamborg.language.analyzers.SentimentAnalyzer;
import kn.uni.hamborg.lucene.analyzer.AnalyzerFactory;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import kn.uni.hamborg.utils.IndexUtils;
import kn.uni.hamborg.lucene.summarizer.Summarizer;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.lucene.summarizer.TtfidfSummarizer;
import kn.uni.hamborg.utils.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ERAAnalysisFrame extends javax.swing.JFrame {

    private static final Logger LOG = Logger.getLogger(ERAAnalysisFrame.class.getSimpleName());

    private static final String defaultQuery = "+pubDate:[20141103 TO 20141111] "
            //   + "-titleStemmed:troops -titleStemmed:cross -titleStemmed:tanks "
            + "+(titleStemmed:(russia* putin) contentStemmed:(russia* putin))";

    private String[] eaIds = new String[]{};
    private final IndexSearcher indexSearcher;
    private final IndexReader indexReader;
    private final Analyzer analyzer;
    private final QueryParser queryParser;
    private ScoreDoc[] origScoreDocs;
    private Query lastQuery = null;

    private final DefaultTableModel tmCountries;
    private final DefaultTableModel tmArticles;
    private final DefaultTableModel tmSummary;
    private final DefaultTableModel tmTokenSummary;
    private final DefaultTableModel tmDates;

    private String[] selectedCountryCodes = null;
    private DateTime[] selectedDates = null;
    /**
     * Stores currently filtered documents sorted by country.
     */
    private Map<String, List<Integer>> filteredDocumentIdsByCountry = null;
    /**
     * Stores currently filtered documents sorted by date.
     */
    private Map<String, List<Integer>> filteredDocumentIdsByDate = null;
    /**
     * current selection of ERA
     */
    private int[] selectedERAIds = null;

    // GUI 
    private final ArticleFrame articleFrame;
    private final static String topicBtnText = "Topics";
    private final static String diffCountries = "Diff. countries ";

    @Subscribe
    public void eaSelectionChange(EASelectionChangedEvent e) {
        eaIds = e.getEaIds();
        refresh();
    }

    @Subscribe
    public void eraSelectionChange(ArticleSelectionChangedEvent e) {
        jButton1.setText(topicBtnText + " " + e.getArticleCount());
    }

    private void refresh() {
        jLabel2.setText("" + eaIds.length);
    }

    /**
     * Creates new form ERAAnalysisFrame
     */
    public ERAAnalysisFrame() throws IOException {
        indexReader = IndexUtils.createIndexReader(IndexUtils.openDirectory(LuceneConfig.INDEX_DIR_DEFAULT));
        indexSearcher = IndexUtils.createIndexSearcher(indexReader);
        analyzer = AnalyzerFactory.createCustomAnalyzer();
        queryParser = QueryParserFactory.createQueryParser(analyzer);

        initComponents();

        tmCountries = createTableModelCountries();
        jTable2.setModel(tmCountries);
        jTable2.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                jTable2SelectionChanged(e);
            }
        });

        tmArticles = createTableModelArticles();
        jTable1.setModel(tmArticles);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(10);
        jTable1.getColumnModel().getColumn(1).setMaxWidth(70);
        jTable1.getColumnModel().getColumn(2).setMaxWidth(70);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(100);
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(800);
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                jTable1SelectionChanged(e);
            }
        });

        tmSummary = createTableModelSummary();
        jTable3.setModel(tmSummary);
        jTable3.getColumnModel().getColumn(0).setMaxWidth(80);
        jTable3.getColumnModel().getColumn(1).setWidth(1000);

        tmTokenSummary = createTableModelSummary();
        jTable4.setModel(tmTokenSummary);
        jTable4.getColumnModel().getColumn(0).setMaxWidth(80);
        jTable4.getColumnModel().getColumn(1).setWidth(1000);

        tmDates = createTableModelDates();
        jTable5.setModel(tmDates);
        jTable5.getColumnModel().getColumn(0).setMinWidth(80);
        jTable5.getColumnModel().getColumn(0).setMaxWidth(80);
        jTable5.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                jTable5SelectionChanged(e);
            }
        });

        articleFrame = new ArticleFrame();
        Events.bus.register(articleFrame);
        Events.bus.register(this);

        jTextField1.setText(defaultQuery);
        jTextField1KeyReleased(null);
    }

    private DefaultTableModel createTableModelDates() {
        return new DefaultTableModel(new String[]{"Date", "Count"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 1:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };
    }

    private DefaultTableModel createTableModelSummary() {
        return new DefaultTableModel(new String[]{"Score", "Info"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    default:
                        return String.class;
                }
            }
        };
    }

    private DefaultTableModel createTableModelArticles() {
        return new DefaultTableModel(new String[]{"Docid", "Score", "Country", "Publisher", "Title", ""}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return Float.class;

                    default:
                        return String.class;
                }
            }
        };
    }

    private DefaultTableModel createTableModelCountries() {
        return new DefaultTableModel(new String[]{"Code", "Name", "Count"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                    case 1:
                        return String.class;
                    case 2:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jLabelDiffCountries = new javax.swing.JLabel();
        jButtonCountrySummary = new javax.swing.JButton();
        jButtonDateSummary = new javax.swing.JButton();
        jTextFieldIgnoreTerms = new javax.swing.JTextField();
        jButtonSentiment = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("#EA:");

        jLabel2.setText("count");

        jTextField1.setText("jTextField1");
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jTextField2.setText("10000");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTable1);

        jTable2.setAutoCreateRowSorter(true);
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jTable2);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(jTable3);

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(jTable4);

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(jTable5);

        jButton1.setText("Selected Sum");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabelDiffCountries.setText("Diff countries: ");

        jButtonCountrySummary.setText("Country Summary");
        jButtonCountrySummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCountrySummaryActionPerformed(evt);
            }
        });

        jButtonDateSummary.setText("Date Summary");
        jButtonDateSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDateSummaryActionPerformed(evt);
            }
        });

        jTextFieldIgnoreTerms.setText("ignore terms");

        jButtonSentiment.setText("Sentiment");
        jButtonSentiment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSentimentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(35, 35, 35)
                .addComponent(jTextField1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldIgnoreTerms, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCountrySummary)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDateSummary)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSentiment)
                .addGap(3, 3, 3))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelDiffCountries)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1275, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane3)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)
                    .addComponent(jButtonCountrySummary)
                    .addComponent(jButtonDateSummary)
                    .addComponent(jTextFieldIgnoreTerms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSentiment))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabelDiffCountries)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                            .addComponent(jScrollPane5))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        if (evt != null && evt.getKeyChar() != '\n') {
            return;
        }

        try {
            /* DateTime dtstart = new DateTime(2014, 11, 4, 0, 0);
             DateTime dtend = new DateTime(2014, 11, 12, 0, 0);
             NumericRangeFilter<Long> nrf = NumericRangeFilter.newLongRange(LightDoc.PUB_DATE,
             dtstart.getMillis(), dtend.getMillis(),
             true, true);
             */
            Query q = queryParser.parse(jTextField1.getText());
            origScoreDocs = indexSearcher.search(q, Integer.valueOf(jTextField2.getText())).scoreDocs;

            LOG.info("search successful (" + origScoreDocs.length + "/" + jTextField2.getText() + "): " + q);
            lastQuery = q;
            showArticles();
            showDates();
            showCountryCounts();

        } catch (ParseException | NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jTextField1KeyReleased

    /**
     * Run topic extraction on selected articles
     *
     * @param evt
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        /*  Document[] selectedDocuments = getSelectedDocuments();
         try {
         TopicAnalyzerMallet.test(indexReader, selectedDocuments);
         } catch (IOException ex) {
         Logger.getLogger(ERAAnalysisFrame.class.getName()).log(Level.SEVERE, null, ex);
         }*/
        final String[] ignoreTerms = jTextFieldIgnoreTerms.getText().toLowerCase().split(" ");
        Map<String, List<Integer>> tmp = new HashMap<>();
        List<Integer> tmpList = new ArrayList<>();
        for (int i : selectedERAIds) {
            tmpList.add(i);
        }
        tmp.put("manually selected", tmpList);
        new CountrySummaryFrame(CountrySummaryFrame.Mode.manual, "manual").refresh(indexReader, analyzer, indexSearcher,
                lastQuery,
                tmp, ignoreTerms, getMinSelectedDate(), getMaxSelectedDate());

    }//GEN-LAST:event_jButton1ActionPerformed

    private DateTime getMinSelectedDate() {
        if (selectedDates == null || selectedDates.length == 0) {
            return null;
        }
        DateTime minDate = selectedDates[0];
        for (DateTime d : selectedDates) {
            if (d.isBefore(minDate)) {
                minDate = d;
            }
        }
        return minDate;
    }

    /**
     * Finds the latest selected date and then it takes the time 23:59 of that
     * date so that the full date is included
     *
     * @return
     */
    private DateTime getMaxSelectedDate() {
        if (selectedDates == null || selectedDates.length == 0) {
            return null;
        }
        DateTime maxDate = selectedDates[0];
        for (DateTime d : selectedDates) {
            if (d.isAfter(maxDate)) {
                maxDate = d;
            }
        }

        // we also need to set the max date to the end of the day
        maxDate = maxDate.plusDays(1).withTimeAtStartOfDay().minusMillis(1);

        return maxDate;
    }

    private void jButtonCountrySummaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCountrySummaryActionPerformed
        final String[] ignoreTerms = jTextFieldIgnoreTerms.getText().toLowerCase().split(" ");

        new CountrySummaryFrame(CountrySummaryFrame.Mode.country, "Date Summary")
                .refresh(indexReader, analyzer, indexSearcher,
                        lastQuery,
                        filteredDocumentIdsByCountry, ignoreTerms, getMinSelectedDate(), getMaxSelectedDate());
    }//GEN-LAST:event_jButtonCountrySummaryActionPerformed

    private void jButtonDateSummaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDateSummaryActionPerformed
        final String[] ignoreTerms = jTextFieldIgnoreTerms.getText().toLowerCase().split(" ");

        if (jTable2.getSelectedRowCount() > 0) {
            new CountrySummaryFrame(CountrySummaryFrame.Mode.date, "" + tmCountries.getValueAt(jTable2.convertRowIndexToModel(jTable2.getSelectedRow()), 0))
                    .refresh(indexReader, analyzer, indexSearcher,
                            lastQuery, filteredDocumentIdsByDate, ignoreTerms, getMinSelectedDate(), getMaxSelectedDate()
                    );
        }
    }//GEN-LAST:event_jButtonDateSummaryActionPerformed

    private void jButtonSentimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSentimentActionPerformed
        final String selectedDateString = DateTimeUtils.toYMDString(selectedDates[0]);
        LOG.info("selected date is " + selectedDateString);
        List<Integer> currentArticles = filteredDocumentIdsByDate.get(selectedDateString);
        LOG.info("current articles # = " + currentArticles.size());
        try {
            final Document[] docs = IndexUtils.getDocsByDocId(indexReader, currentArticles);
            SentimentAnalyzer sa = new SentimentAnalyzer(Language.EN);

            final ExecutorService executor = Executors.newFixedThreadPool(LuceneConfig.INDEX_NUMBER_OF_THREADS);

            List<DocumentSentiment> docSentiments = new ArrayList<>();
            // only for top k
            final int topk = Math.min(10, docs.length);
            LOG.info("adding " + topk + " workers to calc sentiment");
            for (int i = 0; i < topk; i++) {
                Document doc = docs[i];
                executor.execute(
                        new DocumentSentimentWorker(doc,
                                QueryParserFactory.defaultFieldNameContent,
                                //LightDoc.TITLE,
                                //sa, 
                                new SentimentAnalyzer(Language.EN),
                                docSentiments
                        ));
            }

            LOG.info("waiting for all threads to finish");
            ThreadUtils.sleep(1000);
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.DAYS);

            System.out.println("" + docSentiments.size());
            Collections.sort(docSentiments);
            System.out.println("" + docSentiments.size());
            float mean = 0;
            for (int i = 0; i < topk; i++) {
                DocumentSentiment ds = docSentiments.get(i);
                mean += ds.getNormalizedNumericalValue();
                System.out.println("out: " + ds);
            }
            mean /= topk;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ERAAnalysisFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonSentimentActionPerformed

    /**
     * Returns the currently selected articles as Lucene docs.
     *
     * @return
     */
    private Document[] getSelectedDocuments() {
        try {
            Document[] docs = new Document[selectedERAIds.length];
            for (int i = 0; i < selectedERAIds.length; i++) {
                docs[i] = indexReader.document(selectedERAIds[i]);
            }
            return docs;
        } catch (IOException ex) {
            Logger.getLogger(ERAAnalysisFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * article selection
     *
     * @param e
     */
    private void jTable1SelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        int[] indexes = jTable1.getSelectedRows();
        if (indexes.length == 0) {
            return;
        }

        selectedERAIds = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            int lookat = jTable1.convertRowIndexToModel(indexes[i]);
            selectedERAIds[i] = (Integer) tmArticles.getValueAt(lookat, 0);
        }
        LOG.log(Level.INFO, "{0} articles selected", selectedERAIds.length);

        List<Document> selectedDocs = new ArrayList<>();
        try {
            boolean isFirst = true;
            for (int docid : selectedERAIds) {
                Document doc = indexReader.document(docid);
                selectedDocs.add(doc);

                if (isFirst) {
                    isFirst = false;
                    Events.bus.post(new ArticleSelectionChangedEvent(LightDocUtils.getContent(doc), selectedERAIds.length));
                }
            }

            Summarizer summarizer = new TtfidfSummarizer(selectedDocs.toArray(new Document[0]), analyzer);
            StringScore[] topSentences = summarizer.getTopKSentences(LightDoc.CONTENT_STEMMED, 200);
            tmSummary.setRowCount(0);
            for (StringScore s : topSentences) {
                tmSummary.addRow(new Object[]{Double.valueOf(NumberUtils.defaultDecimalFormat.format(s.getScore())), s.getValue()});
            }

            StringScore[] topTokens = summarizer.getTopKTokens(LightDoc.CONTENT_STEMMED, 200);
            tmTokenSummary.setRowCount(0);
            for (StringScore s : topTokens) {
                tmTokenSummary.addRow(new Object[]{Double.valueOf(NumberUtils.defaultDecimalFormat.format(s.getScore())), s.getValue()});
            }

        } catch (IOException ex) {
            Logger.getLogger(ERAAnalysisFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Country selection change
     *
     * @param e
     */
    private void jTable2SelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        int[] indexes = jTable2.getSelectedRows();
        if (indexes.length == 0) {
            return;
        }

        selectedCountryCodes = new String[indexes.length];
        int i = 0;
        for (int index : indexes) {
            int tmpindex = jTable2.convertRowIndexToModel(index);
            final String ccode = (String) tmCountries.getValueAt(tmpindex, 0);
            if (ccode.equals("all")) {
                // call it with null if all is part of the selection as we don't filter in this case
                selectedCountryCodes = null;
                showArticles();
                showDates();
                return;
            }
            selectedCountryCodes[i++] = ccode;
        }
        showArticles();
        showDates();
    }

    /**
     * date changed
     *
     * @param e
     */
    private void jTable5SelectionChanged(ListSelectionEvent e) {
        int[] indexes = jTable5.getSelectedRows();
        if (indexes.length == 0) {
            selectedDates = null;
            return;
        }

        selectedDates = new DateTime[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            int index = jTable5.convertRowIndexToModel(indexes[i]);
            String tmpDate = (String) tmDates.getValueAt(index, 0);
            if (tmpDate.equals("all")) {
                // if the user selected all, this is similar to no selection (i.e., allow any article)
                selectedDates = null;
                break;
            }
            DateTime dt = DateTimeUtils.simpleDateTimeFormatter.parseDateTime(tmpDate).withTimeAtStartOfDay();
            selectedDates[i] = dt;
        }

        showArticles();
    }

    private void showDates() {
        if (origScoreDocs == null) {
            return;
        }

        try {
            tmDates.setRowCount(0);
            TreeMap<String, Integer> dateCount = new TreeMap<>();
            final DateTimeFormatter dtf = DateTimeUtils.simpleDateTimeFormatter;

            int totalCount = 0;
            for (ScoreDoc scoreDoc : origScoreDocs) {
                Document doc = indexReader.document(scoreDoc.doc);
                String id = LightDocUtils.getId(doc);

                String code = LightDocUtils.getCountryCode(doc);
                if (selectedCountryCodes != null) { // we need to filter
                    if (!ArrayUtils.contains(selectedCountryCodes, code)) {
                        // needs to be filtered
                        continue;
                    }
                }

                if (ArrayUtils.contains(eaIds, id)) {
                    continue;
                }

                String date = dtf.print(LightDocUtils.getPubDate(doc));
                Integer count = dateCount.get(date);
                if (count == null) {
                    dateCount.put(date, 1);
                } else {
                    dateCount.put(date, count + 1);
                }
                totalCount++;
            }

            tmDates.addRow(new Object[]{"all", totalCount});
            for (Map.Entry<String, Integer> entrySet : dateCount.entrySet()) {
                String key = entrySet.getKey();
                Integer value = entrySet.getValue();
                // ERA
                tmDates.addRow(new Object[]{key, value});
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     *
     */
    private void showArticles() {
        if (origScoreDocs == null) {
            return;
        }

        try {
            if (eaIds == null || eaIds.length == 0) {
                eaIds = new String[]{};
            }

            /**
             * Stores the ids by country
             */
            filteredDocumentIdsByCountry = new HashMap<>();
            filteredDocumentIdsByDate = new HashMap<>();

            tmArticles.setRowCount(0);
            int skipCounter = 0;
            for (ScoreDoc scoreDoc : origScoreDocs) {
                Document doc = indexReader.document(scoreDoc.doc);
                String id = LightDocUtils.getId(doc);

                String code = LightDocUtils.getCountryCode(doc);
                if (selectedCountryCodes != null) { // we need to filter
                    if (!ArrayUtils.contains(selectedCountryCodes, code)) {
                        // needs to be filtered
                        continue;
                    }
                }

                DateTime date = LightDocUtils.getPubDate(doc).withTimeAtStartOfDay();
                if (selectedDates != null) { // we need to filter
                    if (!ArrayUtils.contains(selectedDates, date)) {
                        // needs to be filtered
                        continue;
                    }
                }

                if (ArrayUtils.contains(eaIds, id)) {
                    skipCounter++;
                    continue;
                }

                // ERA
                tmArticles.addRow(new Object[]{
                    new Integer(scoreDoc.doc),
                    new Float(scoreDoc.score),
                    code,
                    LightDocUtils.getPubGuid(doc),
                    LightDocUtils.getTitle(doc) + " - " + doc.get(LightDoc.CHANNEL_FILE)});

                // also store it in our data structure by country
                List<Integer> countryIds = filteredDocumentIdsByCountry.get(code);
                if (countryIds == null) {
                    countryIds = new ArrayList<>();
                    filteredDocumentIdsByCountry.put(code, countryIds);
                }
                countryIds.add(scoreDoc.doc);

                final String dateString = DateTimeUtils.toYMDString(LightDocUtils.getPubDate(doc).withTimeAtStartOfDay());
                List<Integer> dateIds = filteredDocumentIdsByDate.get(dateString);
                if (dateIds == null) {
                    dateIds = new ArrayList<>();
                    filteredDocumentIdsByDate.put(dateString, dateIds);
                }
                dateIds.add(scoreDoc.doc);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void showCountryCounts() {
        try {
            Map<String, Integer> countryCounts = new HashMap<>();

            for (ScoreDoc scoreDoc : origScoreDocs) {
                Document doc = indexReader.document(scoreDoc.doc);
                String id = LightDocUtils.getId(doc);
                if (ArrayUtils.contains(eaIds, id)) {
                    continue;
                }

                String code = LightDocUtils.getCountryCode(doc);

                Integer ccount = countryCounts.get(code);
                if (ccount == null) {
                    countryCounts.put(code, 1);
                } else {
                    countryCounts.put(code, ccount + 1);
                }
            }

            tmCountries.setRowCount(0);
            double[] counts = new double[countryCounts.size()];
            int i = 0;
            int totalCount = 0;
            for (Map.Entry<String, Integer> entrySet : countryCounts.entrySet()) {
                String code = entrySet.getKey();
                Integer count = entrySet.getValue();
                totalCount += count;
                String country = RegionUtils.getCountryName(code);

                tmCountries.addRow(new Object[]{code, country, count});
                counts[i++] = count;
            }
            tmCountries.insertRow(0, new Object[]{"all", "all countries", totalCount});

            double median = new DescriptiveStatistics(counts).getPercentile(50);
            DecimalFormat df = NumberUtils.defaultDecimalFormat;
            jLabelDiffCountries.setText(diffCountries + countryCounts.size()
                    + ", median=" + median
                    + ", avg=" + df.format(1.0 * totalCount / countryCounts.size())
                    + ", total=" + totalCount);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

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
            java.util.logging.Logger.getLogger(ERAAnalysisFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ERAAnalysisFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ERAAnalysisFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ERAAnalysisFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new ERAAnalysisFrame().setVisible(true);
                } catch (IOException io) {

                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonCountrySummary;
    private javax.swing.JButton jButtonDateSummary;
    private javax.swing.JButton jButtonSentiment;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelDiffCountries;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextFieldIgnoreTerms;
    // End of variables declaration//GEN-END:variables
}
