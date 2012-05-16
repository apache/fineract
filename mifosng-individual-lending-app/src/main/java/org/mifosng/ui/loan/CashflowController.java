package org.mifosng.ui.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.mifosng.data.CurrencyData;
import org.mifosng.data.MoneyData;
import org.mifosng.ui.CommonRestOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @deprecated - some initial work around cashflow for CreoCore that can be deleted later.
 */
@Deprecated
@Controller
public class CashflowController {

//	private final CommonRestOperations commonRestOperations;

	@Autowired
	public CashflowController(final CommonRestOperations commonRestOperations) {
//		this.commonRestOperations = commonRestOperations;
	}

	@RequestMapping(value = "/portfolio/client/{clientId}/cashflow/new", method = RequestMethod.GET)
	public String submitNewCashflowAnalysis(@PathVariable("clientId") final Long clientId) {
		return "redirect:/createCashflowAnalysis?loanId=0&clientId={clientId}";
	}

	// called from webflow
	public void populateLoanDetailsForCashflow(final CashflowFormBean formBean, Long loanId, Long clientId) {
		
		formBean.setBusinessName("Fixed Example Business (Sole Trader)");
		formBean.setClientName("Fixed Example Client");
		
		CurrencyData currency = new CurrencyData("XOF", "", 0, "CFA", "currency.XOF");
		MoneyData amount = MoneyData.of(currency, BigDecimal.valueOf(0));
		
		formBean.setExpectedLoanTerm(12);
		
		List<MultiTermExpense> investments = new ArrayList<MultiTermExpense>();
		investments.add(new MultiTermExpense("equipment purchase", Integer.valueOf(0), amount.getAmount(), formBean.getExpectedLoanTerm()));
		investments.add(new MultiTermExpense("Option two", Integer.valueOf(0), amount.getAmount(), formBean.getExpectedLoanTerm()));
		investments.add(new MultiTermExpense("Option three", Integer.valueOf(0), amount.getAmount(), formBean.getExpectedLoanTerm()));
		
		formBean.setInvestments(investments);
		
		List<OneOffExpense> fixedExpenses = new ArrayList<OneOffExpense>();
		fixedExpenses.add(new OneOffExpense("Fixed one", amount.getAmount()));
		fixedExpenses.add(new OneOffExpense("Fixed two", amount.getAmount()));
		fixedExpenses.add(new OneOffExpense("Fixed three", amount.getAmount()));
		
		formBean.setFixedExpenses(fixedExpenses);
		
		List<UnitInformationOfProduct> products = new ArrayList<UnitInformationOfProduct>();
		products.add(new UnitInformationOfProduct("First Product", Integer.valueOf(10), BigDecimal.valueOf(Double.valueOf("12.00")), BigDecimal.valueOf(Double.valueOf("23.00"))));
		products.add(new UnitInformationOfProduct("Second Product", Integer.valueOf(500), BigDecimal.valueOf(Double.valueOf("10.00")), BigDecimal.valueOf(Double.valueOf("15.00"))));
		
		formBean.setProducts(products);
//		List<ClientReadModel> individualClients = new ArrayList<ClientReadModel>(this.commonRestOperations.retrieveLoan);

//		List<LoanProductReadModel> loanProducts = new ArrayList<LoanProductReadModel>(
//				this.commonRestOperations.retrieveAllLoanProducts());
//
//		loanFormBean.setApplicantOptions(individualClients);
//		if (individualClients.size() == 1) {
//			loanFormBean.setSelectedApplicantOption(individualClients.get(0)
//					.getId());
//		}
//
//		loanFormBean.setLoanProductOptions(loanProducts);
//		if (loanProducts.size() == 1) {
//			loanFormBean.setSelectedLoanProductOption(loanProducts.get(0)
//					.getId());
//		}
	}
}