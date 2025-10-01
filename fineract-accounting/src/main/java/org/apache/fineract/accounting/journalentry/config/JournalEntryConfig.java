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
package org.apache.fineract.accounting.journalentry.config;

import org.apache.fineract.accounting.journalentry.data.JournalEntryDataValidator;
import org.apache.fineract.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Journal Entry related beans in the system.
 * <p>
 * This class is responsible for defining and configuring all necessary beans related to journal entries, including
 * deserializers and validators.
 * <p>
 * All beans are annotated with {@code @ConditionalOnMissingBean} to allow for easy overriding in test configurations or
 * custom implementations.
 *
 * @see org.apache.fineract.accounting.journalentry.data.JournalEntryDataValidator
 * @see org.apache.fineract.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer
 */
@Configuration
public class JournalEntryConfig {

    /**
     * Creates and configures the {@code JournalEntryCommandFromApiJsonDeserializer} bean.
     *
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @return a fully configured {@code JournalEntryCommandFromApiJsonDeserializer} instance
     * @see org.apache.fineract.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer
     */
    @Bean
    @ConditionalOnMissingBean
    public JournalEntryCommandFromApiJsonDeserializer journalEntryCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        return new JournalEntryCommandFromApiJsonDeserializer(fromApiJsonHelper);
    }

    /**
     * Creates and configures the JournalEntryDataValidator bean.
     *
     * @param fromApiJsonHelper
     *            the JSON helper for API requests
     * @return configured JournalEntryDataValidator instance
     */
    @Bean
    @ConditionalOnMissingBean
    public JournalEntryDataValidator journalEntryDataValidator(final FromJsonHelper fromApiJsonHelper) {
        return new JournalEntryDataValidator(fromApiJsonHelper);
    }
}
