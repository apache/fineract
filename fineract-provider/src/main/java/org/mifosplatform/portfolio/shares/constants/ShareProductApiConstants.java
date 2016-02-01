/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.constants;

public interface ShareProductApiConstants {

    //Command Strings 
    public final String PREIEW_DIVIDENDS_COMMAND_STRING = "previewdividends" ;
    public final String POST_DIVIDENdS_COMMAND_STRING = "postdividends" ;
    
    String id_paramname = "id";
    String name_paramname = "name";
    String shortname_paramname = "shortName";
    String description_paramname = "description";
    String externalid_paramname = "externalId";
    String totalshares_paramname = "totalShares";
    String currency_paramname = "currencyCode";
    String digitsafterdecimal_paramname = "digitsAfterDecimal";
    String inmultiplesof_paramname = "inMultiplesOf";
    String totalsharesissued_paramname = "totalSharesIssued";
    String unitprice_paramname = "unitPrice";
    String sharecapital_paramname = "shareCapital";
    String suspenseaccount_paramname = "suspenseAccount";
    String equityaccount_paramname = "equityAccount";
    String minimumshares_paramname = "minimumShares";
    String nominaltshares_paramname = "nominaltShares";
    String maximumshares_paramname = "maximumShares";
    String marketprice_paramname = "marketPrice";
    String charges_paramname = "charges";
    String allowdividendcalculationforinactiveclients_paramname = "allowDividendCalculationForInactiveClients";
    String lockperiod_paramname = "lockPeriod";
    String minimumactiveperiodfordividends_paramname = "minimumActivePeriodForDividends";

    String startdate_paramname = "startDate";
    String sharevalue_paramname = "shareValue";
}
