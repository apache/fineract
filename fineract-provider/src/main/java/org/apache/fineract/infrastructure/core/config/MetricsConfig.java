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

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationPredicate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.jersey.autoconfigure.JerseyApplicationPath;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.util.function.SingletonSupplier;

@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    // Bean registered by JerseyServerMetricsAutoConfiguration
    private static final String JERSEY_OBSERVATION_CUSTOMIZER = "jerseyServerObservationResourceConfigCustomizer";

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Skips the servlet observation for requests to the Jersey application, which records http.server.requests with the
     * resource template itself; otherwise each API request is counted twice, once with uri="UNKNOWN". Applies only
     * while Jersey's own observation is registered, so excluding it brings the servlet observation back.
     */
    @Bean
    public ObservationPredicate jerseyServletObservationPredicate(ListableBeanFactory beanFactory,
            ObjectProvider<JerseyApplicationPath> jerseyApplicationPath) {
        // Resolved on the first request: an eager JerseyApplicationPath closes a cycle through the ObservationRegistry
        Supplier<Optional<String>> jerseyPrefix = SingletonSupplier.of(() -> beanFactory.containsBean(JERSEY_OBSERVATION_CUSTOMIZER)
                ? Optional.of(jerseyApplicationPath.getObject().getPrefix() + "/")
                : Optional.empty());
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext serverContext) {
                HttpServletRequest request = serverContext.getCarrier();
                return jerseyPrefix.get().map(prefix -> !request.getRequestURI().startsWith(request.getContextPath() + prefix))
                        .orElse(true);
            }
            return true;
        };
    }
}
