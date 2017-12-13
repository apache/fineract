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
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSheetPopulator extends AbstractWorkbookPopulator {
	private List<ClientData> allClients;
	private List<OfficeData> officesDataList;

	private Map<String, ArrayList<String>> officeToClients;
	private Map<Integer, Integer[]> officeNameToBeginEndIndexesOfClients;
	private Map<String, Long> clientNameToClientId;
	private Map<String, Long> clientNameToSavingsAccountIds;

	private static final int OFFICE_NAME_COL = 0;
	private static final int CLIENT_NAME_COL = 1;
	private static final int CLIENT_ID_COL = 2;

	public ClientSheetPopulator(final List<ClientData> clients, final List<OfficeData> Offices) {
		this.allClients = clients;
		this.officesDataList = Offices;
	}

	@Override
	public void populate(Workbook workbook,String dateFormat) {
		Sheet clientSheet = workbook.createSheet(TemplatePopulateImportConstants.CLIENT_SHEET_NAME);
		setLayout(clientSheet);
		setOfficeToClientsMap();
		setClientNameToClientIdMap();
		populateClientsByOfficeName(clientSheet);
		clientSheet.protectSheet("");
		setClientNameToSavingsAccountsIdsMap();
	}

	private void setClientNameToClientIdMap() {
		clientNameToClientId = new HashMap<>();
		for (ClientData clientData : allClients) {
			clientNameToClientId.put(clientData.displayName().trim() + "(" + clientData.id() + ")",
					clientData.id());
		}
	}

	private void setLayout(Sheet worksheet) {
		Row rowHeader = worksheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
		rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);
		worksheet.setColumnWidth(OFFICE_NAME_COL, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		for (int colIndex = 1; colIndex <= 10; colIndex++)
			worksheet.setColumnWidth(colIndex, TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
		writeString(OFFICE_NAME_COL, rowHeader, "Office Names");
		writeString(CLIENT_NAME_COL, rowHeader, "Client Names");
		writeString(CLIENT_ID_COL, rowHeader, "Client ID");
	}

	private void setOfficeToClientsMap() {
		officeToClients = new HashMap<>();
		for (ClientData person : allClients) {
			addToOfficeClientMap(person.getOfficeName().trim().replaceAll("[ )(]", "_"),
					person.displayName().trim() + "(" + person.id() + ")");
		}
	}

	private void setClientNameToSavingsAccountsIdsMap(){
		clientNameToSavingsAccountIds=new HashMap<>();
		for (ClientData client: allClients) {
			clientNameToSavingsAccountIds.put(client.displayName().trim() + "(" + client.id() + ")",client.getSavingsAccountId());
		}

	}

	// Guava Multi-map can reduce this.
	private void addToOfficeClientMap(String key, String value) {
		ArrayList<String> values = officeToClients.get(key);
		if (values == null) {
			values = new ArrayList<String>();
		}
		values.add(value);
		officeToClients.put(key, values);
	}

	private void populateClientsByOfficeName(Sheet clientSheet) {
		int rowIndex = 1, startIndex = 1, officeIndex = 0;
		officeNameToBeginEndIndexesOfClients = new HashMap<>();
		Row row = clientSheet.createRow(rowIndex);
		for (OfficeData office : officesDataList) {
			startIndex = rowIndex + 1;
			writeString(OFFICE_NAME_COL, row, office.name());
			ArrayList<String> clientList = new ArrayList<String>();
			if (officeToClients.containsKey(office.name().trim().replaceAll("[ )(]", "_")))
				clientList = officeToClients.get(office.name().trim().replaceAll("[ )(]", "_"));
			if (!clientList.isEmpty()) {
				for (String clientName : clientList) {
					writeString(CLIENT_NAME_COL, row, clientName);
					writeLong(CLIENT_ID_COL, row, clientNameToClientId.get(clientName));
					row = clientSheet.createRow(++rowIndex);
				}
				officeNameToBeginEndIndexesOfClients.put(officeIndex++, new Integer[] { startIndex, rowIndex });
			} else
				officeIndex++;
		}
	}

	public List<ClientData> getClients() {
		return allClients;
	}

	public Integer getClientsSize() {
		return allClients.size();
	}

	public Map<Integer, Integer[]> getOfficeNameToBeginEndIndexesOfClients() {
		return officeNameToBeginEndIndexesOfClients;
	}

	public Map<String, Long> getClientNameToSavingsAccountIds() {
		return clientNameToSavingsAccountIds;
	}
}