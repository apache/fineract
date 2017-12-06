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
package org.apache.fineract.portfolio.client.data;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing the ClientNonPerson
 */
@SuppressWarnings("unused")
public class ClientNonPersonData {
	
	private final CodeValueData constitution;
	private final String incorpNumber;
	private final LocalDate incorpValidityTillDate;
    private final CodeValueData mainBusinessLine;
    private final String remarks;

	//import fields
	private Long mainBusinessLineId;
	private Long constitutionId;
	private String locale;
	private String dateFormat;

	public static ClientNonPersonData importInstance(String incorporationNo, LocalDate incorpValidityTillDate,
			String remarks, Long mainBusinessLineId, Long constitutionId,String locale,String dateFormat){
		return new ClientNonPersonData(incorporationNo,incorpValidityTillDate,remarks,
				mainBusinessLineId,constitutionId,locale,dateFormat);
	}
	private ClientNonPersonData(String incorpNumber, LocalDate incorpValidityTillDate,
			String remarks, Long mainBusinessLineId, Long constitutionId,String locale,String dateFormat) {

		this.incorpNumber = incorpNumber;
		this.incorpValidityTillDate = incorpValidityTillDate;
		this.remarks = remarks;
		this.mainBusinessLineId = mainBusinessLineId;
		this.constitutionId = constitutionId;
		this.dateFormat= dateFormat;
		this.locale= locale;
		this.constitution = null;
		this.mainBusinessLine = null;
	}
    
	public ClientNonPersonData(CodeValueData constitution, String incorpNo, LocalDate incorpValidityTillDate,
			CodeValueData mainBusinessLine, String remarks) {
		super();
		this.constitution = constitution;
		this.incorpNumber = incorpNo;
		this.incorpValidityTillDate = incorpValidityTillDate;
		this.mainBusinessLine = mainBusinessLine;
		this.remarks = remarks;
	}
        
}
