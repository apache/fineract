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

import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import org.springframework.data.domain.Persistable;

/**
 * Abstract base class for entities.
 *
 * Inspired by {@link org.springframework.data.jpa.domain.AbstractPersistable}, but Id is always Long (and this class
 * thus does not require generic parameterization), and auto-generation is of strategy
 * {@link javax.persistence.GenerationType#IDENTITY}.
 *
 * The {@link #equals(Object)} and {@link #hashCode()} methods are NOT implemented here, which is untypical for JPA
 * (it's usually implemented based on the Id), because "we end up with issues on OpenJPA" (TODO clarify this).
 */
@MappedSuperclass
public abstract class AbstractPersistableCustom implements Persistable<Long>, Serializable {

    private static final long serialVersionUID = 9181640245194392646L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    protected void setId(final Long id) {
        this.id = id;
    }

    @Override
    @Transient // DATAJPA-622
    public boolean isNew() {
        return null == this.id;
    }
}
