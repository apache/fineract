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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;

@RequiredArgsConstructor
@Slf4j
public class RecalculateInterestPoster implements Callable<Void> {

    @Setter
    private Collection<Long> loanIds;
    @Setter
    private FineractContext fineractContext;
    private final LoanWritePlatformService loanWritePlatformService;

    @Override
    public Void call() throws JobExecutionException {
        ThreadLocalContextUtil.init(fineractContext);
        if (!loanIds.isEmpty()) {
            List<Throwable> errors = new ArrayList<>();
            for (Long loanId : loanIds) {
                log.debug("Loan ID {}", loanId);
                try {
                    loanWritePlatformService.recalculateInterest(loanId);
                } catch (Exception e) {
                    errors.add(e);
                }
            }
            if (!errors.isEmpty()) {
                throw new JobExecutionException(errors);
            }
        }
        return null;
    }
}
