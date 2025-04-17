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
package org.apache.fineract.infrastructure.core.service;

import org.apache.commons.lang3.StringUtils;

public final class CommandParameterUtil {

    public static final String GENERATE_COLLECTION_SHEET_COMMAND_VALUE = "generateCollectionSheet";
    public static final String SAVE_COLLECTION_SHEET_COMMAND_VALUE = "saveCollectionSheet";
    public static final String INTERMEDIARY_SALE_COMMAND_VALUE = "intermediarySale";
    public static final String SALE_COMMAND_VALUE = "sale";
    public static final String BUY_BACK_COMMAND_VALUE = "buyback";
    public static final String CANCEL_COMMAND_VALUE = "cancel";
    public static final String UPDATE_COMMAND_VALUE = "update";
    public static final String DELETE_COMMAND_VALUE = "delete";

    private CommandParameterUtil() {}

    public static boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
