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
package org.apache.fineract.accounting.glaccount.domain;

import org.apache.fineract.accounting.glaccount.exception.GLAccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link GLAccountRepository} .
 * </p>
 */
@Service
public class GLAccountRepositoryWrapper {

    private final GLAccountRepository repository;

    @Autowired
    public GLAccountRepositoryWrapper(final GLAccountRepository repository) {
        this.repository = repository;
    }

    public GLAccount findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new GLAccountNotFoundException(id));
    }

    // finding account id by glcode for opening balance bulk import
    public GLAccount findOneByGlCodeWithNotFoundDetection(final String glCode) {
        return this.repository.findOneByGlCode(glCode).orElseThrow(() -> new GLAccountNotFoundException(glCode));
    }

}
