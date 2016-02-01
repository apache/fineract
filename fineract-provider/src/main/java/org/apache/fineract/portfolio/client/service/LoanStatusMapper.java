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
package org.apache.fineract.portfolio.client.service;

public class LoanStatusMapper {

    private final Integer statusId;

    public LoanStatusMapper(final Integer statusId) {
        this.statusId = statusId;
    }

    public boolean isPendingApproval() {
        return Integer.valueOf(100).equals(this.statusId);
    }

    public boolean isAwaitingDisbursal() {
        return Integer.valueOf(200).equals(this.statusId);
    }

    public boolean isOpen() {
        return Integer.valueOf(300).equals(this.statusId);
    }

    public boolean isWithdrawnByClient() {
        return Integer.valueOf(400).equals(this.statusId);
    }

    public boolean isRejected() {
        return Integer.valueOf(500).equals(this.statusId);
    }

    public boolean isClosed() {
        return Integer.valueOf(600).equals(this.statusId) || isWithdrawnByClient() || isRejected();
    }
    
    public boolean isOverpaid() {
        return Integer.valueOf(700).equals(this.statusId);
    }
}