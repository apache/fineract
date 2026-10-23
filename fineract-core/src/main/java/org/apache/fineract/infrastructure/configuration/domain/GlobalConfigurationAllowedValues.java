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
package org.apache.fineract.infrastructure.configuration.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyValueNotAllowedException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;

/**
 * Registry of the global configurations whose {@code stringValue} is restricted to a fixed set of values. Values are
 * matched case-insensitively, and the update API stores them in their canonical spelling. Values stored before the
 * restriction existed are not rewritten, and one that is blank or not allowed is reported at startup by
 * {@code GlobalConfigurationAllowedValuesStartupCheck}. Reading such a value either resolves to the configuration's
 * default, which matches the value its changelog inserted, or, for a configuration without a default, fails with
 * {@link GlobalConfigurationPropertyValueNotAllowedException}. Configurations that decide which loans may be
 * transferred, or for how much, have no default: silently substituting one would change financial outcomes.
 * <p>
 * Restricting another configuration takes three steps: add a constant here and list it in {@link #ALL}, read it through
 * {@link AllowedValues#resolve} or {@link AllowedValues#resolveAll} instead of parsing the raw string, and make sure
 * its changelog inserts an allowed value (the integration tests check that every stored value is allowed).
 */
public final class GlobalConfigurationAllowedValues {

    private static final String SEPARATOR = ",";

    public static final AllowedValues<ChargeAccrualDateConfig> CHARGE_ACCRUAL_DATE = single(
            GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE, ChargeAccrualDateConfig.DUE_DATE);
    public static final AllowedValues<NextPaymentDueDateConfig> NEXT_PAYMENT_DUE_DATE = single(
            GlobalConfigurationConstants.NEXT_PAYMENT_DUE_DATE, NextPaymentDueDateConfig.EARLIEST_UNPAID_DATE);
    public static final AllowedValues<ChargeAccrualTimeConfig> WCL_CHARGE_ACCRUAL_TIME = single(
            GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME, ChargeAccrualTimeConfig.EOD);
    public static final AllowedValues<ChargeAccrualTimeConfig> PL_CHARGE_ACCRUAL_TIME = single(
            GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME, ChargeAccrualTimeConfig.EOD);
    public static final AllowedValues<OutstandingInterestStrategyConfig> OUTSTANDING_INTEREST_STRATEGY = singleWithoutDefault(
            GlobalConfigurationConstants.ASSET_OWNER_TRANSFER_OUTSTANDING_INTEREST_CALCULATION_STRATEGY,
            OutstandingInterestStrategyConfig.class);
    public static final AllowedValues<AccountMappingForPaymentTypeConfig> ACCOUNT_MAPPING_FOR_PAYMENT_TYPE = multiple(
            GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_PAYMENT_TYPE, AccountMappingForPaymentTypeConfig.ASSET);
    public static final AllowedValues<AccountMappingForChargeConfig> ACCOUNT_MAPPING_FOR_CHARGE = multiple(
            GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_CHARGE, AccountMappingForChargeConfig.INCOME);
    public static final AllowedValues<LoanStatus> ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER = loanStatuses(
            GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER);
    public static final AllowedValues<LoanStatus> ALLOWED_LOAN_STATUSES_OF_DELAYED_SETTLEMENT_FOR_EXTERNAL_ASSET_TRANSFER = loanStatuses(
            GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_OF_DELAYED_SETTLEMENT_FOR_EXTERNAL_ASSET_TRANSFER);

    public static final List<AllowedValues<?>> ALL = List.of(CHARGE_ACCRUAL_DATE, NEXT_PAYMENT_DUE_DATE, WCL_CHARGE_ACCRUAL_TIME,
            PL_CHARGE_ACCRUAL_TIME, OUTSTANDING_INTEREST_STRATEGY, ACCOUNT_MAPPING_FOR_PAYMENT_TYPE, ACCOUNT_MAPPING_FOR_CHARGE,
            ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER, ALLOWED_LOAN_STATUSES_OF_DELAYED_SETTLEMENT_FOR_EXTERNAL_ASSET_TRANSFER);

    private static final Map<String, AllowedValues<?>> BY_CONFIG_NAME = ALL.stream()
            .collect(Collectors.toUnmodifiableMap(AllowedValues::configName, Function.identity()));

    private GlobalConfigurationAllowedValues() {}

    public static Optional<AllowedValues<?>> find(final String configName) {
        return Optional.ofNullable(BY_CONFIG_NAME.get(configName));
    }

    /**
     * Fills in the allowed values of a restricted configuration; leaves the data untouched otherwise.
     */
    public static GlobalConfigurationPropertyData withAllowedValues(final GlobalConfigurationPropertyData data) {
        find(data.getName()).ifPresent(allowedValues -> data.setAllowedValues(allowedValues.values())
                .setMultipleValuesAllowed(allowedValues.multipleValuesAllowed()));
        return data;
    }

    private static <E extends Enum<E> & GlobalConfigurationValue> AllowedValues<E> single(final String configName, final E defaultValue) {
        return new AllowedValues<>(configName, List.of(defaultValue.getDeclaringClass().getEnumConstants()),
                GlobalConfigurationValue::getValue, false, List.of(defaultValue));
    }

    private static <E extends Enum<E> & GlobalConfigurationValue> AllowedValues<E> singleWithoutDefault(final String configName,
            final Class<E> enumClass) {
        return new AllowedValues<>(configName, List.of(enumClass.getEnumConstants()), GlobalConfigurationValue::getValue, false, List.of());
    }

    private static <E extends Enum<E> & GlobalConfigurationValue> AllowedValues<E> multiple(final String configName, final E defaultValue) {
        return new AllowedValues<>(configName, List.of(defaultValue.getDeclaringClass().getEnumConstants()),
                GlobalConfigurationValue::getValue, true, List.of(defaultValue));
    }

    private static AllowedValues<LoanStatus> loanStatuses(final String configName) {
        final List<LoanStatus> statuses = Arrays.stream(LoanStatus.values()).filter(status -> status != LoanStatus.INVALID).toList();
        return new AllowedValues<>(configName, statuses, Enum::name, true, List.of());
    }

    /**
     * @param configName
     *            the name of the restricted global configuration
     * @param constants
     *            the allowed constants
     * @param toValue
     *            the canonical spelling of a constant
     * @param multipleValuesAllowed
     *            whether the config holds a comma-separated list of allowed values instead of a single one
     * @param defaults
     *            the constants used in place of a stored value that is blank or not allowed; empty when reading such a
     *            value must fail instead
     */
    public record AllowedValues<E extends Enum<E>>(String configName, List<E> constants, Function<E, String> toValue,
            boolean multipleValuesAllowed, List<E> defaults) {

        /**
         * @return the canonical spellings of the allowed values
         */
        public List<String> values() {
            return constants.stream().map(toValue).toList();
        }

        public boolean hasDefault() {
            return !defaults.isEmpty();
        }

        /**
         * @return the canonical form of {@link #defaults}; empty if the configuration has no default
         */
        public String defaultValue() {
            return join(defaults);
        }

        /**
         * Splits a raw config value into its trimmed elements; a single-value config always yields one element.
         */
        public List<String> elements(final String rawValue) {
            if (rawValue == null) {
                return List.of();
            }
            final Stream<String> elements = multipleValuesAllowed ? Arrays.stream(rawValue.split(SEPARATOR, -1)) : Stream.of(rawValue);
            return elements.map(String::trim).toList();
        }

        /**
         * The only place where an element is matched against the allowed values.
         */
        public Optional<E> constantOf(final String element) {
            return element == null ? Optional.empty()
                    : constants.stream().filter(constant -> toValue.apply(constant).equalsIgnoreCase(element.trim())).findFirst();
        }

        public boolean isAllowed(final String rawValue) {
            final List<String> elements = elements(rawValue);
            return !elements.isEmpty() && elements.stream().allMatch(element -> constantOf(element).isPresent());
        }

        /**
         * Rewrites a raw config value with the canonical spelling of each element, dropping duplicates. Elements that
         * are not allowed are kept as they are.
         */
        public String canonicalize(final String rawValue) {
            if (rawValue == null) {
                return null;
            }
            return String.join(SEPARATOR,
                    elements(rawValue).stream().map(element -> constantOf(element).map(toValue).orElse(element)).distinct().toList());
        }

        /**
         * @return the constants of a stored value, or {@link #defaults} if it is blank or any element is not allowed
         * @throws GlobalConfigurationPropertyValueNotAllowedException
         *             if the stored value is blank or not allowed and the configuration has no default
         */
        public List<E> resolveAll(final String storedValue) {
            if (!isAllowed(storedValue)) {
                if (!hasDefault()) {
                    throw new GlobalConfigurationPropertyValueNotAllowedException(configName, storedValue, values());
                }
                return defaults;
            }
            return elements(storedValue).stream().map(element -> constantOf(element).orElseThrow()).distinct().toList();
        }

        /**
         * @return the constant of a stored single value, or the default if it is blank or not allowed
         * @throws GlobalConfigurationPropertyValueNotAllowedException
         *             if the stored value is blank or not allowed and the configuration has no default
         */
        public E resolve(final String storedValue) {
            return resolveAll(storedValue).getFirst();
        }

        /**
         * @return the canonical form of a stored value, or {@link #defaultValue()} if it is blank or not allowed
         * @throws GlobalConfigurationPropertyValueNotAllowedException
         *             if the stored value is blank or not allowed and the configuration has no default
         */
        public String resolveValue(final String storedValue) {
            return join(resolveAll(storedValue));
        }

        private String join(final List<E> values) {
            return String.join(SEPARATOR, values.stream().map(toValue).toList());
        }
    }
}
