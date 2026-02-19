package com.globaloutcomes.phi.di

import com.globaloutcomes.phi.data.scan.BiosenseScanEngine
import com.globaloutcomes.phi.data.scan.StubScanEngine
import com.globaloutcomes.phi.domain.scan.ScanEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Scan Module - Provides ScanEngine implementation
 *
 * CONFIGURATION:
 * - Development: Use StubScanEngine (fake biomarkers, no SDK required)
 * - Production: Use BiosenseScanEngine (real SDK with license key)
 *
 * TO SWITCH TO REAL SDK:
 * 1. Obtain license key from BiosenseSignal
 * 2. Update LICENSE_KEY in BiosenseScanEngine.kt
 * 3. Comment out @Binds for StubScanEngine
 * 4. Uncomment @Binds for BiosenseScanEngine
 * 5. Rebuild project
 *
 * ZERO business logic changes required - abstraction layer protects all code!
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ScanModule {

    /**
     * STUB ENGINE (Active)
     * Use for development without SDK dependency
     * Generates realistic fake biomarkers
     */
    @Binds
    @Singleton
    abstract fun bindStubScanEngine(
        stubScanEngine: StubScanEngine
    ): ScanEngine

    /**
     * REAL ENGINE (Commented out - activate when license key available)
     * Uncomment this and comment out bindStubScanEngine above
     */
//    @Binds
//    @Singleton
//    abstract fun bindBiosenseScanEngine(
//        biosenseScanEngine: BiosenseScanEngine
//    ): ScanEngine
}
