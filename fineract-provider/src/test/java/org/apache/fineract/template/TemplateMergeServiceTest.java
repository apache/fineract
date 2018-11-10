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
package org.apache.fineract.template;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.LoanScheduleTestDataHelper;
import org.apache.fineract.portfolio.loanaccount.MonetaryCurrencyBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.template.domain.Template;
import org.apache.fineract.template.domain.TemplateMapper;
import org.apache.fineract.template.service.TemplateMergeService;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class TemplateMergeServiceTest {

    private TemplateMergeService tms = new TemplateMergeService();
    
    @Before
    public void setUpForEachTestCase() throws Exception {

        Field field = MoneyHelper.class.getDeclaredField("roundingMode");
        field.setAccessible(true);
        field.set(null, RoundingMode.HALF_EVEN);
    }

    

    @Test
    public void compileHelloTemplate() throws Exception {
        String templateText = "Hello Test for Template {{file.name}}!";

        File file = new File("hello");
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("file", file);

        String output = compileTemplateText(templateText, scopes);
        assertEquals("Hello Test for Template hello!", output);
    }

    @Test
    public void compileLoanSummary() throws IOException {
        LocalDate july2nd = new LocalDate(2012, 7, 2);
        MonetaryCurrency usDollars = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
        List<LoanRepaymentScheduleInstallment> installments = LoanScheduleTestDataHelper.createSimpleLoanSchedule(july2nd, usDollars);

        Map<String, Object> scopes = new HashMap<>();
        scopes.put("installments", installments);

        String templateText = Resources.toString(Resources.getResource("template.mustache"), Charsets.UTF_8);
        String expectedOutput = Resources.toString(Resources.getResource("template-expected.html"), Charsets.UTF_8);

        String output = compileTemplateText(templateText, scopes);
        // System.out.println(output);
       // assertEquals(expectedOutput, output);
    }

    @Test
    public void arrayUsingLoop() throws Exception {
        String templateText = "Hello Test for Template{{#data.name}} {{.}}{{/data.name}}!";
        String jsonData = "{\"name\": [ \"Michael\", \"Terence\" ] }";
        String expectedOutput = "Hello Test for Template Michael Terence!";

        Map<String, Object> scopes = new HashMap<>();
        scopes.put("data", createMapFromJSON(jsonData));

        String output = compileTemplateText(templateText, scopes);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void arrayUsingIndex() throws Exception {
        String templateText = "Hello Test for Template {{data.name#1}} & {{data.name#0}}!";
        String jsonData = "{\"name\": [ \"Michael\", \"Terence\" ] }";
        String expectedOutput = "Hello Test for Template Terence & Michael!";

        Map<String, Object> scopes = new HashMap<>();
        scopes.put("data", createMapFromJSON(jsonData));

        String output = compileTemplateText(templateText, scopes);
        assertEquals(expectedOutput, output);
    }

    protected String compileTemplateText(String templateText, Map<String, Object> scope) throws MalformedURLException, IOException {
        List<TemplateMapper> mappers = new ArrayList<>();
        Template template = new Template("TemplateName", templateText, null, null, mappers);
        return tms.compile(template, scope);
    }
    
    protected Map<String, Object> createMapFromJSON(String jsonText) {
        Gson gson = new Gson();
        Type ssMap = new TypeToken<Map<String, Object>>(){}.getType();
        JsonElement json = new JsonParser().parse(jsonText);
        return gson.fromJson(json, ssMap);
    }
}
