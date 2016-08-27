/* Copyright (C) Conflux Technologies Pvt Ltd - All Rights Reserved
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is proprietary and confidential software; you can't redistribute it and/or modify it unless agreed to in writing.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 */

package org.apache.fineract.organisation.provisioning.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningAmountType;
import org.apache.fineract.portfolio.client.domain.LegalForm;

public class ProvisioningEnumerations {

	public static EnumOptionData provisioningAmountType(final int id) {
		return provisioningAmountType(ProvisioningAmountType.fromInt(id));
	}

	public static EnumOptionData provisioningAmountType(final ProvisioningAmountType type) {
		final String codePrefix = "provisioningAmountType.";
		EnumOptionData optionData = null;
		switch (type) {
		case TotalPrincipalOutstanding:
			optionData = new EnumOptionData(ProvisioningAmountType.TotalPrincipalOutstanding.getValue().longValue(),
					codePrefix + ProvisioningAmountType.TotalPrincipalOutstanding.getCode(), "TotalPrincipalOutstanding");
			break;
		case TotalOutstanding:
			optionData = new EnumOptionData(ProvisioningAmountType.TotalOutstanding.getValue().longValue(),
					codePrefix + ProvisioningAmountType.TotalOutstanding.getCode(), "TotalOutstanding");
			break;
		}
		return optionData;
	}
	
	 public static EnumOptionData provisioningAmount(final ProvisioningAmountType provisioningAmount) {
	    	final EnumOptionData optionData = new EnumOptionData(provisioningAmount.getValue().longValue(), provisioningAmount.getCode(),
	                provisioningAmount.toString());
	        return optionData;
	    }
	
	public static List<EnumOptionData> provisioningAmount(final ProvisioningAmountType[] provisioningAmounts) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final ProvisioningAmountType provisioningAmount : provisioningAmounts) {
            optionDatas.add(provisioningAmount(provisioningAmount));
        }
        return optionDatas;
    }
}
