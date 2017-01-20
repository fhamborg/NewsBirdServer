/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kn.uni.hamborg.lucene.search;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import kn.uni.hamborg.analysis.EventArticleExtractor;
import kn.uni.hamborg.config.Events;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.data.emm.EMMItemReader;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.utils.LightDocUtils;
import kn.uni.hamborg.utils.RegionUtils;
import kn.uni.hamborg.gui.era.EASelectionChangedEvent;
import kn.uni.hamborg.gui.era.ERAAnalysisFrame;
import kn.uni.hamborg.language.Language;
import kn.uni.hamborg.language.analyzers.Sentiment;
import kn.uni.hamborg.language.analyzers.SentimentAnalyzer;
import kn.uni.hamborg.lucene.analyzer.AnalyzerFactory;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import kn.uni.hamborg.utils.IndexUtils;
import kn.uni.hamborg.lucene.summarizer.Summarizer;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.lucene.summarizer.TtfidfSummarizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ScoreDocShower extends javax.swing.JFrame {

    private static final Logger LOG = Logger.getLogger(ScoreDocShower.class.getSimpleName());

    // on the big data set we also need the date range query as there is at least one similar event (russian troops
    // cross ukrainian border in august)
    public static final String defaultQuery
            = "+pubDate:[20141103 TO 20141111] "
            + "+contentStemmed:russia* +contentStemmed:ukrain* "
            + "+(titleStemmed:troops titleStemmed:cross titleStemmed:tanks) ";

    private Directory directory;
    private IndexReader ir;
    private ScoreDoc[] scoredocs;
    private IndexSearcher is;
    private Analyzer queryparserAnalyzer;
    private QueryParser queryParser;
    private SentimentAnalyzer sentimentAnalyzer;
    private String[] eaSelectionItemIds = null;
    private ERAAnalysisFrame eraAnalysisFrame = null;

    public String[] getEaSelectionItemIds() {
        return eaSelectionItemIds;
    }

    /**
     * Creates new form ScoreDocShower
     */
    public ScoreDocShower() {
        initComponents();
        try {
            directory = IndexUtils.openDirectory(LuceneConfig.INDEX_DIR_DEFAULT);
            ir = IndexUtils.createIndexReader(directory);
            is = IndexUtils.createIndexSearcher(ir);

            queryparserAnalyzer = AnalyzerFactory.createCustomAnalyzer();
            queryParser = QueryParserFactory.createQueryParser();

            LOG.info("init lucene components successfully");

            eraAnalysisFrame = new ERAAnalysisFrame();
            /* Create and display the form */
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    eraAnalysisFrame.setVisible(true);
                }
            });
            Events.bus.register(eraAnalysisFrame);

            sentimentAnalyzer = new SentimentAnalyzer(Language.EN);

            jTextField3.setText(defaultQuery);
            jTextField3KeyReleased(null);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
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
        jList1 = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jScrollPane4 = new javax.swing.JScrollPane();
        jList3 = new javax.swing.JList();
        jTextField3 = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        jList4 = new javax.swing.JList();
        jScrollPane6 = new javax.swing.JScrollPane();
        jList5 = new javax.swing.JList();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Query Window");

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setMaximumSize(null);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jTextPane1.setAutoscrolls(false);
        jScrollPane2.setViewportView(jTextPane1);

        jList2.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList2ValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(jList2);

        jList3.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList3.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList3ValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(jList3);

        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField3KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField3KeyTyped(evt);
            }
        });

        jList4.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane5.setViewportView(jList4);

        jList5.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList5.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList5ValueChanged(evt);
            }
        });
        jScrollPane6.setViewportView(jList5);

        jTextPane2.setAutoscrolls(false);
        jScrollPane7.setViewportView(jTextPane2);

        jButton1.setText("EA");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField1.setText("10000");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jCheckBox1.setText("Sentiment");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(85, 85, 85))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                    .addComponent(jScrollPane5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)))
            .addComponent(jScrollPane7)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        int pos = jList1.getSelectedIndex();
        if (pos < 0) {
            return;
        }
        if (evt.getValueIsAdjusting()) {
            return;
        }

        jTextPane1.setText("");
        StyledDocument styledDoc = jTextPane1.getStyledDocument();
        Style style = jTextPane1.addStyle("style", null);

        Document[] selectedDocs = new Document[jList1.getSelectedIndices().length];
        try {
            for (int i = 0; i < jList1.getSelectedIndices().length; i++) {
                selectedDocs[i] = ir.document(scoredocs[jList1.getSelectedIndices()[i]].doc);
                String text = LightDocUtils.getContent(selectedDocs[i]);
                Sentiment sentiment = Sentiment.NEUTRAL;
                if (jCheckBox1.isSelected()) {
                    sentiment = sentimentAnalyzer.calcSentiment(text);
                }
                StyleConstants.setBackground(style, getColorSentiment(sentiment));

                try {
                    styledDoc.insertString(styledDoc.getLength(), text + "\n---------\n", style);
                } catch (BadLocationException ex) {
                }

                //  sb.append(getHtmlColorSentiment(sentiment, text) + "\n\n");
            }
            jTextPane1.setCaretPosition(0);

            /*    
             sp.calcTermTfidfInfo(ir, doc, scoredocs[pos].doc, w, LightDoc.TITLE);
             sp.calcTermTfidfInfo(ir, doc, scoredocs[pos].doc, w, LightDoc.TITLE_STEMMED);
             sp.calcTermTfidfInfo(ir, doc, scoredocs[pos].doc, w, LightDoc.CONTENT_STEMMED);
             */
            Writer w = new StringWriter();

            IndexReader tmpReader = IndexUtils.createIndexReader(IndexUtils.createTemporaryDir(selectedDocs));
            Summarizer summarizer = new TtfidfSummarizer(tmpReader, IndexUtils.createIndexSearcher(tmpReader), queryparserAnalyzer);

            StringScore[] topSentences = summarizer.getTopKSentences(LightDoc.CONTENT_STEMMED, 10);

            w.append("\nTop sentences\n\n");
            for (StringScore s : topSentences) {
                w.append("" + s.getScore() + ": " + s.getValue() + "\n\n");
            }
            w.flush();
            jTextPane2.setText(w.toString());
            jTextPane2.setCaretPosition(0);
            w.close();
            tmpReader.close();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, ioe.getMessage(), ioe);
        }
    }//GEN-LAST:event_jList1ValueChanged
    public static void main(String[] args) {
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
        new ScoreDocShower().setVisible(true);
    }
    private void jList2ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList2ValueChanged
        if (jList2.getSelectedIndex() < 0) {
            return;
        }

        // user selected a date
        DefaultListModel dlm = new DefaultListModel();
        jList3.setModel(dlm);

        ListModel dlmCountry = jList4.getModel();
        DefaultListModel dlmDateCountry = new DefaultListModel();
        jList5.setModel(dlmDateCountry);
        List<String> countries = new ArrayList<>();

        String line = (String) jList2.getModel().getElementAt(jList2.getSelectedIndex());
        line = line.split(" ")[0];

        final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");

        ListModel lm = jList1.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            String text = (String) lm.getElementAt(i);
            String date = text.split(":")[1].trim();
            String country = text.split(":")[3].trim();
            if (line.equals(date)) {
                dlm.addElement(text);

                if (countries.contains(country)) {
                } else {
                    countries.add(country);
                    //  countryFirstRow.add(i);
                    dlmDateCountry.addElement("" + i + ": " + country);
                }
            }

        }
    }//GEN-LAST:event_jList2ValueChanged

    private void jTextField3KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField3KeyTyped

    }//GEN-LAST:event_jTextField3KeyTyped

    private void jTextField3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField3KeyReleased
        if (jTextField3.getText().length() < 3) {
            //    return;
        }

        if (evt != null && evt.getKeyChar() != '\n') {
            return;
        }

        try {

            Query q = queryParser.parse(jTextField3.getText());
            ScoreDoc[] scoreDocs = is.search(q, Integer.valueOf(jTextField1.getText())).scoreDocs;
            LOG.info("search successful (" + scoreDocs.length + "/" + jTextField1.getText() + "): " + q);
            showResults(is, scoreDocs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jTextField3KeyReleased

    private void jList3ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList3ValueChanged
        if (jList3.getSelectedIndex() < 0) {
            return;
        }

        ListModel lm = jList1.getModel();
        int[] indices = new int[jList3.getSelectedIndices().length];
        for (int i = 0; i < jList3.getSelectedIndices().length; i++) {
            String list3line = (String) jList3.getModel().getElementAt(jList3.getSelectedIndices()[i]);
            list3line = list3line.replaceAll("\\<.*?>", "");
            int row = Integer.valueOf(list3line.split(":")[0]);
            indices[i] = row;
        }
        jList1.setSelectedIndices(indices);
        //  jList1.ensureIndexIsVisible(jList3);
    }//GEN-LAST:event_jList3ValueChanged

    private void jList5ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList5ValueChanged
        if (jList5.getSelectedIndex() < 0) {
            return;
        }

        ListModel lm = jList1.getModel();
        String list5line = (String) jList5.getModel().getElementAt(jList5.getSelectedIndex());
        int row = Integer.valueOf(list5line.split(":")[0]);
        jList1.setSelectedIndex(row);
        jList1.ensureIndexIsVisible(row);
    }//GEN-LAST:event_jList5ValueChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // start EA extraction
        final EventArticleExtractor eae = new EventArticleExtractor(directory, ir, is, queryparserAnalyzer, queryParser);
        Document[] eaDocs = eae.getEventArticles(jTextField3.getText()).getEventArticleDocuments();
        eaSelectionItemIds = new String[eaDocs.length];
        int i = 0;
        for (Document doc : eaDocs) {
            eaSelectionItemIds[i++] = LightDocUtils.getId(doc);
        }
        Events.bus.post(new EASelectionChangedEvent(eaSelectionItemIds));
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu menu = new JPopupMenu();
            final ListModel lm = jList1.getModel();
            final int[] indices = jList1.getSelectedIndices();
            JMenuItem itemHeader = new JMenuItem("sel items = " + indices.length);
            JMenuItem itemSaveAsEASelection = new JMenuItem("Set as EA selection");
            itemSaveAsEASelection.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    eaSelectionItemIds = new String[indices.length];
                    int i = 0;
                    for (int index : indices) {
                        try {
                            eaSelectionItemIds[i++] = ir.document(scoredocs[index].doc).getField(LightDoc.ID).stringValue();
                            //  System.out.println(ir.document(scoredocs[index].doc).getField(LightDoc.ID).stringValue());
                        } catch (IOException ex) {
                            Logger.getLogger(ScoreDocShower.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    LOG.info("saved " + indices.length + " articles as current EA selection");
                    Events.bus.post(new EASelectionChangedEvent(eaSelectionItemIds));
                }
            });

            menu.add(itemHeader);
            menu.add(itemSaveAsEASelection);
            menu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_jList1MouseClicked

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    public void showResults(final IndexSearcher is, final ScoreDoc[] docs) {
        if (docs == null) {
            return;
        }
        scoredocs = docs;
        this.is = is;
        DefaultListModel dlm = new DefaultListModel();
        jList1.setModel(dlm);
        DefaultListModel dlmDate = new DefaultListModel();
        jList2.setModel(dlmDate);
        DefaultListModel dlmCountry = new DefaultListModel();
        jList4.setModel(dlmCountry);
        List<String> countries = new ArrayList<>();
        //  List<Integer> countryFirstRow = new ArrayList<>();

        final TreeMap<String, Integer> mapDateCount = new TreeMap<>();
        final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");

        for (int i = 0; i < docs.length; i++) {
            ScoreDoc scoreDoc = docs[i];
            try {
                // Document first = is.doc(docs[0].doc);
                Document doc = is.doc(scoreDoc.doc);

                //   double d = CosineDocumentSimilarity.getCosineSimilarity(ir, scoreDocs[0].doc, scoreDoc.doc, LightDoc.CONTENT);
                if (EMMItemReader.filename.equals(doc.get(LightDoc.CHANNEL_FILE))
                        && EMMItemReader.pos == new Integer(doc.get(LightDoc.CHANNEL_POSITION))) {
                //    System.out.println(EMMItemReader.readEMMItem(doc).getText());
                }

                String datestring = dtf.print(LightDocUtils.getPubDate(doc));
                Integer datecount = mapDateCount.get(datestring);
                if (datecount == null) {
                    mapDateCount.put(datestring, 1);
                } else {
                    mapDateCount.put(datestring, datecount + 1);
                }

                if (countries.contains(doc.get(LightDoc.PUB_COUNTRY))) {
                } else {
                    countries.add(doc.get(LightDoc.PUB_COUNTRY));
                    //  countryFirstRow.add(i);
                    dlmCountry.addElement("" + i + ": " + doc.get(LightDoc.PUB_COUNTRY));
                }
                dlm.addElement("<html>" + i + ": "
                        + dtf.print(LightDocUtils.getPubDate(doc)) + ": "
                        + scoreDoc.score + ": "
                        + doc.get(LightDoc.PUB_COUNTRY) + ": "
                        /*+ getHtmlColorSentiment(
                         sentimentAnalyzer.calcSentiment(
                         doc.get(LightDoc.CONTENT).substring(0, (int) Math.min(200, doc.get(LightDoc.CONTENT).length()))),
                         doc.get(LightDoc.TITLE)) + " - "*/
                        + doc.get(LightDoc.TITLE) + " - "
                        + RegionUtils.getCountryName(doc.get(LightDoc.PUB_COUNTRY)) + " - "
                        // + doc.get(LightDoc.CONTENT_STEMMED) + " - "
                        + doc.get(LightDoc.ID) + " - "
                        // + doc.get(LightDoc.PUB_URL) + " - "
                        + "</html>"
                );
            } catch (IOException io) {
                io.printStackTrace();
            }
            // EMMItem item = EMMItemReader.readEMMItem(doc);
            // System.out.println(item.toString());
        }

        for (String date : mapDateCount.keySet()) {
            dlmDate.addElement(date + " " + mapDateCount.get(date));
        }

        setVisible(true);
    }

    private Color getColorSentiment(Sentiment sentiment) {
        switch (sentiment) {
            case GOOD:
                return Color.GREEN;
            case NEUTRAL:
                return Color.WHITE;
            case NEGATIVE:
                return Color.RED;
        }
        return Color.WHITE;
    }

    private String getHtmlColorSentiment(Sentiment sentiment, String text) {
        String color = "";
        switch (sentiment) {
            case GOOD:
                color = "green";
                break;
            case NEUTRAL:

                break;
            case NEGATIVE:
                color = "red";
                break;
        }
        return "<font color=\"" + color + "\">" + text + "</font>";
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JList jList3;
    private javax.swing.JList jList4;
    private javax.swing.JList jList5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    // End of variables declaration//GEN-END:variables
}
