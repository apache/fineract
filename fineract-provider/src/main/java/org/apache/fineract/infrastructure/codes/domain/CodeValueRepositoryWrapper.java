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
package org.apache.fineract.infrastructure.codes.domain;

import org.apache.fineract.infrastructure.codes.exception.CodeValueNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link CodeValueRepository} that is responsible for checking if
 * {@link CodeValue} is returned when using <code>findOne</code> and
 * <code>findByCodeNameAndId</code> repository methods and throwing an
 * appropriate not found exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link CodeValueRepository} is required.
 * </p>
 */
@Service
public class CodeValueRepositoryWrapper {

    private final CodeValueRepository repository;

    @Autowired
    public CodeValueRepositoryWrapper(final CodeValueRepository repository) {
        this.repository = repository;
    }

    public CodeValue findOneWithNotFoundDetection(final Long id) {
        final CodeValue codeValue = this.repository.findOne(id);
        if (codeValue == null) { throw new CodeValueNotFoundException(id); }
        return codeValue;
    }

    public CodeValue findOneByCodeNameAndIdWithNotFoundDetection(final String codeName, final Long id) {
        final CodeValue codeValue = this.repository.findByCodeNameAndId(codeName, id);
        if (codeValue == null) { throw new CodeValueNotFoundException(codeName, id); }
        return codeValue;
    }
    
    public CodeValue findOneByCodeNameAndLabelWithNotFoundDetection(final String codeName, final String label) {
        final CodeValue codeValue = this.repository.findByCodeNameAndLabel(codeName, label);
        if (codeValue == null) { throw new CodeValueNotFoundException(codeName, label); }
        return codeValue;
    }
    
}