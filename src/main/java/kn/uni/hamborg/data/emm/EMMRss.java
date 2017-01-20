/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.emm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
@XmlRootElement(name = "rss")
public class EMMRss {

    private EMMChannel channel;

    public EMMChannel getChannel() {
        return channel;
    }

    @XmlElement(name = "channel")
    public void setChannel(EMMChannel channel) {
        this.channel = channel;
    }
}
