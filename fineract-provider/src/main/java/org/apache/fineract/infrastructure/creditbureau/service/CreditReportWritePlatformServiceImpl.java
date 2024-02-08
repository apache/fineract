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

import jakarta.persistence.PersistenceException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauConfigurations;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauReportData;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditReport;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditReportRepository;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditReportWritePlatformServiceImpl implements CreditReportWritePlatformService {

    public static final String CREDIT_BUREAU_INTEGRATION = "creditBureauIntegration";
    public static final String CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED = "creditBureau.has.not.been.Integrated";
    private final PlatformSecurityContext context;

    private final CreditBureauRepository creditBureauRepository;
    private final CreditReportRepository creditReportRepository;
    private final ThitsaWorksCreditBureauIntegrationWritePlatformService thitsaWorksCreditBureauIntegrationWritePlatformService;

    @Autowired
    public CreditReportWritePlatformServiceImpl(final PlatformSecurityContext context, final CreditBureauRepository creditBureauRepository,
            final CreditReportRepository creditReportRepository,
            final ThitsaWorksCreditBureauIntegrationWritePlatformService thitsaWorksCreditBureauIntegrationWritePlatformService) {
        this.context = context;
        this.creditBureauRepository = creditBureauRepository;
        this.creditReportRepository = creditReportRepository;
        this.thitsaWorksCreditBureauIntegrationWritePlatformService = thitsaWorksCreditBureauIntegrationWritePlatformService;
    }

    @Override
    @Transactional
    public CommandProcessingResult getCreditReport(JsonCommand command) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CREDIT_BUREAU_INTEGRATION);
        try {
            Long creditBureauID = command.longValueOfParameterNamed("creditBureauID");

            String creditBureauName = getCreditBureauName(creditBureauID);

            if (Objects.equals(creditBureauName, CreditBureauConfigurations.THITSAWORKS.toString())) {

                CreditBureauReportData reportobj = this.thitsaWorksCreditBureauIntegrationWritePlatformService
                        .getCreditReportFromThitsaWorks(command);

                Map<String, Object> reportMap = Map.of("name", reportobj.getName(), "gender", reportobj.getGender(), "address",
                        reportobj.getAddress(), "creditScore", reportobj.getCreditScore(), "borrowerInfo", reportobj.getBorrowerInfo(),
                        "openAccounts", reportobj.getOpenAccounts(), "closedAccounts", reportobj.getClosedAccounts());

                return new CommandProcessingResultBuilder().withCreditReport(reportMap).build();
            }

            baseDataValidator.reset().failWithCode(CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED);
            throw new PlatformApiDataValidationException(CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED, CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED,
                    dataValidationErrors);

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleTokenDataIntegrityIssues(dve.getMostSpecificCause());
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleTokenDataIntegrityIssues(throwable);
            return CommandProcessingResult.empty();
        }

    }

    @Override
    @Transactional
    public String addCreditReport(Long bureauId, File creditReport, FormDataContentDisposition fileDetail) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CREDIT_BUREAU_INTEGRATION);
        String creditBureauName = getCreditBureauName(bureauId);
        String responseMessage = null;

        if (Objects.equals(creditBureauName, CreditBureauConfigurations.THITSAWORKS.toString())) {
            responseMessage = this.thitsaWorksCreditBureauIntegrationWritePlatformService.addCreditReport(bureauId, creditReport,
                    fileDetail);
        } else {

            baseDataValidator.reset().failWithCode(CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED);
            throw new PlatformApiDataValidationException(CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED, CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED,
                    dataValidationErrors);

        }

        return responseMessage;

    }

    private String getCreditBureauName(Long creditBureauID) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(CREDIT_BUREAU_INTEGRATION);
        if (creditBureauID != null) {
            Optional<CreditBureau> creditBureau = this.creditBureauRepository.findById(creditBureauID);

            if (!creditBureau.isEmpty()) {
                return creditBureau.get().getName();
            }
        }

        baseDataValidator.reset().failWithCode(CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED);
        throw new PlatformApiDataValidationException(CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED, CREDIT_BUREAU_HAS_NOT_BEEN_INTEGRATED,
                dataValidationErrors);
    }

    // saving the fetched creditreport in database
    @Override
    @Transactional
    public CommandProcessingResult saveCreditReport(Long creditBureauId, String nationalId, JsonCommand command) {

        try {
            this.context.authenticatedUser();

            String creditBureauName = getCreditBureauName(creditBureauId);
            CreditReport creditReport = null;

            if (Objects.equals(creditBureauName, CreditBureauConfigurations.THITSAWORKS.toString())) {

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
            handleTokenDataIntegrityIssues(dve.getMostSpecificCause());
            return CommandProcessingResult.empty();
        } catch (final PersistenceException ee) {
            Throwable throwable = ExceptionUtils.getRootCause(ee.getCause());
            handleTokenDataIntegrityIssues(throwable);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteCreditReport(Long creditBureauId, JsonCommand command) {

        this.context.authenticatedUser();

        String creditBureauName = getCreditBureauName(creditBureauId);
        CreditReport creditReport = null;

        if (Objects.equals(creditBureauName, CreditBureauConfigurations.THITSAWORKS.toString())) {

            String nationalId = command.stringValueOfParameterNamed("nationalId");

            creditReport = creditReportRepository.getThitsaWorksCreditReport(creditBureauId, nationalId);
            try {
                this.creditReportRepository.delete(creditReport);
            } catch (final JpaSystemException | DataIntegrityViolationException dve) {
                throw ErrorHandler.getMappable(dve, "error.msg.cund.unknown.data.integrity.issue",
                        "Unknown data integrity issue with resource: " + dve.getMostSpecificCause().getMessage());
            }
        }
        return new CommandProcessingResultBuilder().withEntityId(creditReport.getId()).build();
    }

    private void handleTokenDataIntegrityIssues(final Throwable realCause) {
        throw ErrorHandler.getMappable(realCause, "error.msg.cund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
