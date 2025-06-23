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
