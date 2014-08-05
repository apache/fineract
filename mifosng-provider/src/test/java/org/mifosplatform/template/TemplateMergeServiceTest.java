/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.template;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanaccount.LoanScheduleTestDataHelper;
import org.mifosplatform.portfolio.loanaccount.MonetaryCurrencyBuilder;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.template.domain.Template;
import org.mifosplatform.template.service.TemplateMergeService;

public class TemplateMergeServiceTest {

    private Template template;
    private final static String TEST_FILE = "src/test/resources/template.mustache";
    private static TemplateMergeService tms;

    @BeforeClass
    public static void init() {
        tms = new TemplateMergeService(
//                new FromJsonHelper()
                );
    }

    @Ignore
    @Test
    public void compileHelloTemplate() throws Exception {
        final String name = "TemplateName";
        final String text = "Hello Test for Template {{template.name}}!";

        this.template = new Template(name, text, null, null, null);

        final HashMap<String, Object> scopes = new HashMap<>();
        scopes.put("template", this.template);

        String output = "";
        output = tms.compile(this.template, scopes);
        assertEquals("Hello Test for Template TemplateName!", output);
    }

    @Ignore
    @Test
    public void compileLoanSummary() throws IOException {

        final LocalDate july2nd = new LocalDate(2012, 7, 2);
        final MonetaryCurrency usDollars = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
        final List<LoanRepaymentScheduleInstallment> installments = LoanScheduleTestDataHelper.createSimpleLoanSchedule(july2nd, usDollars);

        final File file = new File(TEST_FILE);
        final DataInputStream dis = new DataInputStream(new FileInputStream(file));
        final byte[] bytes = new byte[(int) file.length()];
        dis.readFully(bytes);
        final String content = new String(bytes, "UTF-8");

        this.template = new Template("TemplateName", content, null, null, null);

        final HashMap<String, Object> scopes = new HashMap<>();
        scopes.put("installments", installments);

        final String output = tms.compile(this.template, scopes);

        dis.close();

        System.out.println(output);
        dis.close();
    }

}
