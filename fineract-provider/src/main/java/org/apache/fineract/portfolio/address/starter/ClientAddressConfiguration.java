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
package org.apache.fineract.portfolio.address.starter;

import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.portfolio.address.domain.AddressRepository;
import org.apache.fineract.portfolio.address.service.ClientAddressReadService;
import org.apache.fineract.portfolio.address.service.ClientAddressReadServiceImpl;
import org.apache.fineract.portfolio.address.service.ClientAddressWriteService;
import org.apache.fineract.portfolio.address.service.ClientAddressWriteServiceImpl;
import org.apache.fineract.portfolio.address.service.FieldConfigurationReadService;
import org.apache.fineract.portfolio.address.service.FieldConfigurationReadServiceImpl;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ClientAddressConfiguration {

    @Bean
    @ConditionalOnMissingBean(ClientAddressWriteService.class)
    public ClientAddressWriteService clientAddressWriteService(CodeValueRepository codeValueRepository,
            ClientAddressRepository clientAddressRepository, ClientRepositoryWrapper clientRepositoryWrapper,
            AddressRepository addressRepository, ClientAddressRepositoryWrapper clientAddressRepositoryWrapper) {
        return new ClientAddressWriteServiceImpl(codeValueRepository, clientAddressRepository, clientRepositoryWrapper, addressRepository,
                clientAddressRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(ClientAddressReadService.class)
    public ClientAddressReadService clientAddressReadService(JdbcTemplate jdbcTemplate, CodeValueReadPlatformService readService) {
        return new ClientAddressReadServiceImpl(jdbcTemplate, readService);
    }

    @Bean
    @ConditionalOnMissingBean(FieldConfigurationReadService.class)
    public FieldConfigurationReadService fieldConfigurationReadService(JdbcTemplate jdbcTemplate) {
        return new FieldConfigurationReadServiceImpl(jdbcTemplate);
    }
}
