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
package org.apache.fineract.portfolio.validationlimit.api;

import java.util.Set;

public final class ValidationLimitApiConstants {

    private ValidationLimitApiConstants() {}

    public static final String ID = "id";
    public static final String CLIENT_LEVEL_ID = "clientLevelId";
    public static final String MAXIMUM_SINGLE_DEPOSIT_AMOUNT = "maximumSingleDepositAmount";
    public static final String MAXIMUM_CUMULATIVE_BALANCE = "maximumCumulativeBalance";
    public static final String MAXIMUM_DAILY_WITHDRAW_LIMIT = "maximumDailyWithdrawLimit";
    public static final String MAXIMUM_SINGLE_WITHDRAW_LIMIT = "maximumSingleWithdrawLimit";
    public static final String MAXIMUM_CLIENT_SPECIFIC_DAILY_WITHDRAW_LIMIT = "maximumClientSpecificDailyWithdrawLimit";
    public static final String MAXIMUM_CLIENT_SPECIFIC_SINGLE_WITHDRAW_LIMIT = "maximumClientSpecificSingleWithdrawLimit";
    public static final String LOCALE = "locale";

    public static final Set<String> VALIDATION_LIMIT_REQUEST_DATA_PARAMETERS = Set.of(CLIENT_LEVEL_ID, MAXIMUM_SINGLE_DEPOSIT_AMOUNT,
            MAXIMUM_CUMULATIVE_BALANCE, MAXIMUM_SINGLE_WITHDRAW_LIMIT, MAXIMUM_DAILY_WITHDRAW_LIMIT, LOCALE,
            MAXIMUM_CLIENT_SPECIFIC_DAILY_WITHDRAW_LIMIT, MAXIMUM_CLIENT_SPECIFIC_SINGLE_WITHDRAW_LIMIT);

    public static final Set<String> VALIDATION_LIMIT_DATA_PARAMETERS = Set.of(ID, CLIENT_LEVEL_ID, MAXIMUM_SINGLE_DEPOSIT_AMOUNT,
            MAXIMUM_CUMULATIVE_BALANCE, MAXIMUM_SINGLE_WITHDRAW_LIMIT, MAXIMUM_DAILY_WITHDRAW_LIMIT, LOCALE,
            MAXIMUM_CLIENT_SPECIFIC_DAILY_WITHDRAW_LIMIT, MAXIMUM_CLIENT_SPECIFIC_SINGLE_WITHDRAW_LIMIT);
}
