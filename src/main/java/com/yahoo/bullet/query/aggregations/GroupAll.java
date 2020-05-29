package com.yahoo.bullet.query.aggregations;

import com.yahoo.bullet.common.BulletConfig;
import com.yahoo.bullet.common.BulletException;
import com.yahoo.bullet.common.Utilities;
import com.yahoo.bullet.querying.aggregations.GroupStrategy;
import com.yahoo.bullet.querying.aggregations.Strategy;
import com.yahoo.bullet.querying.aggregations.grouping.GroupOperation;
import lombok.Getter;

import java.util.Set;

@Getter
public class GroupAll extends Aggregation {
    private static final long serialVersionUID = 5118426551573371428L;
    private static final BulletException COUNT_FIELD_INVALID_OPERATION =
            new BulletException("COUNT_FIELD is not a valid operation.", "Please remove this operation.");

    private final Set<GroupOperation> operations;

    /**
     * Constructor that creates a GROUP aggregation with a set of group operations.
     *
     * @param operations The non-null set of group operations.
     */
    public GroupAll(Set<GroupOperation> operations) {
        super(null, AggregationType.GROUP);
        Utilities.requireNonNull(operations);
        if (operations.stream().anyMatch(operation -> operation.getType() == GroupOperation.GroupOperationType.COUNT_FIELD)) {
            throw COUNT_FIELD_INVALID_OPERATION;
        }
        this.operations = operations;
    }

    @Override
    public Strategy getStrategy(BulletConfig config) {
        return new GroupStrategy(this, config);
    }

    @Override
    public String toString() {
        return "{size: " + size + ", type: " + type + ", operations: " + operations + "}";
    }
}