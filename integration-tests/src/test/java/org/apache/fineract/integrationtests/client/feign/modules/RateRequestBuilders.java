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
package org.apache.fineract.integrationtests.client.feign.modules;

import java.math.BigDecimal;
import org.apache.fineract.client.models.RateRequest;
import org.apache.fineract.integrationtests.common.Utils;

public final class RateRequestBuilders {

    /** {@code productApply} discriminator for loan products. */
    public static final Integer PRODUCT_APPLY_LOAN = 1;

    public static final BigDecimal DEFAULT_PERCENTAGE = BigDecimal.valueOf(10);
    public static final BigDecimal MODIFIED_PERCENTAGE = BigDecimal.valueOf(15.0);

    private RateRequestBuilders() {}

    public static RateRequest loanRate() {
        return loanRate(PRODUCT_APPLY_LOAN, DEFAULT_PERCENTAGE);
    }

    public static RateRequest loanRate(Integer productApply, BigDecimal percentage) {
        return new RateRequest()//
                .active(true)//
                .name(Utils.uniqueRandomStringGenerator("Rate_Loans_", 6))//
                .percentage(percentage)//
                .productApply(productApply)//
                .locale(FeignTestConstants.LOCALE);
    }

    public static RateRequest modifyRatePercentage() {
        return new RateRequest()//
                .percentage(MODIFIED_PERCENTAGE)//
                .locale(FeignTestConstants.LOCALE);
    }
}
