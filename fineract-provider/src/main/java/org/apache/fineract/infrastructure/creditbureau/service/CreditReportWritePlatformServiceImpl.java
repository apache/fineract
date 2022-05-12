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
package org.apache.fineract.infrastructure.creditbureau.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauConfigurations;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauReportData;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauLoanProductMappingRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditReport;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditReportRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.TokenRepositoryWrapper;
import org.apache.fineract.infrastructure.creditbureau.serialization.CreditBureauTokenCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditReportWritePlatformServiceImpl implements CreditReportWritePlatformService {

    private final PlatformSecurityContext context;
    private final CreditBureauConfigurationRepositoryWrapper configDataRepository;
    private final CreditBureauRepository creditBureauRepository;
    private final CreditReportRepository creditReportRepository;
    private final ThitsaWorksCreditBureauIntegrationWritePlatformService thitsaWorksCreditBureauIntegrationWritePlatformService;
    private final ThitsaWorksCreditBureauIntegrationWritePlatformServiceImpl thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl;
    private final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
    private final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
            .resource("creditBureauIntegration");

    @Autowired
    public CreditReportWritePlatformServiceImpl(final PlatformSecurityContext context, final FromJsonHelper fromApiJsonHelper,
            final TokenRepositoryWrapper tokenRepository, final CreditBureauConfigurationRepositoryWrapper configDataRepository,
            final CreditBureauTokenCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final CreditBureauLoanProductMappingRepository loanProductMappingRepository,
            final CreditBureauRepository creditBureauRepository, final CreditReportRepository creditReportRepository,
            final ThitsaWorksCreditBureauIntegrationWritePlatformService thitsaWorksCreditBureauIntegrationWritePlatformService,
            final ThitsaWorksCreditBureauIntegrationWritePlatformServiceImpl thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl) {
        this.context = context;
        this.configDataRepository = configDataRepository;
        this.creditBureauRepository = creditBureauRepository;
        this.creditReportRepository = creditReportRepository;
        this.thitsaWorksCreditBureauIntegrationWritePlatformService = thitsaWorksCreditBureauIntegrationWritePlatformService;
        this.thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl = thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl;
    }

    @Override
    @Transactional
    public CommandProcessingResult getCreditReport(JsonCommand command) {

        try {
            Long creditBureauID = command.longValueOfParameterNamed("creditBureauID");

            Optional<String> creditBureauName = getCreditBureau(creditBureauID);

            if (creditBureauName.isEmpty()) {
                baseDataValidator.reset().failWithCode("creditBureau.has.not.been.Integrated");
                throw new PlatformApiDataValidationException("creditBureau.has.not.been.Integrated", "creditBureau.has.not.been.Integrated",
                        dataValidationErrors);
            }

            if (Objects.equals(creditBureauName.get(), CreditBureauConfigurations.THITSAWORKS.toString())) {

                CreditBureauReportData reportobj = this.thitsaWorksCreditBureauIntegrationWritePlatformService
                        .getCreditReportFromThitsaWorks(command);

                Map<String, Object> reportMap = Map.of("name", reportobj.getName(), "gender", reportobj.getGender(), "address",
                        reportobj.getAddress(), "creditScore", reportobj.getCreditScore(), "borrowerInfo", reportobj.getBorrowerInfo(),
                        "openAccounts", reportobj.getOpenAccounts(), "closedAccounts", reportobj.getClosedAccounts());

                return new CommandProcessingResultBuilder().withCreditReport(reportMap).build();
            }

            baseDataValidator.reset().failWithCode("creditBureau.has.not.been.Integrated");
            throw new PlatformApiDataValidationException("creditBureau.has.not.been.Integrated", "creditBureau.has.not.been.Integrated",
                    dataValidationErrors);

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleTokenDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleTokenDataIntegrityIssues(command, throwable, ee);
            return CommandProcessingResult.empty();
        }

    }

    @Override
    @Transactional
    public String addCreditReport(Long bureauId, File creditReport, FormDataContentDisposition fileDetail) {

        Optional<String> creditBureauName = getCreditBureau(bureauId);
        String responseMessage = null;

        if (Objects.equals(creditBureauName.get(), CreditBureauConfigurations.THITSAWORKS.toString())) {
            responseMessage = this.thitsaWorksCreditBureauIntegrationWritePlatformService.addCreditReport(bureauId, creditReport,
                    fileDetail);
        } else {

            baseDataValidator.reset().failWithCode("creditBureau.has.not.been.Integrated");
            throw new PlatformApiDataValidationException("creditBureau.has.not.been.Integrated", "creditBureau.has.not.been.Integrated",
                    dataValidationErrors);

        }

        return responseMessage;

    }

    private Optional<String> getCreditBureau(Long creditBureauID) {

        if (creditBureauID != null) {
            Optional<CreditBureau> creditBureau = this.creditBureauRepository.findById(creditBureauID);

            if (creditBureau.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(creditBureau.get().getName());

        }

        return Optional.empty();
    }

    // saving the fetched creditreport in database
    @Override
    @Transactional
    public CommandProcessingResult saveCreditReport(Long creditBureauId, String nationalId, JsonCommand command) {

        try {
            this.context.authenticatedUser();

            Optional<String> creditBureauName = getCreditBureau(creditBureauId);
            CreditReport creditReport = null;

            if (Objects.equals(creditBureauName.get(), CreditBureauConfigurations.THITSAWORKS.toString())) {

                // checks whether creditReport for same nationalId was saved before. if yes, then deletes it & replaces
                // with new one.
                creditReport = creditReportRepository.getThitsaWorksCreditReport(creditBureauId, nationalId);

                if (creditReport != null) {
                    this.creditReportRepository.delete(creditReport);
                }

                String reportData = command.stringValueOfParameterNamed("apiRequestBodyAsJson");

                byte[] creditReportArray = reportData.getBytes(StandardCharsets.UTF_8);
                creditReport = CreditReport.instance(creditBureauId, nationalId, creditReportArray);
                this.creditReportRepository.saveAndFlush(creditReport);

            }

            return new CommandProcessingResultBuilder().withEntityId(creditReport.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleTokenDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleTokenDataIntegrityIssues(command, throwable, ee);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteCreditReport(Long creditBureauId, JsonCommand command) {

        this.context.authenticatedUser();

        Optional<String> creditBureauName = getCreditBureau(creditBureauId);
        CreditReport creditReport = null;

        if (Objects.equals(creditBureauName.get(), CreditBureauConfigurations.THITSAWORKS.toString())) {

            String nationalId = command.stringValueOfParameterNamed("nationalId");

            creditReport = creditReportRepository.getThitsaWorksCreditReport(creditBureauId, nationalId);
            try {
                this.creditReportRepository.delete(creditReport);
            } catch (final JpaSystemException | DataIntegrityViolationException dve) {
                throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                        "Unknown data integrity issue with resource: " + dve.getMostSpecificCause(), dve);
            }
        }
        return new CommandProcessingResultBuilder().withEntityId(creditReport.getId()).build();
    }

    private void handleTokenDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());

    }

}
