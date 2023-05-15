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
package org.apache.fineract.infrastructure.core.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.PlatformInternalServerException;

/**
 * ExternalId Value object
 */
@Getter
@EqualsAndHashCode
public class ExternalId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;
    private static final ExternalId empty = new ExternalId();
    private final String value;

    private ExternalId() {
        this.value = null;
    }

    public ExternalId(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("error.external.id.cannot.be.blank");
        }
        this.value = value;
    }

    /**
     * @return Create a new ExternalId object where value is a newly generated UUID
     */
    public static ExternalId generate() {
        return new ExternalId(UUID.randomUUID().toString());
    }

    /**
     * @return Create and return an empty ExternalId object
     */
    public static ExternalId empty() {
        return empty;
    }

    /**
     * @return whether value is null for the ExternalId object (return true if value is null)
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * Throws exception if value was not set (value is null) for this object
     *
     * @throws PlatformInternalServerException
     *             if value was not set (value is null) for this object
     */
    public void throwExceptionIfEmpty() {
        if (isEmpty()) {
            throw new PlatformInternalServerException("error.external.id.is.not.set", "Internal state violation: External id is not set");
        }
    }
}
