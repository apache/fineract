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

import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.organisation.staff.data.CreateStaffRequest;
import org.apache.fineract.organisation.staff.data.CreateStaffResponse;
import org.apache.fineract.organisation.staff.data.UpdateStaffRequest;
import org.apache.fineract.organisation.staff.data.UpdateStaffResponse;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.organisation.staff.mapper.StaffMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class StaffWritePlatformServiceJpaRepositoryImpl implements StaffWritePlatformService {

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;

    @Transactional
    @Override
    public CreateStaffResponse createStaff(CreateStaffRequest createStaffRequest) {

        try {
            final Long officeId = createStaffRequest.getOfficeId();

            final Staff staff = staffMapper.map(createStaffRequest);

            this.staffRepository.saveAndFlush(staff);

            return CreateStaffResponse.builder().resourceId(staff.getId()).officeId(officeId).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(createStaffRequest, dve.getMostSpecificCause(), dve);
            return new CreateStaffResponse();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleStaffDataIntegrityIssues(createStaffRequest, throwable, dve);
            return new CreateStaffResponse();
        }
    }

    @Transactional
    @Override
    public UpdateStaffResponse updateStaff(final Long staffId, UpdateStaffRequest updateStaffRequest) {

        try {
            final Staff staffForUpdate = this.staffRepository.findById(staffId).orElseThrow(() -> new StaffNotFoundException(staffId));
            staffMapper.updateStaffFromRequest(updateStaffRequest, staffForUpdate);
            this.staffRepository.saveAndFlush(staffForUpdate);
            return UpdateStaffResponse.builder().resourceId(staffId).officeId(staffForUpdate.officeId()).changes(updateStaffRequest)
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(updateStaffRequest, dve.getMostSpecificCause(), dve);
            return new UpdateStaffResponse();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleStaffDataIntegrityIssues(updateStaffRequest, throwable, dve);
            return new UpdateStaffResponse();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleStaffDataIntegrityIssues(final Object requestPayload, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("external_id")) {
            String externalId = null;
            if (requestPayload instanceof CreateStaffRequest createStaffRequest) {
                externalId = createStaffRequest.getExternalId();
            } else if (requestPayload instanceof UpdateStaffRequest updateStaffRequest) {
                externalId = updateStaffRequest.getExternalId();
            }
            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.externalId",
                    "Staff with externalId `" + externalId + "` already exists", "externalId", externalId);
        }

        if (realCause.getMessage().contains("display_name")) {
            String firstname = null;
            String lastname = null;

            if (requestPayload instanceof CreateStaffRequest createRequest) {
                firstname = createRequest.getFirstname();
                lastname = createRequest.getLastname();
            } else if (requestPayload instanceof UpdateStaffRequest updateRequest) {
                firstname = updateRequest.getFirstname();
                lastname = updateRequest.getLastname();
            }

            String displayName = lastname != null ? lastname : "";
            if (StringUtils.isNotBlank(lastname) && StringUtils.isNotBlank(firstname)) {
                displayName = lastname + ", " + firstname;
            }

            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.displayName",
                    "A staff with the given display name '" + displayName + "' already exists", "displayName", displayName);
        }

        log.error("Unhandled data integrity issue", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.staff.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
