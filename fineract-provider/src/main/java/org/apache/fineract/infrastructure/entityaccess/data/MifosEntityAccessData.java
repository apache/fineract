/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.data;

import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntity;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityAccessType;

public class MifosEntityAccessData {
	private MifosEntity firstEntity;
	private MifosEntityAccessType accessType;
	private MifosEntity secondEntity;
	
	public MifosEntityAccessData (
			MifosEntity firstEntity,
			MifosEntityAccessType accessType,
			MifosEntity secondEntity
			) {
		this.firstEntity = firstEntity;
		this.accessType = accessType;
		this.secondEntity = secondEntity;
	}
	
	public MifosEntity getFirstEntity() {
		return this.firstEntity;
	}
	
	public MifosEntityAccessType getAccessType() {
		return this.accessType;
	}
	
	public MifosEntity getSecondEntity() {
		return this.secondEntity;
	}

}
