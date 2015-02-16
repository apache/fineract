/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_entity_relation")
public class MifosEntityRelation extends AbstractPersistable<Long> {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "relationId", orphanRemoval = true)
    private Set<MifosEntityToEntityMapping> mifosEntityToEntityMapping = new HashSet<>();
    
    @Column(name = "from_entity_type", nullable = false, length = 10)
    private String fromEntityType;

    @Column(name = "to_entity_type", nullable = false, length = 10)
    private String toEntityType;

    @Column(name = "code_name", nullable = false, length = 50)
    private String codeName;

   /* private MifosEntityRelation(final String fromEntityType, final String toEntityType, final String codeName) {
        this.fromEntityType = fromEntityType;
        this.toEntityType = toEntityType;
        this.codeName = codeName;
    }*/
    
    
    public MifosEntityRelation() {
        // TODO Auto-generated constructor stub
    }


    
    public Set<MifosEntityToEntityMapping> getMifosEntityToEntityMapping() {
        return this.mifosEntityToEntityMapping;
    }


    
    public void setMifosEntityToEntityMapping(Set<MifosEntityToEntityMapping> mifosEntityToEntityMapping) {
        this.mifosEntityToEntityMapping = mifosEntityToEntityMapping;
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
