/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.OpenNLPConfig;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class OpenNLPParser2 {

    private static final Logger LOG = Logger.getLogger(OpenNLPParser2.class.getSimpleName());

    private final OpenNLPPosAnalyzer posTagger;
    private final Parser parser;
    private final SentenceSplitter sentenceSplitter;

    public OpenNLPParser2() throws IOException {
        DateTime start = DateTime.now();
        sentenceSplitter = new SentenceSplitter();
        posTagger = new OpenNLPPosAnalyzer();
        parser = ParserFactory.create(new ParserModel(new File(OpenNLPConfig.basePath,
                "en" + "-parser-chunking.bin")));
        LOG.log(Level.INFO, "initalization finished in {0} seconds", Seconds.secondsBetween(start, DateTime.now()).getSeconds());
    }
// siehe Olyas mail: erst NP das NN dann VP das Verb und dann auch in der VP das erste NN nach dem Verb
    public String extractFromSentence(String s) {
        Parse root = ParserTool.parseLine(s, parser, 1)[0];
        rec(root, 0);
        return "";
    }

    public void rec(Parse p, int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
        System.out.println(p.getType() + "\t" + p.getCoveredText());
        for (Parse c : p.getChildren()) {
            rec(c, level + 1);
        }
    }

    public static void main(String[] args) throws IOException {
        OpenNLPParser2 p = new OpenNLPParser2();

        String s = "Ukraine and the West have continuously accused Moscow of fuelling a pro-Russian rebellion in the east with troops and weapons, which Russia has denied.";
        System.out.println(p.extractFromSentence(s));
    }

    /**
     * From Online: http://nlp.stanford.edu:8080/parser/index.jsp
     *
     * (ROOT (S (NP (NP (NNP Ukraine)) (CC and) (NP (DT the) (NN West))) (VP
     * (VBP have) (ADVP (RB continuously)) (VP (VBN accused) (NP (NP (NNP
     * Moscow)) (PP (IN of) (S (VP (VBG fuelling) (NP (NP (DT a) (JJ
     * pro-Russian) (NN rebellion)) (PP (IN in) (NP (DT the) (JJ east)))) (PP
     * (IN with) (NP (NP (NNS troops) (CC and) (NNS weapons)) (, ,) (SBAR (WHNP
     * (WDT which)) (S (NP (NNP Russia)) (VP (VBZ has) (VP (VBN
     * denied))))))))))))) (. .)))
     */
    /**
     * From here: Is basically the same, but punctations are not separate (as
     * above)
     *
     * (TOP (S (NP (NP (NNP Ukraine)) (CC and) (NP (DT the) (NNP West))) (VP
     * (VBP have) (ADVP (RB continuously)) (VP (VBN accused) (NP (NNP Moscow))
     * (PP (IN of) (S (VP (VBG fuelling) (NP (NP (DT a) (JJ pro-Russian) (NN
     * rebellion)) (PP (IN in) (NP (DT the) (JJ east)))) (PP (IN with) (NP (NP
     * (NNS troops) (CC and) (NNS weapons,)) (SBAR (WHNP (WDT which)) (S (NP
     * (NNP Russia)) (VP (VBZ has) (VP (VBN denied.))))))))))))))
     *
     */
}
