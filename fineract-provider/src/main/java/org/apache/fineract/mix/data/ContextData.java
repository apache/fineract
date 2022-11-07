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
package org.apache.fineract.mix.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ContextData {

    private String dimensionType;
    private String dimension;
    private Integer periodType;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.dimension == null ? 0 : this.dimension.hashCode());
        result = prime * result + (this.dimensionType == null ? 0 : this.dimensionType.hashCode());
        result = prime * result + (this.periodType == null ? 0 : this.periodType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ContextData)) {
            return false;
        }
        final ContextData other = (ContextData) obj;
        if (this.dimension == null) {
            if (other.dimension != null) {
                return false;
            }
        } else if (!this.dimension.equals(other.dimension)) {
            return false;
        }
        if (this.dimensionType == null) {
            if (other.dimensionType != null) {
                return false;
            }
        } else if (!this.dimensionType.equals(other.dimensionType)) {
            return false;
        }
        if (this.periodType == null) {
            if (other.periodType != null) {
                return false;
            }
        } else if (!this.periodType.equals(other.periodType)) {
            return false;
        }
        return true;
    }
}
