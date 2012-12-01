package org.mifosng.platform.exceptions;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DepositAccounDataValidationtException extends
		AbstractPlatformDomainRuleException {

	public DepositAccounDataValidationtException(BigDecimal preClosureInterestRate, BigDecimal maturityMinInterestRate) {
		super("error.msg.deposit.account.preClosureInterestRate.should.lessthanOrEqualTo.maturityMinInterestRate", "PreClosureInterestRate : "+preClosureInterestRate+"should lessthan or equal to maturityMinInterestRate : "+maturityMinInterestRate, preClosureInterestRate,maturityMinInterestRate);
	}

}
