/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.gui.era;

import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EASelectionChangedEvent {

    private static final Logger LOG = Logger.getLogger(EASelectionChangedEvent.class.getSimpleName());

    private String[] eaIds;

    public EASelectionChangedEvent(String[] eaIds) {
        this.eaIds = eaIds;
    }

    public String[] getEaIds() {
        return eaIds;
    }
}
