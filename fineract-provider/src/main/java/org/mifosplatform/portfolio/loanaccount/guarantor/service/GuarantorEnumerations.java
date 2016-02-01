/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorFundStatusType;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorType;

public class GuarantorEnumerations {

    public static EnumOptionData guarantorType(final int id) {
        return guarantorType(GuarantorType.fromInt(id));
    }

    public static EnumOptionData guarantorType(final GuarantorType guarantorType) {
        final EnumOptionData optionData = new EnumOptionData(guarantorType.getValue().longValue(), guarantorType.getCode(),
                guarantorType.toString());
        return optionData;
    }

    public static List<EnumOptionData> guarantorType(final GuarantorType[] guarantorTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final GuarantorType guarantorType : guarantorTypes) {
            optionDatas.add(guarantorType(guarantorType));
        }
        return optionDatas;
    }

    public static EnumOptionData guarantorFundStatusType(final int id) {
        return guarantorFundStatusType(GuarantorFundStatusType.fromInt(id));
    }

    public static EnumOptionData guarantorFundStatusType(final GuarantorFundStatusType guarantorFundType) {
        final EnumOptionData optionData = new EnumOptionData(guarantorFundType.getValue().longValue(), guarantorFundType.getCode(),
                guarantorFundType.toString());
        return optionData;
    }

}
