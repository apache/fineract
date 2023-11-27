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
package org.apache.fineract.batch.exception;

import static org.springframework.core.ResolvableType.forClassWithGenerics;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exceptionmapper.DefaultExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.FineractExceptionMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @Mock
    DefaultExceptionMapper defaultExceptionMapper;

    @Mock
    ApplicationContext applicationContext;

    @Mock(extraInterfaces = FineractExceptionMapper.class)
    ExceptionMapper<NullPointerException> exceptionMapper;

    @Mock(extraInterfaces = FineractExceptionMapper.class)
    ExceptionMapper<InputMismatchException> inputMismatchMapper;

    @Mock
    Response response;

    @InjectMocks
    ErrorHandler errorHandler;

    @Test
    public void testErrorHandlerFound() {
        // given
        NullPointerException npe = new NullPointerException("Div by zero");
        Mockito.when(applicationContext.getBeanNamesForType(forClassWithGenerics(ExceptionMapper.class, NullPointerException.class)))
                .thenReturn(new String[] { "exceptionMapper1", "exceptionMapper2", "exceptionMapper3" });
        Mockito.when(applicationContext.getBeanNamesForType(FineractExceptionMapper.class))
                .thenReturn(new String[] { "exceptionMapper2", "exceptionMapper4" });
        Mockito.when(applicationContext.getBean("exceptionMapper2")).thenReturn(exceptionMapper);
        Mockito.when(exceptionMapper.toResponse(npe)).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(406);
        Mockito.when(response.getEntity()).thenReturn(Map.of("Exception", "message body"));
        Mockito.when(((FineractExceptionMapper) exceptionMapper).errorCode()).thenReturn(1234);

        // when
        ErrorInfo errorInfo = errorHandler.handle(npe);

        // then
        Assertions.assertEquals(1234, errorInfo.getErrorCode());
        Assertions.assertEquals("{\n  \"Exception\": \"message body\"\n}", errorInfo.getMessage());
        Assertions.assertEquals(406, errorInfo.getStatusCode());
        Mockito.verifyNoMoreInteractions(exceptionMapper);
        Mockito.verifyNoInteractions(defaultExceptionMapper);
    }

    @Test
    public void testErrorHandlerChecksParentException() {
        // given
        InputMismatchException ime = new InputMismatchException("Input Mismatch");
        Mockito.when(applicationContext.getBeanNamesForType(forClassWithGenerics(ExceptionMapper.class, InputMismatchException.class)))
                .thenReturn(new String[] {}); // no direct handler for InputMismatchException
        Mockito.when(applicationContext.getBeanNamesForType(forClassWithGenerics(ExceptionMapper.class, NoSuchElementException.class)))
                .thenReturn(new String[] { "inputMismatchMapper" });
        Mockito.when(applicationContext.getBeanNamesForType(FineractExceptionMapper.class))
                .thenReturn(new String[] { "inputMismatchMapper", "someOtherMapper", "yetAnotherMapper" });
        Mockito.when(applicationContext.getBean("inputMismatchMapper")).thenReturn(inputMismatchMapper);
        Mockito.when(inputMismatchMapper.toResponse(ime)).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(406);
        Mockito.when(response.getEntity()).thenReturn(Map.of("Exception", "message body"));
        Mockito.when(((FineractExceptionMapper) inputMismatchMapper).errorCode()).thenReturn(1234);

        // when
        ErrorInfo errorInfo = errorHandler.handle(ime);

        // then
        Assertions.assertEquals(1234, errorInfo.getErrorCode());
        Assertions.assertEquals("{\n  \"Exception\": \"message body\"\n}", errorInfo.getMessage());
        Assertions.assertEquals(406, errorInfo.getStatusCode());
        Mockito.verifyNoMoreInteractions(inputMismatchMapper);
        Mockito.verifyNoInteractions(exceptionMapper);
        Mockito.verifyNoInteractions(defaultExceptionMapper);
    }

    @Test
    public void testErrorHandlerFallsBackToDefault() {
        // given
        NullPointerException npe = new NullPointerException("Div by zero");
        Mockito.when(applicationContext.getBeanNamesForType(forClassWithGenerics(ExceptionMapper.class, NullPointerException.class)))
                .thenReturn(new String[] {});
        Mockito.when(applicationContext.getBeanNamesForType(FineractExceptionMapper.class))
                .thenReturn(new String[] { "exceptionMapper2", "exceptionMapper4" });
        Mockito.when(defaultExceptionMapper.toResponse(npe)).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(406);
        Mockito.when(response.getEntity()).thenReturn(Map.of("Exception", "message body"));
        Mockito.when(((FineractExceptionMapper) defaultExceptionMapper).errorCode()).thenReturn(1234);

        // when
        ErrorInfo errorInfo = errorHandler.handle(npe);

        // then
        Assertions.assertEquals(1234, errorInfo.getErrorCode());
        Assertions.assertEquals("{\n  \"Exception\": \"message body\"\n}", errorInfo.getMessage());
        Assertions.assertEquals(406, errorInfo.getStatusCode());
        Mockito.verifyNoInteractions(exceptionMapper);
        Mockito.verifyNoMoreInteractions(defaultExceptionMapper);
    }

}
