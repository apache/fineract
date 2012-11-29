package org.mifosplatform.portfolio.fund.service;

import org.mifosplatform.portfolio.fund.command.FundCommand;

public interface FundWritePlatformService {

    Long createFund(FundCommand command);

    Long updateFund(FundCommand command);
}