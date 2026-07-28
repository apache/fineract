package org.apache.fineract.infrastructure.core.service;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhoneNumberValidationService {

    private final FineractProperties fineractProperties;

    public boolean isValid(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return true;
        }
        String regex = fineractProperties.getPhoneValidation().getRegex();
        return Pattern.compile(regex).matcher(phoneNumber).matches();
    }

    public String getRegex() {
        return fineractProperties.getPhoneValidation().getRegex();
    }
}
