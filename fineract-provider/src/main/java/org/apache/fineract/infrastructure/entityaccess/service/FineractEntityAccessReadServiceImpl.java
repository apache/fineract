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

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityRelationData;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityToEntityMappingData;
import org.apache.fineract.infrastructure.entityaccess.data.FineractOfficeEntityAccessData;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelation;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepository;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepositoryWrapper;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingRepository;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class FineractEntityAccessReadServiceImpl implements FineractEntityAccessReadService {

    public static final String OFFICE_ENTITY_ACCESS_CACHE_NAME = "officeEntityAccess";

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private static final Set<String> OFFICE_RELATION_CODE_NAMES = Set.of(FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS.getStr(),
            FineractEntityAccessType.OFFICE_ACCESS_TO_SAVINGS_PRODUCTS.getStr(),
            FineractEntityAccessType.OFFICE_ACCESS_TO_CHARGES.getStr());
    private final FineractEntityRelationRepositoryWrapper fineractEntityRelationRepository;
    private final FineractEntityRelationRepository entityRelationRepository;
    private final FineractEntityToEntityMappingRepository fineractEntityToEntityMappingRepository;
    private final OfficeRepository officeRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;

    @Autowired
    public FineractEntityAccessReadServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            final FineractEntityRelationRepositoryWrapper fineractEntityRelationRepository,
            final FineractEntityRelationRepository entityRelationRepository,
            final FineractEntityToEntityMappingRepository fineractEntityToEntityMappingRepository, final OfficeRepository officeRepository,
            final OfficeRepositoryWrapper officeRepositoryWrapper) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.fineractEntityRelationRepository = fineractEntityRelationRepository;
        this.entityRelationRepository = entityRelationRepository;
        this.fineractEntityToEntityMappingRepository = fineractEntityToEntityMappingRepository;
        this.officeRepository = officeRepository;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
    }

    @Override
    public Collection<FineractEntityRelationData> retrieveAllSupportedMappingTypes() {
        EntityRelationMapper entityMapper = new EntityRelationMapper();
        final String sql = entityMapper.schema();
        final Collection<FineractEntityRelationData> mapTypes = this.jdbcTemplate.query(sql, entityMapper, new Object[] {});
        return mapTypes;
    }

    private static final class EntityRelationMapper implements RowMapper<FineractEntityRelationData> {

        private static final String ENTITY_RELATION_SCHEMA = "select id as id,code_name as mapping_Types from m_entity_relation ";

        public String schema() {
            return ENTITY_RELATION_SCHEMA;
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
        if (!isOfficeRelation(mapId)) {
            return mapTypes;
        }
        final Set<Long> visibleOfficeIds = Set
                .copyOf(this.officeRepository.findIdsByHierarchyLike(this.context.authenticatedUser().getOffice().getHierarchy() + "%"));
        return mapTypes.stream().filter(mapping -> visibleOfficeIds.contains(mapping.getFromId())).toList();
    }

    private boolean isOfficeRelation(final Long mapId) {
        return this.entityRelationRepository.findById(mapId).map(FineractEntityRelation::getCodeName)
                .filter(OFFICE_RELATION_CODE_NAMES::contains).isPresent();
    }

    @Override
    public Collection<FineractEntityToEntityMappingData> retrieveOneMapping(Long mapId) {
        GetOneEntityMapper entityMapper = new GetOneEntityMapper();
        String sql = entityMapper.schema();
        final Collection<FineractEntityToEntityMappingData> mapTypes = this.jdbcTemplate.query(sql, entityMapper, new Object[] { mapId });
        return mapTypes;
    }

    private static final class GetOneEntityMapper implements RowMapper<FineractEntityToEntityMappingData> {

        private static final String GET_ONE_ENTITY_SCHEMA = """
                select eem.rel_id as relId,
                eem.from_id as fromId,eem.to_Id as toId,eem.start_date as startDate,eem.end_date as endDate
                from m_entity_to_entity_mapping eem
                where eem.id= ?\s""";

        GetOneEntityMapper() {}

        public String schema() {
            return GET_ONE_ENTITY_SCHEMA;
        }

        @Override
        public FineractEntityToEntityMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {

            final Long relId = rs.getLong("relId");
            final Long fromId = rs.getLong("fromId");
            final Long toId = rs.getLong("toId");
            final Date startDate = rs.getDate("startDate");
            final Date endDate = rs.getDate("endDate");
            final LocalDate startLocalDate = startDate != null ? startDate.toLocalDate() : null;
            final LocalDate endLocalDate = endDate != null ? endDate.toLocalDate() : null;
            return FineractEntityToEntityMappingData.getRelatedEntities(relId, fromId, toId, startLocalDate, endLocalDate);
        }

    }

    private static final class EntityToEntityMapper implements RowMapper<FineractEntityToEntityMappingData> {

        private static final String ENTITY_TO_ENTITY_SCHEMA = """
                select eem.id as mapId,
                eem.rel_id as relId,
                eem.from_id as from_id,
                eem.to_id as to_id,
                eem.start_date as startDate,
                eem.end_date as endDate,
                case er.code_name
                when 'office_access_to_loan_products' then
                o.name
                when 'office_access_to_savings_products' then
                o.name
                when 'office_access_to_fees/charges' then
                o.name
                when 'role_access_to_loan_products' then
                r.name
                when 'role_access_to_savings_products' then
                r.name
                end as from_name,
                case er.code_name
                when 'office_access_to_loan_products' then
                lp.name
                when 'office_access_to_savings_products' then
                sp.name
                when 'office_access_to_fees/charges' then
                charge.name
                when 'role_access_to_loan_products' then
                lp.name
                when 'role_access_to_savings_products' then
                sp.name
                end as to_name,
                er.code_name
                from m_entity_to_entity_mapping eem
                join m_entity_relation er on eem.rel_id = er.id
                left join m_office o on er.from_entity_type = 1 and eem.from_id = o.id
                left join m_role r on er.from_entity_type = 5 and eem.from_id = r.id
                left join m_product_loan lp on er.to_entity_type = 2 and eem.to_id = lp.id
                left join m_savings_product sp on er.to_entity_type = 3 and eem.to_id = sp.id
                left join m_charge charge on er.to_entity_type = 4 and eem.to_id = charge.id
                where
                er.id = ? and
                ( ? = 0 or from_id = ? ) and
                ( ? = 0 or to_id = ? )\s""";

        EntityToEntityMapper() {}

        public String schema() {
            return ENTITY_TO_ENTITY_SCHEMA;
        }

        @Override
        public FineractEntityToEntityMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long mapId = rs.getLong("mapId");
            final Long relId = rs.getLong("relId");
            final Long fromId = rs.getLong("from_id");
            final Long toId = rs.getLong("to_id");
            final String fromEntity = rs.getString("from_name");
            final String toEntity = rs.getString("to_name");
            final Date startDate = rs.getDate("startDate");
            final Date endDate = rs.getDate("endDate");
            final LocalDate startLocalDate = startDate != null ? startDate.toLocalDate() : null;
            final LocalDate endLocalDate = endDate != null ? endDate.toLocalDate() : null;
            return FineractEntityToEntityMappingData.getRelatedEntities(mapId, relId, fromId, toId, startLocalDate, endLocalDate,
                    fromEntity, toEntity);
        }
    }

    @Override
    @Cacheable(value = OFFICE_ENTITY_ACCESS_CACHE_NAME, key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#accessType.str + '_' + #officeId + '_' + #includeSubOffices)")
    public FineractOfficeEntityAccessData retrieveOfficeEntityAccess(final FineractEntityAccessType accessType, final Long officeId,
            final boolean includeSubOffices) {
        final FineractEntityRelation relation = fineractEntityRelationRepository.findOneByCodeName(accessType.getStr());
        final String hierarchy = officeRepositoryWrapper.findOneWithNotFoundDetection(officeId).getHierarchy();
        final Set<Long> officeIds = new HashSet<>(officeRepository.findIdsByHierarchyIn(hierarchyWithAncestors(hierarchy)));
        if (includeSubOffices) {
            officeIds.addAll(officeRepository.findIdsByHierarchyLike(hierarchy + "%"));
        }
        return FineractOfficeEntityAccessData.from(fineractEntityToEntityMappingRepository.findByRelationId(relation), officeIds);
    }

    static Set<String> hierarchyWithAncestors(final String hierarchy) {
        final Set<String> hierarchies = new LinkedHashSet<>();
        for (int i = hierarchy.indexOf('.'); i >= 0; i = hierarchy.indexOf('.', i + 1)) {
            hierarchies.add(hierarchy.substring(0, i + 1));
        }
        return hierarchies;
    }
}
