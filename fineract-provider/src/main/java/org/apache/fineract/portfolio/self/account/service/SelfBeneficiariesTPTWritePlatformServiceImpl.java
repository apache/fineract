/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.self.account.service;

import static org.apache.fineract.portfolio.self.account.api.SelfBeneficiariesTPTApiConstants.ACCOUNT_NUMBER_PARAM_NAME;
import static org.apache.fineract.portfolio.self.account.api.SelfBeneficiariesTPTApiConstants.ACCOUNT_TYPE_PARAM_NAME;
import static org.apache.fineract.portfolio.self.account.api.SelfBeneficiariesTPTApiConstants.NAME_PARAM_NAME;
import static org.apache.fineract.portfolio.self.account.api.SelfBeneficiariesTPTApiConstants.OFFICE_NAME_PARAM_NAME;
import static org.apache.fineract.portfolio.self.account.api.SelfBeneficiariesTPTApiConstants.TRANSFER_LIMIT_PARAM_NAME;

import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.self.account.data.SelfBeneficiariesTPTDataValidator;
import org.apache.fineract.portfolio.self.account.domain.SelfBeneficiariesTPT;
import org.apache.fineract.portfolio.self.account.domain.SelfBeneficiariesTPTRepository;
import org.apache.fineract.portfolio.self.account.exception.InvalidAccountInformationException;
import org.apache.fineract.portfolio.self.account.exception.InvalidBeneficiaryException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SelfBeneficiariesTPTWritePlatformServiceImpl implements
		SelfBeneficiariesTPTWritePlatformService {

	private final Logger logger;
	private final PlatformSecurityContext context;
	private final SelfBeneficiariesTPTRepository repository;
	private final SelfBeneficiariesTPTDataValidator validator;
	private final LoanRepositoryWrapper loanRepositoryWrapper;
	private final SavingsAccountRepositoryWrapper savingRepositoryWrapper;

	@Autowired
	public SelfBeneficiariesTPTWritePlatformServiceImpl(
			final PlatformSecurityContext context,
			final SelfBeneficiariesTPTRepository repository,
			final SelfBeneficiariesTPTDataValidator validator,
			final LoanRepositoryWrapper loanRepositoryWrapper,
			final SavingsAccountRepositoryWrapper savingRepositoryWrapper) {
		this.context = context;
		this.repository = repository;
		this.validator = validator;
		this.loanRepositoryWrapper = loanRepositoryWrapper;
		this.savingRepositoryWrapper = savingRepositoryWrapper;
		this.logger = LoggerFactory
				.getLogger(SelfBeneficiariesTPTWritePlatformServiceImpl.class);
	}

	@Transactional
	@Override
	public CommandProcessingResult add(JsonCommand command) {
		HashMap<String, Object> params = this.validator
				.validateForCreate(command.json());

		String name = (String) params.get(NAME_PARAM_NAME);
		Integer accountType = (Integer) params.get(ACCOUNT_TYPE_PARAM_NAME);
		String accountNumber = (String) params.get(ACCOUNT_NUMBER_PARAM_NAME);
		String officeName = (String) params.get(OFFICE_NAME_PARAM_NAME);
		Long transferLimit = (Long) params.get(TRANSFER_LIMIT_PARAM_NAME);

		Long accountId = null;
		Long clientId = null;
		Long officeId = null;

		boolean validAccountDetails = true;
		if (accountType.equals(PortfolioAccountType.LOAN)) {
			Loan loan = this.loanRepositoryWrapper.findNonClosedLoanByAccountNumber(accountNumber);
			if (loan != null && loan.getClientId() != null
					&& loan.getOffice().getName().equals(officeName)) {
				accountId = loan.getId();
				officeId = loan.getOfficeId();
				clientId = loan.getClientId();
			} else {
				validAccountDetails = false;
			}
		} else {
			SavingsAccount savings = this.savingRepositoryWrapper
					.findNonClosedAccountByAccountNumber(accountNumber);
			if (savings != null
					&& savings.getClient() != null
					&& savings.getClient().getOffice().getName()
							.equals(officeName)) {
				accountId = savings.getId();
				clientId = savings.getClient().getId();
				officeId = savings.getClient().getOffice().getId();
			} else {
				validAccountDetails = false;
			}
		}

		if (validAccountDetails) {
			try {
				AppUser user = this.context.authenticatedUser();
				SelfBeneficiariesTPT beneficiary = new SelfBeneficiariesTPT(
						user.getId(), name, officeId, clientId, accountId,
						accountType, transferLimit);
				this.repository.save(beneficiary);
				return new CommandProcessingResultBuilder().withEntityId(
						beneficiary.getId()).build();
			} catch (DataAccessException dae) {
				handleDataIntegrityIssues(command, dae);
			}
		}
		throw new InvalidAccountInformationException(officeName, accountNumber,
				PortfolioAccountType.fromInt(accountType).getCode());

	}

	@Transactional
	@Override
	public CommandProcessingResult update(JsonCommand command) {
		HashMap<String, Object> params = this.validator
				.validateForUpdate(command.json());
		AppUser user = this.context.authenticatedUser();
		Long beneficiaryId = command.entityId();
		SelfBeneficiariesTPT beneficiary = this.repository
				.findOne(beneficiaryId);
		if (beneficiary != null
				&& beneficiary.getAppUserId().equals(user.getId())) {
			String name = (String) params.get(NAME_PARAM_NAME);
			Long transferLimit = (Long) params.get(TRANSFER_LIMIT_PARAM_NAME);

			Map<String, Object> changes = beneficiary.update(name,
					transferLimit);
			if (!changes.isEmpty()) {
				try {
					this.repository.save(beneficiary);

					return new CommandProcessingResultBuilder() //
							.withEntityId(beneficiary.getId()) //
							.with(changes).build();
				} catch (DataAccessException dae) {
					handleDataIntegrityIssues(command, dae);
				}

			}
		}
		throw new InvalidBeneficiaryException(beneficiaryId);
	}

	@Transactional
	@Override
	public CommandProcessingResult delete(JsonCommand command) {
		AppUser user = this.context.authenticatedUser();
		Long beneficiaryId = command.entityId();
		SelfBeneficiariesTPT beneficiary = this.repository
				.findOne(beneficiaryId);
		if (beneficiary != null
				&& beneficiary.getAppUserId().equals(user.getId())) {

			beneficiary.setActive(false);
			this.repository.save(beneficiary);

			return new CommandProcessingResultBuilder() //
					.withEntityId(beneficiary.getId()) //
					.build();
		}
		throw new InvalidBeneficiaryException(beneficiaryId);
	}

	private void handleDataIntegrityIssues(final JsonCommand command,
			final DataAccessException dae) {

		final Throwable realCause = dae.getMostSpecificCause();
		if (realCause.getMessage().contains("name")) {

			final String name = command
					.stringValueOfParameterNamed(NAME_PARAM_NAME);
			throw new PlatformDataIntegrityException(
					"error.msg.beneficiary.duplicate.name",
					"Beneficiary with name `" + name + "` already exists",
					NAME_PARAM_NAME, name);
		}

		this.logger.error(dae.getMessage(), dae);
		throw new PlatformDataIntegrityException(
				"error.msg.beneficiary.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource.");
	}
}
