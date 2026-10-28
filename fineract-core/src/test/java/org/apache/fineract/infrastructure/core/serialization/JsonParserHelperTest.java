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
package org.apache.fineract.infrastructure.core.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.time.MonthDay;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonParserHelperTest {

    static final JsonParserHelper onTestUnit = new JsonParserHelper();

    static final String testDataHasValues = "{\n" + "  \"integerNumber\": 10,\n" + "  \"doubleNumber\": 3.14,\n"
            + "  \"string\": \"example\",\n" + "  \"arrayList\": [1, \"two\", 3.0],\n" + "  \"localDate\": \"2024-08-10\",\n"
            + "  \"keyValue\": {\n" + "    \"key\": \"sampleKey\",\n" + "    \"value\": \"sampleValue\"\n" + "  }\n" + "}";

    static final String testDataValuesEmpty = "{\n" + "  \"integerNumber\": null,\n" + "  \"doubleNumber\": 0.0,\n"
            + "  \"string\": \"\",\n" + "  \"arrayList\": [],\n" + "  \"localDate\": null,\n" + "  \"keyValue\": {\n"
            + "    \"key\": \"\",\n" + "    \"value\": null\n" + "  }\n" + "}";

    static final String testDataValuesMissing = "{}";

    @Test
    void parameterExists() {
        JsonElement jsonHasValues = JsonParser.parseString(testDataHasValues);
        JsonElement jsonValuesEmpty = JsonParser.parseString(testDataValuesEmpty);
        JsonElement jsonValuesMissing = JsonParser.parseString(testDataValuesMissing);

        Assertions.assertTrue(onTestUnit.parameterExists("integerNumber", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterExists("doubleNumber", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterExists("string", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterExists("arrayList", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterExists("localDate", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterExists("keyValue", jsonHasValues));

        Assertions.assertTrue(onTestUnit.parameterExists("integerNumber", jsonValuesEmpty));
        Assertions.assertTrue(onTestUnit.parameterExists("doubleNumber", jsonValuesEmpty));
        Assertions.assertTrue(onTestUnit.parameterExists("string", jsonValuesEmpty));
        Assertions.assertTrue(onTestUnit.parameterExists("arrayList", jsonValuesEmpty));
        Assertions.assertTrue(onTestUnit.parameterExists("localDate", jsonValuesEmpty));
        Assertions.assertTrue(onTestUnit.parameterExists("keyValue", jsonValuesEmpty));

        Assertions.assertFalse(onTestUnit.parameterExists("integerNumber", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterExists("doubleNumber", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterExists("string", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterExists("arrayList", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterExists("localDate", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterExists("keyValue", jsonValuesMissing));
    }

    @Test
    void parameterHasValue() {
        JsonElement jsonHasValues = JsonParser.parseString(testDataHasValues);
        JsonElement jsonValuesEmpty = JsonParser.parseString(testDataValuesEmpty);
        JsonElement jsonValuesMissing = JsonParser.parseString(testDataValuesMissing);

        Assertions.assertTrue(onTestUnit.parameterHasValue("integerNumber", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterHasValue("doubleNumber", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterHasValue("string", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterHasValue("arrayList", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterHasValue("localDate", jsonHasValues));
        Assertions.assertTrue(onTestUnit.parameterHasValue("keyValue", jsonHasValues));

        Assertions.assertFalse(onTestUnit.parameterHasValue("integerNumber", jsonValuesEmpty));
        Assertions.assertTrue(onTestUnit.parameterHasValue("doubleNumber", jsonValuesEmpty));
        Assertions.assertFalse(onTestUnit.parameterHasValue("string", jsonValuesEmpty));
        Assertions.assertFalse(onTestUnit.parameterHasValue("arrayList", jsonValuesEmpty));
        Assertions.assertFalse(onTestUnit.parameterHasValue("localDate", jsonValuesEmpty));
        Assertions.assertTrue(onTestUnit.parameterHasValue("keyValue", jsonValuesEmpty));

        Assertions.assertFalse(onTestUnit.parameterHasValue("integerNumber", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterHasValue("doubleNumber", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterHasValue("string", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterHasValue("arrayList", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterHasValue("localDate", jsonValuesMissing));
        Assertions.assertFalse(onTestUnit.parameterHasValue("keyValue", jsonValuesMissing));
    }

    @Test
    void extractMonthDayNamedParsesWithMonthDayFormatAndLocale() {
        JsonElement json = JsonParser.parseString("{\"feeOnMonthDay\": \"15 March\", \"monthDayFormat\": \"dd MMMM\", \"locale\": \"en\"}");

        Assertions.assertEquals(MonthDay.of(3, 15), onTestUnit.extractMonthDayNamed("feeOnMonthDay", json));
    }

    @Test
    void extractMonthDayNamedRejectsMissingMonthDayFormat() {
        JsonElement json = JsonParser.parseString("{\"feeOnMonthDay\": \"15 March\", \"locale\": \"en\"}");

        List<ApiParameterError> errors = Assertions
                .assertThrows(PlatformApiDataValidationException.class, () -> onTestUnit.extractMonthDayNamed("feeOnMonthDay", json))
                .getErrors();

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("validation.msg.missing.monthDayFormat.parameter", errors.get(0).getUserMessageGlobalisationCode());
        Assertions.assertEquals("feeOnMonthDay", errors.get(0).getParameterName());
    }

    @Test
    void extractMonthDayNamedRejectsMissingLocale() {
        JsonElement json = JsonParser.parseString("{\"feeOnMonthDay\": \"15 March\", \"monthDayFormat\": \"dd MMMM\"}");

        List<ApiParameterError> errors = Assertions
                .assertThrows(PlatformApiDataValidationException.class, () -> onTestUnit.extractMonthDayNamed("feeOnMonthDay", json))
                .getErrors();

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("validation.msg.missing.locale.parameter", errors.get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void extractMonthDayNamedRejectsValueNotMatchingMonthDayFormat() {
        JsonElement json = JsonParser.parseString("{\"feeOnMonthDay\": \"14 Oc\", \"monthDayFormat\": \"dd MMMM\", \"locale\": \"en\"}");

        List<ApiParameterError> errors = Assertions
                .assertThrows(PlatformApiDataValidationException.class, () -> onTestUnit.extractMonthDayNamed("feeOnMonthDay", json))
                .getErrors();

        Assertions.assertEquals("validation.msg.invalid.month.day", errors.get(0).getUserMessageGlobalisationCode());
        Assertions.assertEquals("feeOnMonthDay", errors.get(0).getParameterName());
        Assertions.assertEquals("14 Oc", errors.get(0).getValue());
    }
}
