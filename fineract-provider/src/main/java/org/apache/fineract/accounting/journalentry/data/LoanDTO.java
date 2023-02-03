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
package org.apache.fineract.accounting.journalentry.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class LoanDTO {

    @Setter
    private Long loanId;
    @Setter
    private Long loanProductId;
    @Setter
    private Long officeId;
    @Setter
    private String currencyCode;
    @Setter
    private boolean cashBasedAccountingEnabled;
    private final boolean upfrontAccrualBasedAccountingEnabled;
    private final boolean periodicAccrualBasedAccountingEnabled;
    @Setter
    private List<LoanTransactionDTO> newLoanTransactions;
    @Setter
    private boolean markedAsChargeOff;
    @Setter
    private boolean markedAsFraud;
}
