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
package org.apache.fineract.test.data;

public enum LoanRescheduleErrorMessage {

    LOAN_CHARGED_OFF("Loan: %s reschedule installment is not allowed. Loan Account is Charged-off"), LOAN_LOCKED_BY_COB(
            "Loan is locked by the COB job. Loan ID: %s");

    public final String value;

    LoanRescheduleErrorMessage(String value) {
        this.value = value;
    }

    public String getValue(Object... params) {
        return String.format(this.value, params);
    }
}
