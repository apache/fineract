/* Copyright (C) Conflux Technologies Pvt Ltd - All Rights Reserved
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is proprietary and confidential software; you can't redistribute it and/or modify it unless agreed to in writing.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 */

package org.apache.fineract.organisation.provisioning.domain;

public enum ProvisioningAmountType {
	TotalPrincipalOutstanding(1, "provisioningAmountType.TotalPrincipalOutstanding"), //
	TotalOutstanding(2, "provisioningAmountType.TotalOutstanding");

	private final Integer value;
	private final String code;

	private ProvisioningAmountType(final Integer value, final String code) {
		this.value = value;
		this.code = code;
	}

	public Integer getValue() {
		return this.value;
	}

	public String getCode() {
		return this.code;
	}

	public static ProvisioningAmountType fromInt(final Integer frequency) {
		ProvisioningAmountType provisioningAmountType = ProvisioningAmountType.TotalOutstanding;
		if (frequency != null) {
			switch (frequency) {
			case 1:
				provisioningAmountType = ProvisioningAmountType.TotalPrincipalOutstanding;
				break;
			case 2:
				provisioningAmountType = ProvisioningAmountType.TotalOutstanding;
				break;
			}
		}
		return provisioningAmountType;
	}

}
