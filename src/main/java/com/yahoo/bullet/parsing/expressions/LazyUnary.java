package com.yahoo.bullet.parsing.expressions;

import com.google.gson.annotations.Expose;
import com.yahoo.bullet.common.BulletError;
import com.yahoo.bullet.typesystem.Type;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.yahoo.bullet.common.BulletError.makeError;

@Getter
public class LazyUnary extends LazyExpression {
    public static final BulletError LAZY_UNARY_REQUIRES_NON_NULL_OPERAND = makeError("The operand must not be null.", "Please provide an expression for operand.");
    public static final BulletError LAZY_UNARY_REQUIRES_UNARY_OPERATION = makeError("The operation must be unary.", "Please provide a unary operation for op.");
    public static final BulletError LAZY_UNARY_REQUIRES_PRIMITIVE_TYPE = makeError("The type must be primitive (if specified).", "Please provide a primitive type or no type at all.");

    @Expose
    private LazyExpression operand;
    @Expose
    private Operation op;

    public LazyUnary() {
        operand = null;
        op = null;
        type = null;
    }

    @Override
    public Optional<List<BulletError>> initialize() {
        if (operand == null) {
            return Optional.of(Collections.singletonList(LAZY_UNARY_REQUIRES_NON_NULL_OPERAND));
        }
        if (!Operation.UNARY_OPERATIONS.contains(op)) {
            return Optional.of(Collections.singletonList(LAZY_UNARY_REQUIRES_UNARY_OPERATION));
        }
        if (type != null && !Type.PRIMITIVES.contains(type)) {
            return Optional.of(Collections.singletonList(LAZY_UNARY_REQUIRES_PRIMITIVE_TYPE));
        }
        return operand.initialize();
    }

    @Override
    public String getName() {
        return op + " (" + operand.getName() + ")";
    }

    @Override
    public String toString() {
        return "{operand: " + operand + ", op: " + op + ", " + super.toString() + "}";
    }
}
