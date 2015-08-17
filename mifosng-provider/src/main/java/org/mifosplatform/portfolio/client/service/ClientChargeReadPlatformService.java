package org.mifosplatform.portfolio.client.service;

import java.util.Collection;

import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.client.data.ClientChargeData;

public interface ClientChargeReadPlatformService {

    ChargeData retrieveClientChargeTemplate();

    Collection<ClientChargeData> retrieveClientCharges(Long clientId, String status);

    ClientChargeData retrieveClientCharge(Long clientId, Long clientChargeId);

}
