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
package org.apache.fineract.infrastructure.jobs.filter;

import static org.apache.fineract.batch.command.CommandStrategyUtils.isRelativeUrlVersioned;

import io.github.resilience4j.core.functions.Either;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.cob.conditions.LoanCOBEnabledCondition;
import org.apache.fineract.infrastructure.core.filters.BatchRequestPreprocessor;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
@Conditional(LoanCOBEnabledCondition.class)
public class LoanCOBBatchPreprocessor implements BatchRequestPreprocessor {

    private final LoanCOBFilterHelper helper;

    private final PlatformTransactionManager transactionManager;

    @Override
    public Either<RuntimeException, BatchRequest> preprocess(BatchRequest batchRequest) {
        TransactionTemplate tr = new TransactionTemplate(transactionManager);
        tr.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return tr.execute(status -> {
            try {
                String method = batchRequest.getMethod();
                String relativeUrl = "/" + batchRequest.getRelativeUrl();
                if (!isRelativeUrlVersioned(batchRequest.getRelativeUrl())) {
                    // to support pre-versioned relative paths
                    relativeUrl = "/v1/" + batchRequest.getRelativeUrl();
                }
                if (helper.isOnApiList(relativeUrl, method)) {
                    boolean bypassUser = helper.isBypassUser();
                    if (!bypassUser) {
                        List<Long> result = helper.calculateRelevantLoanIds(relativeUrl);
                        if (!result.isEmpty() && helper.isLoanBehind(result)) {
                            helper.executeInlineCob(result);
                        }
                    }
                }
            } catch (LoanNotFoundException e) {
                return Either.right(batchRequest);
            } catch (RuntimeException e) {
                return Either.left(e);
            }
            return Either.right(batchRequest);
        });
    }
}
