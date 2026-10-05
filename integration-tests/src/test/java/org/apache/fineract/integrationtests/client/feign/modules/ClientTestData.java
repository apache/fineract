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

import org.apache.fineract.integrationtests.common.Utils;

public final class ClientTestData {

    public static final String DATETIME_PATTERN = FeignTestConstants.DATETIME_PATTERN;
    public static final String LOCALE = FeignTestConstants.LOCALE;
    public static final Long DEFAULT_OFFICE_ID = 1L;
    public static final Long LEGAL_FORM_PERSON = 1L;
    public static final Long LEGAL_FORM_ENTITY = 2L;

    public static final String DEFAULT_ACTIVATION_DATE = "04 March 2011";
    public static final String DEFAULT_SUBMITTED_ON_DATE = "04 March 2014";
    public static final String CREATED_DATE = Utils.getLocalDateOfTenant().minusDays(5).format(Utils.dateFormatter);
    public static final String CREATED_DATE_PLUS_ONE = Utils.getLocalDateOfTenant().minusDays(4).format(Utils.dateFormatter);
    public static final String CREATED_DATE_PLUS_TWO = Utils.getLocalDateOfTenant().minusDays(3).format(Utils.dateFormatter);

    public static final String CLOSURE_REASON_CODE = "ClientClosureReason";
    public static final String REJECTION_REASON_CODE = "ClientRejectReason";
    public static final String WITHDRAWAL_REASON_CODE = "ClientWithdrawReason";
    public static final String CONSTITUTION_CODE = "Constitution";

    private ClientTestData() {}
}
