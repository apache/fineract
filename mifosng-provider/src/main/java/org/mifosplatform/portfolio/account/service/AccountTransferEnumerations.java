/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.account.PortfolioAccountType;

public class AccountTransferEnumerations {

    public static EnumOptionData accountType(final Integer type) {
        return accountType(PortfolioAccountType.fromInt(type));
    }

    public static EnumOptionData accountType(final PortfolioAccountType type) {

        EnumOptionData optionData = null;

        if (type != null) {
            switch (type) {
                case INVALID:
                break;
                case LOAN:
                    optionData = new EnumOptionData(PortfolioAccountType.LOAN.getValue().longValue(), PortfolioAccountType.LOAN.getCode(),
                            "Loan Account");
                break;
                case SAVINGS:
                    optionData = new EnumOptionData(PortfolioAccountType.SAVINGS.getValue().longValue(),
                            PortfolioAccountType.SAVINGS.getCode(), "Savings Account");
                break;
            }
        }

        return optionData;
    }
}