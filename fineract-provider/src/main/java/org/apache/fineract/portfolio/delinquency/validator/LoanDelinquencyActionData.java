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
package org.apache.fineract.portfolio.delinquency.validator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Data;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;

@Data
public class LoanDelinquencyActionData {

    private Long id;
    private DelinquencyAction action;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long createdById;
    private OffsetDateTime createdOn;
    private Long updatedById;
    private OffsetDateTime lastModifiedOn;

    public LoanDelinquencyActionData(LoanDelinquencyAction loanDelinquencyAction) {
        this.id = loanDelinquencyAction.getId();
        this.action = loanDelinquencyAction.getAction();
        this.startDate = loanDelinquencyAction.getStartDate();
        this.endDate = loanDelinquencyAction.getEndDate();

        loanDelinquencyAction.getCreatedBy().ifPresent(this::setCreatedById);
        loanDelinquencyAction.getLastModifiedBy().ifPresent(this::setUpdatedById);
        this.createdOn = loanDelinquencyAction.getCreatedDateTime();
        this.lastModifiedOn = loanDelinquencyAction.getLastModifiedDateTime();
    }

}
