/*
 *  Copyright 2017, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.common;

import com.yahoo.bullet.record.BulletRecord;
import com.yahoo.bullet.result.RecordBox;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonMap;

public class UtilitiesTest {
    @Test
    public void testCasting() {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", 1L);
        Long actual = Utilities.getCasted(map, "foo", Long.class);
        Assert.assertEquals(actual, (Long) 1L);
    }

    @Test
    public void testFailCasting() {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        Long actual = Utilities.getCasted(map, "foo", Long.class);
        Assert.assertNull(actual);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testCastingOnGenerics() {
        Map<String, Object> map = new HashMap<>();
        Map<Integer, Long> anotherMap = new HashMap<>();
        anotherMap.put(1, 2L);

        map.put("foo", anotherMap);

        Map<String, String> incorrect = Utilities.getCasted(map, "foo", Map.class);
        // It is a map but the generics are incorrect
        Assert.assertNotNull(incorrect);
        String value = incorrect.get(1);
    }

    @Test
    public void testEmptyMap() {
        Assert.assertTrue(Utilities.isEmpty((Map) null));
        Assert.assertTrue(Utilities.isEmpty(Collections.emptyMap()));
        Assert.assertFalse(Utilities.isEmpty(singletonMap("foo", "bar")));
    }

    @Test
    public void testEmptyCollection() {
        Assert.assertTrue(Utilities.isEmpty((List) null));
        Assert.assertTrue(Utilities.isEmpty(Collections.emptyList()));
        Assert.assertFalse(Utilities.isEmpty(Collections.singletonList("foo")));
    }

    @Test
    public void testEmptyString() {
        Assert.assertTrue(Utilities.isEmpty((String) null));
        Assert.assertTrue(Utilities.isEmpty(""));
        Assert.assertFalse(Utilities.isEmpty("foo"));
    }

    @Test
    public void testRequireNonNull() {
        Utilities.requireNonNull(Collections.emptyList());
        Utilities.requireNonNull(Collections.emptySet());
        Utilities.requireNonNull(Collections.emptyMap());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRequireNonNullListNull() {
        Utilities.requireNonNull((List<?>) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRequireNonNullListContainsNull() {
        Utilities.requireNonNull(Collections.singletonList(null));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRequireNonNullSetNull() {
        Utilities.requireNonNull((Set<?>) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRequireNonNullSetContainsNull() {
        Utilities.requireNonNull(Collections.singleton(null));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRequireNonNullMapNull() {
        Utilities.requireNonNull((Map<?, ?>) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRequireNonNullMapContainsNullKey() {
        Utilities.requireNonNull(Collections.singletonMap(null, "foo"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testRequireNonNullMapContainsNullValue() {
        Utilities.requireNonNull(Collections.singletonMap("foo", null));
    }

    @Test
    public void testRounding() {
        Assert.assertEquals(String.valueOf(Utilities.round(1.2000000000001, 5)), "1.2");
        Assert.assertEquals(String.valueOf(Utilities.round(0.7999999999999, 8)), "0.8");
        Assert.assertEquals(String.valueOf(Utilities.round(1.0000000000001, 6)), "1.0");
        // This might be a valid double representation for 1.45 and then rounding to 1 places gives 1.4 instead of 1.5!
        Assert.assertEquals(String.valueOf(Utilities.round(1.4499999999999, 1)), "1.4");
    }

    @Test
    public void testGeneratePoints() {
        Assert.assertEquals(Utilities.generatePoints(0.0, num -> num + 1.0, 5, 1), new double[] { 0.0, 1.0, 2.0, 3.0, 4.0 });
        Assert.assertEquals(Utilities.generatePoints(1.0, num -> num * 2.0, 5, 1), new double[] { 1.0, 2.0, 4.0, 8.0, 16.0 });
        Assert.assertEquals(Utilities.generatePoints(0.5, num -> num * 3.0, 5, 1), new double[] { 0.5, 1.5, 4.5, 13.5, 40.5 });
        Assert.assertEquals(Utilities.generatePoints(0.5, num -> num * 3.0, 5, 0), new double[] { 1.0, 2.0, 5.0, 14.0, 41.0 });
    }

    @Test
    public void testNumericExtraction() {
        BulletRecord record = RecordBox.get().add("foo", "1.20").add("bar", 42L)
                                             .addMap("map_field", Pair.of("foo", 21.0))
                                             .getRecord();

        Assert.assertNull(Utilities.extractFieldAsNumber(null, record));
        Assert.assertNull(Utilities.extractFieldAsNumber("", record));
        Assert.assertNull(Utilities.extractFieldAsNumber("id", record));
        Assert.assertEquals(Utilities.extractFieldAsNumber("foo", record), ((Number) 1.20).doubleValue());
        Assert.assertEquals(Utilities.extractFieldAsNumber("bar", record), ((Number) 42).longValue());
        Assert.assertNull(Utilities.extractFieldAsNumber("map_field.foo", record));
    }
}
