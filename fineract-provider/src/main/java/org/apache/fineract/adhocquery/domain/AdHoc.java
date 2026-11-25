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
package org.apache.fineract.adhocquery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "m_adhoc")
public class AdHoc extends AbstractAuditableCustom {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "query", length = 2000)
    private String query;

    @Column(name = "table_name", length = 100)
    private String tableName;

    @Column(name = "table_fields", length = 2000)
    private String tableFields;

    @Column(name = "email", length = 500)
    private String email;

    @Column(name = "report_run_frequency_code")
    private Long reportRunFrequency;

    @Column(name = "report_run_every")
    private Long reportRunEvery;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
