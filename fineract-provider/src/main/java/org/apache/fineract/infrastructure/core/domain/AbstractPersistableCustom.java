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

import org.springframework.data.domain.Persistable;


@MappedSuperclass
public abstract class AbstractPersistableCustom<PK extends Serializable> implements Persistable<Long> {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.data.domain.Persistable#getId()
         */
        @Override
        public Long getId() {
                return id;
        }

        /**
         * Sets the id of the entity.
         * 
         * @param id the id to set
         */
        protected void setId(final Long id) {

                this.id = id;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.data.domain.Persistable#isNew()
         */
        @Override
        public boolean isNew() {

                return null == this.id;
        }

        //We have removed toString(), hashCode() and equals() methods. By adding them end up issues with OpenJPA
}
