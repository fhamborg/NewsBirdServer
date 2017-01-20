/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kn.uni.hamborg.lucene.field;

import java.io.Reader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class VecTextField extends Field {

    /* Indexed, tokenized, not stored. */
    public static final FieldType TYPE_NOT_STORED = new FieldType();

    /* Indexed, tokenized, stored. */
    public static final FieldType TYPE_STORED = new FieldType();

    static {
        TYPE_NOT_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        TYPE_NOT_STORED.setTokenized(true);
        TYPE_NOT_STORED.setStoreTermVectors(true);
        TYPE_NOT_STORED.setStoreTermVectorPositions(true);
        TYPE_NOT_STORED.freeze();

        TYPE_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setStoreTermVectors(true);
        TYPE_STORED.setStoreTermVectorPositions(true);
        TYPE_STORED.freeze();
    }

// TODO: add sugar for term vectors...?
    /**
     * Creates a new TextField with Reader value.
     */
    public VecTextField(String name, Reader reader, Store store) {
        super(name, reader, store == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
    }

    /**
     * Creates a new TextField with String value.
     */
    public VecTextField(String name, String value, Store store) {
        super(name, value, store == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
    }

    /**
     * Creates a new un-stored TextField with TokenStream value.
     */
    public VecTextField(String name, TokenStream stream) {
        super(name, stream, TYPE_NOT_STORED);

    }
}
