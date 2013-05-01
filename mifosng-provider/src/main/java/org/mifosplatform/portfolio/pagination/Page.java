/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.pagination;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Page<E> {

    private int totalFilteredRecords;
    private List<E> pageItems = new ArrayList<E>();

    public void setPageItems(List<E> pageItems) {
        this.pageItems = pageItems;
    }

    public int getTotalFilteredRecords() {
        return this.totalFilteredRecords;
    }

    public void setTotalFilteredRecords(int totalFilteredRecords) {
        this.totalFilteredRecords = totalFilteredRecords;
    }

    public List<E> getPageItems() {
        return pageItems;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
