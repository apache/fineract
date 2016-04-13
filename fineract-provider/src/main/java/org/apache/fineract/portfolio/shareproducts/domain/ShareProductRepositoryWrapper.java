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
package org.apache.fineract.portfolio.shareproducts.domain;

import org.apache.fineract.portfolio.products.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShareProductRepositoryWrapper {

	private final ShareProductRepository shareProductRepository ;
	
	@Autowired
	public ShareProductRepositoryWrapper(final ShareProductRepository shareProductRepository) {
		this.shareProductRepository = shareProductRepository ;
	}
	
	public ShareProduct findOneWithNotFoundDetection(final Long productId) {
		ShareProduct product = this.shareProductRepository.findOne(productId) ;
		if(product == null) {
			throw new ProductNotFoundException(productId, "share") ;
		}
		return product ;
	}
	
	public void save(ShareProduct product) {
		this.shareProductRepository.save(product) ;
	}
	
	public void saveAndFlush(ShareProduct product) {
		this.shareProductRepository.saveAndFlush(product) ;
	}
}
