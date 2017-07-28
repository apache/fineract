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
package org.apache.fineract.infrastructure.entityaccess.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_entity_relation")
public class FineractEntityRelation extends AbstractPersistableCustom<Long> {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "relationId", orphanRemoval = true)
    private Set<FineractEntityToEntityMapping> fineractEntityToEntityMapping = new HashSet<>();
    
    @Column(name = "from_entity_type", nullable = false, length = 10)
    private String fromEntityType;

    @Column(name = "to_entity_type", nullable = false, length = 10)
    private String toEntityType;

    @Column(name = "code_name", nullable = false, length = 50)
    private String codeName;

   /* private FineractEntityRelation(final String fromEntityType, final String toEntityType, final String codeName) {
        this.fromEntityType = fromEntityType;
        this.toEntityType = toEntityType;
        this.codeName = codeName;
    }*/
    
    
    public FineractEntityRelation() {
        // TODO Auto-generated constructor stub
    }


    
    public Set<FineractEntityToEntityMapping> getFineractEntityToEntityMapping() {
        return this.fineractEntityToEntityMapping;
    }


    
    public void setFineractEntityToEntityMapping(Set<FineractEntityToEntityMapping> fineractEntityToEntityMapping) {
        this.fineractEntityToEntityMapping = fineractEntityToEntityMapping;
    }


    
    public String getFromEntityType() {
        return this.fromEntityType;
    }


    
    public void setFromEntityType(String fromEntityType) {
        this.fromEntityType = fromEntityType;
    }


    
    public String getToEntityType() {
        return this.toEntityType;
    }


    
    public void setToEntityType(String toEntityType) {
        this.toEntityType = toEntityType;
    }


    
    public String getCodeName() {
        return this.codeName;
    }


    
    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }
    
    

    
    
  
}
