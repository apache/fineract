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
package org.apache.fineract.test.factory;

import org.apache.fineract.client.models.ChargeCreateRequest;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargeProductAppliesTo;
import org.apache.fineract.test.data.ChargeTimeType;
import org.apache.fineract.test.helper.Utils;
import org.springframework.stereotype.Component;

@Component
public class WorkingCapitalChargeRequestFactory {

    public static final String DEFAULT_CURRENCY_CODE = "EUR";
    public static final String DEFAULT_LOCALE = "en";
    public static final Double DEFAULT_AMOUNT = 20.0D;
    public static final String DEFAULT_NAME_PREFIX = "WCL_CHARGE_";
    public static final int DEFAULT_NAME_RANDOM_LENGTH = 10;

    public ChargeCreateRequest defaultWorkingCapitalChargeRequest() {
        return new ChargeCreateRequest() //
                .chargeAppliesTo(ChargeProductAppliesTo.WORKING_CAPITAL_LOAN.value) //
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.value) //
                .chargeCalculationType(ChargeCalculationType.FLAT.value) //
                .name(Utils.randomStringGenerator(DEFAULT_NAME_PREFIX, DEFAULT_NAME_RANDOM_LENGTH)) //
                .amount(DEFAULT_AMOUNT) //
                .active(true) //
                .currencyCode(DEFAULT_CURRENCY_CODE) //
                .locale(DEFAULT_LOCALE) //
                .penalty(false);
    }
}
