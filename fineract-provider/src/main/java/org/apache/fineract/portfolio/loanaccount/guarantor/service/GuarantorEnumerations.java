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
package org.apache.fineract.portfolio.loanaccount.guarantor.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorFundStatusType;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorType;

public final class GuarantorEnumerations {

    private GuarantorEnumerations() {

    }

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
