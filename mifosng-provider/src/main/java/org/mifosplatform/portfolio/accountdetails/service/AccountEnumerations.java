/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accountdetails.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;

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
