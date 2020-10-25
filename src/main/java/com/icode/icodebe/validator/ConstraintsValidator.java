package com.icode.icodebe.validator;

import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collector;

@Service
public class ConstraintsValidator {

    private final Validator validator;

    public ConstraintsValidator(Validator validator) {
        this.validator = validator;
    }

    public boolean validate(Object toValidate) {
        final var constraintsViolations = validator.validate(toValidate, Default.class);

        if (!constraintsViolations.isEmpty()) {
            final Supplier<StringJoiner> stringJoinerSupplier = () -> new StringJoiner(" ");
            final BiConsumer<StringJoiner, ConstraintViolation<?>> biConsumer = (joiner, constraintViolation) ->
                    joiner.add(constraintViolation.getPropertyPath().toString())
                            .add(":")
                            .add(constraintViolation.getMessage());

            final var exceptionMessage = constraintsViolations
                    .stream()
                    .collect(Collector.of(stringJoinerSupplier,
                            biConsumer,
                            StringJoiner::merge,
                            StringJoiner::toString));

            throw new ValidationException(exceptionMessage);
        }

        return true;
    }
}
