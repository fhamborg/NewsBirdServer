/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages and allows access to attributes.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class AttributeHolder {

    private static final Logger LOG = Logger.getLogger(AttributeHolder.class.getSimpleName());

    private final Map<String, Object> attributes;

    public AttributeHolder() {
        this.attributes = new HashMap<>();
    }

    /**
     * Adds an attribute to this. An attribute can be any additional information
     * that is then in turn bound to this. Note that there can be only one
     * attribute of one class, it will overwrite the old attribute.
     *
     * @param attribute
     */
    public void addAttribute(Object attribute) {
        addAttribute(attribute.getClass().getSimpleName(), attribute);
    }

    /**
     * Adds an attribute to this.
     *
     * @param key
     * @param value
     */
    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Returns the attribute.
     *
     * @param attributeClass
     * @return
     */
    public Object getAttribute(Class attributeClass) {
        return getAttribute(attributeClass.getSimpleName());
    }

    /**
     * Gets the attribute value of key.
     *
     * @param key
     * @return
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Removes the attribute identified by its class.
     *
     * @param attributeClass
     */
    public void removeAttribute(Class attributeClass) {
        removeAttribute(attributeClass.getSimpleName());
    }

    /**
     * Removes the attribute identified by key.
     *
     * @param key
     */
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
