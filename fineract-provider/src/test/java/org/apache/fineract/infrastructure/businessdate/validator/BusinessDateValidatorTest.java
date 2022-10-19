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
package org.apache.fineract.infrastructure.businessdate.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.time.LocalDate;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateData;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.Test;

public class BusinessDateValidatorTest {

    private BusinessDateDataParserAndValidator businessDateDataParserAndValidator = new BusinessDateDataParserAndValidator(
            new FromJsonHelper());

    @Test
    public void validateAndParseUpdateWithEmptyRequest() {
        String json = "{}";
        JsonCommand command = JsonCommand.from(json);
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> businessDateDataParserAndValidator.validateAndParseUpdate(command));
        assertEquals(
                "PlatformApiDataValidationException{errors=[The parameter `type` is mandatory., The parameter `locale` is mandatory., The parameter `dateFormat` is mandatory., The parameter `date` is mandatory.]}",
                exception.toString());
    }

    @Test
    public void validateAndParseUpdateWithBlankFieldsInRequest() {
        String json = "{\"type\":\"\", \"locale\":\"\",\"dateFormat\":\"\",\"date\":\"\"}";
        JsonCommand command = JsonCommand.from(json);
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> businessDateDataParserAndValidator.validateAndParseUpdate(command));
        assertEquals(
                "PlatformApiDataValidationException{errors=[The parameter `type` is mandatory., The parameter `locale` is mandatory., The parameter `dateFormat` is mandatory., The parameter `date` is mandatory.]}",
                exception.toString());
    }

    @Test
    public void validateAndParseUpdateWithInvalidLocale() {
        String json = "{\"type\":\"BUSINESS_DATE\", \"locale\":\"invalid\",\"dateFormat\":\"yyyy-MM-dd\",\"date\":\"2022-06-11\"}";
        JsonCommand command = JsonCommand.from(json);
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> businessDateDataParserAndValidator.validateAndParseUpdate(command));
        assertEquals("PlatformApiDataValidationException{errors=[The parameter `locale` has an invalid language value: `invalid`.]}",
                exception.toString());
    }

    @Test
    public void validateAndParseUpdateWithInvalidBusinessType() {
        String json = "{\"type\":\"invalid\", \"locale\":\"hu\",\"dateFormat\":\"yyyy-MM-dd\",\"date\":\"2022-06-11\"}";
        JsonCommand command = JsonCommand.from(json);
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> businessDateDataParserAndValidator.validateAndParseUpdate(command));
        assertEquals("PlatformApiDataValidationException{errors=[Failed data validation due to: Invalid Business Type value: `invalid`.]}",
                exception.toString());
    }

    @Test
    public void validateAndParseUpdateWithInvalidDateFormat() {
        String json = "{\"type\":\"BUSINESS_DATE\", \"locale\":\"hu\",\"dateFormat\":\"y2yyy-MM-dd\",\"date\":\"2022-06-11\"}";
        JsonCommand command = JsonCommand.from(json);
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> businessDateDataParserAndValidator.validateAndParseUpdate(command));
        assertEquals(
                "PlatformApiDataValidationException{errors=[The parameter `date` (value=2022-06-11) is invalid based on the dateFormat: `y2yyy-MM-dd` and locale: `hu` provided.]}",
                exception.toString());
    }

    @Test
    public void validateAndParseUpdateWithInvalidDate() {
        String json = "{\"type\":\"BUSINESS_DATE\", \"locale\":\"hu\",\"dateFormat\":\"yyyy-MM-dd\",\"date\":\"2y22-06-11\"}";
        JsonCommand command = JsonCommand.from(json);
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> businessDateDataParserAndValidator.validateAndParseUpdate(command));
        assertEquals(
                "PlatformApiDataValidationException{errors=[The parameter `date` (value=2y22-06-11) is invalid based on the dateFormat: `yyyy-MM-dd` and locale: `hu` provided.]}",
                exception.toString());
    }

    @Test
    public void validateAndParseUpdateWithWrongDate() {
        String json = "{\"type\":\"BUSINESS_DATE\", \"locale\":\"hu\",\"dateFormat\":\"yyyy-MM-dd\",\"date\":\"11-06-2022\"}";
        JsonCommand command = JsonCommand.from(json);
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> businessDateDataParserAndValidator.validateAndParseUpdate(command));
        assertEquals(
                "PlatformApiDataValidationException{errors=[The parameter `date` (value=11-06-2022) is invalid based on the dateFormat: `yyyy-MM-dd` and locale: `hu` provided.]}",
                exception.toString());
    }

    @Test
    public void validateAndParseUpdateWithRightDate() {
        String json = "{\"type\":\"COB_DATE\", \"locale\":\"hu\",\"dateFormat\":\"yyyy-MM-dd\",\"date\":\"2022-06-11\"}";
        JsonCommand command = JsonCommand.from(json);
        BusinessDateData result = businessDateDataParserAndValidator.validateAndParseUpdate(command);
        assertEquals("COB_DATE", result.getType());
        assertEquals("Close of Business Date", result.getDescription());
        assertEquals(LocalDate.of(2022, 6, 11), result.getDate());
    }
}
