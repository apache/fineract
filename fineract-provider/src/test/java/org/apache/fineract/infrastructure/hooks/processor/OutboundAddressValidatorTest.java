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
package org.apache.fineract.infrastructure.hooks.processor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OutboundAddressValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = { "127.0.0.1", "127.1.2.3", "0.0.0.0", "0.1.2.3", "10.0.0.1", "172.16.0.1", "172.31.255.255", "192.168.1.1",
            "169.254.169.254", "100.64.0.1", "100.127.255.255", "224.0.0.1", "255.255.255.255", "::1", "::", "fe80::1", "fc00::1",
            "fd12:3456::1", "ff02::1", "::ffff:127.0.0.1", "::ffff:10.0.0.1", "192.0.0.1", "192.0.0.255", "198.18.0.1", "198.19.255.255",
            "240.0.0.1", "250.1.2.3", "64:ff9b::a00:1", "64:ff9b::7f00:1", "64:ff9b::a9fe:a9fe", "2002:7f00:1::1", "2002:a00:1::",
            "::10.0.0.1", "::127.0.0.1" })
    void restrictsNonPublicAddresses(final String literal) throws UnknownHostException {
        assertTrue(OutboundAddressValidator.isRestricted(InetAddress.getByName(literal)), literal);
    }

    @ParameterizedTest
    @ValueSource(strings = { "8.8.8.8", "1.1.1.1", "172.15.0.1", "172.32.0.1", "100.63.255.255", "100.128.0.1", "169.253.0.1", "192.0.1.1",
            "198.17.255.255", "198.20.0.1", "2606:4700:4700::1111", "2001:4860:4860::8888", "64:ff9b::808:808", "2002:808:808::1",
            "::8.8.8.8" })
    void allowsPublicAddresses(final String literal) throws UnknownHostException {
        assertFalse(OutboundAddressValidator.isRestricted(InetAddress.getByName(literal)), literal);
    }

    @ParameterizedTest
    @ValueSource(strings = { "http://127.0.0.1/hook", "https://localhost/hook", "http://169.254.169.254/latest/meta-data",
            "http://[::1]:8080/", "http://10.1.2.3:9200/", "http://192.168.0.5/", "http://[fe80::1]/", "http://[fd00::1]/",
            "http://100.64.0.1/" })
    void rejectsUrlsPointingToNonPublicHosts(final String url) {
        assertThrows(IllegalArgumentException.class, () -> OutboundAddressValidator.validateUrl(url, false));
    }

    @ParameterizedTest
    @ValueSource(strings = { "file:///etc/passwd", "ftp://8.8.8.8/x", "gopher://8.8.8.8/", "jar:http://8.8.8.8/x!/", "8.8.8.8", "http://",
            "not a url" })
    void rejectsOtherSchemesAndMalformedUrls(final String url) {
        assertThrows(IllegalArgumentException.class, () -> OutboundAddressValidator.validateUrl(url, false));
        assertThrows(IllegalArgumentException.class, () -> OutboundAddressValidator.validateUrl(url, true));
    }

    @Test
    void acceptsPublicAddressLiterals() {
        assertDoesNotThrow(() -> OutboundAddressValidator.validateUrl("https://8.8.8.8/hook", false));
        assertDoesNotThrow(() -> OutboundAddressValidator.validateUrl("http://[2606:4700:4700::1111]/hook", false));
    }

    @Test
    void allowsPrivateAddressesWhenExplicitlyEnabled() {
        assertDoesNotThrow(() -> OutboundAddressValidator.validateUrl("http://127.0.0.1:8080/hook", true));
        assertDoesNotThrow(() -> OutboundAddressValidator.validateUrl("http://10.0.0.5/hook", true));
    }

    @Test
    void filtersResolvedAddresses() throws UnknownHostException {
        final InetAddress publicAddress = InetAddress.getByName("8.8.8.8");
        final InetAddress privateAddress = InetAddress.getByName("10.0.0.1");
        assertTrue(OutboundAddressValidator.filterResolved("h", List.of(publicAddress)).contains(publicAddress));
        assertThrows(UnknownHostException.class,
                () -> OutboundAddressValidator.filterResolved("h", List.of(publicAddress, privateAddress)));
        assertThrows(UnknownHostException.class, () -> OutboundAddressValidator.filterResolved("h", List.of()));
    }

    @Test
    void processorHelperRefusesPrivateTargetsUnlessAllowed() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new ProcessorHelper().assertUrlAllowed("http://169.254.169.254/"));
        assertDoesNotThrow(() -> new ProcessorHelper(true).assertUrlAllowed("http://169.254.169.254/"));
    }
}
