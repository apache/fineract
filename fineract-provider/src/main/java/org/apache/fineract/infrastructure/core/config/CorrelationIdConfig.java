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

import java.util.Arrays;
import org.apache.fineract.infrastructure.core.filters.CorrelationHeaderFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty("fineract.logging.http.correlation-id.enabled")
public class CorrelationIdConfig implements EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public FilterRegistrationBean<CorrelationHeaderFilter> correlationHeaderFilter() {
        FilterRegistrationBean<CorrelationHeaderFilter> filterRegBean = new FilterRegistrationBean<CorrelationHeaderFilter>();
        filterRegBean.setFilter(new CorrelationHeaderFilter(environment));
        filterRegBean.setUrlPatterns(Arrays.asList("/*"));
        return filterRegBean;
    }

}
