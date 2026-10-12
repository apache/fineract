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
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.office.exception.OfficeNotFoundException;
import org.apache.fineract.organisation.staff.data.StaffCreateRequest;
import org.apache.fineract.organisation.staff.data.StaffCreateResponse;
import org.apache.fineract.organisation.staff.data.StaffUpdateRequest;
import org.apache.fineract.organisation.staff.data.StaffUpdateResponse;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.organisation.staff.mapper.StaffCreateRequestMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class StaffWriteServiceImpl implements StaffWriteService {

    private final StaffRepository staffRepository;
    private final OfficeRepository officeRepository;
    private final StaffCreateRequestMapper staffCreateRequestMapper;

    @Transactional
    @Override
    public StaffCreateResponse createStaff(final StaffCreateRequest request) {

        try {
            var staff = staffCreateRequestMapper.map(request);

            var office = officeRepository.findById(request.getOfficeId())
                    .orElseThrow(() -> new OfficeNotFoundException(request.getOfficeId()));
            staff.setOffice(office);

            staff.setDisplayName(
                    StringUtils.isEmpty(staff.getFirstname()) ? staff.getLastname() : staff.getLastname() + ", " + staff.getFirstname());

            staffRepository.saveAndFlush(staff);

            return StaffCreateResponse.builder().resourceId(staff.getId()).officeId(request.getOfficeId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleStaffDataIntegrityIssues(request.getExternalId(), request.getFirstname(), request.getLastname(),
                    dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            var throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleStaffDataIntegrityIssues(request.getExternalId(), request.getFirstname(), request.getLastname(), throwable, dve);
        }
    }

    @Transactional
    @Override
    public StaffUpdateResponse updateStaff(final StaffUpdateRequest request) {

        try {
            var staff = this.staffRepository.findById(request.getId()).orElseThrow(() -> new StaffNotFoundException(request.getId()));

            var changes = new HashMap<String, Object>();

            if (request.getOfficeId() != null) {
                var office = officeRepository.findById(request.getOfficeId())
                        .orElseThrow(() -> new OfficeNotFoundException(request.getOfficeId()));
                staff.setOffice(office);
                changes.put(StaffUpdateRequest.Fields.officeId, request.getOfficeId());
            }
            if (StringUtils.isNotEmpty(request.getFirstname())) {
                staff.setFirstname(request.getFirstname());
                changes.put(StaffUpdateRequest.Fields.firstname, request.getFirstname());
            }
            if (StringUtils.isNotEmpty(request.getLastname())) {
                staff.setLastname(request.getLastname());
                changes.put(StaffUpdateRequest.Fields.lastname, request.getLastname());
            }
            if (StringUtils.isNotEmpty(request.getExternalId())) {
                staff.setExternalId(request.getExternalId());
                changes.put(StaffUpdateRequest.Fields.externalId, request.getExternalId());
            }
            if (StringUtils.isNotEmpty(request.getEmailAddress())) {
                staff.setEmailAddress(request.getEmailAddress());
                changes.put(StaffUpdateRequest.Fields.emailAddress, request.getEmailAddress());
            }
            if (StringUtils.isNotEmpty(request.getMobileNo())) {
                staff.setMobileNo(request.getMobileNo());
                changes.put(StaffUpdateRequest.Fields.mobileNo, request.getMobileNo());
            }
            if (request.getIsLoanOfficer() != null) {
                staff.setLoanOfficer(request.getIsLoanOfficer());
                changes.put(StaffUpdateRequest.Fields.isLoanOfficer, request.getIsLoanOfficer());
            }
            if (request.getIsActive() != null) {
                staff.setActive(request.getIsActive());
                changes.put(StaffUpdateRequest.Fields.isActive, request.getIsActive());
            }

            var response = StaffUpdateResponse.builder().officeId(staff.getOffice().getId()).resourceId(staff.getId());

            if (!changes.isEmpty()) {
                response.changes(changes);

                staff.setDisplayName(StringUtils.isEmpty(staff.getFirstname()) ? staff.getLastname()
                        : staff.getLastname() + ", " + staff.getFirstname());

                staffRepository.saveAndFlush(staff);
            }

            return response.build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleStaffDataIntegrityIssues(request.getExternalId(), request.getFirstname(), request.getLastname(),
                    dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleStaffDataIntegrityIssues(request.getExternalId(), request.getFirstname(), request.getLastname(), throwable, dve);
        }
    }

    private RuntimeException handleStaffDataIntegrityIssues(String externalId, String firstname, String lastname, final Throwable realCause,
            final Exception dve) {
        if (realCause.getMessage().contains("external_id")) {
            return new PlatformDataIntegrityException("error.msg.staff.duplicate.externalId",
                    "Staff with externalId `" + externalId + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("display_name")) {
            var displayName = StringUtils.isEmpty(firstname) ? lastname : lastname + ", " + firstname;
            return new PlatformDataIntegrityException("error.msg.staff.duplicate.displayName",
                    "A staff with the given display name '" + displayName + "' already exists", "displayName", displayName);
        }

        log.error("Error occurred.", dve);

        return ErrorHandler.getMappable(dve, "error.msg.staff.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
