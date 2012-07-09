package org.mifosng.platform.fund.service;

import java.util.Collection;

import org.mifosng.platform.api.data.FundData;

public interface FundReadPlatformService {

	Collection<FundData> retrieveAllFunds();

	FundData retrieveFund(Long fundId);
}