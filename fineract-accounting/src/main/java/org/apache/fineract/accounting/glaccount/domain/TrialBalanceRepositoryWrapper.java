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
package org.apache.fineract.accounting.glaccount.domain;

import java.util.List;
import org.apache.fineract.accounting.trialbalance.exception.TrialBalanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrialBalanceRepositoryWrapper {

    private final TrialBalanceRepository repository;

    @Autowired
    public TrialBalanceRepositoryWrapper(final TrialBalanceRepository repository) {
        this.repository = repository;
    }

    public List<TrialBalance> findNewByOfficeAndAccount(final Long officeId, final Long accountId) {
        final List<TrialBalance> trialBalanceList = this.repository.findNewByOfficeAndAccount(officeId, accountId);
        if (trialBalanceList == null) {
            throw new TrialBalanceNotFoundException(officeId, accountId);
        }
        return trialBalanceList;

    }

    public void save(final List<TrialBalance> tbRows) {
        this.repository.saveAll(tbRows);
    }
}
