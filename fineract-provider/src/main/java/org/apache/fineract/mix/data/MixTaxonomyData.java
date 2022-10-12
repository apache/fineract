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
public class MixTaxonomyData {

    public static final Integer PORTFOLIO = 0;
    public static final Integer BALANCESHEET = 1;
    public static final Integer INCOME = 2;
    public static final Integer EXPENSE = 3;

    @SuppressWarnings("unused")
    private Long id;
    private String name;
    private String namespace;
    private String dimension;
    private Integer type;
    @SuppressWarnings("unused")
    private String description;

    public boolean isPortfolio() {
        return this.type == 5;
    }
}
