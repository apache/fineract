/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.template;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanaccount.LoanScheduleTestDataHelper;
import org.mifosplatform.portfolio.loanaccount.MonetaryCurrencyBuilder;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.template.domain.Template;
import org.mifosplatform.template.domain.TemplateMapper;
import org.mifosplatform.template.service.TemplateMergeService;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class TemplateMergeServiceTest {

    private TemplateMergeService tms = new TemplateMergeService();

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
        assertEquals(expectedOutput, output);
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
