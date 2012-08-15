package org.mifosng.platform.savingproduct.service;

import java.math.BigDecimal;

import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.deposit.domain.DepositProduct;
import org.mifosng.platform.deposit.domain.DepositProductRepository;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.exceptions.DepositProductNotFoundException;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.saving.domain.DepositAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An assembler for turning {@link DepositAccountCommand} into {@link DepositAccount}'s.
 */
@Service
public class DepositAccountAssembler {

	private final ClientRepository clientRepository;
	private final DepositProductRepository depositProductRepository;

	@Autowired
	public DepositAccountAssembler(final ClientRepository clientRepository, final DepositProductRepository depositProductRepository) {
		this.clientRepository = clientRepository;
		this.depositProductRepository = depositProductRepository;
	}

	public DepositAccount assembleFrom(final DepositAccountCommand command) {

		Client client = this.clientRepository.findOne(command.getClientId());
		if (client == null || client.isDeleted()) {
			throw new ClientNotFoundException(command.getClientId());
		}
		
		DepositProduct product = this.depositProductRepository.findOne(command.getProductId());
		if (product == null || product.isDeleted()) {
			throw new DepositProductNotFoundException(command.getProductId());
		} 
		
		// details inherited from product setting (unless allowed to be overridden through account creation api
		Money deposit = Money.of(product.getCurrency(), command.getDepositAmount());
		
		Integer tenureInMonths = product.getTenureInMonths();
		if (command.getTenureInMonths() != null) {
			tenureInMonths = command.getTenureInMonths();
		}
		
		BigDecimal maturityInterestRate = product.getMaturityDefaultInterestRate();
		if (command.getMaturityInterestRate() != null) {
			maturityInterestRate = command.getMaturityInterestRate();
		}
			
		Integer compoundingInterestEvery = product.getInterestCompoundedEvery();
		if (command.getInterestCompoundedEvery() != null) {
			compoundingInterestEvery = command.getInterestCompoundedEvery();
		}
		
		PeriodFrequencyType compoundingInterestFrequency = product.getInterestCompoundedEveryPeriodType();
		if (command.getInterestCompoundedEveryPeriodType() != null) {
			compoundingInterestFrequency = PeriodFrequencyType.fromInt(command.getInterestCompoundedEveryPeriodType());
		}
		
		boolean renewalAllowed = product.isRenewalAllowed();
		if (command.isRenewalAllowedChanged()) {
			renewalAllowed = command.isRenewalAllowed();
		}
		
		boolean preClosureAllowed = product.isPreClosureAllowed();
		if (command.isPreClosureAllowedChanged()) {
			preClosureAllowed = command.isPreClosureAllowed();
		}
		// end of details allowed to be overriden from product
		
		DepositAccount account = DepositAccount.openNew(client, product, command.getExternalId(), 
				deposit, 
				maturityInterestRate, 
				tenureInMonths, compoundingInterestEvery, compoundingInterestFrequency, 
				command.getCommencementDate(), 
				renewalAllowed, preClosureAllowed);
		
		return account;
	}
}