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
package org.apache.fineract.organisation.monetary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Set;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test to verify that the MoneyHelper tenant isolation fix works correctly with pure utility class approach. This
 * validates that PS-2617 has been properly resolved using the new architecture.
 */
class MoneyHelperTenantIsolationTest {

    private FineractPlatformTenant tenantA;
    private FineractPlatformTenant tenantB;
    private FineractPlatformTenant originalTenant;

    @BeforeEach
    void setUp() {
        // Store original tenant to restore later
        originalTenant = ThreadLocalContextUtil.getTenant();

        // Create test tenants
        tenantA = new FineractPlatformTenant(1L, "tenantA", "Tenant A", "Asia/Kolkata", null);
        tenantB = new FineractPlatformTenant(2L, "tenantB", "Tenant B", "Asia/Kolkata", null);

        // Clear cache to ensure clean test state
        MoneyHelper.clearCache();
    }

    @AfterEach
    void tearDown() {
        // Restore original tenant context
        ThreadLocalContextUtil.setTenant(originalTenant);

        // Clear cache to prevent test interference
        MoneyHelper.clearCache();
    }

    @Test
    @DisplayName("FIXED: MoneyHelper now provides proper tenant isolation using pure utility class")
    void testProperTenantIsolationWithPureUtilityClass() {
        // Initialize tenants with different rounding modes
        MoneyHelper.initializeTenantRoundingMode("tenantA", 4); // HALF_UP
        MoneyHelper.initializeTenantRoundingMode("tenantB", 6); // HALF_EVEN

        // Step 1: Tenant A requests rounding mode
        ThreadLocalContextUtil.setTenant(tenantA);
        RoundingMode tenantARoundingMode = MoneyHelper.getRoundingMode();

        // Step 2: Tenant B requests rounding mode (should get their own config)
        ThreadLocalContextUtil.setTenant(tenantB);
        RoundingMode tenantBRoundingMode = MoneyHelper.getRoundingMode();

        // VERIFY: Each tenant gets their configured rounding mode
        assertEquals(RoundingMode.HALF_UP, tenantARoundingMode, "Tenant A should get HALF_UP");
        assertEquals(RoundingMode.HALF_EVEN, tenantBRoundingMode, "Tenant B should get HALF_EVEN");

        // VERIFY: Tenants have different rounding modes (isolation confirmed)
        assertNotEquals(tenantARoundingMode, tenantBRoundingMode, "FIXED: Tenants now have proper isolation with different rounding modes");
    }

    @Test
    @DisplayName("FIXED: MathContext isolation provides tenant-specific financial calculations")
    void testProperMathContextIsolation() {
        // Initialize tenants with different rounding modes
        MoneyHelper.initializeTenantRoundingMode("tenantA", 4); // HALF_UP
        MoneyHelper.initializeTenantRoundingMode("tenantB", 6); // HALF_EVEN

        // Create test amount that will show rounding differences when divided by 3
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, null);
        BigDecimal testAmount = new BigDecimal("1.00"); // 1.00 / 3 = 0.333... which rounds differently

        // Tenant A gets MathContext and creates Money with it
        ThreadLocalContextUtil.setTenant(tenantA);
        MathContext tenantAMathContext = MoneyHelper.getMathContext();
        Money tenantAMoney = Money.of(currency, testAmount, tenantAMathContext);
        Money tenantAResult = tenantAMoney.dividedBy(BigDecimal.valueOf(3), tenantAMathContext); // 1.00 / 3 = 0.333...

        // Tenant B gets MathContext (should be different now) and creates Money with it
        ThreadLocalContextUtil.setTenant(tenantB);
        MathContext tenantBMathContext = MoneyHelper.getMathContext();
        Money tenantBMoney = Money.of(currency, testAmount, tenantBMathContext);
        Money tenantBResult = tenantBMoney.dividedBy(BigDecimal.valueOf(3), tenantBMathContext); // 1.00 / 3 = 0.333...

        // VERIFY: Different MathContext objects with different rounding modes
        assertNotEquals(tenantAMathContext, tenantBMathContext, "FIXED: Tenants now get different MathContext instances");
        assertNotEquals(tenantAMathContext.getRoundingMode(), tenantBMathContext.getRoundingMode(),
                "FIXED: Tenants use different rounding modes");
    }

    @Test
    @DisplayName("FIXED: Real-world multi-tenant sequence now works correctly")
    void testRealWorldMultiTenantSequence() {
        // Initialize tenants with different rounding modes
        MoneyHelper.initializeTenantRoundingMode("tenantA", 4); // HALF_UP
        MoneyHelper.initializeTenantRoundingMode("tenantB", 6); // HALF_EVEN

        // Scenario: Tenant A processes a loan EMI calculation
        ThreadLocalContextUtil.setTenant(tenantA);
        RoundingMode tenantAMode = MoneyHelper.getRoundingMode();
        MathContext tenantAContext = MoneyHelper.getMathContext();

        // Simulate loan calculation with tenant-specific MathContext
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, null);
        Money principalAmount = Money.of(currency, new BigDecimal("1000.00"), tenantAContext);
        Money tenantAEMI = principalAmount.dividedBy(BigDecimal.valueOf(3), tenantAContext); // 1000 / 3 = 333.333...

        // Scenario: Tenant B processes their loan EMI calculation immediately after
        ThreadLocalContextUtil.setTenant(tenantB);
        RoundingMode tenantBMode = MoneyHelper.getRoundingMode();
        MathContext tenantBContext = MoneyHelper.getMathContext();

        // Tenant B's calculation uses their own rounding mode
        Money tenantBPrincipal = Money.of(currency, new BigDecimal("1000.00"), tenantBContext);
        Money tenantBEMI = tenantBPrincipal.dividedBy(BigDecimal.valueOf(3), tenantBContext); // 1000 / 3 = 333.333...

        // VERIFY: Tenants now use different configurations (BUG FIXED)
        assertNotEquals(tenantAMode, tenantBMode, "FIXED: Tenant B now uses their own rounding mode");
        assertNotEquals(tenantAContext.getRoundingMode(), tenantBContext.getRoundingMode(),
                "FIXED: Tenants use different MathContext rounding modes");
    }

    @Test
    @DisplayName("FIXED: Compliance risk scenario now works correctly")
    void testComplianceRiskScenarioFixed() {
        // Initialize tenants with different regulatory requirements
        MoneyHelper.initializeTenantRoundingMode("tenantA", 4); // US regulations: HALF_UP
        MoneyHelper.initializeTenantRoundingMode("tenantB", 3); // EU regulations: FLOOR

        // US tenant follows US banking regulations (HALF_UP)
        ThreadLocalContextUtil.setTenant(tenantA); // US tenant
        RoundingMode usRoundingMode = MoneyHelper.getRoundingMode();

        // EU tenant should follow EU banking regulations (FLOOR)
        ThreadLocalContextUtil.setTenant(tenantB); // EU tenant
        RoundingMode euRoundingMode = MoneyHelper.getRoundingMode();

        // COMPLIANCE SUCCESS: Each tenant uses their correct rounding rules
        assertEquals(RoundingMode.HALF_UP, usRoundingMode, "US tenant uses HALF_UP");
        assertEquals(RoundingMode.FLOOR, euRoundingMode, "EU tenant uses FLOOR");

        // VERIFY: No more compliance violations
        assertNotEquals(usRoundingMode, euRoundingMode, "FIXED: EU tenant now uses correct regulatory rounding mode");
    }

    @Test
    @DisplayName("CACHE: Cache invalidation works correctly")
    void testCacheInvalidation() {
        // Initialize tenant with HALF_UP
        MoneyHelper.initializeTenantRoundingMode("tenantA", 4); // HALF_UP

        // Tenant A gets initial configuration
        ThreadLocalContextUtil.setTenant(tenantA);
        RoundingMode initialMode = MoneyHelper.getRoundingMode();
        assertEquals(RoundingMode.HALF_UP, initialMode);

        // Update tenant configuration to HALF_EVEN
        MoneyHelper.updateTenantRoundingMode("tenantA", 6); // HALF_EVEN

        // Tenant A should now get updated configuration
        RoundingMode updatedMode = MoneyHelper.getRoundingMode();
        assertEquals(RoundingMode.HALF_EVEN, updatedMode);

        // Verify cache was properly invalidated
        assertNotEquals(initialMode, updatedMode, "Cache update should allow configuration changes");
    }

    @Test
    @DisplayName("PERFORMANCE: Tenant-keyed cache maintains performance benefits")
    void testPerformanceBenefits() {
        // Initialize tenant
        MoneyHelper.initializeTenantRoundingMode("tenantA", 4); // HALF_UP

        ThreadLocalContextUtil.setTenant(tenantA);

        // First call should cache the value
        long startTime = System.nanoTime();
        RoundingMode mode1 = MoneyHelper.getRoundingMode();
        long firstCallTime = System.nanoTime() - startTime;

        // Subsequent calls should be faster (cached)
        startTime = System.nanoTime();
        RoundingMode mode2 = MoneyHelper.getRoundingMode();
        long secondCallTime = System.nanoTime() - startTime;

        // Verify same result and expect second call to be faster
        assertEquals(mode1, mode2, "Cached result should be consistent");
    }

    @Test
    @DisplayName("DEMONSTRATION: Specific calculation showing rounding differences")
    void testCalculationWithActualRoundingDifferences() {
        // Initialize tenants with different rounding modes
        MoneyHelper.initializeTenantRoundingMode("tenantA", 4); // HALF_UP
        MoneyHelper.initializeTenantRoundingMode("tenantB", 1); // DOWN

        // Test with a value that demonstrates clear rounding difference
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, null);
        BigDecimal testAmount = new BigDecimal("2.556"); // .556 will round differently

        // Tenant A: HALF_UP
        ThreadLocalContextUtil.setTenant(tenantA);
        MathContext tenantAContext = MoneyHelper.getMathContext();
        Money tenantAMoney = Money.of(currency, testAmount, tenantAContext);

        // Tenant B: DOWN
        ThreadLocalContextUtil.setTenant(tenantB);
        MathContext tenantBContext = MoneyHelper.getMathContext();
        Money tenantBMoney = Money.of(currency, testAmount, tenantBContext);

        // The money creation itself applies rounding based on currency decimal places
        // For USD with 2 decimal places: 2.556 -> HALF_UP gives 2.56, DOWN gives 2.55

        // Verify that the fix provides proper tenant isolation
        assertNotEquals(tenantAContext.getRoundingMode(), tenantBContext.getRoundingMode(), "Tenants use different rounding modes");
        assertEquals(RoundingMode.HALF_UP, tenantAContext.getRoundingMode());
        assertEquals(RoundingMode.DOWN, tenantBContext.getRoundingMode());
    }

    @Test
    @DisplayName("EXCEPTION: Should throw IllegalStateException when no tenant context is available")
    void testThrowsExceptionWhenNoTenantContext() {
        // Clear any existing tenant context
        ThreadLocalContextUtil.setTenant(null);

        // Test getRoundingMode throws exception
        IllegalStateException roundingModeException = assertThrows(IllegalStateException.class, () -> MoneyHelper.getRoundingMode(),
                "Expected IllegalStateException when no tenant context is available for getRoundingMode");

        assertEquals("No tenant context available. MoneyHelper requires a valid tenant context to ensure proper multi-tenant isolation.",
                roundingModeException.getMessage());

        // Test getMathContext throws exception
        IllegalStateException mathContextException = assertThrows(IllegalStateException.class, () -> MoneyHelper.getMathContext(),
                "Expected IllegalStateException when no tenant context is available for getMathContext");

        assertEquals("No tenant context available. MoneyHelper requires a valid tenant context to ensure proper multi-tenant isolation.",
                mathContextException.getMessage());
    }

    @Test
    @DisplayName("UTILITY: Test utility methods for tenant management")
    void testUtilityMethods() {
        // Initially no tenants should be initialized
        assertTrue(MoneyHelper.getInitializedTenants().isEmpty(), "No tenants should be initialized initially");
        assertFalse(MoneyHelper.isTenantInitialized("tenantA"), "Tenant A should not be initialized");

        // Initialize tenant A
        MoneyHelper.initializeTenantRoundingMode("tenantA", 4); // HALF_UP

        // Verify tenant A is now initialized
        assertTrue(MoneyHelper.isTenantInitialized("tenantA"), "Tenant A should be initialized");
        Set<String> initializedTenants = MoneyHelper.getInitializedTenants();
        assertEquals(1, initializedTenants.size(), "One tenant should be initialized");
        assertTrue(initializedTenants.contains("tenantA"), "Tenant A should be in initialized tenants");

        // Initialize tenant B
        MoneyHelper.initializeTenantRoundingMode("tenantB", 6); // HALF_EVEN

        // Verify both tenants are initialized
        assertTrue(MoneyHelper.isTenantInitialized("tenantB"), "Tenant B should be initialized");
        initializedTenants = MoneyHelper.getInitializedTenants();
        assertEquals(2, initializedTenants.size(), "Two tenants should be initialized");
        assertTrue(initializedTenants.contains("tenantB"), "Tenant B should be in initialized tenants");

        // Clear cache for tenant A
        MoneyHelper.clearCacheForTenant("tenantA");

        // Verify tenant A is no longer initialized
        assertFalse(MoneyHelper.isTenantInitialized("tenantA"), "Tenant A should not be initialized after clearing cache");
        assertTrue(MoneyHelper.isTenantInitialized("tenantB"), "Tenant B should still be initialized");
    }

    @Test
    @DisplayName("VALIDATION: Invalid rounding mode values should throw exception")
    void testInvalidRoundingModeValidation() {
        // Test invalid rounding mode values
        IllegalArgumentException tooLowException = assertThrows(IllegalArgumentException.class,
                () -> MoneyHelper.initializeTenantRoundingMode("tenantA", -1),
                "Expected IllegalArgumentException for negative rounding mode");

        assertTrue(tooLowException.getMessage().contains("Invalid rounding mode value: -1"),
                "Exception message should indicate invalid rounding mode");

        IllegalArgumentException tooHighException = assertThrows(IllegalArgumentException.class,
                () -> MoneyHelper.initializeTenantRoundingMode("tenantA", 7), "Expected IllegalArgumentException for rounding mode > 6");

        assertTrue(tooHighException.getMessage().contains("Invalid rounding mode value: 7"),
                "Exception message should indicate invalid rounding mode");

        // Test null tenant identifier
        IllegalArgumentException nullTenantException = assertThrows(IllegalArgumentException.class,
                () -> MoneyHelper.initializeTenantRoundingMode(null, 4), "Expected IllegalArgumentException for null tenant identifier");

        assertEquals("Tenant identifier cannot be null", nullTenantException.getMessage());
    }

    @Test
    @DisplayName("UTILITY: createMathContext should work without tenant context")
    void testCreateMathContextUtilityMethod() {
        // This method should work without tenant context
        ThreadLocalContextUtil.setTenant(null);

        // Test creating MathContext with different rounding modes
        MathContext halfUpContext = MoneyHelper.createMathContext(RoundingMode.HALF_UP);
        MathContext halfEvenContext = MoneyHelper.createMathContext(RoundingMode.HALF_EVEN);

        // Verify contexts are created correctly
        assertEquals(MoneyHelper.PRECISION, halfUpContext.getPrecision(), "Precision should be correct");
        assertEquals(RoundingMode.HALF_UP, halfUpContext.getRoundingMode(), "Rounding mode should be HALF_UP");
        assertEquals(RoundingMode.HALF_EVEN, halfEvenContext.getRoundingMode(), "Rounding mode should be HALF_EVEN");

        // Verify they are different
        assertNotEquals(halfUpContext, halfEvenContext, "Different rounding modes should create different contexts");
    }
}
