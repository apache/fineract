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

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jersey.autoconfigure.JerseyAutoConfiguration;
import org.springframework.boot.jersey.autoconfigure.metrics.JerseyServerMetricsAutoConfiguration;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class MetricsConfigTest {

    private final WebApplicationContextRunner contextRunner = runner(ObservationAutoConfiguration.class, JerseyAutoConfiguration.class,
            JerseyServerMetricsAutoConfiguration.class);

    @Test
    void skipsServletObservationForJerseyRequests() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(predicate(context).test("http.server.requests", servletContext("/fineract-provider/api/v1/offices/1"))).isFalse();
        });
    }

    @Test
    void keepsServletObservationForOtherRequests() {
        contextRunner.run(context -> {
            ObservationPredicate predicate = predicate(context);
            assertThat(predicate.test("http.server.requests", servletContext("/fineract-provider/actuator/health"))).isTrue();
            assertThat(predicate.test("http.server.requests", servletContext("/fineract-provider/apidocs"))).isTrue();
            assertThat(predicate.test("http.server.requests", new Observation.Context())).isTrue();
        });
    }

    @Test
    void keepsServletObservationWhenJerseyMetricsAreExcluded() {
        runner(ObservationAutoConfiguration.class, JerseyAutoConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(predicate(context).test("http.server.requests", servletContext("/fineract-provider/api/v1/offices/1"))).isTrue();
        });
    }

    private static WebApplicationContextRunner runner(Class<?>... autoConfigurations) {
        return new WebApplicationContextRunner().withConfiguration(AutoConfigurations.of(autoConfigurations))
                .withUserConfiguration(TestConfig.class, MetricsConfig.class).withPropertyValues("spring.jersey.application-path=/api");
    }

    private static ObservationPredicate predicate(ApplicationContext context) {
        return context.getBean("jerseyServletObservationPredicate", ObservationPredicate.class);
    }

    private static ServerRequestObservationContext servletContext(String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
        request.setContextPath("/fineract-provider");
        return new ServerRequestObservationContext(request, new MockHttpServletResponse());
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        ResourceConfig resourceConfig() {
            return new ResourceConfig();
        }

        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}
