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

// Altura máxima da barra em pixels (ajuste conforme necessário)
const val BARRA_ALTURA_MAX_PX = 200

class MainActivity : AppCompatActivity() {

    // LineChart
    private lateinit var lineChart: LineChart

    private lateinit var textVelocidade: TextView
    private lateinit var valBarraVelocidade: TextView
    private lateinit var textTemperatura: TextView
    private lateinit var valBarraTemperatura: TextView
    private lateinit var textPressao: TextView
    private lateinit var valBarraPressao: TextView

    private lateinit var textRPM: TextView
    private lateinit var valBarraRPM: TextView

    private lateinit var barraVelocidade: View
    private lateinit var barraTemperatura: View
    private lateinit var barraPressao: View
    private lateinit var barraRPM: View

    private lateinit var api: ApiService
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 2000 // 2 segundos

    // Lista para armazenar histórico dos dados (opcional)
    private val historicoVelocidade = mutableListOf<Entry>()
    private val historicoRPM = mutableListOf<Entry>()
    private val historicoTemperatura = mutableListOf<Entry>()
    private val historicoPressao = mutableListOf<Entry>()
    private var contador = 0

    private val updateRunnable = object : Runnable {
        override fun run() {
            fetchDados()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa o LineChart
        lineChart = findViewById(R.id.lineChart)
        configurarGrafico()

        textVelocidade = findViewById(R.id.textVelocidade)
        valBarraVelocidade = findViewById(R.id.textBarraVelocidadeVal)

        textTemperatura = findViewById(R.id.textTemperatura)
        valBarraTemperatura = findViewById(R.id.textBarraTemperaturaVal)

        textPressao = findViewById(R.id.textPressao)
        valBarraPressao = findViewById(R.id.textBarraPressaoVal)

        textRPM = findViewById(R.id.textRPM)
        valBarraRPM = findViewById(R.id.textBarraRPMval)

        // parte dos graficos
        barraVelocidade = findViewById(R.id.barraVelocidade)
        barraTemperatura = findViewById(R.id.barraTemperatura)
        barraPressao = findViewById(R.id.barraPressao)
        barraRPM = findViewById(R.id.barraRPM)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.158/") // IP do ESP32
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)

        // inicia a atualizacao periodica
        handler.post(updateRunnable)
    }

    private fun configurarGrafico() {
        // Configura a descrição
        val description = Description()
        description.text = "Histórico dos Sensores"
        lineChart.description = description

        // Habilita animação
        lineChart.animateX(1000)

        // Desabilita o eixo Y direito
        lineChart.axisRight.isEnabled = false

        // Habilita a grade
        lineChart.setDrawGridBackground(false)

        // Configura o eixo X
        val xAxis = lineChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.labelRotationAngle = -45f // Rotaciona labels se necessário

        // Configura o eixo Y esquerdo
        val yAxis = lineChart.axisLeft
        yAxis.setDrawGridLines(true)

        // Habilita zoom
        lineChart.setPinchZoom(true)

        // Habilita toque para interação
        lineChart.setTouchEnabled(true)

        // Habilita legenda
        lineChart.legend.isEnabled = true
    }

    private fun atualizarGrafico(dados: Dados?) {
        dados?.let {
            // Incrementa contador para posição X
            contador++

            // Adiciona novos pontos ao histórico
            historicoVelocidade.add(Entry(contador.toFloat(), it.velocidade.toFloat()))
            historicoRPM.add(Entry(contador.toFloat(), it.rpm.toFloat()))
            historicoTemperatura.add(Entry(contador.toFloat(), it.temperatura.toFloat()))
            historicoPressao.add(Entry(contador.toFloat(), it.pressao.toFloat()))

            // Mantém apenas os últimos 20 pontos (opcional)
            if (historicoVelocidade.size > 20) {
                historicoVelocidade.removeAt(0)
                historicoRPM.removeAt(0)
                historicoTemperatura.removeAt(0)
                historicoPressao.removeAt(0)
            }

            // Cria os datasets para cada sensor
            val dataSetVelocidade = LineDataSet(historicoVelocidade, "Velocidade (km/h)")
            val dataSetRPM = LineDataSet(historicoRPM, "RPM")
            val dataSetTemperatura = LineDataSet(historicoTemperatura, "Temperatura (°C)")
            val dataSetPressao = LineDataSet(historicoPressao, "Pressão (hPa)")

            // Configura cores e estilos
            dataSetVelocidade.color = Color.BLUE
            dataSetVelocidade.setCircleColor(Color.BLUE)
            dataSetVelocidade.lineWidth = 2f
            dataSetVelocidade.circleRadius = 4f
            dataSetVelocidade.setDrawCircleHole(false)
            dataSetVelocidade.valueTextSize = 10f

            dataSetRPM.color = Color.GREEN
            dataSetRPM.setCircleColor(Color.GREEN)
            dataSetRPM.lineWidth = 2f
            dataSetRPM.circleRadius = 4f
            dataSetRPM.setDrawCircleHole(false)
            dataSetRPM.valueTextSize = 10f

            dataSetTemperatura.color = Color.MAGENTA
            dataSetTemperatura.setCircleColor(Color.MAGENTA)
            dataSetTemperatura.lineWidth = 2f
            dataSetTemperatura.circleRadius = 4f
            dataSetTemperatura.setDrawCircleHole(false)
            dataSetTemperatura.valueTextSize = 10f

            dataSetPressao.color = Color.RED
            dataSetPressao.setCircleColor(Color.RED)
            dataSetPressao.lineWidth = 2f
            dataSetPressao.circleRadius = 4f
            dataSetPressao.setDrawCircleHole(false)
            dataSetPressao.valueTextSize = 10f

            // Preenche a área abaixo da linha (opcional)
            dataSetVelocidade.setDrawFilled(true)
            dataSetVelocidade.fillColor = Color.BLUE
            dataSetVelocidade.fillAlpha = 50

            dataSetRPM.setDrawFilled(true)
            dataSetRPM.fillColor = Color.GREEN
            dataSetRPM.fillAlpha = 50

            dataSetTemperatura.setDrawFilled(true)
            dataSetTemperatura.fillColor = Color.MAGENTA
            dataSetTemperatura.fillAlpha = 50

            dataSetPressao.setDrawFilled(true)
            dataSetPressao.fillColor = Color.RED
            dataSetPressao.fillAlpha = 50

            // Cria o objeto de dados
            val lineData = LineData(dataSetVelocidade, dataSetRPM, dataSetTemperatura, dataSetPressao)
            lineChart.data = lineData

            // Atualiza o gráfico
            lineChart.invalidate()
        }
    }

    private fun dpToPix(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * Calcula a altura da barra em pixels baseado no valor atual e valor máximo
     */
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

                    // Atualiza o gráfico de linhas
                    atualizarGrafico(dados)

                    // Textos corretos
                    textVelocidade.text = "Velocidade: ${dados?.velocidade} km/h"
                    valBarraVelocidade.text = "${dados?.velocidade} km/h"

                    textTemperatura.text = "Temperatura: ${dados?.temperatura} °C"
                    valBarraTemperatura.text = "${dados?.temperatura} °C"

                    textPressao.text = "Pressão: ${dados?.pressao} hPa"
                    valBarraPressao.text = "${dados?.pressao} hPa"

                    textRPM.text = "RPM: ${dados?.rpm}"
                    valBarraRPM.text = "${dados?.rpm} RPM"

                    dados?.let {
                        val velocidade = it.velocidade.toInt()
                        val rpm = it.rpm.toInt()
                        val temperatura = it.temperatura.toInt()
                        val pressao = it.pressao.toInt()

                        val alturaVelocidade = calcularAlturaBarra(velocidade, VEL_MAX)
                        val alturaRPM = calcularAlturaBarra(rpm, RPM_MAX)
                        val alturaTemperatura = calcularAlturaBarra(temperatura, TEMP_MAX)
                        val alturaPressao = calcularAlturaBarra(pressao, PRESS_MAX)

                        val paramsVelo = barraVelocidade.layoutParams
                        paramsVelo.height = alturaVelocidade
                        barraVelocidade.layoutParams = paramsVelo

                        val paramsRPM = barraRPM.layoutParams
                        paramsRPM.height = alturaRPM
                        barraRPM.layoutParams = paramsRPM

                        val paramsTemp = barraTemperatura.layoutParams
                        paramsTemp.height = alturaTemperatura
                        barraTemperatura.layoutParams = paramsTemp

                        val paramsPress = barraPressao.layoutParams
                        paramsPress.height = alturaPressao
                        barraPressao.layoutParams = paramsPress

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

                val paramsVelo = barraVelocidade.layoutParams
                paramsVelo.height = alturaMinima
                barraVelocidade.layoutParams = paramsVelo

                val paramsRPM = barraRPM.layoutParams
                paramsRPM.height = alturaMinima
                barraRPM.layoutParams = paramsRPM

                val paramsTemp = barraTemperatura.layoutParams
                paramsTemp.height = alturaMinima
                barraTemperatura.layoutParams = paramsTemp

                val paramsPress = barraPressao.layoutParams
                paramsPress.height = alturaMinima
                barraPressao.layoutParams = paramsPress

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