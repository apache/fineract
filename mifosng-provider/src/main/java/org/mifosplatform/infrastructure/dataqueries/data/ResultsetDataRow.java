package org.mifosplatform.infrastructure.dataqueries.data;

import java.util.ArrayList;
import java.util.List;

public class ResultsetDataRow {

    private List<String> row = new ArrayList<String>();

    public ResultsetDataRow() {

    }

    public List<String> getRow() {
        return row;
    }

    public void setRow(List<String> row) {
        this.row = row;
    }
}
