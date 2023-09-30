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
package org.apache.fineract.portfolio.savings.service;

import java.util.Collection;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;

/**
 * @author manoj
 */

@Slf4j
@RequiredArgsConstructor
public class SavingsSchedularInterestPosterTask implements Callable<Void> {

    private final SavingsSchedularInterestPoster interestPoster;
    @Setter
    private FineractContext context;

    @Override
    public Void call() throws org.apache.fineract.infrastructure.jobs.exception.JobExecutionException {
        ThreadLocalContextUtil.init(context);
        interestPoster.postInterest();
        return null;
    }

    public void setSavingAccounts(Collection<SavingsAccountData> savingAccounts) {
        this.interestPoster.setSavingAccounts(savingAccounts);
    }

    public void setBackdatedTxnsAllowedTill(boolean backdatedTxnsAllowedTill) {
        this.interestPoster.setBackdatedTxnsAllowedTill(backdatedTxnsAllowedTill);
    }
}
