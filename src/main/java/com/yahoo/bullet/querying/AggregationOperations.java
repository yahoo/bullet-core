/*
 *  Copyright 2017, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.querying;

import com.google.gson.annotations.SerializedName;
import com.yahoo.bullet.aggregations.CountDistinct;
import com.yahoo.bullet.aggregations.Distribution;
import com.yahoo.bullet.aggregations.GroupAll;
import com.yahoo.bullet.aggregations.GroupBy;
import com.yahoo.bullet.aggregations.Raw;
import com.yahoo.bullet.aggregations.Strategy;
import com.yahoo.bullet.aggregations.TopK;
import com.yahoo.bullet.common.BulletConfig;
import com.yahoo.bullet.common.Utilities;
import com.yahoo.bullet.parsing.Aggregation;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

public class AggregationOperations {
    public enum AggregationType {
        // The alternate value of DISTINCT for GROUP is allowed since having no GROUP operations is implicitly
        // a DISTINCT
        @SerializedName(value = "GROUP", alternate = { "DISTINCT" })
        GROUP,
        @SerializedName("COUNT DISTINCT")
        COUNT_DISTINCT,
        @SerializedName("TOP K")
        TOP_K,
        @SerializedName("DISTRIBUTION")
        DISTRIBUTION,
        // The alternate value of LIMIT for RAW is allowed to preserve backward compatibility.
        @SerializedName(value = "RAW", alternate = { "LIMIT" })
        RAW
    }

    @Getter
    public enum GroupOperationType {
        COUNT("COUNT"),
        SUM("SUM"),
        MIN("MIN"),
        MAX("MAX"),
        AVG("AVG"),
        // COUNT_FIELD operation is only used internally in conjunction with AVG and won't be returned.
        COUNT_FIELD("COUNT_FIELD");

        private String name;

        GroupOperationType(String name) {
            this.name = name;
        }

        /**
         * Checks to see if this String represents this enum.
         *
         * @param name The String version of the enum.
         * @return true if the name represents this enum.
         */
        public boolean isMe(String name) {
            return this.name.equals(name);
        }
    }

    @Getter
    public enum DistributionType {
        QUANTILE("QUANTILE"),
        PMF("PMF"),
        CDF("CDF");

        private String name;

        DistributionType(String name) {
            this.name = name;
        }

        /**
         * Checks to see if this String represents this enum.
         *
         * @param name The String version of the enum.
         * @return true if the name represents this enum.
         */
        public boolean isMe(String name) {
            return this.name.equals(name);
        }
    }

    public interface AggregationOperator extends BiFunction<Number, Number, Number> {
    }

    // If either argument is null, a NullPointerException will be thrown.
    public static final AggregationOperator MIN = (x, y) -> x.doubleValue() <  y.doubleValue() ? x : y;
    public static final AggregationOperator MAX = (x, y) -> x.doubleValue() >  y.doubleValue() ? x : y;
    public static final AggregationOperator SUM = (x, y) -> x.doubleValue() + y.doubleValue();
    public static final AggregationOperator COUNT = (x, y) -> x.longValue() + y.longValue();

    public static final Map<GroupOperationType, AggregationOperator> OPERATORS = new EnumMap<>(GroupOperationType.class);
    static {
        OPERATORS.put(GroupOperationType.COUNT, COUNT);
        OPERATORS.put(GroupOperationType.COUNT_FIELD, COUNT);
        OPERATORS.put(GroupOperationType.SUM, SUM);
        OPERATORS.put(GroupOperationType.MIN, MIN);
        OPERATORS.put(GroupOperationType.MAX, MAX);
        OPERATORS.put(GroupOperationType.AVG, SUM);
    }

    /**
     * Returns a new {@link Strategy} instance that can handle this aggregation.
     *
     * @param aggregation The non-null, initialized {@link Aggregation} instance whose strategy is required.
     * @param config The {@link BulletConfig} containing configuration for the strategy.
     *
     * @return The created instance of a strategy that can implement the Aggregation.
     */
    public static Strategy findStrategy(Aggregation aggregation, BulletConfig config) {
        switch (aggregation.getType()) {
            case COUNT_DISTINCT:
                return new CountDistinct(aggregation, config);
            case DISTRIBUTION:
                return new Distribution(aggregation, config);
            case RAW:
                return new Raw(aggregation, config);
            case TOP_K:
                return new TopK(aggregation, config);
        }

        // If we have any fields -> GroupBy
        return Utilities.isEmpty(aggregation.getFields()) ? new GroupAll(aggregation, config) : new GroupBy(aggregation, config);
    }
}
