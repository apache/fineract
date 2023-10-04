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
package org.apache.fineract.template.starter;

import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.template.domain.TemplateRepository;
import org.apache.fineract.template.service.JpaTemplateDomainService;
import org.apache.fineract.template.service.TemplateDomainService;
import org.apache.fineract.template.service.TemplateMergeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateConfiguration {

    @Bean
    @ConditionalOnMissingBean(TemplateDomainService.class)
    public TemplateDomainService templateDomainService(TemplateRepository templateRepository) {
        return new JpaTemplateDomainService(templateRepository);
    }

    @Bean
    @ConditionalOnMissingBean(TemplateMergeService.class)
    public TemplateMergeService templateMergeService(FineractProperties fineractProperties) {
        return new TemplateMergeService(fineractProperties);
    }

}
