/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymentdetail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PaymentDetailConstants {

    // Code representing Payment Details
    public static final String paymentTypeCodeName = "PaymentType";

    // request parameters
    public static final String paymentTypeParamName = "paymentTypeId";
    public static final String accountNumberParamName = "accountNumber";
    public static final String checkNumberParamName = "checkNumber";
    public static final String routingCodeParamName = "routingCode";
    public static final String receiptNumberParamName = "receiptNumber";
    public static final String bankNumberParamName = "bankNumber";

    // template related part of response
    public static final String officeOptionsParamName = "paymentTypeOptions";

    public static final Set<String> PAYMENT_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(accountNumberParamName,
            checkNumberParamName, routingCodeParamName, receiptNumberParamName, bankNumberParamName));

}