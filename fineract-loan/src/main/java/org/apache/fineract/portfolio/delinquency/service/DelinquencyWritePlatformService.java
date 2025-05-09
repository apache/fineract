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
package org.apache.fineract.portfolio.delinquency.service;

import java.util.List;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketCreateRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketCreateResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketDeleteRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketDeleteResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketUpdateRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketUpdateResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeCreateRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeCreateResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeDeleteRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeDeleteResponse;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeUpdateRequest;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeUpdateResponse;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

public interface DelinquencyWritePlatformService {

    DelinquencyRangeCreateResponse createDelinquencyRange(Command<DelinquencyRangeCreateRequest> command);

    DelinquencyRangeUpdateResponse updateDelinquencyRange(Command<DelinquencyRangeUpdateRequest> command);

    DelinquencyRangeDeleteResponse deleteDelinquencyRange(Command<DelinquencyRangeDeleteRequest> command);

    DelinquencyBucketCreateResponse createDelinquencyBucket(Command<DelinquencyBucketCreateRequest> command);

    DelinquencyBucketUpdateResponse updateDelinquencyBucket(Command<DelinquencyBucketUpdateRequest> command);

    DelinquencyBucketDeleteResponse deleteDelinquencyBucket(Command<DelinquencyBucketDeleteRequest> command);

    CommandProcessingResult applyDelinquencyTagToLoan(Long loanId, JsonCommand command);

    void removeDelinquencyTagToLoan(Loan loan);

    void cleanLoanDelinquencyTags(Loan loan);

    LoanScheduleDelinquencyData calculateDelinquencyData(LoanScheduleDelinquencyData loanScheduleDelinquencyData,
            List<LoanDelinquencyActionData> effectiveDelinquencyList);

    void applyDelinquencyTagToLoan(LoanScheduleDelinquencyData loanDelinquencyData,
            List<LoanDelinquencyActionData> effectiveDelinquencyList);

    CommandProcessingResult createDelinquencyAction(Long loanId, JsonCommand command);

}
