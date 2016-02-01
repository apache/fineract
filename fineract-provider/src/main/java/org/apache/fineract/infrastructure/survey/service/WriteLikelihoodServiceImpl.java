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
package org.apache.fineract.infrastructure.survey.service;

import java.util.List;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.survey.data.LikelihoodDataValidator;
import org.apache.fineract.infrastructure.survey.domain.Likelihood;
import org.apache.fineract.infrastructure.survey.domain.LikelihoodRepository;
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
