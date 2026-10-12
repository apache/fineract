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

package org.apache.fineract.portfolio.workingcapitalloan.serialization;

public final class WorkingCapitalLoanChargeConstants {

    private WorkingCapitalLoanChargeConstants() {}

    public static final String chargeIdParamName = "chargeId";
    public static final String amountParamName = "amount";
    public static final String dueDateParamName = "dueDate";
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";
    public static final String externalIdParamName = "externalId";

    // Adjustment parameters
    public static final String transactionDateParamName = "transactionDate";
    public static final String noteParamName = "note";
    public static final String paymentDetailsParamName = "paymentDetails";
    public static final String paymentTypeIdParamName = "paymentTypeId";
    public static final String accountNumberParamName = "accountNumber";
    public static final String checkNumberParamName = "checkNumber";
    public static final String routingCodeParamName = "routingCode";
    public static final String receiptNumberParamName = "receiptNumber";
    public static final String bankNumberParamName = "bankNumber";

    // Adjustment command
    public static final String ADJUSTMENT_LOAN_CHARGE_COMMAND = "adjustment";
}
