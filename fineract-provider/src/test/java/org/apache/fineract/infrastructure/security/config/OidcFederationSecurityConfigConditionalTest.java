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
package org.apache.fineract.infrastructure.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.filters.IdempotencyStoreHelper;
import org.apache.fineract.infrastructure.core.service.MDCWrapper;
import org.apache.fineract.infrastructure.jobs.filter.ProgressiveLoanModelCheckerFilter;
import org.apache.fineract.infrastructure.security.service.AuthTenantDetailsService;
import org.apache.fineract.infrastructure.security.service.FineractOidcUserService;
import org.apache.fineract.infrastructure.security.service.TenantOidcConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.LazyInitializationBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.annotation.Order;

class OidcFederationSecurityConfigConditionalTest {

    @Test
    void conditionalAnnotationTargetsOidcEnabledProperty() {
        ConditionalOnProperty annotation = OidcFederationSecurityConfig.class.getAnnotation(ConditionalOnProperty.class);

        assertThat(annotation).isNotNull();
        var declared = Stream.concat(Arrays.stream(annotation.value()), Arrays.stream(annotation.name())).toList();
        assertThat(declared).contains("fineract.security.oidc-federation.enabled");
    }

    @Test
    void orderAnnotationIs100() {
        Order annotation = OidcFederationSecurityConfig.class.getAnnotation(Order.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(100);
    }

    @Test
    void configNotLoadedWhenOidcFederationPropertyAbsent() {
        new ApplicationContextRunner().withUserConfiguration(OidcFederationSecurityConfig.class)
                .run(ctx -> assertThat(ctx).doesNotHaveBean(OidcFederationSecurityConfig.class));
    }

    // The application refuses circular references. Lazy initialization keeps this to the two beans that formed one: the
    // filter chain itself needs a servlet environment.
    @Test
    void configurationAndIssuerResolverAreCreatedWithoutCircularReference() {
        new ApplicationContextRunner().withAllowCircularReferences(false)
                .withPropertyValues("fineract.security.oidc-federation.enabled=true")
                .withBean(LazyInitializationBeanFactoryPostProcessor.class)
                .withUserConfiguration(OidcFederationSecurityConfig.class, DynamicJwtIssuerAuthenticationManagerResolver.class)
                .withBean(FineractOidcUserService.class, () -> mock(FineractOidcUserService.class))
                .withBean(AuthTenantDetailsService.class, () -> mock(AuthTenantDetailsService.class))
                .withBean(TenantOidcConfigService.class, () -> mock(TenantOidcConfigService.class))
                .withBean(BusinessDateReadPlatformService.class, () -> mock(BusinessDateReadPlatformService.class))
                .withBean(MDCWrapper.class, () -> mock(MDCWrapper.class))
                .withBean(FineractRequestContextHolder.class, () -> mock(FineractRequestContextHolder.class))
                .withBean(IdempotencyStoreHelper.class, () -> mock(IdempotencyStoreHelper.class))
                .withBean(ProgressiveLoanModelCheckerFilter.class, () -> mock(ProgressiveLoanModelCheckerFilter.class)).run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx.getBean(OidcFederationSecurityConfig.class)).isNotNull();
                    assertThat(ctx.getBean(DynamicJwtIssuerAuthenticationManagerResolver.class)).isNotNull();
                });
    }
}
