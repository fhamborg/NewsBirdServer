/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.config;

import com.google.common.eventbus.EventBus;
import java.util.logging.Logger;

/**
 * This class provides the {@link EventBus} Guave event bus instance that is
 * used globally in the application.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class Events {

    private static final Logger LOG = Logger.getLogger(Events.class.getSimpleName());

    public static final EventBus bus = new EventBus("global");
}
