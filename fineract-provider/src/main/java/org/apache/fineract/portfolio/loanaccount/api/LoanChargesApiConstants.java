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
package org.apache.fineract.portfolio.loanaccount.api;

import java.util.Set;

public final class LoanChargesApiConstants {

    /**
     * Fields a loan charge response may be restricted to with the {@code fields} query parameter. The Batch API still
     * applies this filter, see the loan charge command strategies.
     */
    public static final Set<String> LOAN_CHARGE_RESPONSE_DATA_PARAMETERS = Set.of("id", "chargeId", "name", "penalty", "chargeTimeType",
            "dueAsOfDate", "chargeCalculationType", "percentage", "amountPercentageAppliedTo", "currency", "amountWaived",
            "amountWrittenOff", "amountOutstanding", "amountOrPercentage", "amount", "amountPaid", "chargeOptions", "installmentChargeData",
            "externalId");

    private LoanChargesApiConstants() {}
}
