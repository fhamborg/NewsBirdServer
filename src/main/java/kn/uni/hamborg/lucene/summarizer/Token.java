/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.summarizer;

import java.util.logging.Logger;

/**
 * Represents a token with additional information.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class Token {

    private static final Logger LOG = Logger.getLogger(Token.class.getSimpleName());

    private final String token;
    private double tfidf;

    public Token(String token, double tfidf) {
        this.token = token;
        this.tfidf = tfidf;
    }

    public double getTfidf() {
        return tfidf;
    }

    public void setTfidf(double tfidf) {
        this.tfidf = tfidf;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return token + "[" + getTfidf() + "]";
    }

}
