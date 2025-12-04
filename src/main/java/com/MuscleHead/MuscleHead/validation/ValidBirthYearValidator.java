package com.MuscleHead.MuscleHead.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Year;

public class ValidBirthYearValidator implements ConstraintValidator<ValidBirthYear, Integer> {

    private static final int MINIMUM_AGE = 16;

    @Override
    public void initialize(ValidBirthYear constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Integer birthYear, ConstraintValidatorContext context) {
        if (birthYear == null) {
            return true; // Let @NotNull handle null validation
        }

        int currentYear = Year.now().getValue();
        int maxAllowedBirthYear = currentYear - MINIMUM_AGE;

        return birthYear <= maxAllowedBirthYear;
    }
}
