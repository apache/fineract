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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.security.exception.SqlValidationException;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultSqlValidator implements SqlValidator {

    private final FineractProperties properties;

    private Map<String, Pattern> patterns = new LinkedHashMap<>();
    private Map<String, FineractProperties.FineractSqlValidationProfileProperties> profiles = new LinkedHashMap<>();

    private static final String MAIN_PROFILE = "main";

    @PostConstruct
    public void init() {
        properties.getSqlValidation().getPatterns().forEach(pattern -> {
            log.info("Setup SQL validation pattern: {}", pattern.getName());

            patterns.put(pattern.getName(), Pattern.compile(pattern.getPattern(), Pattern.DOTALL));
        });
        properties.getSqlValidation().getProfiles().forEach(profile -> {
            log.info("Setup SQL validation profile: {}", profile.getName());

            profile.getPatternRefs()
                    .sort(Comparator.comparing(FineractProperties.FineractSqlValidationPatternReferenceProperties::getOrder));

            profiles.put(profile.getName(), profile);
        });

        // consistency checks

        if (!profiles.containsKey(MAIN_PROFILE)) {
            throw new IllegalStateException(
                    "SQL validation profile 'main' missing. This validation profile is the default fallback and has to be provided. NOTE: YOU CANNOT DISABLE SQL VALIDATION!!!");
        }

        // the default profile needs at least one pattern reference
        if (profiles.get(MAIN_PROFILE).getPatternRefs().isEmpty()) {
            throw new IllegalStateException(
                    "SQL Validation pattern references in profile 'main' are empty. Please make sure there is at least one reference available. NOTE: YOU CANNOT DISABLE SQL VALIDATION!!!");
        }

        // the default profile needs to be enabled
        profiles.get(MAIN_PROFILE).setEnabled(true);
    }

    @Override
    public void validate(final String statement) throws SqlValidationException {
        validate(MAIN_PROFILE, statement);
    }

    @Override
    public void validate(final String profile, final String statement) throws SqlValidationException {
        if (StringUtils.isBlank(statement)) {
            return;
        }

        for (var ref : profiles.getOrDefault(profile, profiles.get(MAIN_PROFILE)).getPatternRefs()) {
            Matcher matcher = patterns.get(ref.getName()).matcher(statement);

            if (matcher.matches()) {
                log.warn("SQL validation error: >> {} <<", statement);
                throw new SqlValidationException(String.format("invalid SQL statement (detected '%s' pattern)", ref.getName()));
            }
        }
    }
}
