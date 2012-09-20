package org.mifosng.platform.loan.service;

import java.util.HashSet;
import java.util.Set;

import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.charge.domain.Charge;
import org.mifosng.platform.charge.domain.ChargeRepository;
import org.mifosng.platform.loan.domain.LoanCharge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanChargeAssembler {

	private ChargeRepository chargeRepository;

	@Autowired
	public LoanChargeAssembler(final ChargeRepository chargeRepository) {
		this.chargeRepository = chargeRepository;
	}
	
	public Set<LoanCharge> assembleFrom(final LoanChargeCommand[] charges) {
		
		final Set<LoanCharge> loanCharges = new HashSet<LoanCharge>();
		
		// FIXME - KW - what if loanChargeCommands contains the actual loanCharge id, use loanChargeRepository to fetch it then.
		if (charges != null) {
			for (LoanChargeCommand loanChargeCommand : charges) {
				Charge chargeDefinition = this.chargeRepository.findOne(loanChargeCommand.getChargeId());
				LoanCharge loanCharge = LoanCharge.createNew(chargeDefinition, loanChargeCommand);
				loanCharges.add(loanCharge);
			}
		}
		
		return loanCharges;
	}
}