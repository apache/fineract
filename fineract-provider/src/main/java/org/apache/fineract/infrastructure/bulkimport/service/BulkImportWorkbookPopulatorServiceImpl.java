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
package org.apache.fineract.infrastructure.bulkimport.service;

import org.apache.fineract.accounting.glaccount.api.GLAccountsApiConstants;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.infrastructure.bulkimport.populator.*;
import org.apache.fineract.infrastructure.bulkimport.populator.chartofaccounts.ChartOfAccountsWorkbook;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BulkImportWorkbookPopulatorServiceImpl implements BulkImportWorkbookPopulatorService {

  private final PlatformSecurityContext context;
  private final GLAccountReadPlatformService glAccountReadPlatformService;

  
  @Autowired
  public BulkImportWorkbookPopulatorServiceImpl(final PlatformSecurityContext context,
      final GLAccountReadPlatformService glAccountReadPlatformService) {
    this.context = context;
    this.glAccountReadPlatformService=glAccountReadPlatformService;

  }

	@Override
	public Response getTemplate(final String entityType, final Long officeId, final Long staffId,final Long centerId,
			final Long clientId,final Long groupId, final Long productId,final Long fundId,
			final Long paymentTypeId,final String code,final Long glAccountId) {

		WorkbookPopulator populator = null;
		final Workbook workbook = new HSSFWorkbook();
		if (entityType.trim().equalsIgnoreCase(GLAccountsApiConstants.GLACCOUNTS_RESOURCE_NAME)) {
			populator=populateChartOfAccountsWorkbook(glAccountId);
		}else
			throw new GeneralPlatformDomainRuleException("error.msg.unable.to.find.resource",
					"Unable to find requested resource");
		populator.populate(workbook);
		return buildResponse(workbook, entityType);
	}

	private Response buildResponse(final Workbook workbook, final String entity) {
		String filename = entity + DateUtils.getLocalDateOfTenant().toString() + ".xls";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			workbook.write(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		final ResponseBuilder response = Response.ok(baos.toByteArray());
		response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		response.header("Content-Type", "application/vnd.ms-excel");
		return response.build();
	}

	private List<GLAccountData> fetchGLAccounts(Long glAccountId) {
		List<GLAccountData> glaccounts = null;
		if (glAccountId == null) {
			glaccounts = (List<GLAccountData>) this.glAccountReadPlatformService.retrieveAllGLAccounts(null, null, null,
					null, null, null);
		} else {
			glaccounts = new ArrayList<>();
			glaccounts.add(this.glAccountReadPlatformService.retrieveGLAccountById(glAccountId, null));
		}

		return glaccounts;
	}

	private WorkbookPopulator populateChartOfAccountsWorkbook(Long glAccountId) {
		this.context.authenticatedUser().validateHasReadPermission("GLACCOUNT");
		List<GLAccountData> glAccounts = fetchGLAccounts(glAccountId);
		return new ChartOfAccountsWorkbook(glAccounts);
	}

}