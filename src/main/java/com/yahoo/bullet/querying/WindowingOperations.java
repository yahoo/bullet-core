/*
 *  Copyright 2018, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.querying;

import com.yahoo.bullet.aggregations.Strategy;
import com.yahoo.bullet.common.BulletConfig;
import com.yahoo.bullet.parsing.Aggregation;
import com.yahoo.bullet.parsing.Query;
import com.yahoo.bullet.parsing.Window;
import com.yahoo.bullet.windowing.AdditiveTumbling;
import com.yahoo.bullet.windowing.Basic;
import com.yahoo.bullet.windowing.Reactive;
import com.yahoo.bullet.windowing.Scheme;
import com.yahoo.bullet.windowing.Tumbling;

public class WindowingOperations {
    /**
     * Create a windowing {@link Scheme} for this particular {@link Query}.
     *
     * @param query The configured, initialized query to find a scheme for.
     * @param strategy The aggregation strategy to use for in the windowing scheme.
     * @param config The {@link BulletConfig} to use for configuration.
     * @return A windowing scheme to use for this query.
     */
    public static Scheme findScheme(Query query, Strategy strategy, BulletConfig config) {
        // TODO: Support other windows
        Window window = query.getWindow();

        // No window -> Basic.
        if (window == null) {
            return new Basic(strategy, null, config);
        }

        Window.Classification classification = window.getType();
        Aggregation.Type type = query.getAggregation().getType();

        // If RAW and we have a window that's anything but TIME_TIME, force to Reactive and use a new record by record
        // window instead of the window in the query.
        if (type == Aggregation.Type.RAW && classification != Window.Classification.TIME_TIME) {
            return new Reactive(strategy, Window.oneRecordWindow(config), config);
        }

        if (classification == Window.Classification.TIME_ALL) {
            return new AdditiveTumbling(strategy, window, config);
        }
        // Raw can be Tumbling and all other aggregations default to Tumbling
        return new Tumbling(strategy, window, config);
    }
}
