package com.globaloutcomes.phi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.globaloutcomes.phi.domain.repository.BarangayRepository
import com.globaloutcomes.phi.presentation.navigation.PHINavGraph
import com.globaloutcomes.phi.presentation.theme.PHITheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var barangayRepository: BarangayRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PHITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    PHINavGraph(
                        navController = navController,
                        barangayRepository = barangayRepository
                    )
                }
            }
        }
    }
}
