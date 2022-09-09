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
package org.apache.fineract.infrastructure.campaigns.email.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Immutable data object representing an Email configuration.
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class EmailConfigurationData {

    @SuppressWarnings("unused")
    private Long id;

    private String name;

    private String value;

    /**
     * @return an instance of the EmailConfigurationData class
     **/
    public static EmailConfigurationData instance(Long id, String name, String value) {
        return new EmailConfigurationData().setId(id).setName(name).setValue(value);
    }

}
