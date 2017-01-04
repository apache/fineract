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
package org.apache.fineract.portfolio.products.service;

import java.util.Collection;
import java.util.Set;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.portfolio.products.data.ProductData;

public interface ProductReadPlatformService {

    public Page<ProductData> retrieveAllProducts(Integer offSet, Integer limit);

    public ProductData retrieveOne(final Long productId, boolean includeTemplate);

    public ProductData retrieveTemplate();

    public Set<String> getResponseDataParams();
    
    public Collection<ProductData> retrieveAllForLookup() ;
}
