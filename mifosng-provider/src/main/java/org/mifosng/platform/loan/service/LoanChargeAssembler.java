package org.mifosng.platform.loan.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.charge.domain.Charge;
import org.mifosng.platform.charge.domain.ChargeRepository;
import org.mifosng.platform.loan.domain.LoanCharge;
import org.mifosng.platform.loan.domain.LoanChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanChargeAssembler {

	private ChargeRepository chargeRepository;
	private final LoanChargeRepository loanChargeRepository;

	@Autowired
	public LoanChargeAssembler(final ChargeRepository chargeRepository, final LoanChargeRepository loanChargeRepository) {
		this.chargeRepository = chargeRepository;
		this.loanChargeRepository = loanChargeRepository;
	}
	
	public Set<LoanCharge> assembleFrom(final LoanChargeCommand[] charges, final BigDecimal loanPrincipal) {
		
		final Set<LoanCharge> loanCharges = new HashSet<LoanCharge>();
		
		if (charges != null) {
			for (LoanChargeCommand loanChargeCommand : charges) {
				
				if (loanChargeCommand.getId() == null) {
					final Charge chargeDefinition = this.chargeRepository.findOne(loanChargeCommand.getChargeId());
					final LoanCharge loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition, loanChargeCommand, loanPrincipal);
					loanCharges.add(loanCharge);	
				} else {
					final LoanCharge loanCharge = this.loanChargeRepository.findOne(loanChargeCommand.getId());
					loanCharges.add(loanCharge);	
				}
			}
		}
		
		return loanCharges;
	}
}