/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.dataqueries.domain.Report;
import org.mifosplatform.infrastructure.dataqueries.domain.ReportParameter;
import org.mifosplatform.infrastructure.dataqueries.domain.ReportParameterRepository;
import org.mifosplatform.infrastructure.dataqueries.domain.ReportParameterUsage;
import org.mifosplatform.infrastructure.dataqueries.domain.ReportParameterUsageRepository;
import org.mifosplatform.infrastructure.dataqueries.domain.ReportRepository;
import org.mifosplatform.infrastructure.dataqueries.exception.ReportNotFoundException;
import org.mifosplatform.infrastructure.dataqueries.exception.ReportParameterNotFoundException;
import org.mifosplatform.infrastructure.dataqueries.serialization.ReportCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.Permission;
import org.mifosplatform.useradministration.domain.PermissionRepository;
import org.mifosplatform.useradministration.exception.PermissionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class ReportWritePlatformServiceImpl implements ReportWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ReportWritePlatformServiceImpl.class);

    private final PlatformSecurityContext context;
    private final ReportCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final ReportRepository reportRepository;
    private final ReportParameterUsageRepository reportParameterUsageRepository;
    private final ReportParameterRepository reportParameterRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public ReportWritePlatformServiceImpl(final PlatformSecurityContext context,
            final ReportCommandFromApiJsonDeserializer fromApiJsonDeserializer, final ReportRepository reportRepository,
            final ReportParameterRepository reportParameterRepository, final ReportParameterUsageRepository reportParameterUsageRepository,
            final PermissionRepository permissionRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.reportRepository = reportRepository;
        this.reportParameterRepository = reportParameterRepository;
        this.reportParameterUsageRepository = reportParameterUsageRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createReport(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validate(command.json());

            final Report report = Report.fromJson(command);
            final Set<ReportParameterUsage> reportParameterUsages = assembleSetOfReportParameterUsages(report, command);
            report.update(reportParameterUsages);

            this.reportRepository.save(report);

            final Permission permission = new Permission("report", report.getReportName(), "READ");
            this.permissionRepository.save(permission);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(report.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleReportDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateReport(final Long reportId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validate(command.json());

            final Report report = this.reportRepository.findOne(reportId);
            if (report == null) { throw new ReportNotFoundException(reportId); }

            final Map<String, Object> changes = report.update(command);

            if (changes.containsKey("reportParameters")) {
                final Set<ReportParameterUsage> reportParameterUsages = assembleSetOfReportParameterUsages(report, command);
                final boolean updated = report.update(reportParameterUsages);
                if (!updated) {
                    changes.remove("reportParameters");
                }
            }

            if (!changes.isEmpty()) {
                this.reportRepository.saveAndFlush(report);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(report.getId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleReportDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteReport(final Long reportId) {

        final Report report = this.reportRepository.findOne(reportId);
        if (report == null) { throw new ReportNotFoundException(reportId); }

        if (report.isCoreReport()) {
            //
            throw new PlatformDataIntegrityException("error.msg.cant.delete.core.report", "Core Reports Can't be Deleted", "");
        }

        final Permission permission = this.permissionRepository.findOneByCode("READ" + "_" + report.getReportName());
        if (permission == null) { throw new PermissionNotFoundException("READ" + "_" + report.getReportName()); }

        this.reportRepository.delete(report);
        this.permissionRepository.delete(permission);

        return new CommandProcessingResultBuilder() //
                .withEntityId(reportId) //
                .build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleReportDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("unq_report_name")) {
            final String name = command.stringValueOfParameterNamed("reportName");
            throw new PlatformDataIntegrityException("error.msg.report.duplicate.name", "A report with name '" + name + "' already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.report.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    private Set<ReportParameterUsage> assembleSetOfReportParameterUsages(final Report report, final JsonCommand command) {

        Set<ReportParameterUsage> reportParameterUsages = null;

        if (command.parameterExists("reportParameters")) {
            final JsonArray reportParametersArray = command.arrayOfParameterNamed("reportParameters");
            if (reportParametersArray != null) {

                reportParameterUsages = new HashSet<>();

                for (int i = 0; i < reportParametersArray.size(); i++) {

                    final JsonObject jsonObject = reportParametersArray.get(i).getAsJsonObject();

                    Long id = null;
                    ReportParameterUsage reportParameterUsageItem = null;
                    ReportParameter reportParameter = null;
                    String reportParameterName = null;

                    if (jsonObject.has("id")) {
                        final String idStr = jsonObject.get("id").getAsString();
                        if (StringUtils.isNotBlank(idStr)) {
                            id = Long.parseLong(idStr);
                        }
                    }

                    if (id != null) {
                        // existing report parameter usage
                        reportParameterUsageItem = this.reportParameterUsageRepository.findOne(id);
                        if (reportParameterUsageItem == null) { throw new ReportParameterNotFoundException(id); }

                        // check parameter
                        if (jsonObject.has("parameterId")) {
                            final Long parameterId = jsonObject.get("parameterId").getAsLong();
                            reportParameter = this.reportParameterRepository.findOne(parameterId);
                            if (reportParameter == null || !reportParameterUsageItem.hasParameterIdOf(parameterId)) {
                                //
                                throw new ReportParameterNotFoundException(parameterId);
                            }
                        }

                        if (jsonObject.has("reportParameterName")) {
                            reportParameterName = jsonObject.get("reportParameterName").getAsString();
                            reportParameterUsageItem.updateParameterName(reportParameterName);
                        }
                    } else {
                        // new report parameter usage
                        if (jsonObject.has("parameterId")) {
                            final Long parameterId = jsonObject.get("parameterId").getAsLong();
                            reportParameter = this.reportParameterRepository.findOne(parameterId);
                            if (reportParameter == null) { throw new ReportParameterNotFoundException(parameterId); }
                        } else {
                            throw new PlatformDataIntegrityException("error.msg.parameter.id.mandatory.in.report.parameter",
                                    "parameterId column is mandatory in Report Parameter Entry");
                        }

                        if (jsonObject.has("reportParameterName")) {
                            reportParameterName = jsonObject.get("reportParameterName").getAsString();
                        }

                        reportParameterUsageItem = new ReportParameterUsage(report, reportParameter, reportParameterName);
                    }

                    reportParameterUsages.add(reportParameterUsageItem);
                }
            }
        }

        return reportParameterUsages;
    }
}