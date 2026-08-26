package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.db.PyCodeDatabase
import com.example.data.repository.WorkspaceRepository
import com.example.ui.IdeMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VsCodeBg
import com.example.ui.viewmodel.PyCodeViewModel
import com.example.ui.viewmodel.PyCodeViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: PyCodeViewModel by viewModels {
        val db = PyCodeDatabase.getDatabase(applicationContext)
        val repo = WorkspaceRepository(db.workspaceDao())
        PyCodeViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    color = VsCodeBg,
                    modifier = Modifier.fillMaxSize()
                ) {
                    IdeMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
