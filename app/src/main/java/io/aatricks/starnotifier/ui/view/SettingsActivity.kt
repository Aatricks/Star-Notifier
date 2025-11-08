package io.aatricks.starnotifier.ui.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import io.aatricks.starnotifier.R
import io.aatricks.starnotifier.data.local.SharedPreferencesStorage
import io.aatricks.starnotifier.data.repository.GitHubApiService
import io.aatricks.starnotifier.data.repository.GitHubRepository
import io.aatricks.starnotifier.ui.adapter.RepositoryAdapter
import io.aatricks.starnotifier.ui.viewmodel.SettingsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var repositoryAdapter: RepositoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Create dependencies
        val sharedPreferencesStorage = SharedPreferencesStorage(this)
        val gson = Gson()
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val apiService = retrofit.create(GitHubApiService::class.java)
        val gitHubRepository = GitHubRepository(apiService)
        val workManager = WorkManager.getInstance(this)

        // Create ViewModel
        viewModel = SettingsViewModel(gitHubRepository, sharedPreferencesStorage, workManager)

        repositoryAdapter = RepositoryAdapter()
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        val usernameInput = findViewById<TextInputEditText>(R.id.usernameInput)
        val tokenInput = findViewById<TextInputEditText>(R.id.tokenInput)
        val saveButton = findViewById<MaterialButton>(R.id.saveButton)
        val selectAllButton = findViewById<MaterialButton>(R.id.selectAllButton)
        val recyclerView = findViewById<RecyclerView>(R.id.repositoriesRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = repositoryAdapter

        saveButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val token = tokenInput.text.toString().takeIf { it.isNotBlank() }
            viewModel.saveUserConfig(username, token)
        }

        selectAllButton.setOnClickListener {
            viewModel.toggleSelectAllRepositories()
        }

        repositoryAdapter.onRepositorySelected = { repoName, isSelected ->
            viewModel.updateRepositorySelection(repoName, isSelected)
        }
    }

    private fun observeViewModel() {
        viewModel.repositories.observe(this) { repos ->
            repositoryAdapter.submitList(repos)
            updateSelectAllButtonText(repos)
        }

        viewModel.userConfig.observe(this) { config ->
            config?.let {
                findViewById<TextInputEditText>(R.id.usernameInput).setText(it.username)
                findViewById<TextInputEditText>(R.id.tokenInput).setText(it.personalAccessToken ?: "")
            }
        }

        viewModel.totalsText.observe(this) { totals ->
            findViewById<android.widget.TextView>(R.id.totalsTextView).text = totals
        }
    }

    private fun updateSelectAllButtonText(repos: List<io.aatricks.starnotifier.data.model.Repository>) {
        val selectAllButton = findViewById<MaterialButton>(R.id.selectAllButton)
        val allSelected = repos.all { it.isSelected }
        selectAllButton.text = if (allSelected) "Deselect All Repositories" else "Select All Repositories"
    }
}