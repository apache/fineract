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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanAdjustTransactionEventPublisher {

    private final WorkingCapitalLoanTransactionDataFactory transactionDataFactory;
    private final BusinessEventNotifierService businessEventNotifierService;

    public Map<Long, WorkingCapitalLoanTransactionData> snapshots(final List<WorkingCapitalLoanTransaction> transactions) {
        if (!isPostingEnabled()) {
            return Map.of();
        }
        return transactionDataFactory.create(transactions).stream()
                .collect(Collectors.toMap(WorkingCapitalLoanTransactionData::getId, Function.identity()));
    }

    public void publishReversal(final Long wcLoanId, final WorkingCapitalLoanTransaction reversedTransaction) {
        if (!isPostingEnabled()) {
            return;
        }
        final WorkingCapitalLoanTransactionData snapshot = transactionDataFactory.create(reversedTransaction);
        businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanAdjustTransactionBusinessEvent(
                WorkingCapitalLoanAdjustTransactionBusinessEvent.Data.reversal(snapshot), wcLoanId));
    }

    private boolean isPostingEnabled() {
        return businessEventNotifierService.isExternalEventPostingEnabled(WorkingCapitalLoanAdjustTransactionBusinessEvent.TYPE);
    }

    /** The recording window makes the whole replay reach the consumer as one bulk event instead of N separate ones. */
    public void publishReprocessed(final Long wcLoanId, final List<WorkingCapitalLoanTransactionAdjustment> adjustments) {
        if (adjustments.isEmpty()) {
            return;
        }
        try {
            businessEventNotifierService.startExternalEventRecording();
            adjustments.forEach(adjustment -> businessEventNotifierService.notifyPostBusinessEvent(
                    new WorkingCapitalLoanAdjustTransactionBusinessEvent(new WorkingCapitalLoanAdjustTransactionBusinessEvent.Data(
                            adjustment.previousState(), adjustment.currentState()), wcLoanId)));
            businessEventNotifierService.stopExternalEventRecording();
        } catch (Exception e) {
            businessEventNotifierService.resetEventRecording();
            throw e;
        }
    }

    public record WorkingCapitalLoanTransactionAdjustment(WorkingCapitalLoanTransactionData previousState,
            WorkingCapitalLoanTransactionData currentState) {
    }
}
