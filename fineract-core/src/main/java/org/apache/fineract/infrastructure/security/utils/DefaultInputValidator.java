/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.security.utils;

import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.security.exception.InputValidationException;
import org.apache.fineract.infrastructure.security.service.InputValidator;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultInputValidator implements InputValidator {

    private final FineractProperties properties;

    private Map<String, Pattern> patterns = new LinkedHashMap<>();
    private Map<String, FineractProperties.FineractInputValidationProfileProperties> profiles = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        properties.getInputValidation().getPatterns().forEach(pattern -> {
            log.info("Setup input validation pattern: {}", pattern.getName());
            patterns.put(pattern.getName(), Pattern.compile(pattern.getPattern(), Pattern.DOTALL));
        });

        properties.getInputValidation().getProfiles().forEach(profile -> {
            log.info("Setup input validation profile: {}", profile.getName());
            profile.getPatternRefs()
                    .sort(Comparator.comparing(FineractProperties.FineractInputValidationPatternReferenceProperties::getOrder));
            profiles.put(profile.getName(), profile);
        });
    }

    @Override
    public void validate(final String profile, final String input) throws InputValidationException {
        if (StringUtils.isBlank(input)) {
            return;
        }

        var resolvedProfile = profiles.get(profile);
        if (resolvedProfile == null) {
            log.warn("Input validation profile '{}' not found — rejecting input as unsafe", profile);
            throw new InputValidationException(String.format("no input validation profile registered for '%s'", profile));
        }

        // whitelist: input must match at least one pattern in the profile
        for (var ref : resolvedProfile.getPatternRefs()) {
            if (patterns.get(ref.getName()).matcher(input).matches()) {
                return; // matched — input is valid
            }
        }

        log.warn("Input validation error for profile '{}': >> {} <<", profile, input);
        throw new InputValidationException(String.format("invalid input for profile '%s': does not match any permitted pattern", profile));
    }
}
