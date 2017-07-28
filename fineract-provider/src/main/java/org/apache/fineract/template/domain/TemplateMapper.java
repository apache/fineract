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
package org.apache.fineract.template.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_templatemappers")
public class TemplateMapper extends AbstractPersistableCustom<Long> {

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
