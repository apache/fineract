/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.data;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.portfolio.client.data.ClientData;

@SuppressWarnings("unused")
public class StandingInstructionHistoryData {

    private final Long standingInstructionId;
    private final String name;
    private final OfficeData fromOffice;
    private final ClientData fromClient;
    private final EnumOptionData fromAccountType;
    private final PortfolioAccountData fromAccount;
    private final EnumOptionData toAccountType;
    private final PortfolioAccountData toAccount;
    private final OfficeData toOffice;
    private final ClientData toClient;
    private final BigDecimal amount;
    private final String status;
    private final LocalDate executionTime;
    private final String errorLog;

    public StandingInstructionHistoryData(final Long standingInstructionId, final String name, final OfficeData fromOffice,
            final ClientData fromClient, final EnumOptionData fromAccountType, final PortfolioAccountData fromAccount,
            final EnumOptionData toAccountType, final PortfolioAccountData toAccount, final OfficeData toOffice, final ClientData toClient,
            final BigDecimal amount, final String status, final LocalDate executionTime, final String errorLog) {
        this.standingInstructionId = standingInstructionId;
        this.name = name;
        this.fromOffice = fromOffice;
        this.fromClient = fromClient;
        this.fromAccountType = fromAccountType;
        this.toAccountType = toAccountType;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.toOffice = toOffice;
        this.toClient = toClient;
        this.amount = amount;
        this.errorLog = errorLog;
        this.status = status;
        this.executionTime = executionTime;
    }

}
