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
package org.apache.fineract.portfolio.creditscorecard.provider;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.portfolio.creditscorecard.annotation.ScorecardService;
import org.apache.fineract.portfolio.creditscorecard.service.CreditScorecardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class ScorecardServiceProvider {

    public static final String SERVICE_MISSING = "There is no ScorecardService registered in the ScorecardServiceProvider for this report name: ";

    private static final Logger LOGGER = LoggerFactory.getLogger(ScorecardServiceProvider.class);

    private final Map<String, CreditScorecardService> scorecardServices;

    @SuppressWarnings("unchecked")
    @Autowired
    public ScorecardServiceProvider(List<CreditScorecardService> scorecardServices) {

        var mapBuilder = ImmutableMap.<String, CreditScorecardService>builder();
        for (CreditScorecardService s : scorecardServices) {
            Class<? extends CreditScorecardService> serviceClass = s.getClass();

            if (!serviceClass.isAnnotationPresent(ScorecardService.class)) {
                serviceClass = (Class<? extends CreditScorecardService>) serviceClass.getGenericSuperclass();
            }

            final String name = serviceClass.getAnnotation(ScorecardService.class).name();
            mapBuilder.put(name, s);

            LOGGER.warn("Registered credit scorecard service '{}' for name '{}'", s, name);

        }
        this.scorecardServices = mapBuilder.build();
    }

    public CreditScorecardService getScorecardService(final String serviceName) {
        return scorecardServices.getOrDefault(serviceName, null);
    }
}
