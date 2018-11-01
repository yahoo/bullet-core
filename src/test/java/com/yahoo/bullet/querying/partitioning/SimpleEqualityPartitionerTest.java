/*
 *  Copyright 2018, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.querying.partitioning;

import com.yahoo.bullet.common.BulletConfig;
import com.yahoo.bullet.parsing.Aggregation;
import com.yahoo.bullet.parsing.Clause;
import com.yahoo.bullet.parsing.FilterClause;
import com.yahoo.bullet.parsing.Query;
import com.yahoo.bullet.parsing.StringFilterClause;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.yahoo.bullet.parsing.FilterUtils.makeClause;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class SimpleEqualityPartitionerTest {
    private BulletConfig config;

    @BeforeClass
    public void setup() {
        config = new BulletConfig();
        config.set(BulletConfig.QUERY_PARTITIONER_ENABLE, true);
        config.set(BulletConfig.QUERY_PARTITIONER_CLASS_NAME, SimpleEqualityPartitioner.class.getName());
        config.set(BulletConfig.EQUALITY_PARTITIONER_DELIMITER, "-");
        config.validate();
    }

    private SimpleEqualityPartitioner createPartitioner(String... fields) {
        config.set(BulletConfig.EQUALITY_PARTITIONER_FIELDS, asList(fields));
        config.validate();
        return new SimpleEqualityPartitioner(config);
    }

    private Query createQuery(Clause... filters) {
        Query query = new Query();
        if (filters != null) {
            query.setFilters(asList(filters));
        }
        query.setAggregation(new Aggregation());
        query.configure(config);
        query.initialize();
        return query;
    }

    @Test
    public void testDefaultPartitioningQueryWithNoFilters() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B");
        Assert.assertEquals(partitioner.getKeys(createQuery()), singletonList("null-null"));
    }

    @Test
    public void testDefaultPartitioningQueryWithUnrelatedFilters() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B");
        Query query = createQuery(makeClause("C", singletonList("bar"), Clause.Operation.EQUALS),
                                  makeClause("D", singletonList("baz"), Clause.Operation.EQUALS));
        Assert.assertEquals(partitioner.getKeys(query), singletonList("null-null"));
    }

    @Test
    public void testDefaultPartitioningQueryWithNonEqualityFilters() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B");
        Query query = createQuery(makeClause("A", singletonList("bar"), Clause.Operation.REGEX_LIKE),
                                  makeClause("B", singletonList("baz"), Clause.Operation.CONTAINS_KEY));
        Assert.assertEquals(partitioner.getKeys(query), singletonList("null-null"));
    }

    @Test
    public void testDefaultPartitioningQueryWithOR() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B");
        Query query = createQuery(makeClause(Clause.Operation.OR,
                                             makeClause("A", singletonList("bar"), Clause.Operation.EQUALS),
                                             makeClause("B", singletonList("baz"), Clause.Operation.EQUALS)));
        Assert.assertEquals(partitioner.getKeys(query), singletonList("null-null"));
    }

    @Test
    public void testDefaultPartitioningQueryWithNOT() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B");
        Query query = createQuery(makeClause(Clause.Operation.NOT, makeClause("A", singletonList("bar"), Clause.Operation.EQUALS)));
        Assert.assertEquals(partitioner.getKeys(query), singletonList("null-null"));
    }

    @Test
    public void testDefaultPartitioningForQueryWithMissingValues() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A");
        FilterClause clause = new StringFilterClause();
        clause.setField("A");
        clause.setValues(null);
        clause.setOperation(Clause.Operation.EQUALS);
        Query query = createQuery(clause);
        Assert.assertEquals(partitioner.getKeys(query), singletonList("null"));
    }

    @Test
    public void testDefaultPartitioningForQueryWithMultipleValues() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B");
        Query query = createQuery(makeClause(Clause.Operation.AND,
                                             makeClause("A", asList("foo", "bar"), Clause.Operation.EQUALS),
                                             makeClause("B", singletonList("baz"), Clause.Operation.EQUALS)));
        Assert.assertEquals(partitioner.getKeys(query), singletonList("null-null"));
    }

    @Test
    public void testDefaultPartitioningForQueryWithRepeatedFilters() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B");
        Query query = createQuery(makeClause(Clause.Operation.AND,
                                             makeClause(Clause.Operation.AND,
                                                        makeClause("A", singletonList("quux"), Clause.Operation.EQUALS),
                                                        makeClause("B", singletonList("norf"), Clause.Operation.EQUALS)),
                                             makeClause(Clause.Operation.AND,
                                                        makeClause("B", singletonList("qux"), Clause.Operation.EQUALS),
                                                        makeClause("A", singletonList("bar"), Clause.Operation.EQUALS))));
        Assert.assertEquals(partitioner.getKeys(query), singletonList("null-null"));
    }

    @Test
    public void testPartitioningForQueryWithMissingFields() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B");
        Query query = createQuery(makeClause(Clause.Operation.AND, makeClause("A", singletonList("bar"), Clause.Operation.EQUALS)));
        Assert.assertEquals(partitioner.getKeys(query), singletonList("bar-null"));
    }

    @Test
    public void testPartitioningForQueryWithAllFields() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B", "C");
        Query query = createQuery(makeClause(Clause.Operation.AND,
                                             makeClause("C", singletonList("qux"), Clause.Operation.EQUALS),
                                             makeClause("B", singletonList("baz"), Clause.Operation.EQUALS),
                                             makeClause("A", singletonList("bar"), Clause.Operation.EQUALS)));
        Assert.assertEquals(partitioner.getKeys(query), singletonList("bar-baz-qux"));
    }

    @Test
    public void testPartitioningForQueryWithNestedFields() {
        SimpleEqualityPartitioner partitioner = createPartitioner("A", "B", "C", "D.e");
        Query query = createQuery(makeClause(Clause.Operation.AND,
                                             makeClause(Clause.Operation.AND,
                                                        makeClause("B", singletonList("quux"), Clause.Operation.EQUALS),
                                                        makeClause("D.e", singletonList("norf"), Clause.Operation.EQUALS)),
                                             makeClause(Clause.Operation.AND,
                                                        makeClause("C", singletonList("qux"), Clause.Operation.EQUALS),
                                                        makeClause("A", singletonList("bar"), Clause.Operation.EQUALS))));
        Assert.assertEquals(partitioner.getKeys(query), singletonList("bar-quux-qux-norf"));
    }
}