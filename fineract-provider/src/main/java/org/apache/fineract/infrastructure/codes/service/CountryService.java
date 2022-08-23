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
package org.apache.fineract.infrastructure.codes.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.CodeConstants;
import org.apache.fineract.infrastructure.codes.data.CountryData;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConfigurationProperties
public class CountryService {

    @Bean
    public Map<Locale, Collection<CountryData>> getCountriesList() {
        log.info("Countries Code Loaded.");

        Map<Locale, Collection<CountryData>> countriesLocale = new HashMap<Locale, Collection<CountryData>>();

        Locale localeEn = new Locale(CodeConstants.ENGLISH_LOCALE);
        Collection<CountryData> countryListEn = extractedCountryData(localeEn);
        countriesLocale.put(localeEn, countryListEn);

        Locale localeFr = new Locale(CodeConstants.FRENCH_LOCALE);
        Collection<CountryData> countryListFr = extractedCountryData(localeFr);
        countriesLocale.put(localeFr, countryListFr);

        return countriesLocale;
    }

    private Collection<CountryData> extractedCountryData(Locale localeFr) {
        ResourceBundle countriesBundle = ResourceBundle.getBundle("countries", localeFr);

        Enumeration<String> countriesCode = countriesBundle.getKeys();

        Collection<CountryData> countryList = new ArrayList<>();
        while (countriesCode.hasMoreElements()) {
            String countryKey = countriesCode.nextElement();
            String countryValue = countriesBundle.getString(countryKey);
            CountryData countryData = CountryData.instance(countryKey, countryValue);
            countryList.add(countryData);
        }
        return countryList;
    }

    @Bean
    public Map<Locale, Map<String, String>> getCountryMapping() {
        log.info("Country Mapping Loaded.");

        Map<Locale, Map<String, String>> countryMapping = new HashMap<Locale, Map<String, String>>();

        Locale localeEn = new Locale(CodeConstants.ENGLISH_LOCALE);
        Map<String, String> countryMappingEn = extractCountryMapping(localeEn);
        countryMapping.put(localeEn, countryMappingEn);

        Locale localeFr = new Locale(CodeConstants.FRENCH_LOCALE);
        Map<String, String> countryMappingFr = extractCountryMapping(localeFr);
        countryMapping.put(localeFr, countryMappingFr);

        return countryMapping;
    }

    private static Map<String, String> extractCountryMapping(Locale locale) {
        ResourceBundle countriesBundle = ResourceBundle.getBundle("countries", locale);

        Map<String, String> country = new HashMap<String, String>();

        Enumeration<String> countriesKey = countriesBundle.getKeys();
        while (countriesKey.hasMoreElements()) {
            String countryKey = countriesKey.nextElement();
            String countryValue = countriesBundle.getString(countryKey);
            country.put(countryKey, countryValue);
        }
        return country;
    }
}
