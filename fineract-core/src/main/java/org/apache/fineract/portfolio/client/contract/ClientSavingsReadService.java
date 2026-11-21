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
package org.apache.fineract.portfolio.client.contract;

/**
 * Core read-contract exposing only the savings checks required by the client feature, so that the client module does
 * not need a compile-time dependency on the savings domain/repository types. Implemented by the savings module.
 */
public interface ClientSavingsReadService {

    /**
     * Validates that a savings product with the given id exists, throwing {@code SavingsProductNotFoundException} if
     * not.
     */
    void validateSavingsProductExists(Long savingsProductId);

    /**
     * Returns {@code true} if the client has any savings account that is active, approved or submitted-and-pending,
     * i.e. a non-closed savings account that prevents the client from being closed.
     */
    boolean hasNonClosedSavingsAccountsForClient(Long clientId);

    /**
     * Returns {@code true} if the savings account with the given id belongs to the given client. Throws
     * {@code SavingsAccountNotFoundException} if the savings account does not exist.
     */
    boolean isSavingsAccountForClient(Long savingsId, Long clientId);
}
