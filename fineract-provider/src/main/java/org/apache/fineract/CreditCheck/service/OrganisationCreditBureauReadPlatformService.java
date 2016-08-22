package org.apache.fineract.CreditCheck.service;

import java.util.Collection;

import org.apache.fineract.CreditCheck.data.OrganisationCreditbureauData;

public interface OrganisationCreditBureauReadPlatformService 
{
    Collection<OrganisationCreditbureauData> retrieveOrgCreditBureau();
}
