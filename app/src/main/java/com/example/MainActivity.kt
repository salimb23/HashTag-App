package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.db.AppDatabase
import com.example.data.repository.HashtagRepository
import com.example.ui.screens.MainHashtagScreen
import androidx.lifecycle.ViewModelProvider
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HashtagViewModel
import com.example.ui.viewmodel.HashtagViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: HashtagViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Database, Repository, and ViewModel using standard lifecycle-aware ViewModelProvider
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = HashtagRepository(database.savedHashtagsDao())
        viewModel = ViewModelProvider(this, HashtagViewModelFactory(repository))[HashtagViewModel::class.java]
        
        // Mandatory enableEdgeToEdge for modern Material Design 3 screen sizing
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainHashtagScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
