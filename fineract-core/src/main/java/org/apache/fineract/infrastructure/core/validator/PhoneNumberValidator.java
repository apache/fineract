package org.apache.fineract.infrastructure.core.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.PhoneNumberValidationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private final PhoneNumberValidationService phoneNumberValidationService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return phoneNumberValidationService.isValid(value);
    }
}
