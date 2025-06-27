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
package org.apache.fineract.validation.config;

import jakarta.validation.MessageInterpolator;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;
import yakworks.i18n.icu.ICUBundleMessageSource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ValidationConfig {

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ICUBundleMessageSource();
        messageSource.setBasenames("classpath:fineract/validation/messages");
        messageSource.setCacheSeconds(3600);
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }

    @Bean
    public MessageInterpolator messageInterpolator() {
        var resourceBundleLocator = new MessageSourceResourceBundleLocator(messageSource());
        var messageInterpolator = new ResourceBundleMessageInterpolator(resourceBundleLocator);
        return new RecursiveLocaleContextMessageInterpolator(messageInterpolator);
    }

    @Bean
    @Primary
    public Validator validator() {
        var localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setMessageInterpolator(messageInterpolator());

        return localValidatorFactoryBean;
    }

    private static final class RecursiveLocaleContextMessageInterpolator extends AbstractMessageInterpolator {

        private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("\\{([^}]+)\\}");

        private final MessageInterpolator interpolator;

        private RecursiveLocaleContextMessageInterpolator(MessageInterpolator interpolator) {
            this.interpolator = interpolator;
        }

        @Override
        public String interpolate(MessageInterpolator.Context context, Locale locale, String message) {
            int level = 0;
            while (containsPlaceholder(message) && (level++ < 2)) {
                message = this.interpolator.interpolate(message, context, locale);
            }
            return message;
        }

        private boolean containsPlaceholder(String code) {
            return PATTERN_PLACEHOLDER.matcher(code).find();
        }
    }
}
