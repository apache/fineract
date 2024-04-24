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
package org.apache.fineract.infrastructure.core.api.mvc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.fineract.infrastructure.core.domain.ExternalId;

/**
 * Filters empty ExternalId instances during JSON serialization with Jackson.
 */
public class JacksonExternalIdExcludeFilter {

    @Override
    @SuppressFBWarnings(value = { "EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS",
            "EQ_UNUSUAL" }, justification = "The equals method is only designed to exclude empty ExternalId by Jackson.")

    public boolean equals(final Object obj) {
        return obj == null || (obj instanceof ExternalId externalId && externalId.isEmpty());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
