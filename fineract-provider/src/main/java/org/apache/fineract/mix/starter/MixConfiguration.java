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
package org.apache.fineract.mix.starter;

import org.apache.fineract.mix.domain.MixTaxonomyMappingRepository;
import org.apache.fineract.mix.service.MixTaxonomyMappingReadPlatformService;
import org.apache.fineract.mix.service.MixTaxonomyMappingReadPlatformServiceImpl;
import org.apache.fineract.mix.service.MixTaxonomyMappingWritePlatformService;
import org.apache.fineract.mix.service.MixTaxonomyMappingWritePlatformServiceImpl;
import org.apache.fineract.mix.service.MixTaxonomyReadPlatformService;
import org.apache.fineract.mix.service.MixTaxonomyReadPlatformServiceImpl;
import org.apache.fineract.mix.service.NamespaceReadPlatformService;
import org.apache.fineract.mix.service.NamespaceReadPlatformServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MixConfiguration {

    @Bean
    @ConditionalOnMissingBean(MixTaxonomyMappingReadPlatformService.class)
    public MixTaxonomyMappingReadPlatformService mixTaxonomyMappingReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new MixTaxonomyMappingReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(MixTaxonomyMappingWritePlatformService.class)
    public MixTaxonomyMappingWritePlatformService mixTaxonomyMappingWritePlatformService(MixTaxonomyMappingRepository mappingRepository) {
        return new MixTaxonomyMappingWritePlatformServiceImpl(mappingRepository);
    }

    @Bean
    @ConditionalOnMissingBean(MixTaxonomyReadPlatformService.class)
    public MixTaxonomyReadPlatformService mixTaxonomyReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new MixTaxonomyReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(NamespaceReadPlatformService.class)
    public NamespaceReadPlatformService namespaceReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new NamespaceReadPlatformServiceImpl(jdbcTemplate);
    }

}
