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
package org.apache.fineract.command.starter;

import java.util.List;
import org.apache.fineract.command.persistence.converter.JsonNodeReadingConverter;
import org.apache.fineract.command.persistence.converter.JsonNodeWritingConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories(basePackages = { "org.apache.fineract.**.domain" })
@ComponentScan("org.apache.fineract.command.persistence")
class CommandPersistenceConfiguration {

    @Bean
    JdbcCustomConversions customConversions(JsonNodeReadingConverter jsonNodeReadingConverter,
            JsonNodeWritingConverter jsonNodeWritingConverter) {
        return new JdbcCustomConversions(List.of(jsonNodeReadingConverter, jsonNodeWritingConverter));
    }
}
