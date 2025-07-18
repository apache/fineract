package org.apache.fineract.organisation.monetary.data;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CurrencyCreateRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String code;
  private String name;
  private Integer decimalPlaces;
  private Integer inMultiplesOf;
  private String displaySymbol;
  private String nameCode;

}
