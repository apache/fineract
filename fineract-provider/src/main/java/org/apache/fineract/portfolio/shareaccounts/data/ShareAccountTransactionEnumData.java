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
package org.apache.fineract.portfolio.shareaccounts.data;

public class ShareAccountTransactionEnumData {

    private final Long id;
    private final String code;
    private final String value;

    private final boolean isApplied;
    private final boolean isApproved;
    private final boolean isRejected;
    private final boolean isPurchased;
    private final boolean isRedeemed;
    private final boolean isChargePayment;

    public ShareAccountTransactionEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.isApplied = Long.valueOf(100).equals(this.id);
        this.isApproved = Long.valueOf(300).equals(this.id);
        this.isRejected = Long.valueOf(400).equals(this.id);
        this.isPurchased = Long.valueOf(500).equals(this.id);
        this.isRedeemed = Long.valueOf(600).equals(this.id);
        this.isChargePayment = Long.valueOf(700).equals(this.id);
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isApplied() {
        return this.isApplied;
    }

    public boolean isApproved() {
        return this.isApproved;
    }

    public boolean isRejected() {
        return this.isRejected;
    }

    public boolean isPurchased() {
        return this.isPurchased;
    }

    public boolean isRedeemed() {
        return this.isRedeemed;
    }

    
    public boolean isChargePayment() {
        return this.isChargePayment;
    }

}
