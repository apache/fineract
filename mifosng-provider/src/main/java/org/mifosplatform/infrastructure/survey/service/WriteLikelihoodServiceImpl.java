/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.survey.data.LikelihoodDataValidator;
import org.mifosplatform.infrastructure.survey.domain.Likelihood;
import org.mifosplatform.infrastructure.survey.domain.LikelihoodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Created by Cieyou on 3/12/14.
 */
@Service
public class WriteLikelihoodServiceImpl implements WriteLikelihoodService {

    private final static Logger logger = LoggerFactory.getLogger(PovertyLineService.class);
    private final PlatformSecurityContext context;
    private final LikelihoodDataValidator likelihoodDataValidator;
    private final LikelihoodRepository repository;

    @Autowired
    WriteLikelihoodServiceImpl(final PlatformSecurityContext context, final LikelihoodDataValidator likelihoodDataValidator,
            final LikelihoodRepository repository) {
        this.context = context;
        this.likelihoodDataValidator = likelihoodDataValidator;
        this.repository = repository;

    }

    @Override
    public CommandProcessingResult update(Long likelihoodId, JsonCommand command) {

        this.context.authenticatedUser();

        try {

            this.likelihoodDataValidator.validateForUpdate(command);

            final Likelihood likelihood = this.repository.findOne(likelihoodId);

            if (!likelihood.update(command).isEmpty()) {
                this.repository.save(likelihood);

                if (likelihood.isActivateCommand(command)) {
                    List<Likelihood> likelihoods = this.repository
                            .findByPpiNameAndLikeliHoodId(likelihood.getPpiName(), likelihood.getId());

                    for (Likelihood aLikelihood : likelihoods) {
                        aLikelihood.disable();
                    }
                    this.repository.save(likelihoods);
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
