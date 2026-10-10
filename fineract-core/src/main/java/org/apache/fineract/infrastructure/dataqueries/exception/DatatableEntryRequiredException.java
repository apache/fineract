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
package org.apache.fineract.infrastructure.dataqueries.exception;

import java.util.List;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;

/**
 * A {@link AbstractPlatformDomainRuleException} thrown when datatable resources are not found.
 */
public class DatatableEntryRequiredException extends AbstractPlatformDomainRuleException {

    public DatatableEntryRequiredException(String datatableName) {
        super("error.msg.entry.required.in.datatable." + datatableName,
                "Please fill in the data table " + datatableName + " before continuing.", datatableName);
    }

    public DatatableEntryRequiredException(List<String> datatableNames, String entityName, Integer status) {
        super("error.msg.entry.required.in.datatable." + datatableNames,
                buildRequiredEntryMessage(datatableNames, EntityTables.fromEntityName(entityName), StatusEnum.fromInt(status)),
                datatableNames.toString());
    }

    private static String buildRequiredEntryMessage(List<String> datatableNames, EntityTables entity, StatusEnum status) {
        final String tables = (datatableNames.size() == 1 ? "data table " : "data tables ") + datatableNames;
        if (status == null || entity == null) {
            return "Please fill in the " + tables + " before continuing.";
        }
        return "Please fill in the " + tables + " before " + actionOf(status) + " this "
                + entity.getHumanReadableName().toLowerCase(Locale.ROOT) + ".";
    }

    private static String actionOf(StatusEnum status) {
        return switch (status) {
            case CREATE -> "creating";
            case APPROVE -> "approving";
            case ACTIVATE -> "activating";
            case WITHDRAWN -> "withdrawing";
            case REJECTED -> "rejecting";
            case CLOSE -> "closing";
            case WRITE_OFF -> "writing off";
            case RESCHEDULE -> "rescheduling";
            case OVERPAY -> "overpaying";
            case DISBURSE -> "disbursing";
        };
    }

    public DatatableEntryRequiredException(String datatableName, Long appTableId) {
        super("error.msg.entry.cannot.be.deleted.datatable." + datatableName + ".attached.to.entity.datatable.check",
                "The entry cannot be deleted, due to datatable " + datatableName + " is attached to an Entity-Datatable check",
                datatableName, appTableId);
    }
}
