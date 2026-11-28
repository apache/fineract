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
package org.apache.fineract.portfolio.client.service;

import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierDeleteRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierDeleteResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierUpdateRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierUpdateResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierUpdateResponse.ClientIdentifierUpdateChanges;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierRepository;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierStatus;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientIdentifierNotFoundException;
import org.apache.fineract.portfolio.client.exception.DuplicateClientIdentifierException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class ClientIdentifierWriteServiceImpl implements ClientIdentifierWriteService {

    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;

    @Transactional
    @Override
    public ClientIdentifierCreateResponse createClientIdentifier(final ClientIdentifierCreateRequest request) {
        String documentKey = null;
        String documentTypeLabel = null;
        Long documentTypeId = null;
        try {
            final Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(request.getClientId());
            // Any code value is accepted here, same as the legacy path: document types are not restricted to a
            // particular code.
            final CodeValue documentType = codeValueRepository.findOneWithNotFoundDetection(request.getDocumentTypeId());
            documentKey = request.getDocumentKey();
            documentTypeId = documentType.getId();
            documentTypeLabel = documentType.getLabel();

            final ClientIdentifier entity = ClientIdentifier.builder().client(client).documentType(documentType)
                    .documentKey(StringUtils.defaultIfEmpty(request.getDocumentKey(), null))
                    .description(StringUtils.defaultIfEmpty(request.getDescription(), null))
                    .issuanceDate(parseDate("issuanceDate", request.getIssuanceDate(), request.getDateFormat(), request.getLocale()))
                    .expiryDate(parseDate("expiryDate", request.getExpiryDate(), request.getDateFormat(), request.getLocale())).build();

            final ClientIdentifierStatus statusEnum = ClientIdentifierStatus.valueOf(
                    StringUtils.defaultIfEmpty(request.getStatus(), ClientIdentifierStatus.ACTIVE.name()).toUpperCase(Locale.ROOT));
            entity.setStatus(statusEnum.getValue());
            if (statusEnum.isActive()) {
                entity.setActive(statusEnum.getValue());
            }

            clientIdentifierRepository.saveAndFlush(entity);

            return ClientIdentifierCreateResponse.builder().resourceId(entity.getId()).clientId(request.getClientId())
                    .officeId(client.officeId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(documentTypeId, documentTypeLabel, documentKey, dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(documentTypeId, documentTypeLabel, documentKey, throwable, dve);
        }
    }

    @Transactional
    @Override
    public ClientIdentifierUpdateResponse updateClientIdentifier(final ClientIdentifierUpdateRequest request) {
        // Defaults for the duplicate-key exception message: fall back to the identifier's current document type and
        // key when this update doesn't touch them, so the message is never missing them.
        String documentKey = null;
        String documentTypeLabel = null;
        Long documentTypeId = null;
        try {
            // Same "at least one real field" check as the legacy validateForUpdate: status/description alone
            // aren't enough to count as a meaningful update.
            if (request.getDocumentTypeId() == null && request.getDocumentKey() == null && !request.isIssuanceDateProvided()
                    && !request.isExpiryDateProvided()) {
                throw singleParameterError("id", "validation.msg.clientIdentifier.no.parameters.for.update",
                        "No parameters passed for update.");
            }

            final Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(request.getClientId());
            final ClientIdentifier entity = clientIdentifierRepository.findById(request.getId())
                    .orElseThrow(() -> new ClientIdentifierNotFoundException(request.getId()));
            if (entity.getClient() == null || !entity.getClient().getId().equals(request.getClientId())) {
                throw new ClientIdentifierNotFoundException(request.getId());
            }

            documentKey = entity.getDocumentKey();
            documentTypeId = entity.getDocumentType().getId();
            documentTypeLabel = entity.getDocumentType().getLabel();

            final ClientIdentifierUpdateChanges changes = new ClientIdentifierUpdateChanges();
            boolean changed = false;

            if (request.getDocumentTypeId() != null && !request.getDocumentTypeId().equals(entity.getDocumentType().getId())) {
                // Any code value is accepted here, same as the legacy path.
                final CodeValue documentType = codeValueRepository.findOneWithNotFoundDetection(request.getDocumentTypeId());
                entity.setDocumentType(documentType);
                changes.setDocumentTypeId(request.getDocumentTypeId());
                changed = true;
                documentTypeId = documentType.getId();
                documentTypeLabel = documentType.getLabel();
            }
            if (request.getDocumentKey() != null) {
                if (StringUtils.isBlank(request.getDocumentKey())) {
                    throw singleParameterError("documentKey", "org.apache.fineract.portfolio.client.identifier.document-key.not-blank",
                            "The parameter `documentKey` cannot be blank.");
                }
                if (!request.getDocumentKey().equals(entity.getDocumentKey())) {
                    entity.setDocumentKey(request.getDocumentKey());
                    changes.setDocumentKey(request.getDocumentKey());
                    changed = true;
                    documentKey = request.getDocumentKey();
                }
            }
            if (request.getDescription() != null && !request.getDescription().equals(entity.getDescription())) {
                entity.setDescription(StringUtils.defaultIfEmpty(request.getDescription(), null));
                changes.setDescription(request.getDescription());
                changed = true;
            }
            if (StringUtils.isNotBlank(request.getStatus())) {
                // Raw valueOf, same as the legacy update path: unlike create, update never upper-cases this.
                final ClientIdentifierStatus statusEnum = ClientIdentifierStatus.valueOf(request.getStatus());
                entity.setStatus(statusEnum.getValue());
                changes.setStatus(request.getStatus());
                changed = true;
            }
            changed |= applyDateChange("issuanceDate", request.isIssuanceDateProvided(), request.getIssuanceDate(), request.getDateFormat(),
                    request.getLocale(), entity::getIssuanceDate, entity::setIssuanceDate, changes::setIssuanceDate);
            changed |= applyDateChange("expiryDate", request.isExpiryDateProvided(), request.getExpiryDate(), request.getDateFormat(),
                    request.getLocale(), entity::getExpiryDate, entity::setExpiryDate, changes::setExpiryDate);

            if (changed) {
                clientIdentifierRepository.saveAndFlush(entity);
            }
            // changes is always returned; with nothing changed it serializes as {}, same as the legacy result
            return ClientIdentifierUpdateResponse.builder().resourceId(entity.getId()).clientId(request.getClientId())
                    .officeId(client.officeId()).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(documentTypeId, documentTypeLabel, documentKey, dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(documentTypeId, documentTypeLabel, documentKey, throwable, dve);
        }
    }

    @Transactional
    @Override
    public ClientIdentifierDeleteResponse deleteClientIdentifier(final ClientIdentifierDeleteRequest request) {
        final Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(request.getClientId());
        final ClientIdentifier entity = clientIdentifierRepository.findById(request.getId())
                .orElseThrow(() -> new ClientIdentifierNotFoundException(request.getId()));
        if (entity.getClient() == null || !entity.getClient().getId().equals(request.getClientId())) {
            throw new ClientIdentifierNotFoundException(request.getId());
        }
        clientIdentifierRepository.delete(entity);
        return ClientIdentifierDeleteResponse.builder().resourceId(request.getId()).clientId(request.getClientId())
                .officeId(client.officeId()).build();
    }

    /**
     * Not sent leaves the stored date unchanged; an explicit {@code null} or an empty string clears it; anything else
     * is parsed. Only recorded in {@code changes} if it actually differs from the current value.
     *
     * @return whether the stored date changed
     */
    private boolean applyDateChange(final String parameterName, final boolean provided, final String value, final String dateFormat,
            final String locale, final Supplier<LocalDate> getter, final Consumer<LocalDate> setter,
            final Consumer<LocalDate> changeRecorder) {
        if (!provided && value == null) {
            return false;
        }
        final LocalDate parsed = parseDate(parameterName, value, dateFormat, locale);
        if (Objects.equals(parsed, getter.get())) {
            return false;
        }
        setter.accept(parsed);
        changeRecorder.accept(parsed);
        return true;
    }

    /**
     * Delegates to {@link JsonParserHelper#convertFrom}, not {@link DateUtils#parseLocalDate}, so parsing keeps the
     * legacy path's case-insensitive/lenient/STRICT-resolver behaviour and still requires dateFormat and locale
     * whenever a date is actually sent.
     */
    private LocalDate parseDate(final String parameterName, final String value, final String dateFormat, final String localeValue) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        final Locale locale = StringUtils.isBlank(localeValue) ? null : JsonParserHelper.localeFromString(localeValue);
        return JsonParserHelper.convertFrom(value, parameterName, dateFormat, locale);
    }

    private PlatformApiDataValidationException singleParameterError(final String parameterName, final String messageKey,
            final String defaultMessage) {
        final List<ApiParameterError> errors = List.of(ApiParameterError.parameterError(messageKey, defaultMessage, parameterName));
        return new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", errors);
    }

    private RuntimeException handleDataIntegrityIssues(final Long documentTypeId, final String documentTypeLabel, final String documentKey,
            final Throwable realCause, final Exception dve) {
        if (realCause.getMessage() != null && realCause.getMessage().contains("unique_active_client_identifier")) {
            return new DuplicateClientIdentifierException(documentTypeLabel);
        } else if (realCause.getMessage() != null && realCause.getMessage().contains("unique_identifier_key")) {
            return new DuplicateClientIdentifierException(documentTypeId, documentTypeLabel, documentKey);
        }
        log.error("Error occurred.", dve);
        return ErrorHandler.getMappable(dve, "error.msg.clientIdentifier.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
