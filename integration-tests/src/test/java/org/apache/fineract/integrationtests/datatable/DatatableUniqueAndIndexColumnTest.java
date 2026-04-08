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
package org.apache.fineract.integrationtests.datatable;

import static org.apache.fineract.integrationtests.common.Utils.initializeRESTAssured;
import static org.apache.fineract.integrationtests.datatable.DatatableEntity.LOAN;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.PutDataTablesRequest;
import org.apache.fineract.client.models.PutDataTablesRequestAddColumns;
import org.apache.fineract.client.models.PutDataTablesRequestChangeColumns;
import org.apache.fineract.client.models.PutDataTablesResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

public class DatatableUniqueAndIndexColumnTest {

    private DatatableHelper datatableHelper;

    @BeforeEach
    public void setup() {
        initializeRESTAssured();
        this.datatableHelper = new DatatableHelper();
    }

    @Test
    public void testDatableCreationWithUniqueAndIndexedColumns() {
        // given
        String datatableName = DatatableTestNameGenerator.generateDatatableName(LOAN);
        String column1Name = "itsanumber";
        String column2Name = "itsastring";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(LOAN.getReferencedTableName());
        request.setMultiRow(false);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(true);
        column1HeaderRequestData.setIndexed(true);

        request.addColumnsItem(column1HeaderRequestData);

        PostColumnHeaderData column2HeaderRequestData = new PostColumnHeaderData();
        column2HeaderRequestData.setName(column2Name);
        column2HeaderRequestData.setType("String");
        column2HeaderRequestData.setMandatory(false);
        column2HeaderRequestData.setLength(10L);
        column2HeaderRequestData.setCode("");
        column2HeaderRequestData.setUnique(false);
        column2HeaderRequestData.setIndexed(true);

        request.addColumnsItem(column2HeaderRequestData);

        // when
        PostDataTablesResponse response = datatableHelper.createDatatable(request);

        // then
        assertThat(response.getResourceIdentifier()).isNotBlank();

        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);

        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertThat(columnHeaderData).isNotNull().hasSize(5);

        List<NameUniqueIndexedHeaderData> expected = List.of(new NameUniqueIndexedHeaderData(column1Name, true, true),
                new NameUniqueIndexedHeaderData(column2Name, false, true));

        NameUniqueIndexedHeaderData.Mapper mapper = Mappers.getMapper(NameUniqueIndexedHeaderData.Mapper.class);
        List<NameUniqueIndexedHeaderData> data = mapper.map(columnHeaderData);

        assertThat(data).containsAll(expected);
    }

    @Test
    public void testDatableModificationWithUniqueAndIndexedColumns() {
        // given
        // region Datatable creation
        String datatableName = DatatableTestNameGenerator.generateDatatableName(LOAN);
        String column1Name = "itsanumber";
        String column2Name = "itsastring";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(LOAN.getReferencedTableName());
        request.setMultiRow(false);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(true);
        column1HeaderRequestData.setIndexed(true);

        request.addColumnsItem(column1HeaderRequestData);

        PostColumnHeaderData column2HeaderRequestData = new PostColumnHeaderData();
        column2HeaderRequestData.setName(column2Name);
        column2HeaderRequestData.setType("String");
        column2HeaderRequestData.setMandatory(false);
        column2HeaderRequestData.setLength(10L);
        column2HeaderRequestData.setCode("");
        column2HeaderRequestData.setUnique(false);
        column2HeaderRequestData.setIndexed(true);

        request.addColumnsItem(column2HeaderRequestData);

        PostDataTablesResponse response = datatableHelper.createDatatable(request);

        assertThat(response.getResourceIdentifier()).isNotBlank();
        // endregion

        // region Datatable update
        PutDataTablesRequest updateRequest = new PutDataTablesRequest();
        updateRequest.setApptableName(LOAN.getReferencedTableName());

        String column3Name = "number1";
        String column4Name = "number2";

        PutDataTablesRequestAddColumns addColumn1 = new PutDataTablesRequestAddColumns();
        addColumn1.setName(column3Name);
        addColumn1.setType("Number");
        addColumn1.setMandatory(false);
        addColumn1.setCode("");
        addColumn1.setUnique(true);
        addColumn1.setIndexed(false);

        updateRequest.addAddColumnsItem(addColumn1);

        PutDataTablesRequestAddColumns addColumn2 = new PutDataTablesRequestAddColumns();
        addColumn2.setName(column4Name);
        addColumn2.setType("Number");
        addColumn2.setMandatory(false);
        addColumn2.setCode("");
        addColumn2.setUnique(false);
        addColumn2.setIndexed(true);

        updateRequest.addAddColumnsItem(addColumn2);

        PutDataTablesRequestChangeColumns changeColumns = new PutDataTablesRequestChangeColumns();
        changeColumns.setName(column1Name);
        String newColumnName = column1Name + "new";
        changeColumns.setNewName(newColumnName);
        changeColumns.setUnique(false);
        changeColumns.setIndexed(true);

        updateRequest.addChangeColumnsItem(changeColumns);

        // endregion
        // when
        PutDataTablesResponse updateResponse = datatableHelper.updateDatatable(datatableName, updateRequest);

        // then
        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);

        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertThat(columnHeaderData).isNotNull().hasSize(7);

        List<NameUniqueIndexedHeaderData> expected = List.of(new NameUniqueIndexedHeaderData(column3Name, true, true),
                new NameUniqueIndexedHeaderData(column4Name, false, true), new NameUniqueIndexedHeaderData(newColumnName, false, true));

        NameUniqueIndexedHeaderData.Mapper mapper = Mappers.getMapper(NameUniqueIndexedHeaderData.Mapper.class);
        List<NameUniqueIndexedHeaderData> data = mapper.map(columnHeaderData);

        assertThat(data).containsAll(expected);
    }

    @Test
    public void testDatableCreationMakesFkColumnIndexedIfMultirow() {
        // given
        String datatableName = DatatableTestNameGenerator.generateDatatableName(LOAN);
        String column1Name = "itsanumber";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(LOAN.getReferencedTableName());
        request.setMultiRow(true);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(false);
        column1HeaderRequestData.setIndexed(false);

        request.addColumnsItem(column1HeaderRequestData);

        // when
        PostDataTablesResponse response = datatableHelper.createDatatable(request);

        // then
        assertThat(response.getResourceIdentifier()).isNotBlank();

        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);

        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertThat(columnHeaderData).isNotNull().hasSize(5);

        List<NameUniqueIndexedHeaderData> expected = List.of(new NameUniqueIndexedHeaderData("id", true, true),
                new NameUniqueIndexedHeaderData("loan_id", false, true), new NameUniqueIndexedHeaderData(column1Name, false, false),
                new NameUniqueIndexedHeaderData("created_at", false, false), new NameUniqueIndexedHeaderData("updated_at", false, false));

        NameUniqueIndexedHeaderData.Mapper mapper = Mappers.getMapper(NameUniqueIndexedHeaderData.Mapper.class);
        List<NameUniqueIndexedHeaderData> data = mapper.map(columnHeaderData);

        assertThat(data).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testDatableCreationMakesFkColumnIndexedIfNotMultirow() {
        // given
        String datatableName = DatatableTestNameGenerator.generateDatatableName(LOAN);
        String column1Name = "itsanumber";

        PostDataTablesRequest request = new PostDataTablesRequest();
        request.setDatatableName(datatableName);
        request.setApptableName(LOAN.getReferencedTableName());
        request.setMultiRow(false);

        PostColumnHeaderData column1HeaderRequestData = new PostColumnHeaderData();
        column1HeaderRequestData.setName(column1Name);
        column1HeaderRequestData.setType("Number");
        column1HeaderRequestData.setMandatory(false);
        column1HeaderRequestData.setLength(10L);
        column1HeaderRequestData.setCode("");
        column1HeaderRequestData.setUnique(false);
        column1HeaderRequestData.setIndexed(false);

        request.addColumnsItem(column1HeaderRequestData);

        // when
        PostDataTablesResponse response = datatableHelper.createDatatable(request);

        // then
        assertThat(response.getResourceIdentifier()).isNotBlank();

        GetDataTablesResponse dataTable = datatableHelper.getDataTableDetails(datatableName);

        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertThat(columnHeaderData).isNotNull().hasSize(4);

        List<NameUniqueIndexedHeaderData> expected = List.of(new NameUniqueIndexedHeaderData("loan_id", true, true),
                new NameUniqueIndexedHeaderData(column1Name, false, false), new NameUniqueIndexedHeaderData("created_at", false, false),
                new NameUniqueIndexedHeaderData("updated_at", false, false));

        NameUniqueIndexedHeaderData.Mapper mapper = Mappers.getMapper(NameUniqueIndexedHeaderData.Mapper.class);
        List<NameUniqueIndexedHeaderData> data = mapper.map(columnHeaderData);

        assertThat(data).containsExactlyInAnyOrderElementsOf(expected);
    }

    @RequiredArgsConstructor
    @Data
    public static class NameUniqueIndexedHeaderData {

        private final String name;
        private final boolean unique;
        private final boolean indexed;

        @org.mapstruct.Mapper
        public interface Mapper {

            @Mappings({ @Mapping(target = "name", source = "columnName"), @Mapping(target = "unique", source = "isColumnUnique"),
                    @Mapping(target = "indexed", source = "isColumnIndexed") })
            NameUniqueIndexedHeaderData map(ResultsetColumnHeaderData source);

            List<NameUniqueIndexedHeaderData> map(List<ResultsetColumnHeaderData> source);
        }
    }
}
