/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import kn.uni.hamborg.data.light.LightDoc;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * This represents a dimension which can be used to filter the dataset. Such a
 * dimension consists of values. TODO add more description.
 *
 * work in progress for countries. however, we need to have a base class and few
 * sub classes of this for different dims.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public abstract class FilterDimension implements List<FilterValue> {

    protected final ImmutableList<FilterValue> filterValues;

    public static Class[] FilterDimensionClasses = new Class[]{
        CountryCodeFilterDimension.class, TitleContainsFilterDimension.class,
        ContentContainsFilterDimension.class, DescriptionContainsFilterDimension.class,
        PublishDayFilterDimension.class, RecipientFilterDimension.class
    };

    protected FilterDimension(ImmutableList<FilterValue> filterValues) {
        this.filterValues = filterValues;
    }

    /**
     * Returns a {@link Query} which returns all {@link Document}s of this
     * {@link FilterDimension}. For example, if the {@link FilterDimension} is
     * {@code DE, RU, US} then all documents published in one of these countries
     * will be returned by using this {@link Query}.
     *
     * @return
     */
    public Query getAnyQuery() {
        BooleanQuery bq = new BooleanQuery();
        for (FilterValue filterValue : filterValues) {
            Query q = filterValue.getFilterQuery();
            bq.add(q, BooleanClause.Occur.SHOULD);
        }

        return bq;
    }

    @Override
    public Iterator<FilterValue> iterator() {
        return filterValues.iterator();
    }

    /**
     * Creates a {@link FilterDimension} based on the given country codes.
     *
     * @param countryCodes
     * @return
     */
    public static FilterDimension createForCountryCodes(List<String> countryCodes) {
        return CountryCodeFilterDimension.createFilterDimension(countryCodes);
    }

    public static FilterDimension createForTextContains(String fieldname, List<String> containsString) {
        return TextContainsFilterDimension.createFilterDimension(fieldname, containsString);
    }

    public static FilterDimension createForRecipients(List<String> recipients) {
        return RecipientFilterDimension.createFilterDimension(recipients);
    }

    public static FilterDimension createForPublishDays(List<String> days) {
        return PublishDayFilterDimension.createFilterDimension(days);
    }

    public static FilterDimension createFromSimpleClassName(String simpleClassName, String[] values, QueryParser queryParser) {
        if (simpleClassName.equals(CountryCodeFilterDimension.class.getSimpleName())) {
            return createForCountryCodes(Arrays.asList(values));
        } else if (simpleClassName.equals(TitleContainsFilterDimension.class.getSimpleName())) {
            return createForTextContains(LightDoc.TITLE_STEMMED, Arrays.asList(values));
        } else if (simpleClassName.equals(ContentContainsFilterDimension.class.getSimpleName())) {
            return createForTextContains(LightDoc.CONTENT_STEMMED, Arrays.asList(values));
        } else if (simpleClassName.equals(DescriptionContainsFilterDimension.class.getSimpleName())) {
            return createForTextContains(LightDoc.DESCRIPTION_STEMMED, Arrays.asList(values));
        } else if (simpleClassName.equals(RecipientFilterDimension.class.getSimpleName())) {
            return createForRecipients(Arrays.asList(values));
        } else if (simpleClassName.equals(PublishDayFilterDimension.class.getSimpleName())) {
            return createForPublishDays(Arrays.asList(values));
        }

        throw new IllegalArgumentException("'" + simpleClassName + "' unknown");
    }

    public int[] docCountArray() {
        int[] arr = new int[filterValues.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = filterValues.get(i).getNumberOfDocs();
        }
        return arr;
    }

    public String[] asStringArray() {
        String[] arr = new String[filterValues.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = filterValues.get(i).getDescriptor();
        }

        return arr;
    }

    @Override
    public int size() {
        return filterValues.size();
    }

    @Override
    public boolean isEmpty() {
        return filterValues.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return filterValues.contains(o);
    }

    @Override
    public Object[] toArray() {
        return filterValues.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return filterValues.toArray(a);
    }

    @Override
    public boolean add(FilterValue e) {
        return filterValues.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return filterValues.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return filterValues.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends FilterValue> c) {
        return filterValues.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends FilterValue> c) {
        return filterValues.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return filterValues.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return filterValues.retainAll(c);
    }

    @Override
    public void clear() {
        filterValues.clear();
    }

    @Override
    public FilterValue get(int index) {
        return filterValues.get(index);
    }

    @Override
    public FilterValue set(int index, FilterValue element) {
        return filterValues.set(index, element);
    }

    @Override
    public void add(int index, FilterValue element) {
        filterValues.add(index, element);
    }

    @Override
    public FilterValue remove(int index) {
        return filterValues.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return filterValues.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return filterValues.lastIndexOf(o);
    }

    @Override
    public ListIterator<FilterValue> listIterator() {
        return filterValues.listIterator();
    }

    @Override
    public ListIterator<FilterValue> listIterator(int index) {
        return filterValues.listIterator(index);
    }

    @Override
    public List<FilterValue> subList(int fromIndex, int toIndex) {
        return filterValues.subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
