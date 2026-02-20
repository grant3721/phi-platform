package com.globaloutcomes.phi.domain.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkConnectivityObserverTest {

    private lateinit var networkObserver: NetworkConnectivityObserver
    private lateinit var mockContext: Context
    private lateinit var mockConnectivityManager: ConnectivityManager
    private lateinit var mockNetwork: Network
    private lateinit var mockNetworkCapabilities: NetworkCapabilities

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockConnectivityManager = mockk(relaxed = true)
        mockNetwork = mockk()
        mockNetworkCapabilities = mockk()

        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager

        networkObserver = NetworkConnectivityObserver(mockContext)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `isCurrentlyOnline returns true when network has internet capability`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        // Act
        val isOnline = networkObserver.isCurrentlyOnline()

        // Assert
        assertTrue(isOnline)
    }

    @Test
    fun `isCurrentlyOnline returns false when no active network`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns null

        // Act
        val isOnline = networkObserver.isCurrentlyOnline()

        // Assert
        assertFalse(isOnline)
    }

    @Test
    fun `isCurrentlyOnline returns false when network lacks internet capability`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        // Act
        val isOnline = networkObserver.isCurrentlyOnline()

        // Assert
        assertFalse(isOnline)
    }

    @Test
    fun `isCurrentlyOnline returns false when network not validated`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false

        // Act
        val isOnline = networkObserver.isCurrentlyOnline()

        // Assert
        assertFalse(isOnline)
    }

    @Test
    fun `isOnWifi returns true when connected via WiFi`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true

        // Act
        val isOnWifi = networkObserver.isOnWifi()

        // Assert
        assertTrue(isOnWifi)
    }

    @Test
    fun `isOnWifi returns false when not connected via WiFi`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false

        // Act
        val isOnWifi = networkObserver.isOnWifi()

        // Assert
        assertFalse(isOnWifi)
    }

    @Test
    fun `isOnCellular returns true when connected via cellular`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true

        // Act
        val isOnCellular = networkObserver.isOnCellular()

        // Assert
        assertTrue(isOnCellular)
    }

    @Test
    fun `isOnCellular returns false when not connected via cellular`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns mockNetwork
        every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false

        // Act
        val isOnCellular = networkObserver.isOnCellular()

        // Assert
        assertFalse(isOnCellular)
    }

    @Test
    fun `isOnWifi returns false when no active network`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns null

        // Act
        val isOnWifi = networkObserver.isOnWifi()

        // Assert
        assertFalse(isOnWifi)
    }

    @Test
    fun `isOnCellular returns false when no active network`() {
        // Arrange
        every { mockConnectivityManager.activeNetwork } returns null

        // Act
        val isOnCellular = networkObserver.isOnCellular()

        // Assert
        assertFalse(isOnCellular)
    }
}
