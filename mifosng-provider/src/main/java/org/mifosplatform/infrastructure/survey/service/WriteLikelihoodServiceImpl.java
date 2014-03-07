package org.mifosplatform.infrastructure.survey.service;

import com.google.gson.JsonElement;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.survey.data.LikelihoodDataValidator;
import org.mifosplatform.infrastructure.survey.data.LikelihoodStatus;
import org.mifosplatform.infrastructure.survey.domain.Likelihood;
import org.mifosplatform.infrastructure.survey.domain.LikelihoodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * Created by Cieyou on 3/12/14.
 */
@Service
public class WriteLikelihoodServiceImpl implements WriteLikelihoodService{

    private final static Logger logger = LoggerFactory.getLogger(PovertyLineService.class);
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final PlatformSecurityContext context;
    private final LikelihoodDataValidator likelihoodDataValidator;
    private final LikelihoodRepository repository;

    @Autowired
    WriteLikelihoodServiceImpl (final RoutingDataSource dataSource,
                                final PlatformSecurityContext context,
                                final LikelihoodDataValidator likelihoodDataValidator,
                                final LikelihoodRepository repository){
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.context = context;
        this.likelihoodDataValidator = likelihoodDataValidator;
        this.repository = repository;

    }

    @Override
    public CommandProcessingResult update(Long likelihoodId, JsonCommand command)
    {

        this.context.authenticatedUser();

        try {

            this.likelihoodDataValidator.validateForUpdate(command);

            final Likelihood likelihood = this.repository.findOne(likelihoodId);



            String disableLikelihoodSql = "update ppi_likelihoods_ppi set enabled = "+ LikelihoodStatus.DISABLED
                        + " WHERE ppi_likelihoods_ppi.id IN "
                        + " ( SELECT t.id FROM ( SELECT * FROM ppi_likelihoods_ppi ) as t " +
                                     " WHERE t.ppi_name = '" + likelihood.getPpiName()
                                   + "' AND t.id !=" +likelihood.getId()+ " ) ";


            if (!likelihood.update(command).isEmpty()) {
                this.repository.save(likelihood);

                if(likelihood.isActivateCommand(command))
                {
                    // disable the other likelihood
                    jdbcTemplate.execute(disableLikelihoodSql);
                }

            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(likelihood.getId()).build();

        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }

    }


    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.likelihood.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
