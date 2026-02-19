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
package org.apache.fineract.mix.domain;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("mix_taxonomy")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public final class MixTaxonomy implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("namespace_id")
    private Long namespaceId;

    @Column("dimension")
    private String dimension;

    @Column("type")
    private Integer type;

    @Column("description")
    private String description;

    // TODO: this is never used, but creates an error on MySQL (tinyint vs boolean mapping)
    // @Column("need_mapping")
    // private Boolean needMapping;
}
