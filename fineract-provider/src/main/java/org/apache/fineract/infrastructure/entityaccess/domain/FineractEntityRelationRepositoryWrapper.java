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

import org.apache.fineract.infrastructure.entityaccess.exception.FineractEntityAccessNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FineractEntityRelationRepositoryWrapper {

    private final FineractEntityRelationRepository fineractEntityRelationRepository;

    @Autowired
    private FineractEntityRelationRepositoryWrapper(final FineractEntityRelationRepository fineractEntityRelationRepository) {
        this.fineractEntityRelationRepository = fineractEntityRelationRepository;

    }
    
    public FineractEntityRelation findOneWithNotFoundDetection(final Long id) {
        final FineractEntityRelation fineractEntityRelation = this.fineractEntityRelationRepository.findOne(id);
        if (fineractEntityRelation == null) { throw new FineractEntityAccessNotFoundException(id); }
        return fineractEntityRelation;
    }
    
    public FineractEntityRelation findOneByCodeName(final String codeName) {
    	    	 final FineractEntityRelation fineractEntityRelation = this.fineractEntityRelationRepository.findOneByCodeName(codeName) ;
    	    	 if (fineractEntityRelation == null) { throw new FineractEntityAccessNotFoundException(codeName); }
    	         return fineractEntityRelation;
    	    }

}
