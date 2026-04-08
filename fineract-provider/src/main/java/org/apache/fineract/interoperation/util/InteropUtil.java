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
package org.apache.fineract.interoperation.util;

import java.util.Locale;

public final class InteropUtil {

    private InteropUtil() {

    }

    public static final String ISO8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS[-HH:MM]";
    public static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd";

    public static final Locale DEFAULT_LOCALE = Locale.US;

    public static final String ROOT_PATH = "interoperation";
    public static final String DEFAULT_ROUTING_CODE = "INTEROPERATION";

    public static final String ENTITY_NAME_IDENTIFIER = "INTERID";
    public static final String ENTITY_NAME_REQUEST = "INTERREQUEST";
    public static final String ENTITY_NAME_QUOTE = "INTERQUOTE";
    public static final String ENTITY_NAME_TRANSFER = "INTERTRANSFER";

    public static final String ACTION_TRANSFER_PREPARE = "PREPARE";
    public static final String ACTION_TRANSFER_COMMIT = "CREATE";
    public static final String ACTION_TRANSFER_RELEASE = "RELEASE";

    public static final String PARAM_LOCALE = "locale";
    public static final String PARAM_DATE_FORMAT = "dateFormat";

    public static final String PARAM_TRANSACTION_CODE = "transactionCode";
    public static final String PARAM_REQUEST_CODE = "requestCode";
    public static final String PARAM_QUOTE_CODE = "quoteCode";
    public static final String PARAM_TRANSFER_CODE = "transferCode";
    public static final String PARAM_ACCOUNT_ID = "accountId";
    public static final String PARAM_AMOUNT_TYPE = "amountType";
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_FEES = "fees";
    public static final String PARAM_FSP_FEE = "fspFee";
    public static final String PARAM_FSP_COMMISSION = "fspCommission";
    public static final String PARAM_TRANSACTION_TYPE = "transactionType";
    public static final String PARAM_TRANSACTION_ROLE = "transactionRole";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_GEO_CODE = "geoCode";

    public static final String PARAM_CURRENCY = "currency";

    public static final String PARAM_SCENARIO = "scenario";
    public static final String PARAM_SUB_SCENARIO = "subScenario";
    public static final String PARAM_INITIATOR = "initiator";
    public static final String PARAM_INITIATOR_TYPE = "initiatorType";
    public static final String PARAM_REFUND_INFO = "refundInfo";
    public static final String PARAM_BALANCE_OF_PAYMENTS = "balanceOfPayments";
    public static final String PARAM_EXPIRATION = "expiration";

    public static final String PARAM_LATITUDE = "latitude";
    public static final String PARAM_LONGITUDE = "latitude";

    public static final String PARAM_EXTENSION_LIST = "extensionList";
    public static final String PARAM_KEY = "key";
    public static final String PARAM_VALUE = "value";
}
