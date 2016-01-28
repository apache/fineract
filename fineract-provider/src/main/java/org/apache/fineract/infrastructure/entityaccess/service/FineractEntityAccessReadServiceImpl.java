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
package org.apache.fineract.infrastructure.entityaccess.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataServiceImpl;
import org.apache.fineract.infrastructure.entityaccess.FineractEntityAccessConstants;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityAccessData;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityRelationData;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityToEntityMappingData;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntity;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityType;
import org.apache.fineract.infrastructure.entityaccess.exception.FineractEntityAccessConfigurationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class FineractEntityAccessReadServiceImpl implements FineractEntityAccessReadService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final static Logger logger = LoggerFactory.getLogger(GenericDataServiceImpl.class);

    @Autowired
    public FineractEntityAccessReadServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.fineract.infrastructure.entityaccess.service.
     * FineractEntityAccessReadService#getSQLQueryWithListOfIDsForEntityAccess
     * (Long,
     * org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityType,
     * org.apache.fineract.infrastructure.entityaccess.domain.
     * FineractEntityAccessType,
     * org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityType,
     * boolean)
     * 
     * This method returns the list of entity IDs as a comma separated list Or
     * null if there is no entity restrictions or if there
     */
    @Override
    public String getSQLQueryInClause_WithListOfIDsForEntityAccess(Long firstEntityId, FineractEntityType firstEntityType,
            FineractEntityAccessType accessType, FineractEntityType secondEntityType, boolean includeAllOffices) {
        Collection<FineractEntityAccessData> accesslist = retrieveEntityAccessFor(firstEntityId, firstEntityType, accessType, secondEntityType,
                includeAllOffices);
        String returnIdListStr = null;
        StringBuffer accessListCSVStrBuf = null;
        if ((accesslist != null) && (accesslist.size() > 0)) {
            logger.debug("Found " + accesslist.size() + " access type restrictions while getting entity access configuration for "
                    + firstEntityType.getType() + ":" + firstEntityId + " with type " + accessType.toStr() + " against "
                    + secondEntityType.getType());
            accessListCSVStrBuf = new StringBuffer(" ");
            for (int i = 0; i < accesslist.size(); i++) {
                FineractEntityAccessData accessData = (FineractEntityAccessData) accesslist.toArray()[i];
                if (accessData == null) { throw new FineractEntityAccessConfigurationException(firstEntityId, firstEntityType, accessType,
                        secondEntityType); }
                if (accessData.getSecondEntity().getId() == 0) { // If there is
                                                                 // any ID that
                                                                 // zero, then
                                                                 // allow access
                                                                 // to all
                    accessListCSVStrBuf = null;
                    break;
                }
                if (i > 0) {
                    accessListCSVStrBuf.append(',');
                }
                accessListCSVStrBuf.append(accessData.getSecondEntity().getId());
            }

        } else {
            logger.debug("Found zero access type restrictions while getting entity access configuration for " + firstEntityType.getType()
                    + ":" + firstEntityId + " with type " + accessType.toStr() + " against " + secondEntityType.getType());
            accessListCSVStrBuf = new StringBuffer();
            accessListCSVStrBuf.append("false"); // Append false so that no rows
                                                 // will be returned
        }
        if (accessListCSVStrBuf != null) {
            returnIdListStr = accessListCSVStrBuf.toString();
        }
        logger.debug("List of IDs applicable:" + returnIdListStr);
        return returnIdListStr;
    }

    @Override
    public Collection<FineractEntityAccessData> retrieveEntityAccessFor(Long firstEntityId, FineractEntityType firstEntityType,
            FineractEntityAccessType accessType, FineractEntityType secondEntityType, boolean includeAllSubOffices) {
        final AppUser currentUser = this.context.authenticatedUser();

        final String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = null;
        if (includeAllSubOffices) {
            hierarchySearchString = "." + "%";
        } else {
            hierarchySearchString = hierarchy + "%";
        }
        String sql = getSQLForRetriveEntityAccessFor(firstEntityType, accessType, secondEntityType);

        Collection<FineractEntityAccessData> entityAccessData = null;
        FineractEntityAccessDataMapper mapper = new FineractEntityAccessDataMapper();

        if (includeAllSubOffices && (firstEntityType.getTable().equals("m_office"))) {
            sql += " where firstentity.hierarchy like ? order by firstEntity.hierarchy";
            entityAccessData = this.jdbcTemplate.query(sql, mapper, new Object[] { firstEntityId, hierarchySearchString });
        } else {
            entityAccessData = this.jdbcTemplate.query(sql, mapper, new Object[] { firstEntityId });
        }

        return entityAccessData;
    }

    private String getSQLForRetriveEntityAccessFor(FineractEntityType firstEntityType, FineractEntityAccessType accessType,
            FineractEntityType secondEntityType) {
        StringBuffer str = new StringBuffer("select eea.entity_id as entity_id, entity_type as entity_type, ");
        str.append("access_type_code_value_id as access_id, cv.code_value as access_type_desc, c.code_name as code, ");
        str.append("firstentity.id as first_entity_id, firstentity.name as entity_name, ");
        str.append("otherentity.id as second_entity_id, otherentity.name as second_entity_name, ");
        str.append("eea.second_entity_type as second_entity_type ");
        str.append("from m_entity_to_entity_access eea ");
        str.append("left join m_code_value cv on (cv.code_value = ");
        str.append("'");
        str.append(accessType.toStr());
        str.append("' ");
        str.append("and eea.access_type_code_value_id = cv.id) ");
        str.append("left join m_code c on (c.code_name = '");
        str.append(FineractEntityAccessConstants.ENTITY_ACCESS_CODENAME);
        str.append("' and cv.code_id = c.id) ");
        str.append("left join ");
        str.append(firstEntityType.getTable());
        str.append(" firstentity on (eea.entity_type = ");
        str.append("'");
        str.append(firstEntityType.getType());
        str.append("'");
        str.append(" and eea.entity_id = firstentity.id)        left join ");
        str.append(secondEntityType.getTable());
        str.append(" otherentity on (eea.second_entity_type = ");
        str.append("'");
        str.append(secondEntityType.getType());
        str.append("' ");
        str.append("and eea.second_entity_id = otherentity.id) ");
        str.append("where eea.access_type_code_value_id = cv.id ");
        str.append("and eea.entity_id = ? ");
        logger.debug(str.toString());
        return str.toString();
    }

    private static final class FineractEntityAccessDataMapper implements RowMapper<FineractEntityAccessData> {

        @Override
        public FineractEntityAccessData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final String entityType = rs.getString("entity_type");
            final Long entityId = rs.getLong("entity_id");
            // final String entityName = rs.getString("entity_name");
            // final Long accessId = rs.getLong("access_id");
            final String accessTypeDesc = rs.getString("access_type_desc");
            // final String code = rs.getString("code");
            final Long secondEntityId = rs.getLong("second_entity_id");
            // final String secondEntityName =
            // rs.getString("second_entity_name");
            final String secondEntityType = rs.getString("second_entity_type");

            FineractEntity firstEntity = null;
            FineractEntityType etype = FineractEntityType.get(entityType);
            if (etype != null) {
                firstEntity = new FineractEntity(entityId, etype);
            }

            FineractEntity secondEntity = null;
            FineractEntityType secondetype = FineractEntityType.get(secondEntityType);
            if (etype != null) {
                secondEntity = new FineractEntity(secondEntityId, secondetype);
            }

            FineractEntityAccessType accessType = null;
            if (accessTypeDesc != null) {
                accessType = FineractEntityAccessType.get(accessTypeDesc);
            }

            FineractEntityAccessData returnFineractEntityAccessData = null;
            if (firstEntity != null && secondEntity != null && accessType != null) {
                returnFineractEntityAccessData = new FineractEntityAccessData(firstEntity, accessType, secondEntity);
            }
            return returnFineractEntityAccessData;
        }
    }

    @Override
    public String getSQLQueryInClauseIDList_ForLoanProductsForOffice(Long officeId, boolean includeAllOffices) {

        FineractEntityType firstEntityType = FineractEntityType.OFFICE;
        FineractEntityAccessType accessType = FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS;
        FineractEntityType secondEntityType = FineractEntityType.LOAN_PRODUCT;

        return getSQLQueryInClause_WithListOfIDsForEntityAccess(officeId, firstEntityType, accessType, secondEntityType, includeAllOffices);
    }

    @Override
    public String getSQLQueryInClauseIDList_ForSavingsProductsForOffice(Long officeId, boolean includeAllOffices) {

        FineractEntityType firstEntityType = FineractEntityType.OFFICE;
        FineractEntityAccessType accessType = FineractEntityAccessType.OFFICE_ACCESS_TO_SAVINGS_PRODUCTS;
        FineractEntityType secondEntityType = FineractEntityType.SAVINGS_PRODUCT;

        return getSQLQueryInClause_WithListOfIDsForEntityAccess(officeId, firstEntityType, accessType, secondEntityType, includeAllOffices);
    }

    @Override
    public String getSQLQueryInClauseIDList_ForChargesForOffice(Long officeId, boolean includeAllOffices) {

        FineractEntityType firstEntityType = FineractEntityType.OFFICE;
        FineractEntityAccessType accessType = FineractEntityAccessType.OFFICE_ACCESS_TO_CHARGES;
        FineractEntityType secondEntityType = FineractEntityType.CHARGE;

        return getSQLQueryInClause_WithListOfIDsForEntityAccess(officeId, firstEntityType, accessType, secondEntityType, includeAllOffices);
    }

    @Override
    public Collection<FineractEntityRelationData> retrieveAllSupportedMappingTypes() {
        EntityRelationMapper entityMapper = new EntityRelationMapper();
        final String sql = entityMapper.schema();
        final Collection<FineractEntityRelationData> mapTypes = this.jdbcTemplate.query(sql, entityMapper, new Object[] {});
        return mapTypes;
    }

    private static final class EntityRelationMapper implements RowMapper<FineractEntityRelationData> {

        private final StringBuilder sqlBuilder = new StringBuilder("select id as id,code_name as mapping_Types from m_entity_relation ");

        public String schema() {
            return this.sqlBuilder.toString();
        }

        @Override
        public FineractEntityRelationData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long mappingTypesId = rs.getLong("id");
            final String mappingTypes = rs.getString("mapping_Types");
            return FineractEntityRelationData.getMappingTypes(mappingTypesId, mappingTypes);
        }
    }

    @Override
    public Collection<FineractEntityToEntityMappingData> retrieveEntityToEntityMappings(Long mapId, Long fromId, Long toId) {

        EntityToEntityMapper entityToEntityMapper = new EntityToEntityMapper();
        String sql = entityToEntityMapper.schema();
        final Collection<FineractEntityToEntityMappingData> mapTypes = this.jdbcTemplate.query(sql, entityToEntityMapper,
                new Object[] { mapId, fromId, fromId, toId, toId });
        return mapTypes;

    }

    @Override
    public Collection<FineractEntityToEntityMappingData> retrieveOneMapping(Long mapId) {
        GetOneEntityMapper entityMapper = new GetOneEntityMapper();
        String sql = entityMapper.schema();
        final Collection<FineractEntityToEntityMappingData> mapTypes = this.jdbcTemplate.query(sql, entityMapper, new Object[] { mapId });
        return mapTypes;
    }

    private static final class GetOneEntityMapper implements RowMapper<FineractEntityToEntityMappingData> {

        private final String schema;

        public GetOneEntityMapper() {

            StringBuffer str = new StringBuffer("select eem.rel_id as relId, ");
            str.append("eem.from_id as fromId,eem.to_Id as toId,eem.start_date as startDate,eem.end_date as endDate ");
            str.append("from m_entity_to_entity_mapping eem ");
            str.append("where eem.id= ? ");
            this.schema = str.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public FineractEntityToEntityMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long relId = rs.getLong("relId");
            final Long fromId = rs.getLong("fromId");
            final Long toId = rs.getLong("toId");
            final Date startDate = rs.getDate("startDate");
            final Date endDate = rs.getDate("endDate");
            return FineractEntityToEntityMappingData.getRelatedEntities(relId, fromId, toId, startDate, endDate);
        }

    }

    private static final class EntityToEntityMapper implements RowMapper<FineractEntityToEntityMappingData> {

        private final String schema;

        public EntityToEntityMapper() {

            StringBuffer str = new StringBuffer("select eem.id as mapId, ");
            str.append("eem.rel_id as relId, ");
            str.append("eem.from_id as from_id, ");
            str.append("eem.to_id as to_id, ");
            str.append("eem.start_date as startDate, ");
            str.append("eem.end_date as endDate, ");
            str.append("case er.code_name ");
            str.append("when 'office_access_to_loan_products' then ");
            str.append("o.name ");
            str.append("when 'office_access_to_savings_products' then ");
            str.append("o.name ");
            str.append("when 'office_access_to_fees/charges' then ");
            str.append("o.name ");
            str.append("when 'role_access_to_loan_products' then ");
            str.append("r.name ");
            str.append("when 'role_access_to_savings_products' then ");
            str.append("r.name ");
            str.append("end as from_name, ");
            str.append("case er.code_name ");
            str.append("when 'office_access_to_loan_products' then ");
            str.append("lp.name ");
            str.append("when 'office_access_to_savings_products' then ");
            str.append("sp.name ");
            str.append("when 'office_access_to_fees/charges' then ");
            str.append("charge.name ");
            str.append("when 'role_access_to_loan_products' then ");
            str.append("lp.name ");
            str.append("when 'role_access_to_savings_products' then ");
            str.append("sp.name ");
            str.append("end as to_name, ");
            str.append("er.code_name ");
            str.append("from m_entity_to_entity_mapping eem ");
            str.append("join m_entity_relation er on eem.rel_id = er.id ");
            str.append("left join m_office o on er.from_entity_type = 1 and eem.from_id = o.id ");
            str.append("left join m_role r on er.from_entity_type = 5 and eem.from_id = r.id ");
            str.append("left join m_product_loan lp on er.to_entity_type = 2 and eem.to_id = lp.id ");
            str.append("left join m_savings_product sp on er.to_entity_type = 3 and eem.to_id = sp.id ");
            str.append("left join m_charge charge on er.to_entity_type = 4 and eem.to_id = charge.id ");
            str.append("where ");
            str.append("er.id = ? and ");
            str.append("( ? = 0 or from_id = ? ) and ");
            str.append("( ? = 0 or to_id = ? ) ");

            this.schema = str.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public FineractEntityToEntityMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long mapId = rs.getLong("mapId");
            final Long relId = rs.getLong("relId");
            final Long fromId = rs.getLong("from_id");
            final Long toId = rs.getLong("to_id");
            final String fromEntity = rs.getString("from_name");
            final String toEntity = rs.getString("to_name");
            final Date startDate = rs.getDate("startDate");
            final Date endDate = rs.getDate("endDate");
            return FineractEntityToEntityMappingData.getRelatedEntities(mapId, relId, fromId, toId, startDate, endDate, fromEntity, toEntity);
        }
    }

}
