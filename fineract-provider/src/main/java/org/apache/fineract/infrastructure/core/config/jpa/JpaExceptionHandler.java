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
package org.apache.fineract.infrastructure.core.config.jpa;

import java.sql.SQLIntegrityConstraintViolationException;
import javax.servlet.http.HttpServletRequest;
import org.apache.fineract.infrastructure.core.exception.CommandUnderProcessingException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.ExceptionHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class JpaExceptionHandler implements ExceptionHandler, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public JpaExceptionHandler() {}

    @Override
    public Object handleException(RuntimeException exception) {
        if (exception instanceof DatabaseException) {
            Throwable dbException = ((DatabaseException) exception).getInternalException();
            if (dbException instanceof SQLIntegrityConstraintViolationException) {
                String requestIdempotencyKey = null;
                HttpServletRequest request = null;
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    if (requestAttributes instanceof ServletRequestAttributes) {
                        request = ((ServletRequestAttributes) requestAttributes).getRequest();
                        requestIdempotencyKey = request.getHeader(
                                applicationContext.getEnvironment().getProperty("fineract.idempotency-key-header-name", "Idempotency-Key"));
                    }
                }

                if (requestIdempotencyKey != null) {
                    SQLIntegrityConstraintViolationException sqlICVException = (SQLIntegrityConstraintViolationException) dbException;
                    String message = sqlICVException.getMessage();
                    if (message != null) {
                        String exMessage = message.toLowerCase();
                        if (exMessage.contains("duplicate entry") && exMessage.contains("portfolio_command_source")) {
                            throw new CommandUnderProcessingException(request.getMethod(), request.getContextPath(), requestIdempotencyKey,
                                    null);
                        } else {
                            throw exception;
                        }
                    }
                }
            }
        }

        throw exception;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
