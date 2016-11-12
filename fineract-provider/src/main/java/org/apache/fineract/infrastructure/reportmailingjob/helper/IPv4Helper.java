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
package org.apache.fineract.infrastructure.reportmailingjob.helper;

import java.net.InetAddress;

import org.apache.commons.lang.StringUtils;

/**
 * This utility provides methods to either convert an IPv4 address to its long format or a 32bit dotted format.
 *
 * @see http://hawkee.com/snippet/9731/
 */
public class IPv4Helper {
    /**
     * Returns the long format of the provided IP address.
     *
     * @param ipAddress the IP address
     * @return the long format of <code>ipAddress</code>
     * @throws IllegalArgumentException if <code>ipAddress</code> is invalid
     */
    public static long ipAddressToLong(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            throw new IllegalArgumentException("ip address cannot be null or empty");
        }
        
        String[] octets = ipAddress.split(java.util.regex.Pattern.quote("."));
        
        if (octets.length != 4) {
            throw new IllegalArgumentException("invalid ip address");
        }
        
        long ip = 0;
        
        for (int i = 3; i >= 0; i--) {
            long octet = Long.parseLong(octets[3 - i]);
            
            if (octet > 255 || octet < 0) {
                throw new IllegalArgumentException("invalid ip address");
            }
            
            ip |= octet << (i * 8);
        }
        
        return ip;
    }

    /**
     * Returns the 32bit dotted format of the provided long ip.
     *
     * @param ip the long ip
     * @return the 32bit dotted format of <code>ip</code>
     * @throws IllegalArgumentException if <code>ip</code> is invalid
     */
    public static String longToIpAddress(long ip) {
        // if ip is bigger than 255.255.255.255 or smaller than 0.0.0.0
        if (ip > 4294967295l || ip < 0) {
            throw new IllegalArgumentException("invalid ip");
        }
        
        StringBuilder ipAddress = new StringBuilder();
        
        for (int i = 3; i >= 0; i--) {
            int shift = i * 8;
            ipAddress.append((ip & (0xff << shift)) >> shift);
            if (i > 0) {
                ipAddress.append(".");
            }
        }
        
        return ipAddress.toString();
    }
    
    /** 
     * check if an IP Address is within a given range of IP Addresses
     * 
     * @param ipAddress -- the IP Address to be checked
     * @param startOfRange -- the first IP address in the range
     * @param endOfRange -- the last IP address in the range
     * @return boolean true if IP address is in the range of IP addresses
     **/
    public static boolean ipAddressIsInRange(final String ipAddress, final String startOfRange, final String endOfRange) {
        final long ipAddressToLong = ipAddressToLong(ipAddress);
        final long startOfRangeToLong = ipAddressToLong(startOfRange);
        final long endOfRangeToLong = ipAddressToLong(endOfRange);
        
        long diff = ipAddressToLong - startOfRangeToLong;
        
        return (diff >= 0 && (diff <= (endOfRangeToLong - startOfRangeToLong)));
    }
    
    /** 
     * check if the java application is running on a local machine
     * 
     * @return true if the application is running on a local machine else false
     **/
    public static boolean applicationIsRunningOnLocalMachine() {
        boolean isRunningOnLocalMachine = false;
        
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            
            if (localHost != null) {
                final String hostAddress = localHost.getHostAddress();
                final String startOfIpAddressRange = "127.0.0.0";
                final String endOfIpAddressRange = "127.255.255.255";
                
                if (StringUtils.isNotEmpty(hostAddress)) {
                    isRunningOnLocalMachine = ipAddressIsInRange(hostAddress, startOfIpAddressRange, endOfIpAddressRange);
                }
            }
        }
        
        catch (Exception exception) { }
                
        return isRunningOnLocalMachine;
    }
    
    /** 
     * check if the java application is not running on a local machine
     * 
     * @return true if the application is not running on a local machine else false
     **/
    public static boolean applicationIsNotRunningOnLocalMachine() {
        return !applicationIsRunningOnLocalMachine();
    }
}
