package org.mifosng.platform.loan.domain;

public class DailyEquivalentInterestRebateCalculatorFactory implements
		InterestRebateCalculatorFactory {

	@Override
	public InterestRebateCalculator createCalcualtor(
			final InterestMethod loanRepaymentScheduleMethod, final AmortizationMethod amortizationMethod) {

		InterestRebateCalculator interestRebateCalculator = null;

		switch (loanRepaymentScheduleMethod) {
		case DECLINING_BALANCE:
			switch (amortizationMethod) {
			case EQUAL_INSTALLMENTS:
				interestRebateCalculator = new DailyEquivalentDecliningBalanceInterestRebateCalculator();
				break;
			default:
				interestRebateCalculator = new DailyEquivalentDecliningBalanceInterestRebateCalculator();
				break;
			}
			break;
		case FLAT:
			interestRebateCalculator = new DailyEquivalentFlatInterestRebateCalculator();
			break;
		default:
			interestRebateCalculator = new DailyEquivalentFlatInterestRebateCalculator();
			break;
		}

		return interestRebateCalculator;
	}

}
