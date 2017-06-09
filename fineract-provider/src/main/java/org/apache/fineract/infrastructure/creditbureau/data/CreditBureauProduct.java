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
package org.apache.fineract.infrastructure.creditbureau.data;

public class CreditBureauProduct {

	private final long creditBureauProductId;

	private final String creditBureauProductName;

	private final long creditBureauMasterId;

	private CreditBureauProduct(final long creditBureauProductId, final String creditBureauProductName,
			final long creditBureauMasterId) {
		this.creditBureauProductId = creditBureauProductId;
		this.creditBureauProductName = creditBureauProductName;
		this.creditBureauMasterId = creditBureauMasterId;
	}

	public static CreditBureauProduct instance(final long creditBureauProductId, final String creditBureauProductName,
			final long creditBureauMasterId) {
		return new CreditBureauProduct(creditBureauProductId, creditBureauProductName, creditBureauMasterId);
	}

	public long getCreditBureauProductId() {
		return this.creditBureauProductId;
	}

	public String getCreditBureauProductName() {
		return this.creditBureauProductName;
	}

	public long getCreditBureauMasterId() {
		return this.creditBureauMasterId;
	}

}
