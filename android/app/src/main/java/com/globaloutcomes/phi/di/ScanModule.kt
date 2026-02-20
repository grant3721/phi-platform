package com.globaloutcomes.phi.di

import com.globaloutcomes.phi.data.scan.StubScanEngine
import com.globaloutcomes.phi.domain.scan.ScanEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for scan dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ScanModule {

    /**
     * Binds ScanEngine interface to StubScanEngine implementation
     * When real BiosenseSignal SDK is available, create BiosenseScanEngine
     * and bind it here instead - no other code changes needed!
     */
    @Binds
    @Singleton
    abstract fun bindScanEngine(
        stubScanEngine: StubScanEngine
    ): ScanEngine
}
