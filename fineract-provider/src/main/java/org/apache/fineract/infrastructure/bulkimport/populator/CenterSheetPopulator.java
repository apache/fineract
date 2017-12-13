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

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.group.data.CenterData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CenterSheetPopulator extends AbstractWorkbookPopulator {
	private List<CenterData> allCenters;
	private List<OfficeData> offices;

	private Map<String, ArrayList<String>> officeToCenters;
	private Map<Integer, Integer[]> officeNameToBeginEndIndexesOfCenters;
	private Map<String, Long> centerNameToCenterId;

	private static final int OFFICE_NAME_COL = 0;
	private static final int CENTER_NAME_COL = 1;
	private static final int CENTER_ID_COL = 2;

	public CenterSheetPopulator(List<CenterData> centers, List<OfficeData> offices) {
		this.allCenters = centers;
		this.offices = offices;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		Sheet centerSheet = workbook.createSheet(TemplatePopulateImportConstants.CENTER_SHEET_NAME);
		setLayout(centerSheet);
		setOfficeToCentersMap();
		centerNameToCenterIdMap();
		populateCentersByOfficeName(centerSheet);
		centerSheet.protectSheet("");
	}

	private void centerNameToCenterIdMap() {
		centerNameToCenterId = new HashMap<String, Long>();
		for (CenterData centerData : allCenters) {
			centerNameToCenterId.put(centerData.getName(), centerData.getId());
		}
	}

	private void populateCentersByOfficeName(Sheet centerSheet) {
		int rowIndex = 1, officeIndex = 0, startIndex = 1;
		officeNameToBeginEndIndexesOfCenters = new HashMap<Integer, Integer[]>();
		Row row = centerSheet.createRow(rowIndex);
		for (OfficeData office : offices) {
			startIndex = rowIndex + 1;
			writeString(OFFICE_NAME_COL, row, office.name());
			ArrayList<String> centersList = new ArrayList<String>();

		if (officeToCenters.containsKey(office.name().trim().replaceAll("[ )(]", "_"))){
				centersList = officeToCenters.get(office.name().trim().replaceAll("[ )(]", "_"));
			if (!centersList.isEmpty()) {
				for (String centerName : centersList) {
					writeString(CENTER_NAME_COL, row, centerName);
					writeLong(CENTER_ID_COL, row, centerNameToCenterId.get(centerName));
					row = centerSheet.createRow(++rowIndex);
				}
				officeNameToBeginEndIndexesOfCenters.put(officeIndex++, new Integer[] { startIndex, rowIndex });
			} else {
				officeNameToBeginEndIndexesOfCenters.put(officeIndex++, new Integer[] { startIndex, rowIndex + 1 });
			}
		  }
		}
	}

	private void setLayout(Sheet worksheet) {
		Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		for (int colIndex = 0; colIndex <= 10; colIndex++)
			worksheet.setColumnWidth(colIndex, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		writeString(OFFICE_NAME_COL, rowHeader, "Office Names");
		writeString(CENTER_NAME_COL, rowHeader, "Center Names");
		writeString(CENTER_ID_COL, rowHeader, "Center ID");
	}

	private void setOfficeToCentersMap() {
		officeToCenters = new HashMap<>();
		for (CenterData center : allCenters) {
			add(center.getOfficeName().trim().replaceAll("[ )(]", "_"), center.getName().trim());
		}
	}

	// Guava Multi-map can reduce this.
	private void add(String key, String value) {
		ArrayList<String> values = officeToCenters.get(key);
		if (values == null) {
			values = new ArrayList<>();
		}
		values.add(value);
		officeToCenters.put(key, values);
	}

	public Map<Integer, Integer[]> getOfficeNameToBeginEndIndexesOfCenters() {
		return officeNameToBeginEndIndexesOfCenters;
	}

}