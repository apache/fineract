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
import org.junit.Test;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
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
		tms = new TemplateMergeService(new FromJsonHelper());
	}
	
	@Test
	public void compileHelloTemplate() throws Exception {
		String name = "TemplateName";
		String text = "Hello Test for Template {{template.name}}!";
		
		template = new Template(name, text, null, null, null);
		
		HashMap<String, Object> scopes = new HashMap<String, Object>();
	    scopes.put("template", template);
		
		String output ="";
		output = tms.compile(template, scopes);
		assertEquals("Hello Test for Template TemplateName!", output);
	}
	
	
	//TODO:ASSERT!!
	@Test
	public void compileLoanSummary() throws IOException{
		
		LocalDate july2nd = new LocalDate(2012, 7, 2);
		MonetaryCurrency usDollars = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
		List<LoanRepaymentScheduleInstallment> installments
			= LoanScheduleTestDataHelper.createSimpleLoanSchedule(july2nd, usDollars);
		
		File file = new File(TEST_FILE);
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
        byte[] bytes = new byte[(int) file.length()];
        dis.readFully(bytes);
        String content = new String(bytes, "UTF-8");
		
		template = new Template("TemplateName", content, null, null, null);
		
		HashMap<String, Object> scopes = new HashMap<String, Object>();
	    scopes.put("installments", installments);
		
		String output = tms.compile(template, scopes);
		
		dis.close();
		
		System.out.println(output);
		dis.close();
	}

}
