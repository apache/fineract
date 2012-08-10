package org.mifosng.platform.savingproduct.service;

import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.saving.domain.DepositAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An assembler for turning {@link DepositAccountCommand} into {@link DepositAccount}'s.
 */
@Service
public class DepositAccountAssembler {

	private final ClientRepository clientRepository;

	@Autowired
	public DepositAccountAssembler(final ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	public DepositAccount assembleFrom(final DepositAccountCommand command) {

		Client client = this.clientRepository.findOne(command.getClientId());
		if (client == null || client.isDeleted()) {
			throw new ClientNotFoundException(command.getClientId());
		} 
		
		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		Money deposit = Money.of(currency, command.getDepositAmount());
		
		return new DepositAccount(client, command.getExternalId(), deposit, command.getInterestRate(), command.getTermInMonths());
	}
}