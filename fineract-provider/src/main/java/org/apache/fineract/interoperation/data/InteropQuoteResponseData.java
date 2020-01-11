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

import org.apache.fineract.interoperation.domain.InteropActionState;
import org.joda.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class InteropQuoteResponseData extends InteropResponseData {

    @NotNull
    private final String quoteCode;

    private MoneyData fspFee;

    private MoneyData fspCommission;

    
    private InteropQuoteResponseData(Long resourceId, Long officeId, Long commandId, Map<String, Object> changesOnly,
                                     @NotNull String transactionCode, @NotNull InteropActionState state, LocalDateTime expiration,
                                     List<ExtensionData> extensionList, @NotNull String quoteCode, MoneyData fspFee, MoneyData fspCommission) {
        super(resourceId, officeId, commandId, changesOnly, transactionCode, state, expiration, extensionList);
        this.quoteCode = quoteCode;
        this.fspFee = fspFee;
        this.fspCommission = fspCommission;
    }

    public static InteropQuoteResponseData build(Long commandId, @NotNull String transactionCode, @NotNull InteropActionState state,
                                                 LocalDateTime expiration, List<ExtensionData> extensionList, @NotNull String quoteCode,
                                                 MoneyData fspFee, MoneyData fspCommission) {
        return new InteropQuoteResponseData(null, null, commandId, null, transactionCode, state, expiration, extensionList,
                quoteCode, fspFee, fspCommission);
    }

    public static InteropQuoteResponseData build(@NotNull String transactionCode, @NotNull InteropActionState state, LocalDateTime expiration,
                                                 List<ExtensionData> extensionList, @NotNull String quoteCode, MoneyData fspFee,
                                                 MoneyData fspCommission) {
        return build(null, transactionCode, state, expiration, extensionList, quoteCode, fspFee, fspCommission);
    }

    public static InteropQuoteResponseData build(Long commandId, @NotNull String transactionCode, @NotNull InteropActionState state,
                                                 @NotNull String quoteCode) {
        return build(commandId, transactionCode, state, null, null, quoteCode, null, null);
    }

    public static InteropQuoteResponseData build(@NotNull String transactionCode, @NotNull InteropActionState state, @NotNull String quoteCode) {
        return build(null, transactionCode, state, quoteCode);
    }

    public String getQuoteCode() {
        return quoteCode;
    }

    public MoneyData getFspFee() {
        return fspFee;
    }

    public void setFspFee(MoneyData fspFee) {
        this.fspFee = fspFee;
    }

    public MoneyData getFspCommission() {
        return fspCommission;
    }

    public void setFspCommission(MoneyData fspCommission) {
        this.fspCommission = fspCommission;
    }
}
