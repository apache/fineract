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
package org.apache.fineract.portfolio.loanaccount.service;

import java.util.Collection;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;

public interface LoanChargeWritePlatformService {

    CommandProcessingResult addLoanCharge(Long loanId, JsonCommand command);

    CommandProcessingResult loanChargeRefund(Long loanId, JsonCommand command);

    CommandProcessingResult undoWaiveLoanCharge(JsonCommand command);

    CommandProcessingResult updateLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    CommandProcessingResult waiveLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    CommandProcessingResult deleteLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    CommandProcessingResult payLoanCharge(Long loanId, Long loanChargeId, JsonCommand command, boolean isChargeIdIncludedInJson);

    CommandProcessingResult adjustmentForLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    void applyOverdueChargesForLoan(Long loanId, Collection<OverdueLoanScheduleData> overdueLoanScheduleDataList);
}
