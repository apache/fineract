/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.audit.data;

import org.joda.time.DateTime;

/**
 * Immutable data object representing client data.
 */
public final class AuditData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String actionName;
    @SuppressWarnings("unused")
    private final String entityName;
    @SuppressWarnings("unused")
    private final Long resourceId;
    @SuppressWarnings("unused")
    private final String maker;
    @SuppressWarnings("unused")
    private final DateTime madeOnDate;
    @SuppressWarnings("unused")
    private final String checker;
    @SuppressWarnings("unused")
    private final DateTime checkedOnDate;
    @SuppressWarnings("unused")
    private final String processingResult;
    @SuppressWarnings("unused")
    private final String commandAsJson;

    public AuditData(final Long id, final String actionName, final String entityName, final Long resourceId, final String maker,
            final DateTime madeOnDate, final String checker, final DateTime checkedOnDate, final String processingResult,
            final String commandAsJson) {

        this.id = id;
        this.actionName = actionName;
        this.entityName = entityName;
        this.resourceId = resourceId;
        this.maker = maker;
        this.madeOnDate = madeOnDate;
        this.checker = checker;
        this.checkedOnDate = checkedOnDate;
        this.commandAsJson = commandAsJson;
        this.processingResult = processingResult;
    }
}