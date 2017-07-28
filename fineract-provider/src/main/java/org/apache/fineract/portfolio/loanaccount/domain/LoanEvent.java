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
package org.apache.fineract.portfolio.loanaccount.domain;

/**
 *
 */
public enum LoanEvent {

    LOAN_CREATED, //
    LOAN_REJECTED, //
    LOAN_WITHDRAWN, //
    LOAN_APPROVED, //
    LOAN_APPROVAL_UNDO, //
    LOAN_RECOVERY_PAYMENT, //
    LOAN_DISBURSED, //
    LOAN_DISBURSAL_UNDO, //
    LOAN_DISBURSAL_UNDO_LAST, //
    LOAN_REPAYMENT_OR_WAIVER, //
    REPAID_IN_FULL, //
    WRITE_OFF_OUTSTANDING, //
    WRITE_OFF_OUTSTANDING_UNDO, //
    LOAN_RESCHEDULE, //
    INTERST_REBATE_OWED, //
    LOAN_OVERPAYMENT, //
    LOAN_CHARGE_PAYMENT, //
    LOAN_CLOSED, //
    LOAN_EDIT_MULTI_DISBURSE_DATE, //
    LOAN_REFUND, //
    LOAN_FORECLOSURE;
}
