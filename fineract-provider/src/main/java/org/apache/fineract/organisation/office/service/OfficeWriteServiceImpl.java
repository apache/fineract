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
package org.apache.fineract.organisation.office.service;

import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessReadServiceImpl;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeCreateRequest;
import org.apache.fineract.organisation.office.data.OfficeCreateResponse;
import org.apache.fineract.organisation.office.data.OfficeUpdateRequest;
import org.apache.fineract.organisation.office.data.OfficeUpdateResponse;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.exception.RootOfficeParentCannotBeUpdated;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class OfficeWriteServiceImpl implements OfficeWriteService {

    @Getter
    private final PlatformSecurityContext context;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "offices", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')"),
            @CacheEvict(value = "officesForDropdown", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'ofd')"),
            @CacheEvict(value = FineractEntityAccessReadServiceImpl.OFFICE_ENTITY_ACCESS_CACHE_NAME, allEntries = true) })
    public OfficeCreateResponse createOffice(final OfficeCreateRequest request) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final Long parentId = request.getParentId();
            final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, parentId);
            final ExternalId externalId = ExternalIdFactory.produce(request.getExternalId());
            final LocalDate openingDate = parseOpeningDate(request.getOpeningDate(), request.getDateFormat(), request.getLocale());
            final Office office = Office.create(parent, request.getName(), openingDate, externalId);

            // pre save to generate id for use in office hierarchy
            this.officeRepositoryWrapper.saveAndFlush(office);
            office.generateHierarchy();
            this.officeRepositoryWrapper.save(office);

            return OfficeCreateResponse.builder().officeId(office.getId()).resourceId(office.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleOfficeDataIntegrityIssues(request.getName(), request.getExternalId(), dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleOfficeDataIntegrityIssues(request.getName(), request.getExternalId(), throwable, dve);
        }
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "offices", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')"),
            @CacheEvict(value = "officesForDropdown", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'ofd')"),
            @CacheEvict(value = "officesById", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#request.id)", condition = "#request.id != null"),
            @CacheEvict(value = FineractEntityAccessReadServiceImpl.OFFICE_ENTITY_ACCESS_CACHE_NAME, allEntries = true) })
    public OfficeUpdateResponse updateOffice(final OfficeUpdateRequest request) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final Office office = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, request.getId());

            final LocalDate newOpeningDate = request.getOpeningDate() != null
                    ? parseOpeningDate(request.getOpeningDate(), request.getDateFormat(), request.getLocale())
                    : null;
            final ExternalId externalId = request.getExternalId() != null ? ExternalIdFactory.produce(request.getExternalId()) : null;
            final Map<String, Object> changes = office.update(request.getName(), newOpeningDate, externalId);

            if (request.getParentId() != null) {
                if (office.getParent() == null) {
                    throw new RootOfficeParentCannotBeUpdated();
                }
                if (!Objects.equals(request.getParentId(), office.getParent().getId())) {
                    final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, request.getParentId());
                    office.update(parent);
                    changes.put("parentId", request.getParentId());
                }
            }

            if (newOpeningDate != null && changes.containsKey("openingDate")) {
                changes.put("openingDate", request.getOpeningDate());
                if (request.getDateFormat() != null) {
                    changes.put("dateFormat", request.getDateFormat());
                }
                if (request.getLocale() != null) {
                    changes.put("locale", request.getLocale());
                }
            }

            if (!changes.isEmpty()) {
                this.officeRepositoryWrapper.saveAndFlush(office);
            }

            return OfficeUpdateResponse.builder().officeId(office.getId()).resourceId(office.getId()).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleOfficeDataIntegrityIssues(request.getName(), request.getExternalId(), dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleOfficeDataIntegrityIssues(request.getName(), request.getExternalId(), throwable, dve);
        }
    }

    private LocalDate parseOpeningDate(final String openingDateStr, final String dateFormat, final String localeStr) {
        if (StringUtils.isBlank(openingDateStr)) {
            return null;
        }
        final Locale clientLocale = StringUtils.isNotBlank(localeStr) ? JsonParserHelper.localeFromString(localeStr) : Locale.getDefault();
        if (StringUtils.isNotBlank(dateFormat)) {
            return JsonParserHelper.convertFrom(openingDateStr, "openingDate", dateFormat, clientLocale);
        }
        try {
            return LocalDate.parse(openingDateStr);
        } catch (final DateTimeParseException e) {
            return JsonParserHelper.convertFrom(openingDateStr, "openingDate", dateFormat, clientLocale);
        }
    }

    private Office validateUserPriviledgeOnOfficeAndRetrieve(final AppUser currentUser, final Long officeId) {
        final Long userOfficeId = currentUser.getOffice().getId();
        final Office userOffice = this.officeRepositoryWrapper.findOfficeHierarchy(userOfficeId);
        if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(officeId)) {
            throw new NoAuthorizationException("User does not have sufficient priviledges to act on the provided office.");
        }

        Office officeToReturn = userOffice;
        if (!userOffice.identifiedBy(officeId)) {
            officeToReturn = this.officeRepositoryWrapper.findOfficeHierarchy(officeId);
        }

        return officeToReturn;
    }

    public PlatformSecurityContext getContext() {
        return this.context;
    }

    private RuntimeException handleOfficeDataIntegrityIssues(final String name, final String externalId, final Throwable realCause,
            final Exception dve) {
        if (realCause != null && realCause.getMessage() != null) {
            if (realCause.getMessage().contains("externalid_org")) {
                return new PlatformDataIntegrityException("error.msg.office.duplicate.externalId",
                        "Office with externalId `" + externalId + "` already exists", "externalId", externalId);
            } else if (realCause.getMessage().contains("name_org")) {
                return new PlatformDataIntegrityException("error.msg.office.duplicate.name",
                        "Office with name `" + name + "` already exists", "name", name);
            }
        }

        log.error("Error occurred.", dve);
        return ErrorHandler.getMappable(dve, "error.msg.office.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
