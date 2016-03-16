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
package org.apache.fineract.portfolio.paymenttype.data;

public class PaymentTypeData {

    @SuppressWarnings("unused")
    private Long id;
    @SuppressWarnings("unused")
    private String name;
    @SuppressWarnings("unused")
    private String description;
    @SuppressWarnings("unused")
    private Boolean isCashPayment;
    @SuppressWarnings("unused")
    private Long position;
    @SuppressWarnings("unused")
	private String fundSourceAccountName;
    @SuppressWarnings("unused")
	private Long fundSourceAccountId;

    public PaymentTypeData(final Long id, final String name, final String description, final Boolean isCashPayment, final Long position,
    		final Long fundSourceAccountId, final String fundSourceAccountName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isCashPayment = isCashPayment;
        this.position = position;
        this.fundSourceAccountId = fundSourceAccountId;
        this.fundSourceAccountName = fundSourceAccountName;
    }

    public static PaymentTypeData instance(final Long id, final String name, final String description, final Boolean isCashPayment,
            final Long position, final Long fundSourceAccountId, final String fundSourceAccountName) {
        return new PaymentTypeData(id, name, description, isCashPayment, position, fundSourceAccountId, fundSourceAccountName);
    }

    public static PaymentTypeData instance(final Long id, final String name) {
        String description = null;
        Boolean isCashPayment = null;
        Long position = null;
        Long fundSourceAccountId = null;
        String fundSourceAccountName = null;
        return new PaymentTypeData(id, name, description, isCashPayment, position, fundSourceAccountId, fundSourceAccountName);
    }
}
