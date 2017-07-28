/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.accountnumberformat.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.fineract.infrastructure.accountnumberformat.data.AccountNumberFormatData;

public class AccountNumberFormatConstants {

    // resource name for validation
    public static final String ENTITY_NAME = "accountNumberFormat";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // REST end point
    public static final String resourceRelativeURL = "/accountnumberformats";

    // request parameters
    public static final String idParamName = "id";
    public static final String accountTypeParamName = "accountType";
    public static final String prefixTypeParamName = "prefixType";

    // response parameters

    // associations related part of response

    // template related part of response
    public static final String accountTypeOptionsParamName = "accountTypeOptions";
    public static final String prefixTypeOptionsParamName = "prefixTypeOptions";

    /**
     * These parameters will match the class level parameters of
     * {@link AccountNumberFormatData}. Where possible, we try to get response
     * parameters to match those of request parameters.
     */

    // Error messages codes
    public static final String EXCEPTION_DUPLICATE_ACCOUNT_TYPE = "error.msg.account.number.format.duplicate.account.type";
    public static final String EXCEPTION_ACCOUNT_NUMBER_FORMAT_NOT_FOUND = "error.msg.account.number.format.id.invalid";
    // JPA related constants
    public static final String ACCOUNT_NUMBER_FORMAT_TABLE_NAME = "c_account_number_format";
    public static final String ACCOUNT_TYPE_ENUM_COLUMN_NAME = "account_type_enum";
    public static final String PREFIX_TYPE_ENUM_COLUMN_NAME = "prefix_type_enum";
    public static final String ACCOUNT_TYPE_UNIQUE_CONSTRAINT_NAME = "account_type_enum";

}
