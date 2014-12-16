/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.system;

import java.util.HashMap;

import com.google.gson.Gson;

public class AccountNumberPreferencesTestBuilder {
	private String clientAccountType = "1";
	private String clientPrefixType = "101";
	private String loanAccountType = "2";
	private String loanPrefixType = "1";
	private String savingsAccountType = "3";
	private String savingsPrefixType = "1";

	public String clientBuild() {
		final HashMap<String, Object> map = new HashMap<>();
		map.put("accountType", clientAccountType);
		map.put("prefixType", clientPrefixType);

		return new Gson().toJson(map);
	}

	public String loanBuild() {
		final HashMap<String, Object> map = new HashMap<>();
		map.put("accountType", loanAccountType);
		map.put("prefixType", loanPrefixType);

		return new Gson().toJson(map);
	}

	public String savingsBuild() {
		final HashMap<String, Object> map = new HashMap<>();
		map.put("accountType", savingsAccountType);
		map.put("prefixType", savingsPrefixType);

		return new Gson().toJson(map);
	}

	public String invalidDataBuild(String accountType, String prefixType) {
		final HashMap<String, Object> map = new HashMap<>();
		map.put("accountType", accountType);
		map.put("prefixType", prefixType);

		return new Gson().toJson(map);
	}

	public String updatePrefixType(final String prefixType) {

		final HashMap<String, Object> map = new HashMap<>();
		map.put("prefixType", prefixType);
		return new Gson().toJson(map);
	}
}
