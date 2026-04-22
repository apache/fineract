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

package org.apache.fineract.cob.workingcapitalloan.businessstep;

import static org.apache.fineract.infrastructure.core.diagnostics.performance.MeasuringUtil.measure;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanDelinquencyClassificationService;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WorkingCapitalLoanDelinquencyClassificationBusinessStep extends WorkingCapitalLoanCOBBusinessStep {

    private final WorkingCapitalLoanDelinquencyClassificationService delinquencyClassificationService;

    @Override
    public WorkingCapitalLoan execute(WorkingCapitalLoan loan) {
        if (loan == null) {
            log.debug("Ignoring Working Capital delinquency tag processing for null loan.");
            return null;
        }
        String externalId = Optional.ofNullable(loan.getExternalId()).map(ExternalId::getValue).orElse(null);
        measure(() -> setDelinquencyBucketTags(loan, externalId), duration -> {
            log.debug(
                    "Ending Working Capital delinquency tag processing for loan with Id [{}], account number [{}], external Id [{}], finished in [{}]ms",
                    loan.getId(), loan.getAccountNumber(), externalId, duration.toMillis());
        });
        return loan;
    }

    public void setDelinquencyBucketTags(WorkingCapitalLoan loan, String externalId) {
        try {
            log.debug(
                    "Starting Working Capital delinquency tag processing for Working Capital Loan with Id [{}], account number [{}], external Id [{}]",
                    loan.getId(), loan.getAccountNumber(), externalId);

            if (loan.getLoanProductRelatedDetails() != null && loan.getLoanProductRelatedDetails().getDelinquencyBucket() != null) {
                log.debug("Evaluate {} Working Capital Delinquency bucket", loan.getLoanProductRelatedDetails().getDelinquencyBucket());
                delinquencyClassificationService.classifyDelinquency(loan, ThreadLocalContextUtil.getBusinessDate().plusDays(1),
                        loan.getLoanProductRelatedDetails().getDelinquencyBucket());
            } else {
                log.debug("Skipping... Delinquency bucket is not configured for Working Capital Loan {}.", loan.getId());
            }
        } catch (RuntimeException re) {
            log.error(
                    "Received exception while processing delinquency tag for Working Capital Loan with Id [{}], account number [{}], external Id [{}]",
                    loan.getId(), loan.getAccountNumber(), externalId, re);

            throw re;
        }
    }

    @Override
    public String getEnumStyledName() {
        return "WC_LOAN_DELINQUENCY_CLASSIFICATION";
    }

    @Override
    public String getHumanReadableName() {
        return "Working Capital Loan Delinquency Classification Business Step";
    }
}
