package org.mifosng.platform.savingproduct.service;

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
		
		// currency details inherited from product setting
		Money deposit = Money.of(product.getCurrency(), command.getDepositAmount());
		
		// default to months for now.
		Integer interestCompoundingPeriodType = PeriodFrequencyType.MONTHS.getValue();
		if (command.getInterestCompoundedEveryPeriodType() != null) {
			interestCompoundingPeriodType = command.getInterestCompoundedEveryPeriodType();
		}
		PeriodFrequencyType compoundingInterestFrequency = PeriodFrequencyType.fromInt(interestCompoundingPeriodType);
		
//		boolean renewalAllowed = product.isr
		
		DepositAccount account = DepositAccount.openNew(client, product, command.getExternalId(), deposit, command.getMaturityInterestRate(), 
				command.getTermInMonths(), command.getInterestCompoundedEvery(), compoundingInterestFrequency, command.getCommencementDate());
		
		return account;
	}
}