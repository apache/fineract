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

import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;

public final class LoanChargeRequestFactory {

    public static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final Long DEFAULT_CHARGE_ID = 1L;
    public static final double DEFAULT_CHARGE_AMOUNT = 1;

    private LoanChargeRequestFactory() {}

    public static PostLoansLoanIdChargesRequest defaultLoanChargeRequest() {
        return new PostLoansLoanIdChargesRequest().chargeId(DEFAULT_CHARGE_ID).amount(DEFAULT_CHARGE_AMOUNT).locale(DEFAULT_LOCALE)
                .dateFormat(DEFAULT_DATE_FORMAT);
    }

    public static PostLoansLoanIdChargesChargeIdRequest defaultLoanChargeWaiveRequest(String dueDate) {
        return new PostLoansLoanIdChargesChargeIdRequest().dateFormat(DEFAULT_DATE_FORMAT).locale(DEFAULT_LOCALE).transactionDate(dueDate);
    }
}
