/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MifosEntityApiResourceConstants {

    public static final String MIFOS_ENTITY_RESOURCE_NAME = "MifosEntity";
    public static final String mappingTypes = "mappingTypes";
    public static final String mapId = "mapId";
    public static final String relId = "relId";
    public static final String fromEnityType = "fromId";
    public static final String toEntityType = "toId";
    public static final String startDate = "startDate";
    public static final String endDate = "endDate";
    public static final String LOCALE = "locale";
    public static final String DATE_FORMAT = "dateFormat";

    public static final String OFFICE_ACCESS_TO_LOAN_PRODUCTS = " office_access_to_loan_products ";
    public static final String OFFICE_ACCESS_TO_SAVINGS_PRODUCTS = " office_access_to_savings_products ";
    public static final String OFFICE_ACCESS_TO_CHARGES_FEES = " office_access_to_fees/charges ";
    public static final String ROLE_ACCESS_TO_LOAN_PRODUCTS = " role_access_to_loan_products ";
    public static final String ROLE_ACCESS_TO_SAVINGS_PRODUCTS = " role_access_to_savings_products ";

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(mappingTypes));

    public static final Set<String> FETCH_ENTITY_TO_ENTITY_MAPPINGS = new HashSet<>(Arrays.asList(mapId,relId,fromEnityType, toEntityType));

    public static final Set<String> CREATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(fromEnityType,
            toEntityType, startDate, LOCALE, DATE_FORMAT, endDate));

    public static final Set<String> UPDATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(relId,fromEnityType,
            toEntityType, startDate,LOCALE, DATE_FORMAT, endDate));

}
