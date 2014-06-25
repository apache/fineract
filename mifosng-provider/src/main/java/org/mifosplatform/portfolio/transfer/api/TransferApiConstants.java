/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.transfer.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TransferApiConstants {

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // request parameters
    public static final String idParamName = "id";
    public static final String destinationGroupIdParamName = "destinationGroupId";
    public static final String clients = "clients";
    public static final String inheritDestinationGroupLoanOfficer = "inheritDestinationGroupLoanOfficer";
    public static final String newStaffIdParamName = "staffId";
    public static final String transferActiveLoans = "transferActiveLoans";
    public static final String destinationOfficeIdParamName = "destinationOfficeId";
    public static final String note = "note";

    public static final Set<String> TRANSFER_CLIENTS_BETWEEN_GROUPS_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, destinationGroupIdParamName, clients, inheritDestinationGroupLoanOfficer, newStaffIdParamName,
            transferActiveLoans));

    public static final Set<String> PROPOSE_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, destinationOfficeIdParamName, transferActiveLoans, note));

    public static final Set<String> ACCEPT_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(Arrays.asList(newStaffIdParamName,
            destinationGroupIdParamName, note));

    public static final Set<String> PROPOSE_AND_ACCEPT_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, destinationOfficeIdParamName, transferActiveLoans, newStaffIdParamName, destinationGroupIdParamName, note));

    public static final Set<String> REJECT_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(Arrays.asList(note));

    public static final Set<String> WITHDRAW_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(Arrays.asList(note));

}