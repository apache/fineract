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
package org.apache.fineract.infrastructure.core.exception;

import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.slf4j.Logger;

public class ExceptionHelper {

    public static String fetchExceptionMessage(Exception e) {
        String rootCause = null;
        if (e instanceof AbstractPlatformResourceNotFoundException) {
            AbstractPlatformResourceNotFoundException execption = (AbstractPlatformResourceNotFoundException) e;
            rootCause = execption.getGlobalisationMessageCode();
        } else if (e instanceof AbstractPlatformDomainRuleException) {
            AbstractPlatformDomainRuleException execption = (AbstractPlatformDomainRuleException) e;
            rootCause = execption.getGlobalisationMessageCode();
        } else if (e instanceof AbstractPlatformServiceUnavailableException) {
            AbstractPlatformServiceUnavailableException execption = (AbstractPlatformServiceUnavailableException) e;
            rootCause = execption.getGlobalisationMessageCode();
        } else if (e instanceof PlatformApiDataValidationException) {
            PlatformApiDataValidationException execption = (PlatformApiDataValidationException) e;
            rootCause = execption.getGlobalisationMessageCode();
        } else if (e instanceof PlatformDataIntegrityException) {
            PlatformDataIntegrityException execption = (PlatformDataIntegrityException) e;
            rootCause = execption.getGlobalisationMessageCode();
        } else if (e instanceof PlatformInternalServerException) {
            PlatformInternalServerException execption = (PlatformInternalServerException) e;
            rootCause = execption.getGlobalisationMessageCode();
        } else if (e.getCause() != null) {
            rootCause = e.getCause().getMessage();
        }
        return rootCause;
    }

    public static void handleExceptions(Exception exception, StringBuilder sb, String errorMessage, final Long entityId,
            final Logger logger) {
        if (exception instanceof PlatformApiDataValidationException) {
            PlatformApiDataValidationException e = (PlatformApiDataValidationException) exception;
            final List<ApiParameterError> errors = e.getErrors();
            for (final ApiParameterError error : errors) {
                logger.error(errorMessage + entityId + " with message " + error.getDeveloperMessage());
                sb.append(errorMessage).append(entityId).append(" with message ").append(error.getDeveloperMessage());
            }
        } else if (exception instanceof AbstractPlatformDomainRuleException) {
            AbstractPlatformDomainRuleException ex = (AbstractPlatformDomainRuleException) exception;
            logger.error(errorMessage + entityId + " with message " + ex.getDefaultUserMessage());
            sb.append(errorMessage).append(entityId).append(" with message ").append(ex.getDefaultUserMessage());
        } else {
            String rootCause = ExceptionHelper.fetchExceptionMessage(exception);
            logger.error(errorMessage + entityId + " with message " + rootCause);
            sb.append(errorMessage).append(entityId).append(" with message ").append(rootCause);
        }
    }
}
