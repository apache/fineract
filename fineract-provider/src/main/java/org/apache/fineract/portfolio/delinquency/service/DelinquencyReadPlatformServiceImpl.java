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
package org.apache.fineract.portfolio.delinquency.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistory;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.exception.DelinquencyBucketNotFoundException;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DelinquencyReadPlatformServiceImpl implements DelinquencyReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final DelinquencyRangeRepository repositoryRange;
    private final DelinquencyBucketRepository repositoryBucket;
    private final LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory;
    private final DelinquencyRangeMapper mapperRange;
    private final DelinquencyBucketMapper mapperBucket;
    private final LoanAssembler loanAssembler;

    @Override
    public Collection<DelinquencyRangeData> retrieveAllDelinquencyRanges() {
        final List<DelinquencyRange> delinquencyRangeList = repositoryRange.findAll();
        return mapperRange.map(delinquencyRangeList);
    }

    @Override
    public DelinquencyRangeData retrieveDelinquencyRange(Long delinquencyRangeId) {
        DelinquencyRange delinquencyRangeList = repositoryRange.getReferenceById(delinquencyRangeId);
        return mapperRange.map(delinquencyRangeList);
    }

    @Override
    public Collection<DelinquencyBucketData> retrieveAllDelinquencyBuckets() {
        final List<DelinquencyBucket> delinquencyRangeList = repositoryBucket.findAll();
        return mapperBucket.map(delinquencyRangeList);
    }

    @Override
    public DelinquencyBucketData retrieveDelinquencyBucket(Long delinquencyBucketId) {
        try {
            final DelinquencyBucket delinquencyBucket = repositoryBucket.getReferenceById(delinquencyBucketId);
            final DelinquencyBucketData delinquencyBucketData = mapperBucket.map(delinquencyBucket);

            final DelinquencyBucketMappingMapper rmMappings = new DelinquencyBucketMappingMapper();
            final String sql = "select " + rmMappings.schema() + " where buc.id = ? order by dran.min_age_days";
            List<DelinquencyRangeData> ranges = this.jdbcTemplate.query(sql, rmMappings, delinquencyBucketId); // NOSONAR
            delinquencyBucketData.setRanges(ranges);

            return delinquencyBucketData;
        } catch (final EmptyResultDataAccessException e) {
            throw DelinquencyBucketNotFoundException.notFound(delinquencyBucketId, e);
        }
    }

    @Override
    public DelinquencyRangeData retrieveCurrentDelinquencyTag(Long loanId) {
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        Optional<LoanDelinquencyTagHistory> optLoanDelinquencyTag = this.repositoryLoanDelinquencyTagHistory.findByLoanAndLiftedOnDate(loan,
                null);
        if (optLoanDelinquencyTag.isPresent()) {
            return mapperRange.map(optLoanDelinquencyTag.get().getDelinquencyRange());
        }
        return null;
    }

    private static final class DelinquencyBucketMappingMapper implements RowMapper<DelinquencyRangeData> {

        public String schema() {
            return " dran.id as id, dran.classification as classification, dran.min_age_days as minimumAgeDays, dran.max_age_days as maximumAgeDays from m_delinquency_bucket buc "
                    + " inner join m_delinquency_bucket_mappings dmap on dmap.delinquency_bucket_id = buc.id "
                    + " inner join m_delinquency_range dran on dran.id = dmap.delinquency_range_id ";
        }

        @Override
        public DelinquencyRangeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String classification = rs.getString("classification");
            final Integer minimumAgeDays = rs.getInt("minimumAgeDays");
            final Integer maximumAgeDays = rs.getInt("maximumAgeDays");

            return new DelinquencyRangeData(id, classification, minimumAgeDays, maximumAgeDays);
        }
    }

}
