/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.dataqueries.domain.Report;
import org.mifosplatform.infrastructure.dataqueries.domain.ReportRepository;
import org.mifosplatform.infrastructure.dataqueries.exception.ReportNotFoundException;
import org.mifosplatform.infrastructure.dataqueries.serialization.ReportCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportWritePlatformServiceImpl implements
		ReportWritePlatformService {

	private final static Logger logger = LoggerFactory
			.getLogger(ReportWritePlatformServiceImpl.class);

	private final PlatformSecurityContext context;
	private final ReportCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ReportRepository reportRepository;

	@Autowired
	public ReportWritePlatformServiceImpl(
			final PlatformSecurityContext context,
			final ReportCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final ReportRepository reportRepository) {
		this.context = context;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.reportRepository = reportRepository;
	}

	@Transactional
	@Override
	public CommandProcessingResult createReport(final JsonCommand command) {

		try {
			context.authenticatedUser();

			this.fromApiJsonDeserializer.validateForCreate(command.json());

			final Report report = Report.fromJson(command);

			this.reportRepository.save(report);

			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId())
					.withEntityId(report.getId()).build();
		} catch (DataIntegrityViolationException dve) {
			handleReportDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult updateReport(final Long reportId,
			final JsonCommand command) {

		try {
			context.authenticatedUser();

			this.fromApiJsonDeserializer.validateForUpdate(command.json());

			final Report report = this.reportRepository.findOne(reportId);
			if (report == null) {
				throw new ReportNotFoundException(reportId);
			}

			final Map<String, Object> changes = report.update(command);
			if (!changes.isEmpty()) {
				this.reportRepository.saveAndFlush(report);
			}

			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId())
					.withEntityId(report.getId()).with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			handleReportDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult deleteReport(final Long reportId) {

		final Report report = this.reportRepository.findOne(reportId);
		if (report == null) {
			throw new ReportNotFoundException(reportId);
		}

		this.reportRepository.delete(report);

		return new CommandProcessingResultBuilder().withEntityId(reportId)
				.build();
	}

	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue
	 * is.
	 */
	private void handleReportDataIntegrityIssues(final JsonCommand command,
			DataIntegrityViolationException dve) {

		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("unq_report_name")) {
			final String name = command
					.stringValueOfParameterNamed("reportName");
			throw new PlatformDataIntegrityException(
					"error.msg.report.duplicate.name", "A report with name '"
							+ name + "' already exists", "name", name);
		}

		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException(
				"error.msg.report.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());
	}
}