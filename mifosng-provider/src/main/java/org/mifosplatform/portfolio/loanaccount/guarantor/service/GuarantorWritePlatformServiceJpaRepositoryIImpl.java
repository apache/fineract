package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepository;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.guarantor.GuarantorConstants.GUARANTOR_JSON_INPUT_PARAMS;
import org.mifosplatform.portfolio.loanaccount.guarantor.command.GuarantorCommand;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.Guarantor;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorRepository;
import org.mifosplatform.portfolio.loanaccount.guarantor.exception.GuarantorNotFoundException;
import org.mifosplatform.portfolio.loanaccount.guarantor.exception.InvalidGuarantorException;
import org.mifosplatform.portfolio.loanaccount.guarantor.serialization.GuarantorCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuarantorWritePlatformServiceJpaRepositoryIImpl implements GuarantorWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GuarantorWritePlatformServiceJpaRepositoryIImpl.class);

    private final ClientRepository clientRepository;
    private final StaffRepository staffRepository;
    private final LoanRepository loanRepository;
    private final GuarantorRepository guarantorRepository;
    private final GuarantorCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public GuarantorWritePlatformServiceJpaRepositoryIImpl(final LoanRepository loanRepository,
            final GuarantorRepository guarantorRepository, final ClientRepository clientRepository, final StaffRepository staffRepository,
            final GuarantorCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.loanRepository = loanRepository;
        this.clientRepository = clientRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.guarantorRepository = guarantorRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    @Transactional
    public CommandProcessingResult createGuarantor(final JsonCommand command) {
        try {
            final GuarantorCommand guarantorCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            guarantorCommand.validateForCreate();

            final Long loanId = command.getLoanId();
            Loan loan = retrieveLoanById(loanId);
            Guarantor guarantor = null;
            guarantor = Guarantor.fromJson(loan, command);

            validateGuarantorBusinessRules(guarantor);

            this.guarantorRepository.saveAndFlush(guarantor);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(guarantor.getOfficeId())
                    .withEntityId(guarantor.getId()).withLoanId(loanId).build();
        } catch (DataIntegrityViolationException dve) {
            handleGuarantorDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }

    }

    @Override
    @Transactional
    public CommandProcessingResult updateGuarantor(final Long guarantorId, final JsonCommand command) {
        try {
            final GuarantorCommand guarantorCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            guarantorCommand.validateForUpdate();

            // TODO: Vishwas need to fetch by both loan and Guarantor Id
            final Guarantor guarantorForUpdate = this.guarantorRepository.findOne(guarantorId);
            if (guarantorForUpdate == null) { throw new GuarantorNotFoundException(guarantorId); }

            final Map<String, Object> changesOnly = guarantorForUpdate.update(command);

            if (changesOnly.containsKey(GUARANTOR_JSON_INPUT_PARAMS.ENTITY_ID)
                    || changesOnly.containsKey(GUARANTOR_JSON_INPUT_PARAMS.GUARANTOR_TYPE_ID)) {
                validateGuarantorBusinessRules(guarantorForUpdate);
            }

            if (!changesOnly.isEmpty()) {
                this.guarantorRepository.save(guarantorForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(guarantorForUpdate.getOfficeId())
                    .withEntityId(guarantorForUpdate.getId()).withOfficeId(guarantorForUpdate.getLoanId()).with(changesOnly).build();
        } catch (DataIntegrityViolationException dve) {
            handleGuarantorDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult removeGuarantor(final Long guarantorId) {
        // TODO: Vishwas need to fetch by both loan and Guarantor Id
        final Guarantor guarantorForDelete = this.guarantorRepository.findOne(guarantorId);
        if (guarantorForDelete == null) { throw new GuarantorNotFoundException(guarantorId); }

        this.guarantorRepository.delete(guarantorForDelete);

        return new CommandProcessingResultBuilder().withEntityId(guarantorId).withLoanId(guarantorForDelete.getLoanId())
                .withOfficeId(guarantorForDelete.getOfficeId()).build();
    }

    /**
     * @param loanId
     * @param loan
     * @param guarantor
     */
    private void validateGuarantorBusinessRules(Guarantor guarantor) {
        // validate guarantor conditions
        if (guarantor.isExistingCustomer()) {
            // check client exists
            Client client = clientRepository.findOne(guarantor.getEntityId());
            if (client == null) { throw new ClientNotFoundException(guarantor.getEntityId()); }

            // validate that the client is not set as a self guarantor
            if (guarantor.getClientId().equals(guarantor.getEntityId())) { throw new InvalidGuarantorException(guarantor.getEntityId(),
                    guarantor.getLoanId()); }
        } else if (guarantor.isExistingEmployee()) {
            Staff staff = staffRepository.findOne(guarantor.getEntityId());
            if (staff == null) { throw new StaffNotFoundException(guarantor.getEntityId()); }
        }
    }

    private Loan retrieveLoanById(final Long loanId) {
        Loan loan = loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        return loan;
    }

    /**
     * Throws an exception for any data Integrity issue
     * 
     * @param command
     * @param dve
     */
    @SuppressWarnings("unused")
    private void handleGuarantorDataIntegrityIssues(final JsonCommand command, DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.guarantor.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource Guarantor: " + realCause.getMessage());
    }

}