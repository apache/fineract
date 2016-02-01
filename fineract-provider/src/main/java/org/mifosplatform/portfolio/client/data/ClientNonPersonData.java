/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.data;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;

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
