/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

public class ClientStatusChecker {
	public static void verifyClientIsActive(final HashMap clientStatusHashMap) {
		assertEquals((int) clientStatusHashMap.get("id"), 300);
	}

	
	 public static void verifyClientClosed(final HashMap clientStatusHashMap) {
	        System.out.println("\n-------------------------------------- VERIFYING CLIENT IS CLOSED ------------------------------------");
	        assertEquals((int) clientStatusHashMap.get("id"), 600);
	        System.out.println("Client Status:" + clientStatusHashMap + "\n");
	    }
	 public static void verifyClientPending(final HashMap clientStatusHashMap) {
             System.out.println("\n-------------------------------------- VERIFYING CLIENT IS PENDING ------------------------------------");
             assertEquals((int) clientStatusHashMap.get("id"), 100);
             System.out.println("Client Status:" + clientStatusHashMap + "\n");
         }
	 public static void verifyClientRejected(final HashMap clientStatusHashMap) {
             System.out.println("\n-------------------------------------- VERIFYING CLIENT IS REJECTED ------------------------------------");
             assertEquals((int) clientStatusHashMap.get("id"), 700);
             System.out.println("Client Status:" + clientStatusHashMap + "\n");
         }
	 
	 public static void verifyClientActiavted(final HashMap clientStatusHashMap) {
             System.out.println("\n-------------------------------------- VERIFYING CLIENT IS ACTIVATED ------------------------------------");
             assertEquals((int) clientStatusHashMap.get("id"), 300);
             System.out.println("Client Status:" + clientStatusHashMap + "\n");
         }
	 
	 public static void verifyClientWithdrawn(final HashMap clientStatusHashMap) {
             System.out.println("\n-------------------------------------- VERIFYING CLIENT IS WITHDRAWN ------------------------------------");
             assertEquals((int) clientStatusHashMap.get("id"), 800);
             System.out.println("Client Status:" + clientStatusHashMap + "\n");
         }
        
	
}
