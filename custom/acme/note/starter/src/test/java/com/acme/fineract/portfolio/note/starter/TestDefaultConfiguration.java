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
package com.acme.fineract.portfolio.note.starter;

import static org.mockito.Mockito.mock;

import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSourceServiceFactory;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.note.serialization.NoteCommandFromApiJsonDeserializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@EnableConfigurationProperties({ FineractProperties.class })
public class TestDefaultConfiguration {
    // NOTE: unfortunately an abastract base class that contains all these mock functions won't work

    @Bean
    public FromJsonHelper fromJsonHelper() {
        return mock(FromJsonHelper.class);
    }

    @Bean
    public RoutingDataSourceServiceFactory routingDataSourceServiceFactory() {
        return mock(RoutingDataSourceServiceFactory.class);
    }

    @Bean
    public RoutingDataSource routingDataSource() {
        return mock(RoutingDataSource.class);
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return mock(JdbcTemplate.class);
    }

    @Bean
    public NoteRepository noteRepository() {
        return mock(NoteRepository.class);
    }

    @Bean
    public ClientRepositoryWrapper clientRepository() {
        return mock(ClientRepositoryWrapper.class);
    }

    @Bean
    public GroupRepository groupRepository() {
        return mock(GroupRepository.class);
    }

    @Bean
    public LoanRepositoryWrapper loanRepository() {
        return mock(LoanRepositoryWrapper.class);
    }

    @Bean
    public LoanTransactionRepository loanTransactionRepository() {
        return mock(LoanTransactionRepository.class);
    }

    @Bean
    public NoteCommandFromApiJsonDeserializer fromApiJsonDeserializer(FromJsonHelper fromJsonHelper) {
        return new NoteCommandFromApiJsonDeserializer(fromJsonHelper);
    }
}
