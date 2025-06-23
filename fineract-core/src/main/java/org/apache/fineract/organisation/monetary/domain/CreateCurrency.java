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
package org.apache.fineract.organisation.monetary.domain;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "m_currency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCurrency extends AbstractPersistableCustom<Long>{
	private static final long serialVersionUID = 7985707786610158363L;

	@Column(name = "code", nullable = false, length = 3)
   public String code;
	
	@Column(name = "name", nullable = false, length = 50)
   public String name;
   
   @Column(name = "decimal_places", nullable = false)
   public Integer decimalPlaces;
   
   @Column(name = "currency_multiplesof")
   public Integer inMultiplesOf;
   
   @Column(name = "display_symbol", nullable = true, length = 10)
   public String displaySymbol;
   
   @Column(name = "internationalized_name_code", nullable = false, length = 50)
   public String nameCode;
   
}
