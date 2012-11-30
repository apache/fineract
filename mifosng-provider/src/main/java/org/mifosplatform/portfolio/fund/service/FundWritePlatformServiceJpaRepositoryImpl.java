package org.mifosplatform.portfolio.fund.service;

import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.exceptions.FundNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.configuration.service.ConfigurationDomainService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.fund.command.FundCommand;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.fund.domain.FundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FundWritePlatformServiceJpaRepositoryImpl implements FundWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(FundWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final FundRepository fundRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public FundWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final FundRepository fundRepository,
            final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.fundRepository = fundRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public Long createFund(final FundCommand command) {

        try {
            context.authenticatedUser();
            command.validateForCreate();

            Fund fund = Fund.createNew(command.getName(), command.getExternalId());

            this.fundRepository.save(fund);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_FUND") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

            return fund.getId();
        } catch (DataIntegrityViolationException dve) {
            handleFundDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long updateFund(final FundCommand command) {

        try {
            context.authenticatedUser();
            command.validateForUpdate();

            final Long fundId = command.getId();
            Fund fund = this.fundRepository.findOne(fundId);
            if (fund == null) { throw new FundNotFoundException(fundId); }
            fund.update(command);

            this.fundRepository.save(fund);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_FUND") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

            return fund.getId();
        } catch (DataIntegrityViolationException dve) {
            handleFundDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleFundDataIntegrityIssues(final FundCommand command, DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("fund_externalid_org")) {
            throw new PlatformDataIntegrityException("error.msg.fund.duplicate.externalId", "A fund with external id '"
                    + command.getExternalId() + "' already exists", "externalId", command.getExternalId());
        } else if (realCause.getMessage().contains("fund_name_org")) { throw new PlatformDataIntegrityException(
                "error.msg.fund.duplicate.name", "A fund with name '" + command.getName() + "' already exists", "name", command.getName()); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.fund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}