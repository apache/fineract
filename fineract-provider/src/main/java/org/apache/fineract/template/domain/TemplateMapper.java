/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.template.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_templatemappers")
public class TemplateMapper extends AbstractPersistable<Long> {

    @Column(name = "mapperorder")
    private int mapperorder;

    @Column(name = "mapperkey")
    private String mapperkey;

    @Column(name = "mappervalue")
    private String mappervalue;

    protected TemplateMapper() {}

    public TemplateMapper(final int mapperorder, final String mapperkey, final String mappervalue) {
        this.mapperorder = mapperorder;
        this.mapperkey = mapperkey;
        this.mappervalue = mappervalue;
    }

    public String getMapperkey() {
        return this.mapperkey;
    }

    public int getMapperorder() {
        return this.mapperorder;
    }

    public void setMapperorder(final int mapperorder) {
        this.mapperorder = mapperorder;
    }

    public void setMapperkey(final String mapperkey) {
        this.mapperkey = mapperkey;
    }

    public String getMappervalue() {
        return this.mappervalue;
    }

    public void setMappervalue(final String mappervalue) {
        this.mappervalue = mappervalue;
    }

}
