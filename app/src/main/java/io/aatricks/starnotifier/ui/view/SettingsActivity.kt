package io.aatricks.starnotifier.ui.view

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.navigation.NavigationView
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
        val gitHubRepository = GitHubRepository(apiService, sharedPreferencesStorage)
        val workManager = WorkManager.getInstance(this)

        // Create ViewModel
        viewModel = SettingsViewModel(gitHubRepository, sharedPreferencesStorage, workManager)

        repositoryAdapter = RepositoryAdapter()
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        
        // Enable swipe gesture to open drawer
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        
        // Setup toolbar with drawer toggle
        toolbar.setNavigationOnClickListener {
            drawerLayout.open()
        }
        
        // Get views from navigation drawer
        val usernameInput = navigationView.findViewById<TextInputEditText>(R.id.usernameInput)
        val tokenInput = navigationView.findViewById<TextInputEditText>(R.id.tokenInput)
        val saveButton = navigationView.findViewById<MaterialButton>(R.id.saveButton)
        
        val selectAllButton = findViewById<MaterialButton>(R.id.selectAllButton)
        val recyclerView = findViewById<RecyclerView>(R.id.repositoriesRecyclerView)
        val trafficModeToggle = findViewById<MaterialButtonToggleGroup>(R.id.trafficModeToggle)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = repositoryAdapter

        saveButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val token = tokenInput.text.toString().takeIf { it.isNotBlank() }
            viewModel.saveUserConfig(username, token)
            drawerLayout.close()
        }

        selectAllButton.setOnClickListener {
            viewModel.toggleSelectAllRepositories()
        }
        
        // Set up traffic mode toggle (default to lifetime)
        trafficModeToggle.check(R.id.lifetimeButton)
        trafficModeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.lifetimeButton -> viewModel.setTrafficMode(true)
                    R.id.twoWeeksButton -> viewModel.setTrafficMode(false)
                }
            }
        }

        repositoryAdapter.onRepositorySelected = { repoName, isSelected ->
            viewModel.updateRepositorySelection(repoName, isSelected)
        }
    }

    private fun observeViewModel() {
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val usernameInput = navigationView.findViewById<TextInputEditText>(R.id.usernameInput)
        val tokenInput = navigationView.findViewById<TextInputEditText>(R.id.tokenInput)
        
        viewModel.repositories.observe(this) { repos ->
            repositoryAdapter.submitList(repos)
            updateSelectAllButtonText(repos)
        }

        viewModel.userConfig.observe(this) { config ->
            config?.let {
                usernameInput.setText(it.username)
                tokenInput.setText(it.personalAccessToken ?: "")
            }
        }
        
        viewModel.totalStars.observe(this) { stars ->
            findViewById<android.widget.TextView>(R.id.totalStarsTextView).text = formatNumber(stars)
        }
        
        viewModel.totalForks.observe(this) { forks ->
            findViewById<android.widget.TextView>(R.id.totalForksTextView).text = formatNumber(forks)
        }
        
        viewModel.totalViews.observe(this) { views ->
            findViewById<android.widget.TextView>(R.id.totalViewsTextView).text = formatNumber(views)
        }
        
        viewModel.totalClones.observe(this) { clones ->
            findViewById<android.widget.TextView>(R.id.totalClonesTextView).text = formatNumber(clones)
        }
    }
    
    private fun formatNumber(num: Int): String {
        return when {
            num >= 1000000 -> String.format("%.1fM", num / 1000000.0)
            num >= 1000 -> String.format("%.1fk", num / 1000.0)
            else -> num.toString()
        }
    }

    private fun updateSelectAllButtonText(repos: List<io.aatricks.starnotifier.data.model.Repository>) {
        val selectAllButton = findViewById<MaterialButton>(R.id.selectAllButton)
        val allSelected = repos.all { it.isSelected }
        selectAllButton.text = if (allSelected) 
            getString(R.string.deselect_all_repositories) 
        else 
            getString(R.string.select_all_repositories)
    }
}