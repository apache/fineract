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

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.codes.exception.CodeValueNotFoundException;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.query.QMCode;
import org.apache.fineract.query.QMCodeValue;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeValueReadPlatformServiceImpl implements CodeValueReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final SQLQueryFactory queryFactory;
    private final PlatformSecurityContext context;
    private final CodeValueRepository codeValueRepository;
    private final CodeValueMapper codeValueMapper;

    private final QMCode code = new QMCode("c");
    private final QMCodeValue cv = new QMCodeValue("cv");

    private static final class CodeValueDataMapper implements RowMapper<CodeValueData> {

        private String schema() {
            return " cv.id as id, cv.code_value as value, cv.code_id as codeId, cv.code_description as description, cv.order_position as position,"
                    + " cv.is_active isActive, cv.is_mandatory as mandatory from m_code_value as cv join m_code c on cv.code_id = c.id ";
        }

        @Override
        public CodeValueData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String value = rs.getString("value");
            final Integer position = rs.getInt("position");
            final String description = rs.getString("description");
            final boolean isActive = rs.getBoolean("isActive");
            final boolean mandatory = rs.getBoolean("mandatory");

            return CodeValueData.instance(id, value, position, description, isActive, mandatory);
        }
    }

    @Override
    @Cacheable(value = "code_values", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#code+'cv')")
    public List<CodeValueData> retrieveCodeValuesByCode(final String code) {

        this.context.authenticatedUser();

        final CodeValueDataMapper rm = new CodeValueDataMapper();
        final String sql = "select " + rm.schema() + "where c.code_name like ? and cv.is_active = true order by position";

        return this.jdbcTemplate.query(sql, rm, new Object[] { code }); // NOSONAR
    }

    @Override
    @Cacheable(value = "code_values", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#codeId+'cv')")
    public Collection<CodeValueData> retrieveAllCodeValues(final Long codeId) {

        this.context.authenticatedUser();

        final CodeValueDataMapper rm = new CodeValueDataMapper();
        final String sql = "select " + rm.schema() + "where cv.code_id = ? order by position";

        return this.jdbcTemplate.query(sql, rm, new Object[] { codeId }); // NOSONAR
    }

    @Override
    @Cacheable(value = "code_values", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#codeValueId+'cv_by_id')")
    public CodeValueData retrieveCodeValue(final Long codeValueId) {

        this.context.authenticatedUser();
        CodeValueData codeValueData = new CodeValueData();

        // returns null if nothing found does not throw.
        Tuple result = queryFactory.select(cv.id, cv.codeValue, cv.codeDescription, cv.orderPosition, cv.isActive, cv.isMandatory).from(cv)
                .innerJoin(code).on(cv.codeId.eq(code.id)).where(cv.id.eq(codeValueId.intValue())).fetchOne();

        if (result == null) {
            throw new CodeValueNotFoundException(codeValueId);
        }

        codeValueData.setId(result.get(cv.id).longValue());
        codeValueData.setName(result.get(cv.codeValue));
        codeValueData.setDescription(result.get(cv.codeDescription));
        codeValueData.setPosition(result.get(cv.orderPosition));
        codeValueData.setActive(result.get(cv.isActive));
        codeValueData.setMandatory(result.get(cv.isMandatory));

        return codeValueData;
    }

    @Override
    public List<CodeValueData> retrieveAllCodeValues(String codeName) {
        // TODO: review whether it is needed.
        this.context.authenticatedUser();
        return codeValueMapper.map(codeValueRepository.findByCodeName(codeName));
    }

    @Override
    public CodeValueData retrieveCodeValue(String codeName, Long codeValueId) {
        // TODO: review whether it is needed.
        this.context.authenticatedUser();
        CodeValueData codeValueData = codeValueMapper.map(codeValueRepository.findByCodeNameAndId(codeName, codeValueId));
        if (codeValueData == null) {
            throw new CodeValueNotFoundException(codeName, codeValueId);
        }
        return codeValueData;
    }
}
