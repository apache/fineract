package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorType;

public class GuarantorEnumerations {

    public static EnumOptionData guarantorType(final int id) {
        return guarantorType(GuarantorType.fromInt(id));
    }

    public static EnumOptionData guarantorType(final GuarantorType guarantorType) {
        EnumOptionData optionData = new EnumOptionData(guarantorType.getValue().longValue(), guarantorType.getCode(),
                guarantorType.toString());
        return optionData;
    }

    public static List<EnumOptionData> guarantorType(GuarantorType[] guarantorTypes) {
        List<EnumOptionData> optionDatas = new ArrayList<EnumOptionData>();
        for (GuarantorType guarantorType : guarantorTypes) {
            optionDatas.add(guarantorType(guarantorType));
        }
        return optionDatas;
    }

}
