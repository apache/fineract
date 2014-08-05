/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Cieyou on 2/26/14.
 */
public class DataTableApiConstant {

    public static final Integer CATEGORY_PPI = 200;
    public static final Integer CATEGORY_DEFAULT = 100;

    public static final String categoryParamName ="category";
    public static final String localParamName = "locale";
    public static final Set<String> REGISTER_PARAMS = new HashSet<>(Arrays.asList(categoryParamName,localParamName));

    public static final String DATATABLE_RESOURCE_NAME ="dataTables";

}
