/*
 *  Copyright 2021, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.query.tablefunctions;

import com.yahoo.bullet.querying.tablefunctors.TableFunctor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter @AllArgsConstructor
public abstract class TableFunction implements Serializable {
    private static final long serialVersionUID = 4126801547249854808L;

    // If true, the function returns null if the input is empty or null. If false, the function returns nothing.
    protected final boolean outer;
    protected final TableFunctionType type;

    /**
     *
     * @return
     */
    public abstract TableFunctor getTableFunctor();

    @Override
    public String toString() {
        return "{outer: " + outer + ", type: " + type + "}";
    }
}
