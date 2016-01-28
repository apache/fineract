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
package org.apache.fineract.portfolio.interestratechart.incentive;

import java.math.BigDecimal;

import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.interestratechart.domain.InterestIncentivesFields;

public class IncentiveDTO {

    private final Client client;
    private final BigDecimal interest;
    private final InterestIncentivesFields incentives;

    public IncentiveDTO(final Client client, final BigDecimal interest, final InterestIncentivesFields incentives) {
        this.client = client;
        this.interest = interest;
        this.incentives = incentives;
    }

    public Client client() {
        return this.client;
    }

    public BigDecimal interest() {
        return this.interest;
    }

    public InterestIncentivesFields incentives() {
        return this.incentives;
    }
}
