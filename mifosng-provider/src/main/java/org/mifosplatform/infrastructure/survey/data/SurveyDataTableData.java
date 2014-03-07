package org.mifosplatform.infrastructure.survey.data;

import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;

import java.util.List;

/**
 * Created by Cieyou on 2/27/14.
 */
public class SurveyDataTableData {

    @SuppressWarnings("unused")
    private final DatatableData datatableData;

    @SuppressWarnings("unused")
    private final boolean enabled ;



    public static SurveyDataTableData create(final DatatableData datatableData, final boolean enabled) {

        return new SurveyDataTableData(datatableData, enabled);
    }

    private SurveyDataTableData(final DatatableData datatableData, final boolean enabled) {
        this.datatableData = datatableData;
        this.enabled = enabled;
    }
}
