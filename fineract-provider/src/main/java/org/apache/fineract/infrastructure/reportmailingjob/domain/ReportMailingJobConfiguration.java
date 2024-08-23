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
package org.apache.fineract.infrastructure.reportmailingjob.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_report_mailing_job_configuration", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "name" }, name = "unique_name") })
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ReportMailingJobConfiguration extends AbstractPersistableCustom<Long> {

    private static final long serialVersionUID = 3099279770861263184L;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;

    /**
     * creates an instance of the ReportMailingJobConfiguration class
     *
     * @return ReportMailingJobConfiguration object
     **/
    public static ReportMailingJobConfiguration newInstance(final String name, final String value) {
        return new ReportMailingJobConfiguration().setName(name).setValue(value);
    }
}
