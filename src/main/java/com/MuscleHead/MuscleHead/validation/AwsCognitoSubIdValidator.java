package com.MuscleHead.MuscleHead.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;
import java.util.regex.Pattern;

public class AwsCognitoSubIdValidator implements ConstraintValidator<AwsCognitoSubId, String> {

    // AWS Cognito sub IDs are UUIDs in the format:
    // xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public void initialize(AwsCognitoSubId constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String subId, ConstraintValidatorContext context) {
        if (subId == null || subId.isBlank()) {
            return false; // Use @NotBlank for null/blank validation
        }

        // Check if it matches UUID pattern
        if (!UUID_PATTERN.matcher(subId.trim()).matches()) {
            return false;
        }

        // Additional validation: try to parse as UUID to ensure it's valid
        try {
            UUID.fromString(subId.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
