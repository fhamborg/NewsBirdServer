/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.config.StatConfig;
import kn.uni.hamborg.language.analyzers.AggregatedSubjectivity;
import kn.uni.hamborg.language.analyzers.MPQASubjectivityAnalyzer;
import kn.uni.hamborg.lucene.analyzer.AnalyzerFactory;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import kn.uni.hamborg.utils.DateTimeUtils;
import kn.uni.hamborg.utils.ExcelWriter;
import kn.uni.hamborg.utils.IndexUtils;
import kn.uni.hamborg.utils.LightDocUtils;
import kn.uni.hamborg.utils.NumberUtils;
import kn.uni.hamborg.utils.PersistenceUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Bits;

/**
 * Provides functionality to find something out about subjectivity in our data.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class SubjectivityAnalysis {

    private static final Logger LOG = Logger.getLogger(SubjectivityAnalysis.class.getSimpleName());

    private static final File index = LuceneConfig.INDEX_DIR_DEFAULT;

    private static final String affixStats = "stats_";

    private enum Task {

        country, publisher, daydate
    };

    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private Directory directory;
    private QueryParser queryParser;
    private Analyzer analyzer;
    private final MPQASubjectivityAnalyzer subjectivityAnalyzer;

    public SubjectivityAnalysis() throws IOException {
        this(IndexUtils.openDirectory(index));
    }

    public SubjectivityAnalysis(Directory directory) throws IOException {
        this.directory = directory;
        indexReader = IndexUtils.createIndexReader(directory);
        indexSearcher = IndexUtils.createIndexSearcher(indexReader);
        analyzer = AnalyzerFactory.createCustomAnalyzer();
        queryParser = QueryParserFactory.createQueryParser(analyzer);
        subjectivityAnalyzer = new MPQASubjectivityAnalyzer();
    }

    private void subjectivityAnalysis(Task task, boolean onlineAggregation) throws IOException {
        final Map<String, AggregatedSubjectivity> countryCodeAndAggrSubjectivityPerDocument = new HashMap<>();

        // code to iterate the index
        final int samplingRate = 10;
        final long starttime = new Date().getTime();
        final Random rand = new Random(starttime);
        Bits liveDocs = MultiFields.getLiveDocs(indexReader);
        LOG.log(Level.INFO, "max doc id is = {0}", indexReader.maxDoc());
        for (int i = 0; i < indexReader.maxDoc(); i++) {
            if (liveDocs != null && !liveDocs.get(i)) {
                continue;
            }

            final Document doc = indexReader.document(i);
            if (i % 10000 == 0) {
                double remaining = indexReader.maxDoc() - i;
                long curtime = new Date().getTime();
                double difftime = (curtime - starttime);
                double speed = i * 1.0 / (double) difftime;
                double remainingtime = remaining / speed;
                LOG.log(Level.INFO, "{0} / {1} - remaining [m] {2}", new Object[]{i, indexReader.maxDoc(), NumberUtils.defaultDecimalFormat.format(remainingtime / 1000 / 60)});
            }
            if (samplingRate < 100 && rand.nextInt(100) > samplingRate) {
                continue;
            }
            // place your own code below
            String key = null;
            Set<String> infos = new HashSet<>();
            switch (task) {
                case country:
                    key = LightDocUtils.getCountryCode(doc);
                    break;
                case publisher:
                    key = LightDocUtils.getPubGuid(doc);
                    infos.add(LightDocUtils.getCountryCode(doc));
                    break;
                case daydate:
                    key = DateTimeUtils.toYMDString(LightDocUtils.getPubDate(doc));
                    break;
                default:
                    throw new IllegalArgumentException("unknown " + task);
            }
            final String content = LightDocUtils.getContent(doc);
            final String title = LightDocUtils.getTitle(doc);

            AggregatedSubjectivity aggrSubj = countryCodeAndAggrSubjectivityPerDocument.get(key);
            AggregatedSubjectivity tmp = subjectivityAnalyzer.calcSubjectivity(content, true);
            tmp.addInfos(infos);

            if (aggrSubj == null) {
                countryCodeAndAggrSubjectivityPerDocument.put(key, tmp);
            } else {
                aggrSubj.aggregateWith(tmp);
            }

        }

        File results = getFileById(task);
        results.delete();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(results));
        oos.writeObject(countryCodeAndAggrSubjectivityPerDocument);
        oos.close();

        LOG.log(Level.INFO, "finished analysis ''{0}'', written to disk", task);
    }

    private File getFileById(Task taskname) {
        switch (taskname) {
            case daydate:
                return StatConfig.pathSubjectivityModelDayDate;
            case country:
                return StatConfig.pathSubjectivityModelCountry;
            case publisher:
                return StatConfig.pathSubjectivityModelPublisher;
            default:
                throw new IllegalArgumentException("unknown: " + taskname);
        }
    }

    private void work(Task task) throws Exception {
        LOG.log(Level.INFO, "reading in POJOs from {0}", getFileById(task));
        Map<String, AggregatedSubjectivity> countryCodeAndAggrSubjectivityPerDocument
                = (Map<String, AggregatedSubjectivity>) PersistenceUtils.loadObject(getFileById(task));
        LOG.info("reading finished");

        File file = new File(StatConfig.pathSubjectivityResults, affixStats + task + ".xls");
        file.delete();
        ExcelWriter excel = new ExcelWriter(file,
                new String[]{"id", "tokens", "reltotal", "relweak", "relstrong", "relweakmin", "relweakmax",
                    "relstrongmin", "relstrongmax", "infos"});

        LOG.log(Level.INFO, "exporting {0} entries", countryCodeAndAggrSubjectivityPerDocument.size());
        for (Map.Entry<String, AggregatedSubjectivity> entrySet : countryCodeAndAggrSubjectivityPerDocument.entrySet()) {
            String cc = entrySet.getKey();
            AggregatedSubjectivity perItemAggr = entrySet.getValue();
            excel.addRow(new Object[]{
                cc, perItemAggr.getCountTokens(), perItemAggr.getRelativeTotalSubjectivity(),
                perItemAggr.getRelativeWeakSubjectivity(), perItemAggr.getRelativeStrongSubjectivity(),
                perItemAggr.getMinRelativeWeakSubjectivity(), perItemAggr.getMaxRelativeWeakSubjectivity(),
                perItemAggr.getMinRelativeStrongSubjectivity(), perItemAggr.getMaxRelativeStrongSubjectivity(),
                perItemAggr.getInfos().toString()
            });


            /*  System.out.println(cc);
             AggregatedSubjectivity perCountryAggr = new AggregatedSubjectivity(aggrSubjPerDoc);
             System.out.println(perCountryAggr.toString());
             System.out.println("");*/
        }

        countryCodeAndAggrSubjectivityPerDocument = null;
        System.gc();
        LOG.info("finished, now finalizing on disk");
        excel.close();
    }

    public static void main(String[] args) throws Exception {
        SubjectivityAnalysis sa = new SubjectivityAnalysis();
        /* sa.subjectivityAnalysis(Task.country, true);
         sa.work(Task.country);
         sa.subjectivityAnalysis(Task.publisher, true);
         sa.work(Task.publisher);*/
        sa.subjectivityAnalysis(SubjectivityAnalysis.Task.daydate, true);
        sa.work(Task.daydate);

    }
}
