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
package org.apache.fineract.infrastructure.hooks.service;

import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.contentTypeName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.payloadURLName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.webTemplateName;

import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.hooks.data.HookCreateRequest;
import org.apache.fineract.infrastructure.hooks.data.HookCreateResponse;
import org.apache.fineract.infrastructure.hooks.data.HookDeleteRequest;
import org.apache.fineract.infrastructure.hooks.data.HookDeleteResponse;
import org.apache.fineract.infrastructure.hooks.data.HookUpdateRequest;
import org.apache.fineract.infrastructure.hooks.data.HookUpdateResponse;
import org.apache.fineract.infrastructure.hooks.domain.Hook;
import org.apache.fineract.infrastructure.hooks.domain.HookConfiguration;
import org.apache.fineract.infrastructure.hooks.domain.HookRepository;
import org.apache.fineract.infrastructure.hooks.domain.HookResource;
import org.apache.fineract.infrastructure.hooks.domain.HookSchema;
import org.apache.fineract.infrastructure.hooks.domain.HookTemplate;
import org.apache.fineract.infrastructure.hooks.domain.HookTemplateRepository;
import org.apache.fineract.infrastructure.hooks.exception.HookNotFoundException;
import org.apache.fineract.infrastructure.hooks.exception.HookTemplateNotFoundException;
import org.apache.fineract.infrastructure.hooks.mapper.HookEventMapper;
import org.apache.fineract.infrastructure.hooks.processor.ProcessorHelper;
import org.apache.fineract.template.domain.Template;
import org.apache.fineract.template.domain.TemplateRepository;
import org.apache.fineract.template.exception.TemplateNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = HookWritePlatformService.class, ignored = HookWritePlatformServiceImpl.class)
public class HookWritePlatformServiceImpl implements HookWritePlatformService {

    private final HookRepository hookRepository;
    private final HookTemplateRepository hookTemplateRepository;
    private final TemplateRepository ugdTemplateRepository;
    private final ProcessorHelper processorHelper;
    private final HookEventMapper hookEventMapper;

    @Transactional
    @Override
    @CacheEvict(value = "hooks", allEntries = true)
    public HookCreateResponse createHook(HookCreateRequest request) {

        try {
            var template = retrieveHookTemplateBy(request.getName());
            var resources = hookEventMapper.map(request.getEvents());
            var configurations = assembleConfig(request.getConfig(), template);

            Template ugdTemplate = null;

            if (request.getTemplateId() != null) {
                ugdTemplate = ugdTemplateRepository.findById(request.getTemplateId())
                        .orElseThrow(() -> new TemplateNotFoundException(request.getTemplateId()));
            }

            var hook = new Hook().setIsActive(Boolean.TRUE.equals(request.getIsActive())).setTemplate(template).setUgdTemplate(ugdTemplate)
                    .setConfig(configurations).setEvents(resources)
                    .setName(StringUtils.isNotBlank(request.getDisplayName()) ? request.getDisplayName().trim() : template.getName());
            hook.getConfig().forEach(hookConfiguration -> hookConfiguration.setHook(hook));
            hook.getEvents().forEach(hookResource -> hookResource.setHook(hook));

            validateHookRules(template, configurations, resources);

            this.hookRepository.saveAndFlush(hook);

            return HookCreateResponse.builder().resourceId(hook.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleHookDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleHookDataIntegrityIssues(request.getName(), throwable, dve);
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "hooks", allEntries = true)
    public HookUpdateResponse updateHook(HookUpdateRequest request) {

        try {
            var hook = hookRepository.findById(request.getId()).orElseThrow(() -> new HookNotFoundException(request.getId()));
            var changes = new HashMap<String, Object>();

            if (!Objects.equals(request.getDisplayName(), hook.getName())) {
                changes.put(HookUpdateRequest.Fields.displayName, request.getDisplayName());
            }
            if (!Objects.equals(request.getIsActive(), hook.getIsActive())) {
                changes.put(HookUpdateRequest.Fields.isActive, request.getIsActive());
            }
            var optionalTemplateId = Optional.ofNullable(request.getTemplateId());

            if (optionalTemplateId.isPresent() && !Objects.equals(request.getTemplateId(),
                    Optional.ofNullable(hook.getTemplate()).map(AbstractPersistableCustom::getId).orElse(null))) {
                changes.put(HookUpdateRequest.Fields.templateId, request.getTemplateId());

                var ugdTemplate = ugdTemplateRepository.findById(request.getTemplateId()).orElse(null);

                if (ugdTemplate == null) {
                    changes.remove(HookUpdateRequest.Fields.templateId);
                    throw new TemplateNotFoundException(request.getTemplateId());
                }

                hook.setUgdTemplate(ugdTemplate);
            }
            if (Objects.nonNull(request.getEvents()) && !request.getEvents().isEmpty()) {
                changes.put(HookUpdateRequest.Fields.events, request.getEvents());

                hook.setEvents(hookEventMapper.map(request.getEvents()));
                hook.getEvents().forEach(hookResource -> hookResource.setHook(hook));
            }
            if (Objects.nonNull(request.getConfig()) && !request.getConfig().isEmpty()) {
                changes.put(HookUpdateRequest.Fields.config, request.getConfig());

                hook.setConfig(assembleConfig(request.getConfig(), hook.getTemplate()));
                hook.getConfig().forEach(hookConfiguration -> hookConfiguration.setHook(hook));
            }

            if (!changes.isEmpty()) {
                hookRepository.saveAndFlush(hook);
            }

            return HookUpdateResponse.builder().resourceId(hook.getId()).changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleHookDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleHookDataIntegrityIssues(request.getName(), throwable, dve);
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "hooks", allEntries = true)
    public HookDeleteResponse deleteHook(HookDeleteRequest request) {
        var hook = hookRepository.findById(request.getId()).orElseThrow(() -> new HookNotFoundException(request.getId()));

        try {
            this.hookRepository.delete(hook);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause(), e);
        }
        return HookDeleteResponse.builder().resourceId(request.getId()).build();
    }

    private HookTemplate retrieveHookTemplateBy(final String templateName) {
        var template = this.hookTemplateRepository.findOne(templateName);

        if (template == null) {
            throw new HookTemplateNotFoundException(templateName);
        }

        return template;
    }

    private Set<HookConfiguration> assembleConfig(final Map<String, String> hookConfig, final HookTemplate template) {

        final Set<HookConfiguration> configuration = new HashSet<>();
        final Set<HookSchema> fields = template.getFields();

        for (final Map.Entry<String, String> configEntry : hookConfig.entrySet()) {
            for (final HookSchema field : fields) {
                final String fieldName = field.getFieldName();
                if (fieldName.equalsIgnoreCase(configEntry.getKey())) {

                    final HookConfiguration config = HookConfiguration.createNewWithoutHook(field.getFieldType(), configEntry.getKey(),
                            configEntry.getValue());
                    configuration.add(config);
                    break;
                }
            }

        }

        return configuration;
    }

    private void validateHookRules(final HookTemplate template, final Set<HookConfiguration> config, Set<HookResource> events) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("hook");

        if (!template.getName().equalsIgnoreCase(webTemplateName) && hookRepository.findOneByTemplateId(template.getId()) != null) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("multiple.non.web.template.hooks.not.supported");
        }

        for (final HookConfiguration conf : config) {
            final String fieldValue = conf.getFieldValue();
            if (conf.getFieldName().equals(contentTypeName)) {
                if ((!fieldValue.equalsIgnoreCase("json") && !fieldValue.equalsIgnoreCase("form"))) {
                    baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("content.type.must.be.json.or.form");
                }
            }

            if (conf.getFieldName().equals(payloadURLName)) {
                try {
                    var service = processorHelper.createWebHookService(fieldValue);
                    service.sendEmptyRequest(fieldValue).execute();
                } catch (IOException | IllegalArgumentException re) {
                    baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("url.invalid");
                }
            }
        }

        if (events == null || events.isEmpty()) {
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("registered.events.cannot.be.empty");
        }

        for (final HookSchema field : template.getFields()) {
            if (!field.isOptional()) {
                boolean found = false;

                for (final HookConfiguration conf : config) {
                    if (field.getFieldName().equals(conf.getFieldName())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    baseDataValidator.reset().value(field.getFieldName())
                            .failWithCodeNoParameterAddedToErrorCode("required.config.field.not.provided");
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private RuntimeException handleHookDataIntegrityIssues(final String name, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("hook_name")) {
            return new PlatformDataIntegrityException("error.msg.hook.duplicate.name", "A hook with name '" + name + "' already exists",
                    "name", name);
        }
        return ErrorHandler.getMappable(dve, "error.msg.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
