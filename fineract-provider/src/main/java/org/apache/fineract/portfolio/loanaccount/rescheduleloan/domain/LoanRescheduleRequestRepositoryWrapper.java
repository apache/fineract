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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain;

import org.apache.fineract.portfolio.loanaccount.rescheduleloan.exception.LoanRescheduleRequestNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanRescheduleRequestRepositoryWrapper {

    private final LoanRescheduleRequestRepository loanRescheduleRequestRepository;

    @Autowired
    public LoanRescheduleRequestRepositoryWrapper(final LoanRescheduleRequestRepository loanRescheduleRequestRepository) {
        this.loanRescheduleRequestRepository = loanRescheduleRequestRepository;
    }

    public LoanRescheduleRequest findOneWithNotFoundDetection(final Long id) {
        return this.findOneWithNotFoundDetection(id, false);
    }

    @Transactional(readOnly = true)
    public LoanRescheduleRequest findOneWithNotFoundDetection(final Long id, boolean loadLazyCollections) {
        final LoanRescheduleRequest loanRescheduleRequest = this.loanRescheduleRequestRepository.findOne(id);
        if (loanRescheduleRequest == null) { throw new LoanRescheduleRequestNotFoundException(id); }
        if (loadLazyCollections) {
            loanRescheduleRequest.getLoan().initializeLazyCollections();
        }
        return loanRescheduleRequest;
    }

}
