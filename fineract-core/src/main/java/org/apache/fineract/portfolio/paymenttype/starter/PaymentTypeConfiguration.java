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
package org.apache.fineract.portfolio.paymenttype.starter;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeDataValidator;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.mapper.PaymentTypeMapper;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformServiceImpl;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeWriteService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeWriteServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentTypeConfiguration {

    @Bean
    @ConditionalOnMissingBean(PaymentTypeReadPlatformService.class)
    PaymentTypeReadPlatformService paymentTypeReadPlatformService(PlatformSecurityContext context, PaymentTypeMapper paymentTypeMapper,
            PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper) {
        return new PaymentTypeReadPlatformServiceImpl(context, paymentTypeMapper, paymentTypeRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(PaymentTypeWriteService.class)
    PaymentTypeWriteService paymentTypeWriteService(PaymentTypeRepository repository, PaymentTypeRepositoryWrapper repositoryWrapper,
            PaymentTypeDataValidator fromApiJsonDeserializer) {
        return new PaymentTypeWriteServiceImpl(repository, repositoryWrapper, fromApiJsonDeserializer);
    }
}
