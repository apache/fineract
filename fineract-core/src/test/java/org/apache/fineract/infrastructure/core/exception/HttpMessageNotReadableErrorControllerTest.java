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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Tests for {@link HttpMessageNotReadableErrorController}, in particular the FINERACT-2724 behaviour where a
 * non-numeric value submitted for a numeric field is reported as a field-specific "not a valid number" validation error
 * rather than the generic invalid-JSON error.
 */
public class HttpMessageNotReadableErrorControllerTest {

    private final HttpMessageNotReadableErrorController controller = new HttpMessageNotReadableErrorController();

    @Test
    public void nonNumericValueForNumericFieldReturnsFieldSpecificValidationError() throws Exception {
        final InvalidFormatException invalidFormatException = invalidFormatExceptionFor("not-a-number", BigDecimal.class);
        invalidFormatException.prependPath(HttpMessageNotReadableErrorControllerTest.class, "txnAmount");
        final HttpMessageNotReadableException exception = new HttpMessageNotReadableException("JSON parse error", invalidFormatException,
                null);

        final Response response = controller.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        final ApiParameterError error = (ApiParameterError) response.getEntity();
        assertNotNull(error);
        assertEquals("validation.msg.invalid.decimal.format", error.getUserMessageGlobalisationCode());
        assertEquals("txnAmount", error.getParameterName());
        assertEquals("not-a-number", error.getValue());
        assertEquals("The parameter `txnAmount` has value: not-a-number which is not a valid number.", error.getDefaultUserMessage());
    }

    @Test
    public void invalidFormatOnNonNumericFieldFallsBackToGenericInvalidJsonError() throws Exception {
        final InvalidFormatException invalidFormatException = invalidFormatExceptionFor("not-a-date", LocalDate.class);
        invalidFormatException.prependPath(HttpMessageNotReadableErrorControllerTest.class, "txnDate");
        final HttpMessageNotReadableException exception = new HttpMessageNotReadableException("JSON parse error", invalidFormatException,
                null);

        final Response response = controller.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        final ApiParameterError error = (ApiParameterError) response.getEntity();
        assertNotNull(error);
        assertEquals("error.msg.invalid.json.data", error.getUserMessageGlobalisationCode());
        assertEquals("id", error.getParameterName());
    }

    @Test
    public void malformedJsonSyntaxFallsBackToGenericInvalidJsonError() {
        final JsonParseException jsonParseException = new JsonParseException((JsonParser) null, "Unexpected end-of-input");
        final HttpMessageNotReadableException exception = new HttpMessageNotReadableException("JSON parse error", jsonParseException, null);

        final Response response = controller.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        final ApiParameterError error = (ApiParameterError) response.getEntity();
        assertNotNull(error);
        assertEquals("error.msg.invalid.json.data", error.getUserMessageGlobalisationCode());
        assertEquals("id", error.getParameterName());
    }

    private InvalidFormatException invalidFormatExceptionFor(final String value, final Class<?> targetType) throws Exception {
        final JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser('"' + value + '"')) {
            parser.nextToken();
            return InvalidFormatException.from(parser, "Cannot deserialize value", value, targetType);
        }
    }

}
