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
package org.apache.fineract.infrastructure.bulkimport.populator;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public class GlAccountSheetPopulator extends AbstractWorkbookPopulator {
	private List<GLAccountData> allGlAccounts;

	private static final int ID_COL = 0;
	private static final int ACCOUNT_NAME_COL = 1;

	public GlAccountSheetPopulator(List<GLAccountData> glAccounts) {
		this.allGlAccounts = glAccounts;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		int rowIndex = 1;
		Sheet glAccountSheet = workbook.createSheet(TemplatePopulateImportConstants.GL_ACCOUNTS_SHEET_NAME);
		setLayout(glAccountSheet);
		populateglAccounts(glAccountSheet, rowIndex);
		glAccountSheet.protectSheet("");

	}

	private void setLayout(Sheet worksheet) {
		worksheet.setColumnWidth(ID_COL, TemplatePopulateImportConstants.SMALL_COL_SIZE);
		worksheet.setColumnWidth(ACCOUNT_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		writeString(ID_COL, rowHeader, "Gl Account ID");
		writeString(ACCOUNT_NAME_COL, rowHeader, "Gl Account Name");
	}

	private void populateglAccounts(Sheet GlAccountSheet, int rowIndex) {
		for (GLAccountData glAccount : allGlAccounts) {
			Row row = GlAccountSheet.createRow(rowIndex);
			writeLong(ID_COL, row, glAccount.getId());
			writeString(ACCOUNT_NAME_COL, row, glAccount.getName().trim().replaceAll("[ )(]", "_"));
			rowIndex++;
		}
	}

	public Integer getGlAccountNamesSize() {
		return allGlAccounts.size();
	}

}