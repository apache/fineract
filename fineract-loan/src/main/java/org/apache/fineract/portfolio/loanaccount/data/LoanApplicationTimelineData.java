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
package org.apache.fineract.portfolio.loanaccount.data;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Data object represent the important time-line events of a loan application and loan.
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationTimelineData {

    private LocalDate submittedOnDate;
    private String submittedByUsername;
    private String submittedByFirstname;
    private String submittedByLastname;
    private LocalDate rejectedOnDate;
    private String rejectedByUsername;
    private String rejectedByFirstname;
    private String rejectedByLastname;
    private LocalDate withdrawnOnDate;
    private String withdrawnByUsername;
    private String withdrawnByFirstname;
    private String withdrawnByLastname;
    private LocalDate approvedOnDate;
    private String approvedByUsername;
    private String approvedByFirstname;
    private String approvedByLastname;
    private LocalDate expectedDisbursementDate;
    private LocalDate actualDisbursementDate;
    private String disbursedByUsername;
    private String disbursedByFirstname;
    private String disbursedByLastname;
    private LocalDate closedOnDate;
    private String closedByUsername;
    private String closedByFirstname;
    private String closedByLastname;

    private LocalDate actualMaturityDate;
    private LocalDate expectedMaturityDate;
    private LocalDate writeOffOnDate;
    private String writeOffByUsername;
    private String writeOffByFirstname;
    private String writeOffByLastname;

    private LocalDate chargedOffOnDate;
    private String chargedOffByUsername;
    private String chargedOffByFirstname;
    private String chargedOffByLastname;

    public LocalDate getDisbursementDate() {
        LocalDate disbursementDate = this.expectedDisbursementDate;
        if (this.actualDisbursementDate != null) {
            disbursementDate = this.actualDisbursementDate;
        }
        return disbursementDate;
    }
}
