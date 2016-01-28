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
package org.apache.fineract.commands.data;

import java.util.Collection;
import java.util.List;

import org.apache.fineract.useradministration.data.AppUserData;

/**
 * Immutable data object representing audit search results.
 */
public final class AuditSearchData {

    @SuppressWarnings("unused")
    private final Collection<AppUserData> appUsers;
    @SuppressWarnings("unused")
    private final List<String> actionNames;
    @SuppressWarnings("unused")
    private final List<String> entityNames;
    @SuppressWarnings("unused")
    private final Collection<ProcessingResultLookup> processingResults;

    public AuditSearchData(final Collection<AppUserData> appUsers, final List<String> apiOperations, final List<String> resources,
            final Collection<ProcessingResultLookup> processingResults) {
        this.appUsers = appUsers;
        this.actionNames = apiOperations;
        this.entityNames = resources;
        this.processingResults = processingResults;
    }
}