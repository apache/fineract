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

import com.google.common.base.Splitter;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatusChangeHistory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatusChangeHistoryRepository;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoanStatusChangeHistoryListener {

    private final Set<LoanStatus> loanStatuses = new HashSet<>();

    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanStatusChangeHistoryRepository loanStatusChangeHistoryRepository;
    private final FineractProperties fineractProperties;

    @PostConstruct
    public void addListeners() {
        loanStatuses.addAll(getLoanStatuses(fineractProperties.getLoan().getStatusChangeHistoryStatuses()));
        if (loanStatuses.size() > 0) {
            businessEventNotifierService.addPostBusinessEventListener(LoanStatusChangedBusinessEvent.class,
                    new LoanStatusChangedListener());
        }
    }

    Set<LoanStatus> getLoanStatuses(String str) {
        Set<LoanStatus> result = new HashSet<>();
        if ("NONE".equals(StringUtils.trim(str))) {
            return result;
        } else if ("ALL".equals(StringUtils.trim(str))) {
            return Arrays.stream(LoanStatus.values()).collect(Collectors.toSet());
        } else {
            List<String> split = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(str);
            for (int i = 0; i < split.size(); i++) {
                try {
                    result.add(Enum.valueOf(LoanStatus.class, split.get(i)));
                } catch (IllegalArgumentException iae) {
                    throw new RuntimeException("Invalid loan status: " + split.get(i), iae);
                }
            }
        }
        return result;
    }

    protected final class LoanStatusChangedListener implements BusinessEventListener<LoanStatusChangedBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanStatusChangedBusinessEvent event) {
            final Loan loan = event.get();
            log.debug("Loan Status change for loan {} with status {}", loan.getId(), loan.getStatus());
            if (loanStatuses.contains(loan.getStatus())) {
                LoanStatusChangeHistory loanStatusChangeHistory = new LoanStatusChangeHistory(loan, loan.getStatus(),
                        DateUtils.getBusinessLocalDate());
                loanStatusChangeHistoryRepository.saveAndFlush(loanStatusChangeHistory);
            }
        }
    }
}
