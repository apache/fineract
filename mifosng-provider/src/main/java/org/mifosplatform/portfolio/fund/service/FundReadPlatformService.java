package org.mifosplatform.portfolio.fund.service;

import java.util.Collection;

import org.mifosplatform.portfolio.fund.data.FundData;

public interface FundReadPlatformService {

    Collection<FundData> retrieveAllFunds();

    FundData retrieveFund(Long fundId);
}