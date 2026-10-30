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
package org.apache.fineract.accounting.closure.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.closure.data.GLClosureCreateRequest;
import org.apache.fineract.accounting.closure.data.GLClosureCreateResponse;
import org.apache.fineract.accounting.closure.data.GLClosureDeleteRequest;
import org.apache.fineract.accounting.closure.data.GLClosureDeleteResponse;
import org.apache.fineract.accounting.closure.data.GLClosureUpdateRequest;
import org.apache.fineract.accounting.closure.data.GLClosureUpdateResponse;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.closure.exception.GLClosureDuplicateException;
import org.apache.fineract.accounting.closure.exception.GLClosureInvalidDeleteException;
import org.apache.fineract.accounting.closure.exception.GLClosureInvalidException;
import org.apache.fineract.accounting.closure.exception.GLClosureInvalidException.GlClosureInvalidReason;
import org.apache.fineract.accounting.closure.exception.GLClosureNotFoundException;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GLClosureWritePlatformServiceJpaRepositoryImpl implements GLClosureWritePlatformService {

    private final GLClosureRepository glClosureRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;

    @Transactional
    @Override
    public GLClosureCreateResponse createGLClosure(final GLClosureCreateRequest request) {
        try {
            final Long officeId = request.getOfficeId();
            final Office office = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            final LocalDate closureDate = request.getClosingDate();
            if (DateUtils.isDateInTheFuture(closureDate)) {
                throw new GLClosureInvalidException(GlClosureInvalidReason.FUTURE_DATE, closureDate);
            }
            final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(officeId);
            if (latestGLClosure != null && DateUtils.isAfter(latestGLClosure.getClosingDate(), closureDate)) {
                throw new GLClosureInvalidException(GlClosureInvalidReason.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate());
            }
            final GLClosure glClosure = new GLClosure(office, closureDate, request.getComments());

            this.glClosureRepository.saveAndFlush(glClosure);

            return GLClosureCreateResponse.builder().officeId(officeId).resourceId(glClosure.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleGLClosureIntegrityIssues(request, throwable, dve);
            return null;
        }
    }

    @Transactional
    @Override
    public GLClosureUpdateResponse updateGLClosure(final GLClosureUpdateRequest request) {
        final Long glClosureId = request.getId();
        final GLClosure glClosure = this.glClosureRepository.findById(glClosureId)
                .orElseThrow(() -> new GLClosureNotFoundException(glClosureId));

        glClosure.setComments(request.getComments());

        this.glClosureRepository.saveAndFlush(glClosure);

        return GLClosureUpdateResponse.builder().officeId(glClosure.getOffice().getId()).resourceId(glClosure.getId())
                .comments(glClosure.getComments()).build();
    }

    @Transactional
    @Override
    public GLClosureDeleteResponse deleteGLClosure(final GLClosureDeleteRequest request) {
        final Long glClosureId = request.getId();
        final GLClosure glClosure = this.glClosureRepository.findById(glClosureId)
                .orElseThrow(() -> new GLClosureNotFoundException(glClosureId));

        final LocalDate closureDate = glClosure.getClosingDate();
        final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(glClosure.getOffice().getId());
        if (latestGLClosure != null && DateUtils.isAfter(latestGLClosure.getClosingDate(), closureDate)) {
            throw new GLClosureInvalidDeleteException(latestGLClosure.getOffice().getId(), latestGLClosure.getOffice().getName(),
                    latestGLClosure.getClosingDate());
        }

        this.glClosureRepository.delete(glClosure);

        return GLClosureDeleteResponse.builder().officeId(glClosure.getOffice().getId()).resourceId(glClosure.getId()).build();
    }

    private void handleGLClosureIntegrityIssues(final GLClosureCreateRequest request, final Throwable realCause,
            final NonTransientDataAccessException dve) {
        if (realCause.getMessage() != null && realCause.getMessage().contains("office_id_closing_date")) {
            throw new GLClosureDuplicateException(request.getOfficeId(), request.getClosingDate());
        }

        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.glClosure.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource GL Closure: " + realCause.getMessage());
    }
}
