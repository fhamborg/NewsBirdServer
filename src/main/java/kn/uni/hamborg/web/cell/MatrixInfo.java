/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.cell;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.topic.Topic;

/**
 * Represents all information belonging to the main matrix visualization.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class MatrixInfo {

    private static final Logger LOG = Logger.getLogger(MatrixInfo.class.getSimpleName());

    private final List<Cell> cells;
    private final String[] rows;
    private final int[] rowDocCounts;
    private final String[] columns;
    private final int[] columnDocCounts;
    private final Map<Integer, Topic> topics;

    public MatrixInfo(List<Cell> cells, String[] rows, int[] rowDocCounts, String[] columns, int[] columnDocCounts, Map<Integer, Topic> topics) {
        this.cells = cells;
        this.rows = rows;
        this.rowDocCounts = rowDocCounts;
        this.columns = columns;
        this.columnDocCounts = columnDocCounts;
        this.topics = topics;
    }
}
