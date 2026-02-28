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
package org.apache.fineract.organisation.staff.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.StaffRequest;
import org.apache.fineract.organisation.staff.serialization.StaffCommandFromApiJsonDeserializer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffWritePlatformServiceJpaRepositoryImpl implements StaffWritePlatformService {

    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final StaffCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Transactional
    @Override
    public CommandProcessingResult createStaff(final JsonCommand command) {
        final StaffRequest request = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        try {
            final Office staffOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(request.getOfficeId());
            final Staff staff = Staff.fromRequest(staffOffice, request);
            this.staffRepositoryWrapper.save(staff);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(staff.getId())
                    .withOfficeId(staff.officeId()).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(request, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateStaff(final Long staffId, final JsonCommand command) {
        final StaffRequest request = this.fromApiJsonDeserializer.commandFromApiJson(command.json()); // We move the
                                                                                                      // 'try' here so
                                                                                                      // it covers all
                                                                                                      // your logic
                                                                                                      // below
        try {
            final Staff staffForUpdate = this.staffRepositoryWrapper.findOneWithNotFoundDetection(staffId);
            final Map<String, Object> changes = staffForUpdate.update(request);

            if (changes.containsKey("officeId")) {
                final Office newOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(request.getOfficeId());
                staffForUpdate.changeOffice(newOffice);
            }

            if (!changes.isEmpty()) {
                this.staffRepositoryWrapper.save(staffForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(staffId)
                    .withOfficeId(staffForUpdate.officeId()).with(changes).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(request, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }
    }

    private void handleStaffDataIntegrityIssues(final StaffRequest request, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("external_id")) {
            final String externalId = request.getExternalId();
            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.externalId",
                    "Staff with externalId `" + externalId + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("display_name")) {
            // FIX: Use 'request' getters, not 'command'
            final String lastname = request.getLastname();
            String displayName = lastname;
            if (StringUtils.isNotBlank(request.getFirstname())) {
                displayName = lastname + ", " + request.getFirstname();
            }
            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.displayName",
                    "A staff with the given display name '" + displayName + "' already exists", "displayName", displayName);
        }

        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.staff.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
