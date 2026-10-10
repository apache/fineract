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
package org.apache.fineract.portfolio.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnValueData;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.junit.jupiter.api.Test;

class SearchUtilTest {

    private static final String COLUMN_NAME = "DocType_cd_DocType";

    private final SearchUtil searchUtil = new SearchUtil(mock(SqlValidator.class));
    private final DatabaseSpecificSQLGenerator sqlGenerator = mock(DatabaseSpecificSQLGenerator.class);

    private static ResultsetColumnHeaderData codeLookupColumn() {
        return ResultsetColumnHeaderData.detailed(COLUMN_NAME, "INT", null, true, false,
                List.of(new ResultsetColumnValueData(11, "Passport"), new ResultsetColumnValueData(12, "ID card")), "DocType", false, false,
                DatabaseType.POSTGRESQL);
    }

    private Object parse(String value) {
        return searchUtil.parseColumnValue(codeLookupColumn(), value, null, null, null, false, sqlGenerator);
    }

    @Test
    void codeLookupColumnReturnsAllowedCodeValueId() {
        assertEquals(12, parse("12"));
    }

    @Test
    void codeLookupColumnRejectsCodeValueIdNotInAllowedList() {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class, () -> parse("99"));
        assertInvalidColumnValue(exception);
    }

    @Test
    void codeLookupColumnRejectsNonNumericValueAsValidationError() {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class, () -> parse("abc"));
        assertInvalidColumnValue(exception);
    }

    private static void assertInvalidColumnValue(PlatformApiDataValidationException exception) {
        assertEquals(1, exception.getErrors().size());
        ApiParameterError error = exception.getErrors().get(0);
        assertEquals("error.msg.invalid.columnValue", error.getUserMessageGlobalisationCode());
    }
}
