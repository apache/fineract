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
package org.apache.fineract.organisation.workingdays.handler;

import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysUpdateRequest;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysUpdateResponse;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysWritePlatformService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateWorkingDaysCommandHandler implements CommandHandler<WorkingDaysUpdateRequest, WorkingDaysUpdateResponse> {

    private final WorkingDaysWritePlatformService workingDaysWritePlatformService;

    @Transactional
    @Override
    public WorkingDaysUpdateResponse handle(Command<WorkingDaysUpdateRequest> command) {
        WorkingDaysUpdateRequest request = command.getPayload();
        Map<String, Object> changes = this.workingDaysWritePlatformService.updateWorkingDays(request);
        if (changes == null) {
            changes = Collections.emptyMap();
        }

        return WorkingDaysUpdateResponse.builder().resourceId((Long) changes.get("resourceId")).changes(changes)
                .recurrence(request.getRecurrence()).repaymentRescheduleType(request.getRepaymentRescheduleType())
                .extendTermForDailyRepayments(request.getExtendTermForDailyRepayments())
                .extendTermForRepaymentsOnHolidays(request.getExtendTermForRepaymentsOnHolidays()).build();
    }

}
