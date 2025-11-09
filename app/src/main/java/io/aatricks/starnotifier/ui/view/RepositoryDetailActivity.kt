package io.aatricks.starnotifier.ui.view

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import io.aatricks.starnotifier.R
import io.aatricks.starnotifier.data.model.Repository
import java.text.SimpleDateFormat
import java.util.*

class RepositoryDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_REPOSITORY = "extra_repository"
    }

    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository_detail)

        // Get repository from intent
        val repoJson = intent.getStringExtra(EXTRA_REPOSITORY)
        repository = Gson().fromJson(repoJson, Repository::class.java)

        setupViews()
        setupCharts()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Repository Traffic"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.repoNameTextView).text = repository.name
        findViewById<TextView>(R.id.starsTextView).text = formatNumber(repository.currentStars)
        findViewById<TextView>(R.id.forksTextView).text = formatNumber(repository.currentForks)
        
        findViewById<TextView>(R.id.viewsCountTextView).text = 
            "Total (2 weeks): ${formatNumber(repository.twoWeekViews)}"
        findViewById<TextView>(R.id.clonesCountTextView).text = 
            "Total (2 weeks): ${formatNumber(repository.twoWeekClones)}"
    }

    private fun setupCharts() {
        setupViewsChart()
        setupClonesChart()
    }

    private fun setupViewsChart() {
        val viewsChart = findViewById<LineChart>(R.id.viewsChart)
        
        if (repository.viewsData.isEmpty()) {
            viewsChart.setNoDataText("No traffic data available")
            return
        }

        val entries = repository.viewsData.mapIndexed { index, trafficEntry ->
            Entry(index.toFloat(), trafficEntry.count.toFloat())
        }

        val dataSet = LineDataSet(entries, "Views per day").apply {
            color = getColor(R.color.chart_primary)
            setCircleColor(getColor(R.color.chart_primary))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }

        val lineData = LineData(dataSet)
        viewsChart.data = lineData

        configureChart(viewsChart, repository.viewsData.map { it.timestamp })
        
        // Add click listener to show values
        viewsChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val count = it.y.toInt()
                    val index = it.x.toInt()
                    if (index >= 0 && index < repository.viewsData.size) {
                        val timestamp = repository.viewsData[index].timestamp
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                            val date = inputFormat.parse(timestamp)
                            val dateStr = date?.let { d -> dateFormat.format(d) } ?: timestamp
                            Toast.makeText(this@RepositoryDetailActivity, "$dateStr: $count views", Toast.LENGTH_SHORT).show()
                        } catch (ex: Exception) {
                            Toast.makeText(this@RepositoryDetailActivity, "$count views", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onNothingSelected() {}
        })
    }

    private fun setupClonesChart() {
        val clonesChart = findViewById<LineChart>(R.id.clonesChart)
        
        if (repository.clonesData.isEmpty()) {
            clonesChart.setNoDataText("No traffic data available")
            return
        }

        val entries = repository.clonesData.mapIndexed { index, trafficEntry ->
            Entry(index.toFloat(), trafficEntry.count.toFloat())
        }

        val dataSet = LineDataSet(entries, "Clones per day").apply {
            color = getColor(R.color.chart_secondary)
            setCircleColor(getColor(R.color.chart_secondary))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }

        val lineData = LineData(dataSet)
        clonesChart.data = lineData

        configureChart(clonesChart, repository.clonesData.map { it.timestamp })
        
        // Add click listener to show values
        clonesChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val count = it.y.toInt()
                    val index = it.x.toInt()
                    if (index >= 0 && index < repository.clonesData.size) {
                        val timestamp = repository.clonesData[index].timestamp
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                            val date = inputFormat.parse(timestamp)
                            val dateStr = date?.let { d -> dateFormat.format(d) } ?: timestamp
                            Toast.makeText(this@RepositoryDetailActivity, "$dateStr: $count clones", Toast.LENGTH_SHORT).show()
                        } catch (ex: Exception) {
                            Toast.makeText(this@RepositoryDetailActivity, "$count clones", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onNothingSelected() {}
        })
    }

    private fun configureChart(chart: LineChart, timestamps: List<String>) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        if (index >= 0 && index < timestamps.size) {
                            try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                                val date = inputFormat.parse(timestamps[index])
                                return date?.let { dateFormat.format(it) } ?: ""
                            } catch (e: Exception) {
                                return ""
                            }
                        }
                        return ""
                    }
                }
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 1f
            }
            
            axisRight.isEnabled = false
            
            invalidate()
        }
    }

    private fun formatNumber(num: Int): String {
        return when {
            num >= 1000000 -> String.format("%.1fM", num / 1000000.0)
            num >= 1000 -> String.format("%.1fk", num / 1000.0)
            else -> num.toString()
        }
    }
}
