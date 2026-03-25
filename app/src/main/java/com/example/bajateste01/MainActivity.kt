package com.example.bajateste01

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//imports do MPAndroidChart para LineChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.components.Description

// Constantes de máximo para cada sensor
const val VEL_MAX = 40      // velocidade máxima em km/h
const val RPM_MAX = 4000    // RPM máximo
const val TEMP_MAX = 130    // temperatura máxima em °C
const val PRESS_MAX = 100   // pressão máxima em hPa

// Altura máxima da barra em pixels
const val BARRA_ALTURA_MAX_PX = 200

class MainActivity : AppCompatActivity() {

    // Gráfico genérico
    private lateinit var lineChart: LineChart

    // Gráficos individuais
    private lateinit var lineChartVEL: LineChart
    private lateinit var lineChartRPM: LineChart
    private lateinit var lineChartTEMP: LineChart
    private lateinit var lineChartPRESS: LineChart

    // TextViews
    private lateinit var textVelocidade: TextView
    private lateinit var valBarraVelocidade: TextView
    private lateinit var textTemperatura: TextView
    private lateinit var valBarraTemperatura: TextView
    private lateinit var textPressao: TextView
    private lateinit var valBarraPressao: TextView
    private lateinit var textRPM: TextView
    private lateinit var valBarraRPM: TextView

    // Barras
    private lateinit var barraVelocidade: View
    private lateinit var barraTemperatura: View
    private lateinit var barraPressao: View
    private lateinit var barraRPM: View

    private lateinit var api: ApiService
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 2000

    // Históricos
    private val historicoVelocidade = mutableListOf<Entry>()
    private val historicoRPM = mutableListOf<Entry>()
    private val historicoTemperatura = mutableListOf<Entry>()
    private val historicoPressao = mutableListOf<Entry>()

    // Contadores separados
    private var contador = 0
    private var contadorVEL = 0
    private var contadorRPM = 0
    private var contadorTEMP = 0
    private var contadorPRESS = 0

    private val updateRunnable = object : Runnable {
        override fun run() {
            fetchDados()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa o gráfico genérico
        lineChart = findViewById(R.id.lineChart)

        // Inicializa os gráficos individuais
        lineChartVEL = findViewById(R.id.lineChartVEL)
        lineChartRPM = findViewById(R.id.lineChartRPM)
        lineChartTEMP = findViewById(R.id.lineChartTEMP)
        lineChartPRESS = findViewById(R.id.lineChartPRESS)

        // Configura os gráficos
        configurarGraficoGenérico()
        configurarGrafico(lineChartVEL, "Velocidade")
        configurarGrafico(lineChartRPM, "RPM")
        configurarGrafico(lineChartTEMP, "Temperatura")
        configurarGrafico(lineChartPRESS, "Pressão")

        // Inicializa TextViews
        textVelocidade = findViewById(R.id.textVelocidade)
        valBarraVelocidade = findViewById(R.id.textBarraVelocidadeVal)
        textTemperatura = findViewById(R.id.textTemperatura)
        valBarraTemperatura = findViewById(R.id.textBarraTemperaturaVal)
        textPressao = findViewById(R.id.textPressao)
        valBarraPressao = findViewById(R.id.textBarraPressaoVal)
        textRPM = findViewById(R.id.textRPM)
        valBarraRPM = findViewById(R.id.textBarraRPMval)

        // Inicializa barras
        barraVelocidade = findViewById(R.id.barraVelocidade)
        barraTemperatura = findViewById(R.id.barraTemperatura)
        barraPressao = findViewById(R.id.barraPressao)
        barraRPM = findViewById(R.id.barraRPM)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.158/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)

        handler.post(updateRunnable)
    }

    private fun configurarGraficoGenérico() {
        val description = Description()
        description.text = "Histórico dos Sensores"
        lineChart.description = description
        lineChart.animateX(1000)
        lineChart.axisRight.isEnabled = false
        lineChart.setDrawGridBackground(false)

        val xAxis = lineChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.labelRotationAngle = -45f

        val yAxis = lineChart.axisLeft
        yAxis.setDrawGridLines(true)

        lineChart.setPinchZoom(true)
        lineChart.setTouchEnabled(true)
        lineChart.legend.isEnabled = true
    }

    private fun configurarGrafico(chart: LineChart, titulo: String) {
        val description = Description()
        description.text = titulo
        chart.description = description
        chart.animateX(1000)
        chart.axisRight.isEnabled = false
        chart.setDrawGridBackground(false)

        val xAxis = chart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.labelRotationAngle = -45f

        val yAxis = chart.axisLeft
        yAxis.setDrawGridLines(true)

        chart.setPinchZoom(true)
        chart.setTouchEnabled(true)
        chart.legend.isEnabled = true
    }

    private fun atualizarGraficoGenerico(dados: Dados) {
        contador++

        historicoVelocidade.add(Entry(contador.toFloat(), dados.velocidade))
        historicoRPM.add(Entry(contador.toFloat(), dados.rpm))
        historicoTemperatura.add(Entry(contador.toFloat(), dados.temperatura))
        historicoPressao.add(Entry(contador.toFloat(), dados.pressao))

        if (historicoVelocidade.size > 20) {
            historicoVelocidade.removeAt(0)
            historicoRPM.removeAt(0)
            historicoTemperatura.removeAt(0)
            historicoPressao.removeAt(0)
        }

        val dataSetVelocidade = LineDataSet(historicoVelocidade, "Velocidade (km/h)")
        val dataSetRPM = LineDataSet(historicoRPM, "RPM")
        val dataSetTemperatura = LineDataSet(historicoTemperatura, "Temperatura (°C)")
        val dataSetPressao = LineDataSet(historicoPressao, "Pressão (hPa)")

        dataSetVelocidade.color = Color.BLUE
        dataSetVelocidade.setCircleColor(Color.BLUE)
        dataSetVelocidade.lineWidth = 2f
        dataSetVelocidade.circleRadius = 4f
        dataSetVelocidade.setDrawFilled(true)
        dataSetVelocidade.fillColor = Color.BLUE
        dataSetVelocidade.fillAlpha = 50

        dataSetRPM.color = Color.GREEN
        dataSetRPM.setCircleColor(Color.GREEN)
        dataSetRPM.lineWidth = 2f
        dataSetRPM.circleRadius = 4f
        dataSetRPM.setDrawFilled(true)
        dataSetRPM.fillColor = Color.GREEN
        dataSetRPM.fillAlpha = 50

        dataSetTemperatura.color = Color.MAGENTA
        dataSetTemperatura.setCircleColor(Color.MAGENTA)
        dataSetTemperatura.lineWidth = 2f
        dataSetTemperatura.circleRadius = 4f
        dataSetTemperatura.setDrawFilled(true)
        dataSetTemperatura.fillColor = Color.MAGENTA
        dataSetTemperatura.fillAlpha = 50

        dataSetPressao.color = Color.RED
        dataSetPressao.setCircleColor(Color.RED)
        dataSetPressao.lineWidth = 2f
        dataSetPressao.circleRadius = 4f
        dataSetPressao.setDrawFilled(true)
        dataSetPressao.fillColor = Color.RED
        dataSetPressao.fillAlpha = 50

        val lineData = LineData(dataSetVelocidade, dataSetRPM, dataSetTemperatura, dataSetPressao)
        lineChart.data = lineData
        lineChart.invalidate()
    }

    private fun atualizarGraficoVEL(dados: Dados) {
        contadorVEL++
        historicoVelocidade.add(Entry(contadorVEL.toFloat(), dados.velocidade))

        if (historicoVelocidade.size > 20) {
            historicoVelocidade.removeAt(0)
        }

        val dataSetVelocidade = LineDataSet(historicoVelocidade, "Velocidade (km/h)")
        dataSetVelocidade.color = Color.BLUE
        dataSetVelocidade.setCircleColor(Color.BLUE)
        dataSetVelocidade.lineWidth = 2f
        dataSetVelocidade.circleRadius = 4f
        dataSetVelocidade.setDrawFilled(true)
        dataSetVelocidade.fillColor = Color.BLUE
        dataSetVelocidade.fillAlpha = 50

        val lineData = LineData(dataSetVelocidade)
        lineChartVEL.data = lineData
        lineChartVEL.invalidate()
    }

    private fun atualizarGraficoRPM(dados: Dados) {
        contadorRPM++
        historicoRPM.add(Entry(contadorRPM.toFloat(), dados.rpm))

        if (historicoRPM.size > 20) {
            historicoRPM.removeAt(0)
        }

        val dataSetRPM = LineDataSet(historicoRPM, "RPM")
        dataSetRPM.color = Color.GREEN
        dataSetRPM.setCircleColor(Color.GREEN)
        dataSetRPM.lineWidth = 2f
        dataSetRPM.circleRadius = 4f
        dataSetRPM.setDrawFilled(true)
        dataSetRPM.fillColor = Color.GREEN
        dataSetRPM.fillAlpha = 50

        val lineData = LineData(dataSetRPM)
        lineChartRPM.data = lineData
        lineChartRPM.invalidate()
    }

    private fun atualizarGraficoTEMP(dados: Dados) {
        contadorTEMP++
        historicoTemperatura.add(Entry(contadorTEMP.toFloat(), dados.temperatura))

        if (historicoTemperatura.size > 20) {
            historicoTemperatura.removeAt(0)
        }

        val dataSetTemperatura = LineDataSet(historicoTemperatura, "Temperatura (°C)")
        dataSetTemperatura.color = Color.MAGENTA
        dataSetTemperatura.setCircleColor(Color.MAGENTA)
        dataSetTemperatura.lineWidth = 2f
        dataSetTemperatura.circleRadius = 4f
        dataSetTemperatura.setDrawFilled(true)
        dataSetTemperatura.fillColor = Color.MAGENTA
        dataSetTemperatura.fillAlpha = 50

        val lineData = LineData(dataSetTemperatura)
        lineChartTEMP.data = lineData
        lineChartTEMP.invalidate()
    }

    private fun atualizarGraficoPRESS(dados: Dados) {
        contadorPRESS++
        historicoPressao.add(Entry(contadorPRESS.toFloat(), dados.pressao))

        if (historicoPressao.size > 20) {
            historicoPressao.removeAt(0)
        }

        val dataSetPressao = LineDataSet(historicoPressao, "Pressão (hPa)")
        dataSetPressao.color = Color.RED
        dataSetPressao.setCircleColor(Color.RED)
        dataSetPressao.lineWidth = 2f
        dataSetPressao.circleRadius = 4f
        dataSetPressao.setDrawFilled(true)
        dataSetPressao.fillColor = Color.RED
        dataSetPressao.fillAlpha = 50

        val lineData = LineData(dataSetPressao)
        lineChartPRESS.data = lineData
        lineChartPRESS.invalidate()
    }

    private fun dpToPix(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun calcularAlturaBarra(valorAtual: Int, valorMaximo: Int): Int {
        val porcentagem = (valorAtual.toFloat() / valorMaximo.toFloat() * 100).coerceIn(0f, 100f)
        val alturaEmPixels = (BARRA_ALTURA_MAX_PX * porcentagem / 100).toInt()
        return alturaEmPixels.coerceAtLeast(10)
    }

    private fun fetchDados() {
        api.getDados().enqueue(object : Callback<Dados> {
            override fun onResponse(call: Call<Dados>, response: Response<Dados>) {
                if (response.isSuccessful) {
                    val dados = response.body()

                    dados?.let {
                        // Atualiza todos os gráficos
                        atualizarGraficoGenerico(it)
                        atualizarGraficoVEL(it)
                        atualizarGraficoRPM(it)
                        atualizarGraficoTEMP(it)
                        atualizarGraficoPRESS(it)

                        // Atualiza textos
                        textVelocidade.text = "Velocidade: ${it.velocidade} km/h"
                        valBarraVelocidade.text = "${it.velocidade} km/h"

                        textTemperatura.text = "Temperatura: ${it.temperatura} °C"
                        valBarraTemperatura.text = "${it.temperatura} °C"

                        textPressao.text = "Pressão: ${it.pressao} hPa"
                        valBarraPressao.text = "${it.pressao} hPa"

                        textRPM.text = "RPM: ${it.rpm}"
                        valBarraRPM.text = "${it.rpm} RPM"

                        // Atualiza barras com multiplicador *2
                        val velocidade = it.velocidade.toInt().coerceIn(0, VEL_MAX)
                        val rpm = it.rpm.toInt().coerceIn(0, RPM_MAX)
                        val temperatura = it.temperatura.toInt().coerceIn(0, TEMP_MAX)
                        val pressao = it.pressao.toInt().coerceIn(0, PRESS_MAX)

                        val alturaVelocidade = calcularAlturaBarra(velocidade, VEL_MAX) * 2
                        val alturaRPM = calcularAlturaBarra(rpm, RPM_MAX) * 2
                        val alturaTemperatura = calcularAlturaBarra(temperatura, TEMP_MAX) * 2
                        val alturaPressao = calcularAlturaBarra(pressao, PRESS_MAX) * 2

                        barraVelocidade.layoutParams.height = alturaVelocidade
                        barraRPM.layoutParams.height = alturaRPM
                        barraTemperatura.layoutParams.height = alturaTemperatura
                        barraPressao.layoutParams.height = alturaPressao

                        barraVelocidade.requestLayout()
                        barraRPM.requestLayout()
                        barraTemperatura.requestLayout()
                        barraPressao.requestLayout()
                    }
                }
            }

            override fun onFailure(call: Call<Dados>, t: Throwable) {
                textVelocidade.text = "Erro de conexão"
                valBarraVelocidade.text = "-"
                textRPM.text = "Erro de conexão"
                valBarraRPM.text = "-"
                textTemperatura.text = "Erro de conexão"
                valBarraTemperatura.text = "-"
                textPressao.text = "Erro de conexão"
                valBarraPressao.text = "-"

                val alturaMinima = dpToPix(10)

                barraVelocidade.layoutParams.height = alturaMinima
                barraRPM.layoutParams.height = alturaMinima
                barraTemperatura.layoutParams.height = alturaMinima
                barraPressao.layoutParams.height = alturaMinima

                barraVelocidade.requestLayout()
                barraRPM.requestLayout()
                barraTemperatura.requestLayout()
                barraPressao.requestLayout()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}