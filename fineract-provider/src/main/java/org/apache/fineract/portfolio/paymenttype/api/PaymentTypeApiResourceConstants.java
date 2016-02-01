/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymenttype.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PaymentTypeApiResourceConstants {

    public static final String RESOURCE_NAME = "paymenttype";
    public static final String ENTITY_NAME = "PAYMENTTYPE";

    public static final String resourceNameForPermissions = "PAYMENT_TYPE";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String ISCASHPAYMENT = "isCashPayment";
    public static final String POSITION = "position";

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ID, NAME, DESCRIPTION, ISCASHPAYMENT));

    public static final Set<String> CREATE_PAYMENT_TYPE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(NAME, DESCRIPTION,
            ISCASHPAYMENT, POSITION));

    public static final Set<String> UPDATE_PAYMENT_TYPE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(NAME, DESCRIPTION,
            ISCASHPAYMENT, POSITION));
}
