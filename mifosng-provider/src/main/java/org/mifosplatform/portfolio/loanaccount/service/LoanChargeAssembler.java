package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;
import org.mifosplatform.portfolio.charge.exception.ChargeIsNotActiveException;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeNotFoundException;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class LoanChargeAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final ChargeRepository chargeRepository;
    private final LoanChargeRepository loanChargeRepository;

    @Autowired
    public LoanChargeAssembler(final FromJsonHelper fromApiJsonHelper, final ChargeRepository chargeRepository,
            final LoanChargeRepository loanChargeRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chargeRepository = chargeRepository;
        this.loanChargeRepository = loanChargeRepository;
    }

    public Set<LoanCharge> fromParsedJson(final JsonElement element) {

        final Set<LoanCharge> loanCharges = new HashSet<LoanCharge>();

        final BigDecimal principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("principal", element);

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final Locale locale = fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.has("charges") && topLevelJsonElement.get("charges").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("charges").getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {

                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();

                    final Long id = fromApiJsonHelper.extractLongNamed("id", loanChargeElement);
                    final Long chargeId = fromApiJsonHelper.extractLongNamed("chargeId", loanChargeElement);
                    final BigDecimal amount = fromApiJsonHelper.extractBigDecimalNamed("amount", loanChargeElement, locale);
                    final Integer chargeTimeType = fromApiJsonHelper.extractIntegerNamed("chargeTimeType", loanChargeElement, locale);
                    final Integer chargeCalculationType = fromApiJsonHelper.extractIntegerNamed("chargeCalculationType", loanChargeElement,
                            locale);
                    final LocalDate specifiedDueDate = fromApiJsonHelper.extractLocalDateNamed("specifiedDueDate", loanChargeElement,
                            dateFormat, locale);

                    if (id == null) {
                        final Charge chargeDefinition = this.chargeRepository.findOne(chargeId);
                        if (chargeDefinition == null || chargeDefinition.isDeleted()) { throw new ChargeNotFoundException(chargeId); }

                        if (!chargeDefinition.isActive()) { throw new ChargeIsNotActiveException(chargeId, chargeDefinition.getName()); }
                        ChargeTimeType chargeTime = null;
                        if (chargeTimeType != null) {
                            ChargeTimeType.fromInt(chargeTimeType);
                        }
                        ChargeCalculationType chargeCalculation = null;
                        if (chargeCalculationType != null) {
                            ChargeCalculationType.fromInt(chargeCalculationType);
                        }
                        final LoanCharge loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition, principal, amount, chargeTime,
                                chargeCalculation, specifiedDueDate);
                        loanCharges.add(loanCharge);
                    } else {
                        final Long loanChargeId = id;
                        final LoanCharge loanCharge = this.loanChargeRepository.findOne(loanChargeId);
                        if (loanCharge == null) { throw new LoanChargeNotFoundException(loanChargeId); }

                        loanCharge.update(amount, specifiedDueDate, principal);

                        loanCharges.add(loanCharge);
                    }
                }
            } else {
                // no charges passed, use existing ones against loan
            }

        }

        return loanCharges;
    }
}