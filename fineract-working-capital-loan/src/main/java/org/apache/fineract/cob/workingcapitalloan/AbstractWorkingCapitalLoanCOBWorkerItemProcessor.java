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
package org.apache.fineract.cob.workingcapitalloan;

import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.processor.AbstractItemProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanModelProcessingService;
import org.springframework.lang.NonNull;

public abstract class AbstractWorkingCapitalLoanCOBWorkerItemProcessor extends AbstractItemProcessor<WorkingCapitalLoan> {

    private final WorkingCapitalLoanModelProcessingService modelProcessingService;

    public AbstractWorkingCapitalLoanCOBWorkerItemProcessor(final COBBusinessStepService cobBusinessStepService,
            final WorkingCapitalLoanModelProcessingService modelProcessingService) {
        super(cobBusinessStepService);
        this.modelProcessingService = modelProcessingService;
    }

    /**
     * Brings a model written by an older version of the calculation up to date before any business step reads it.
     *
     * <p>
     * Ahead of the steps rather than as one of them: the steps read the schedule to decide what to post, so one running
     * against a model that has silently lost part of itself would post against the wrong figures. This is the same
     * place the progressive term-loan processor rebuilds, for the same reason.
     */
    @Override
    public WorkingCapitalLoan process(@NonNull final WorkingCapitalLoan item) throws Exception {
        if (modelProcessingService.requiresModelRecalculation(item.getId())) {
            modelProcessingService.recalculateModelAndSave(item.getId());
        }
        return super.process(item);
    }

    @Override
    public void setLastRun(WorkingCapitalLoan processedLoan) {
        processedLoan.setLastClosedBusinessDate(getBusinessDate());
    }

}
