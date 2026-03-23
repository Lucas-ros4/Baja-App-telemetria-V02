package com.example.bajateste01

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
import kotlin.math.min

// Constantes de máximo para cada sensor
const val VEL_MAX = 40      // velocidade máxima em km/h
const val RPM_MAX = 4000    // RPM máximo
const val TEMP_MAX = 130    // temperatura máxima em °C
const val PRESS_MAX = 100   // pressão máxima em hPa

// Altura máxima da barra em pixels (ajuste conforme necessário)
const val BARRA_ALTURA_MAX_PX = 200

class MainActivity : AppCompatActivity() {

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

    private val updateRunnable = object : Runnable {
        override fun run() {
            fetchDados()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    private fun dpToPix(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * Calcula a altura da barra em pixels baseado no valor atual e valor máximo
     * @param valorAtual Valor atual do sensor
     * @param valorMaximo Valor máximo esperado para o sensor
     * @return Altura em pixels (entre 10px e BARRA_ALTURA_MAX_PX)
     */
    private fun calcularAlturaBarra(valorAtual: Int, valorMaximo: Int): Int {
        // Calcula a porcentagem (garantindo que não ultrapasse 100%)
        val porcentagem = (valorAtual.toFloat() / valorMaximo.toFloat() * 100).coerceIn(0f, 100f)

        // Converte porcentagem para pixels (mínimo 10px para ainda ser visível)
        val alturaEmPixels = (BARRA_ALTURA_MAX_PX * porcentagem / 100).toInt()

        // Garante altura mínima de 10px
        return alturaEmPixels.coerceAtLeast(10)
    }

    private fun fetchDados() {
        api.getDados().enqueue(object : Callback<Dados> {
            override fun onResponse(call: Call<Dados>, response: Response<Dados>) {
                if (response.isSuccessful) {
                    val dados = response.body()

                    // CORRIGIDO: Textos corretos
                    textVelocidade.text = "Velocidade: ${dados?.velocidade} km/h"
                    valBarraVelocidade.text = "${dados?.velocidade} km/h"

                    textTemperatura.text = "Temperatura: ${dados?.temperatura} °C"
                    valBarraTemperatura.text = "${dados?.temperatura} °C"

                    textPressao.text = "Pressão: ${dados?.pressao} hPa"
                    valBarraPressao.text = "${dados?.pressao} hPa"

                    textRPM.text = "RPM: ${dados?.rpm}"
                    valBarraRPM.text = "${dados?.rpm} RPM"

                    dados?.let {
                        // Converte os valores para Int
                        val velocidade = it.velocidade.toInt()
                        val rpm = it.rpm.toInt()
                        val temperatura = it.temperatura.toInt()
                        val pressao = it.pressao.toInt()

                        // Calcula as alturas em porcentagem convertida para pixels
                        val alturaVelocidade = calcularAlturaBarra(velocidade, VEL_MAX)
                        val alturaRPM = calcularAlturaBarra(rpm, RPM_MAX)
                        val alturaTemperatura = calcularAlturaBarra(temperatura, TEMP_MAX)
                        val alturaPressao = calcularAlturaBarra(pressao, PRESS_MAX)

                        // Atualiza a altura da barra de velocidade
                        val paramsVelo = barraVelocidade.layoutParams
                        paramsVelo.height = alturaVelocidade
                        barraVelocidade.layoutParams = paramsVelo

                        // Atualiza a altura da barra de RPM
                        val paramsRPM = barraRPM.layoutParams
                        paramsRPM.height = alturaRPM
                        barraRPM.layoutParams = paramsRPM

                        // Atualiza a altura da barra de temperatura
                        val paramsTemp = barraTemperatura.layoutParams
                        paramsTemp.height = alturaTemperatura
                        barraTemperatura.layoutParams = paramsTemp

                        // Atualiza a altura da barra de pressão
                        val paramsPress = barraPressao.layoutParams
                        paramsPress.height = alturaPressao
                        barraPressao.layoutParams = paramsPress

                        // Força o redesenho das views
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

                // Define altura mínima (10px) em caso de erro
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

                // Força o redesenho
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