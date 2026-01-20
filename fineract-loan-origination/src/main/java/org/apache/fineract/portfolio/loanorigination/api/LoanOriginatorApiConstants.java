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
package org.apache.fineract.portfolio.loanorigination.api;

import java.util.Set;

public final class LoanOriginatorApiConstants {

    private LoanOriginatorApiConstants() {}

    public static final String RESOURCE_NAME = "LOAN_ORIGINATOR";
    public static final String RESOURCE_PATH = "/loan-originators";

    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    public static final String ORIGINATOR_TYPE_CODE_NAME = "LoanOriginatorType";
    public static final String CHANNEL_TYPE_CODE_NAME = "LoanOriginationChannelType";

    public static final String EXTERNAL_ID_PARAM = "externalId";
    public static final String NAME_PARAM = "name";
    public static final String STATUS_PARAM = "status";
    public static final String ORIGINATOR_TYPE_ID_PARAM = "originatorTypeId";
    public static final String CHANNEL_TYPE_ID_PARAM = "channelTypeId";

    public static final Set<String> CREATE_REQUEST_PARAMS = Set.of(EXTERNAL_ID_PARAM, NAME_PARAM, STATUS_PARAM, ORIGINATOR_TYPE_ID_PARAM,
            CHANNEL_TYPE_ID_PARAM);

    public static final Set<String> UPDATE_REQUEST_PARAMS = Set.of(NAME_PARAM, STATUS_PARAM, ORIGINATOR_TYPE_ID_PARAM,
            CHANNEL_TYPE_ID_PARAM);

    public static final Set<String> RESPONSE_PARAMS = Set.of("id", EXTERNAL_ID_PARAM, NAME_PARAM, STATUS_PARAM, ORIGINATOR_TYPE_ID_PARAM,
            CHANNEL_TYPE_ID_PARAM);
}
