/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.data;

public class LookupTableData {

    private Long id;
    private String key;
    private String description;
    private Integer valueFrom;
    private Integer valueTo;
    private Double score;

    public LookupTableData() {
        super();
    }

    public LookupTableData(final Long id, final String key, final String description, final Integer valueFrom,
                           final Integer valueTo, final Double score) {
        super();
        this.id = id;
        this.key = key;
        this.description = description;
        this.valueFrom = valueFrom;
        this.valueTo = valueTo;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
