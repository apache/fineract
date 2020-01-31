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
package org.apache.fineract.interoperation.data;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.apache.fineract.interoperation.domain.InteropActionState;
import org.joda.time.LocalDateTime;

public class InteropTransactionRequestResponseData extends InteropResponseData {

    @NotNull
    private final String requestCode;


    private InteropTransactionRequestResponseData(Long resourceId, Long officeId, Long commandId, Map<String, Object> changesOnly,
                                                  @NotNull String transactionCode, @NotNull InteropActionState state, LocalDateTime expiration,
                                                  List<ExtensionData> extensionList, @NotNull String requestCode) {
        super(resourceId, officeId, commandId, changesOnly, transactionCode, state, expiration, extensionList);
        this.requestCode = requestCode;
    }

    public static InteropTransactionRequestResponseData build(Long commandId, @NotNull String transactionCode, @NotNull InteropActionState state,
                                                              LocalDateTime expiration, List<ExtensionData> extensionList, @NotNull String requestCode) {
        return new InteropTransactionRequestResponseData(null, null, commandId, null, transactionCode, state, expiration, extensionList, requestCode);
    }

    public static InteropTransactionRequestResponseData build(@NotNull String transactionCode, @NotNull InteropActionState state,
                                                              LocalDateTime expiration, List<ExtensionData> extensionList, @NotNull String requestCode) {
        return build(null, transactionCode, state, expiration, extensionList, requestCode);
    }

    public static InteropTransactionRequestResponseData build(Long commandId, @NotNull String transactionCode, @NotNull InteropActionState state,
                                                              @NotNull String requestCode) {
        return build(commandId, transactionCode, state, null, null, requestCode);
    }

    public static InteropTransactionRequestResponseData build(@NotNull String transactionCode, @NotNull InteropActionState state,
                                                              @NotNull String requestCode) {
        return build(null, transactionCode, state, requestCode);
    }

    public String getRequestCode() {
        return requestCode;
    }
}
