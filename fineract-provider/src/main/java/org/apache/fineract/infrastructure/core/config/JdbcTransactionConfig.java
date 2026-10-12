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

package org.apache.fineract.infrastructure.core.config;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import java.util.List;
import org.apache.fineract.infrastructure.core.persistence.ExtendedDataSourceTransactionManager;
import org.apache.fineract.infrastructure.core.persistence.TransactionLifecycleCallback;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class JdbcTransactionConfig {

    @Bean
    public PlatformTransactionManager jdbcTransactionManager(FineractProperties fineractProperties, RoutingDataSource dataSource,
            ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers, List<TransactionLifecycleCallback> callbacks) {
        boolean readOnly = fineractProperties.getMode().isReadOnlyMode();
        ExtendedDataSourceTransactionManager transactionManager = new ExtendedDataSourceTransactionManager(readOnly);
        transactionManager.setDataSource(dataSource);
        transactionManager.setLifecycleCallbacks(callbacks);
        transactionManager.setValidateExistingTransaction(true);
        transactionManagerCustomizers.ifAvailable(customizers -> customizers.customize(transactionManager));
        return transactionManager;
    }

    @Bean("requiresNewTransactionJdbcTemplate")
    public TransactionTemplate requiresNewTransactionJdbcTemplate(
            @Qualifier("jdbcTransactionManager") PlatformTransactionManager jdbcTransactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(jdbcTransactionManager);
        transactionTemplate.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }
}
