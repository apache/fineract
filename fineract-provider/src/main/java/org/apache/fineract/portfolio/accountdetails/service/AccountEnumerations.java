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
package org.apache.fineract.portfolio.accountdetails.service;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;

public class AccountEnumerations {

    public static EnumOptionData loanType(final Integer loanTypeId) {
        return loanType(AccountType.fromInt(loanTypeId));
    }

    public static EnumOptionData loanType(final String name) {
        return loanType(AccountType.fromName(name));
    }

    public static EnumOptionData loanType(final AccountType type) {
        EnumOptionData optionData = new EnumOptionData(AccountType.INVALID.getValue().longValue(), AccountType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
                optionData = new EnumOptionData(AccountType.INVALID.getValue().longValue(), AccountType.INVALID.getCode(), "Invalid");
            break;
            case INDIVIDUAL:
                optionData = new EnumOptionData(AccountType.INDIVIDUAL.getValue().longValue(), AccountType.INDIVIDUAL.getCode(),
                        "Individual");
            break;
            case GROUP:
                optionData = new EnumOptionData(AccountType.GROUP.getValue().longValue(), AccountType.GROUP.getCode(), "Group");
            break;
            case JLG:
                optionData = new EnumOptionData(AccountType.JLG.getValue().longValue(), AccountType.JLG.getCode(), "JLG");
            break;
        }

        return optionData;
    }

}
