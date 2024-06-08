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
package org.apache.fineract.infrastructure.jobs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "job_parameters")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class JobParameter extends AbstractPersistableCustom<Long> {

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "parameter_name", nullable = true)
    private String parameterName;

    @Column(name = "parameter_value", nullable = true)
    private String parameterValue;

    public static JobParameter getInstance(final Long jobId, final String parameterName, final String parameterValue) {
        return new JobParameter().setJobId(jobId).setParameterName(parameterName).setParameterValue(parameterValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JobParameter)) {
            return false;
        }
        JobParameter jobParameter = (JobParameter) obj;
        return Objects.equals(jobParameter.getJobId(), this.getJobId())
                && Objects.equals(jobParameter.getParameterName(), this.getParameterName())
                && Objects.equals(jobParameter.getParameterValue(), this.getParameterValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, parameterName, parameterValue);
    }
}
