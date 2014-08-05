/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.data;

import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;

public class SurveyDataTableData {

    @SuppressWarnings("unused")
    private final DatatableData datatableData;

    @SuppressWarnings("unused")
    private final boolean enabled;

    public static SurveyDataTableData create(final DatatableData datatableData, final boolean enabled) {

        return new SurveyDataTableData(datatableData, enabled);
    }

    private SurveyDataTableData(final DatatableData datatableData, final boolean enabled) {
        this.datatableData = datatableData;
        this.enabled = enabled;
    }
}
