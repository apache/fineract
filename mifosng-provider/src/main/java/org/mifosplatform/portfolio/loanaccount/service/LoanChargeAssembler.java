package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.charge.exception.ChargeIsNotActiveException;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeNotFoundException;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanChargeAssembler {

	private final ChargeRepository chargeRepository;
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
					final Long chargeDefinitionId = loanChargeCommand.getChargeId();
					final Charge chargeDefinition = this.chargeRepository.findOne(chargeDefinitionId);
					if (chargeDefinition == null || chargeDefinition.isDeleted()) {
						throw new ChargeNotFoundException(chargeDefinitionId);
					}
					
					if (!chargeDefinition.isActive()) {
						throw new ChargeIsNotActiveException(chargeDefinitionId, chargeDefinition.getName());
					}
					final LoanCharge loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition, loanChargeCommand, loanPrincipal);
					loanCharges.add(loanCharge);	
				} else {
					final Long loanChargeId = loanChargeCommand.getId();
					final LoanCharge loanCharge = this.loanChargeRepository.findOne(loanChargeId);
					if (loanCharge == null) {
						throw new LoanChargeNotFoundException(loanChargeId);
					}
					
					loanCharge.update(loanChargeCommand, loanPrincipal);
					
					loanCharges.add(loanCharge);	
				}
			}
		}
		
		return loanCharges;
	}

	public Set<LoanCharge> assembleFrom(final LoanChargeCommand[] charges, final Set<Charge> chargesInheritedFromProduct, final BigDecimal loanPrincipal) {
		
		Set<LoanCharge> loanCharges = new HashSet<LoanCharge>();
		
		if (charges != null) {
			loanCharges = assembleFrom(charges, loanPrincipal);
		}
		// TODO - kw - dont inherit charges from product by default, user may remove charges (that were default) during creation - expect 
		//             charges to be passed along in request at time of creation
//		else {
//			for (Charge productCharge : chargesInheritedFromProduct) {
//				loanCharges.add(LoanCharge.createNew(productCharge));
//			}
//		}
		
		return loanCharges;
	}
}