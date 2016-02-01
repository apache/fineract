/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.data;

public class LookupTableEntry {

    private Integer valueFrom;
    private Integer valueTo;
    private Double score;

    public LookupTableEntry() {
        super();
    }

    public LookupTableEntry(final Integer valueFrom, final Integer valueTo, final Double score) {
        super();
        this.valueFrom = valueFrom;
        this.valueTo = valueTo;
        this.score = score;
    }

    public Integer getValueFrom() {
        return valueFrom;
    }

    public void setValueFrom(Integer valueFrom) {
        this.valueFrom = valueFrom;
    }

    public Integer getValueTo() {
        return valueTo;
    }

    public void setValueTo(Integer valueTo) {
        this.valueTo = valueTo;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
