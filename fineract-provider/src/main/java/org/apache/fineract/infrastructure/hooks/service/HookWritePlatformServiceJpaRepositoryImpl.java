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

import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.actionNameParamName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.configParamName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.contentTypeName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.entityNameParamName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.eventsParamName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.nameParamName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.payloadURLName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.templateIdParamName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.webTemplateName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.hooks.domain.Hook;
import org.apache.fineract.infrastructure.hooks.domain.HookConfiguration;
import org.apache.fineract.infrastructure.hooks.domain.HookRepository;
import org.apache.fineract.infrastructure.hooks.domain.HookResource;
import org.apache.fineract.infrastructure.hooks.domain.HookTemplate;
import org.apache.fineract.infrastructure.hooks.domain.HookTemplateRepository;
import org.apache.fineract.infrastructure.hooks.domain.Schema;
import org.apache.fineract.infrastructure.hooks.exception.HookNotFoundException;
import org.apache.fineract.infrastructure.hooks.exception.HookTemplateNotFoundException;
import org.apache.fineract.infrastructure.hooks.processor.ProcessorHelper;
import org.apache.fineract.infrastructure.hooks.processor.WebHookService;
import org.apache.fineract.infrastructure.hooks.serialization.HookCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.template.domain.Template;
import org.apache.fineract.template.domain.TemplateRepository;
import org.apache.fineract.template.exception.TemplateNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit.RetrofitError;

@Service
public class HookWritePlatformServiceJpaRepositoryImpl
        implements
            HookWritePlatformService {

    private final PlatformSecurityContext context;
    private final HookRepository hookRepository;
    private final HookTemplateRepository hookTemplateRepository;
    private final TemplateRepository ugdTemplateRepository;
    private final HookCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public HookWritePlatformServiceJpaRepositoryImpl(
            final PlatformSecurityContext context,
            final HookRepository hookRepository,
            final HookTemplateRepository hookTemplateRepository,
            final TemplateRepository ugdTemplateRepository,
            final HookCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final FromJsonHelper fromApiJsonHelper) {
        this.context = context;
        this.hookRepository = hookRepository;
        this.hookTemplateRepository = hookTemplateRepository;
        this.ugdTemplateRepository = ugdTemplateRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Transactional
    @Override
    @CacheEvict(value = "hooks", allEntries = true)
    public CommandProcessingResult createHook(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final HookTemplate template = retrieveHookTemplateBy(command
                    .stringValueOfParameterNamed(nameParamName));
            final String configJson = command.jsonFragment(configParamName);
            final Set<HookConfiguration> config = assembleConfig(
                    command.mapValueOfParameterNamed(configJson), template);
            final JsonArray events = command
                    .arrayOfParameterNamed(eventsParamName);
            final Set<HookResource> allEvents = assembleSetOfEvents(events);
            Template ugdTemplate = null;
            if (command.hasParameter(templateIdParamName)) {
                final Long ugdTemplateId = command
                        .longValueOfParameterNamed(templateIdParamName);
                ugdTemplate = this.ugdTemplateRepository.findOne(ugdTemplateId);
                if (ugdTemplate == null) {
                    throw new TemplateNotFoundException(ugdTemplateId);
                }
            }
            final Hook hook = Hook.fromJson(command, template, config,
                    allEvents, ugdTemplate);

            validateHookRules(template, config, allEvents);

            this.hookRepository.save(hook);

            return new CommandProcessingResultBuilder()
                    .withCommandId(command.commandId())
                    .withEntityId(hook.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleHookDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleHookDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "hooks", allEntries = true)
    public CommandProcessingResult updateHook(final Long hookId,
            final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Hook hook = retrieveHookBy(hookId);
            final HookTemplate template = hook.getHookTemplate();
            final Map<String, Object> changes = hook.update(command);

            if (!changes.isEmpty()) {

                if (changes.containsKey(templateIdParamName)) {
                    final Long ugdTemplateId = command
                            .longValueOfParameterNamed(templateIdParamName);
                    final Template ugdTemplate = this.ugdTemplateRepository
                            .findOne(ugdTemplateId);
                    if (ugdTemplate == null) {
                        changes.remove(templateIdParamName);
                        throw new TemplateNotFoundException(ugdTemplateId);
                    }
                    hook.updateUgdTemplate(ugdTemplate);
                }

                if (changes.containsKey(eventsParamName)) {
                    final Set<HookResource> events = assembleSetOfEvents(command
                            .arrayOfParameterNamed(eventsParamName));
                    final boolean updated = hook.updateEvents(events);
                    if (!updated) {
                        changes.remove(eventsParamName);
                    }
                }

                if (changes.containsKey(configParamName)) {
                    final String configJson = command
                            .jsonFragment(configParamName);
                    final Set<HookConfiguration> config = assembleConfig(
                            command.mapValueOfParameterNamed(configJson),
                            template);
                    final boolean updated = hook.updateConfig(config);
                    if (!updated) {
                        changes.remove(configParamName);
                    }
                }

                this.hookRepository.saveAndFlush(hook);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(hookId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleHookDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleHookDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "hooks", allEntries = true)
    public CommandProcessingResult deleteHook(final Long hookId) {

        this.context.authenticatedUser();
        final Hook hook = retrieveHookBy(hookId);
        try {
            this.hookRepository.delete(hook);
        } catch (final DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException(
                    "error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: "
                            + e.getMostSpecificCause());
        }
        return new CommandProcessingResultBuilder().withEntityId(hookId)
                .build();
    }

    private Hook retrieveHookBy(final Long hookId) {
        final Hook hook = this.hookRepository.findOne(hookId);
        if (hook == null) {
            throw new HookNotFoundException(hookId);
        }
        return hook;
    }

    private HookTemplate retrieveHookTemplateBy(final String templateName) {
        final HookTemplate template = this.hookTemplateRepository
                .findOne(templateName);
        if (template == null) {
            throw new HookTemplateNotFoundException(templateName);
        }
        return template;
    }

    private Set<HookConfiguration> assembleConfig(
            final Map<String, String> hookConfig, final HookTemplate template) {

        final Set<HookConfiguration> configuration = new HashSet<>();
        final Set<Schema> fields = template.getSchema();

        for (final Entry<String, String> configEntry : hookConfig.entrySet()) {
            for (final Schema field : fields) {
                final String fieldName = field.getFieldName();
                if (fieldName.equalsIgnoreCase(configEntry.getKey())) {

                    final HookConfiguration config = HookConfiguration
                            .createNewWithoutHook(field.getFieldType(),
                                    configEntry.getKey(),
                                    configEntry.getValue());
                    configuration.add(config);
                    break;
                }
            }

        }

        return configuration;
    }

    private Set<HookResource> assembleSetOfEvents(final JsonArray eventsArray) {

        final Set<HookResource> allEvents = new HashSet<>();

        for (int i = 0; i < eventsArray.size(); i++) {

            final JsonObject eventElement = eventsArray.get(i)
                    .getAsJsonObject();

            final String entityName = this.fromApiJsonHelper
                    .extractStringNamed(entityNameParamName, eventElement);
            final String actionName = this.fromApiJsonHelper
                    .extractStringNamed(actionNameParamName, eventElement);
            final HookResource event = HookResource.createNewWithoutHook(
                    entityName, actionName);
            allEvents.add(event);
        }

        return allEvents;
    }

    private void validateHookRules(final HookTemplate template,
            final Set<HookConfiguration> config, Set<HookResource> events) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
                dataValidationErrors).resource("hook");

        if (!template.getName().equalsIgnoreCase(webTemplateName)
                && this.hookRepository.findOneByTemplateId(template.getId()) != null) {
            final String errorMessage = "multiple.non.web.template.hooks.not.supported";
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                    errorMessage);
        }

        for (final HookConfiguration conf : config) {
            final String fieldValue = conf.getFieldValue();
            if (conf.getFieldName().equals(contentTypeName)) {
                if (!(fieldValue.equalsIgnoreCase("json") || fieldValue
                        .equalsIgnoreCase("form"))) {
                    final String errorMessage = "content.type.must.be.json.or.form";
                    baseDataValidator.reset()
                            .failWithCodeNoParameterAddedToErrorCode(
                                    errorMessage);
                }
            }

            if (conf.getFieldName().equals(payloadURLName)) {
                try {
                    final WebHookService service = ProcessorHelper
                            .createWebHookService(fieldValue);
                    service.sendEmptyRequest();
                } catch (RetrofitError re) {
                    // Swallow error if it's because of method not supported or
                    // if url throws 404 - required for integration test,
                    // url generated on 1st POST request
                    if (re.getResponse() == null) {
                        String errorMessage = "url.invalid";
                        baseDataValidator.reset()
                                .failWithCodeNoParameterAddedToErrorCode(
                                        errorMessage);
                    }
                }
            }
        }

        if (events == null || events.isEmpty()) {
            final String errorMessage = "registered.events.cannot.be.empty";
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(
                    errorMessage);
        }

        final Set<Schema> fields = template.getSchema();
        for (final Schema field : fields) {
            if (!field.isOptional()) {
                boolean found = false;
                for (final HookConfiguration conf : config) {
                    if (field.getFieldName().equals(conf.getFieldName())) {
                        found = true;
                    }
                }
                if (!found) {
                    final String errorMessage = "required.config.field."
                            + "not.provided";
                    baseDataValidator
                            .reset()
                            .value(field.getFieldName())
                            .failWithCodeNoParameterAddedToErrorCode(
                                    errorMessage);
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void handleHookDataIntegrityIssues(final JsonCommand command, final Throwable realCause,
            final Exception dve) {
        if (realCause.getMessage().contains("hook_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException(
                    "error.msg.hook.duplicate.name", "A hook with name '"
                            + name + "' already exists", "name", name);
        }

        throw new PlatformDataIntegrityException(
                "error.msg.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: "
                        + realCause.getMessage());
    }
}
