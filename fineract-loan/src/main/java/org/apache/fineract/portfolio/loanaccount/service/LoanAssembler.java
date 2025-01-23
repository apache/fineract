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

import java.util.Map;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.useradministration.domain.AppUser;

public interface LoanAssembler {

    Loan assembleFrom(Long accountId);

    Loan assembleFrom(Long accountId, boolean loadLazyCollections);

    Loan assembleFrom(ExternalId externalId);

    Loan assembleFrom(ExternalId externalId, boolean loadLazyCollections);

    Loan assembleFrom(JsonCommand command);

    void setHelpers(Loan loanAccount);

    void accountNumberGeneration(JsonCommand command, Loan loan);

    CodeValue findCodeValueByIdIfProvided(Long codeValueId);

    Fund findFundByIdIfProvided(Long fundId);

    Staff findLoanOfficerByIdIfProvided(Long loanOfficerId);

    Map<String, Object> updateFrom(JsonCommand command, Loan loan);

    Map<String, Object> updateLoanApplicationAttributesForWithdrawal(Loan loan, JsonCommand command, AppUser currentUser);

    Map<String, Object> updateLoanApplicationAttributesForRejection(Loan loan, JsonCommand command, AppUser currentUser);
}
