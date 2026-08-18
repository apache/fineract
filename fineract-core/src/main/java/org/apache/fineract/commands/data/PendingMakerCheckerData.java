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

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * Lightweight DTO representing a single pending maker-checker entry for a resource.
 */
@Getter
@Builder
public class PendingMakerCheckerData {

    /** The maker-checker command source id (m_portfolio_command_source.id) */
    private final Long id;

    /** e.g. "APPROVE", "DISBURSE", "CREATE", "ACTIVATE" */
    private final String actionName;

    /** e.g. "LOAN", "CLIENT", "SAVINGSACCOUNT" */
    private final String entityName;

    /** Human-readable label, e.g. "APPROVE_LOAN" */
    private final String permissionCode;

    /** Username of the maker who submitted this command */
    private final String makerUsername;

    /** When the maker submitted this command */
    private final OffsetDateTime madeOnDate;
}
