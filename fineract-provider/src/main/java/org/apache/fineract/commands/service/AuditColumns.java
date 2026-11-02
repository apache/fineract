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
package org.apache.fineract.commands.service;

import static org.apache.fineract.commands.domain.CommandProcessingResultType.AWAITING_APPROVAL;
import static org.apache.fineract.commands.domain.CommandProcessingResultType.REJECTED;
import static org.apache.fineract.commands.domain.CommandProcessingResultType.UNDER_PROCESSING;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder.WhereLogicalOperator;

/**
 * Each identifier of a command is reported twice by the audit API: the value the command returned and the value it was
 * submitted with. Rows written before the result columns existed hold the returned values in the request columns, so
 * these helpers pick the right column per row.
 */
public final class AuditColumns {

    private static final String RESULT_IDS_WRITTEN = "aud.result_ids_written";
    private static final String SUBMITTED_IDS_KEPT = "aud.submitted_ids_kept";
    private static final String STATUS = "aud.status";
    /*
     * Statuses of commands that never reached the point where their submitted identifiers used to be overwritten, for
     * rows inserted before they were kept.
     */
    private static final List<Integer> NEVER_OVERWRITTEN = Stream.of(AWAITING_APPROVAL, REJECTED, UNDER_PROCESSING)
            .map(CommandProcessingResultType::getValue).toList();

    private AuditColumns() {}

    /**
     * SQL expression for the returned value of the given request column, e.g. {@code resource_id}. Commands that were
     * never processed have no returned values; for those the submitted value is reported, as it always was.
     */
    public static String returned(final String column) {
        return "(CASE WHEN " + RESULT_IDS_WRITTEN + " = true THEN aud.result_" + column + " ELSE aud." + column + " END)";
    }

    /**
     * SQL expression for the submitted value of the given request column. Rows inserted before submitted identifiers
     * were kept had them overwritten by the returned ones once processing got that far, which a processed or failed
     * status cannot rule out; the value is null on those rows.
     */
    public static String submitted(final String column) {
        return "(CASE WHEN " + SUBMITTED_IDS_KEPT + " = true OR " + RESULT_IDS_WRITTEN + " = true OR " + STATUS + " IN ("
                + NEVER_OVERWRITTEN.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ") THEN aud." + column + " END)";
    }

    /**
     * Adds an equality filter on the returned value of the given request column, if the value is not null.
     */
    public static void addReturnedCriteria(final SQLBuilder criteria, final String column, final Object value) {
        if (value == null) {
            return;
        }
        // AND binds tighter than OR: (written AND result_column = ?) OR (not written AND column = ?)
        criteria.addSubOperation((SQLBuilder sub) -> {
            sub.addCriteria(RESULT_IDS_WRITTEN + " =", true, WhereLogicalOperator.NONE);
            sub.addCriteria("aud.result_" + column + " =", value, WhereLogicalOperator.AND);
            sub.addCriteria(RESULT_IDS_WRITTEN + " =", false, WhereLogicalOperator.OR);
            sub.addCriteria("aud." + column + " =", value, WhereLogicalOperator.AND);
        });
    }

    /**
     * Adds an equality filter on the submitted value of the given request column, if the value is not null.
     */
    public static void addSubmittedCriteria(final SQLBuilder criteria, final String column, final Object value) {
        if (value == null) {
            return;
        }
        criteria.addCriteria("aud." + column + " =", value);
        criteria.addSubOperation((SQLBuilder sub) -> {
            sub.addCriteria(SUBMITTED_IDS_KEPT + " =", true, WhereLogicalOperator.NONE);
            sub.addCriteria(RESULT_IDS_WRITTEN + " =", true, WhereLogicalOperator.OR);
            NEVER_OVERWRITTEN.forEach(status -> sub.addCriteria(STATUS + " =", status, WhereLogicalOperator.OR));
        });
    }
}
