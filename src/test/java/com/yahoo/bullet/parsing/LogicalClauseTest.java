/*
 *  Copyright 2017, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.parsing;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static com.yahoo.bullet.querying.FilterOperations.FilterType.AND;
import static com.yahoo.bullet.querying.FilterOperations.FilterType.EQUALS;
import static com.yahoo.bullet.querying.FilterOperations.FilterType.NOT;
import static com.yahoo.bullet.querying.FilterOperations.FilterType.OR;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class LogicalClauseTest {
    @Test
    public void testDefaults() {
        LogicalClause logicalClause = new LogicalClause();
        Assert.assertNull(logicalClause.getOperation());
        Assert.assertNull(logicalClause.getClauses());
    }

    @Test
    public void testToString() {
        LogicalClause logicalClause = new LogicalClause();
        Assert.assertEquals(logicalClause.toString(), "{operation: null, clauses: null}");
        logicalClause.setClauses(emptyList());
        Assert.assertEquals(logicalClause.toString(), "{operation: null, clauses: []}");
        logicalClause.setOperation(OR);
        Assert.assertEquals(logicalClause.toString(), "{operation: OR, clauses: []}");

        FilterClause clauseA = new FilterClause();
        clauseA.setField("foo");
        clauseA.setOperation(EQUALS);
        clauseA.setValues(asList("a", "b"));
        LogicalClause clauseB = new LogicalClause();
        clauseB.setOperation(NOT);
        clauseB.setClauses(singletonList(clauseA));
        logicalClause.setClauses(asList(clauseA, clauseB));
        Assert.assertEquals(logicalClause.toString(), "{operation: OR, clauses: [" +
                                                        "{operation: EQUALS, field: foo, values: [a, b]}, " +
                                                        "{operation: NOT, " +
                                                         "clauses: [{operation: EQUALS, field: foo, values: [a, b]}]" +
                                                        "}" +
                                                      "]}");

    }

    @Test
    public void testInitializeWithNoOperation() {
        LogicalClause clause = new LogicalClause();
        Optional<List<Error>> optionalErrors = clause.initialize();
        Assert.assertTrue(optionalErrors.isPresent());
        List<Error> errors = optionalErrors.get();
        Assert.assertEquals(errors.get(0), Clause.OPERATION_MISSING);
    }
    @Test
    public void testInitializeWithOperation() {
        LogicalClause clause = new LogicalClause();
        clause.setOperation(AND);;
        Optional<List<Error>> errors = clause.initialize();
        Assert.assertFalse(errors.isPresent());
    }
}
