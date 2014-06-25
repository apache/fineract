/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.cache.command;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.cache.CacheApiConstants;
import org.mifosplatform.infrastructure.cache.domain.CacheType;
import org.mifosplatform.infrastructure.cache.service.CacheWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.reflect.TypeToken;

@Service
public class UpdateCacheCommandHandler implements NewCommandSourceHandler {

    private final CacheWritePlatformService cacheService;

    @Autowired
    public UpdateCacheCommandHandler(final CacheWritePlatformService cacheService) {
        this.cacheService = cacheService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        command.checkForUnsupportedParameters(typeOfMap, json, CacheApiConstants.REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(CacheApiConstants.RESOURCE_NAME.toLowerCase());

        final int cacheTypeEnum = command.integerValueSansLocaleOfParameterNamed(CacheApiConstants.cacheTypeParameter);
        baseDataValidator.reset().parameter(CacheApiConstants.cacheTypeParameter).value(Integer.valueOf(cacheTypeEnum)).notNull()
                .isOneOfTheseValues(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3));

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

        final CacheType cacheType = CacheType.fromInt(cacheTypeEnum);

        final Map<String, Object> changes = this.cacheService.switchToCache(cacheType);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).build();
    }
}