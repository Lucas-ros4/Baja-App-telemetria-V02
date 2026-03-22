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

        textPressao = findViewById(R.id.textRPM)
        valBarraRPM = findViewById(R.id.textBarraRPMval)

        // parte dos graficos-CORRIGIDO
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

    private fun fetchDados() {
        api.getDados().enqueue(object : Callback<Dados> {
            override fun onResponse(call: Call<Dados>, response: Response<Dados>) {
                if (response.isSuccessful) {
                    val dados = response.body()
                    textVelocidade.text = "Velocidade: ${dados?.velocidade} km/h"
                    valBarraVelocidade.text = "${dados?.velocidade}km/h"
                    textTemperatura.text = "Temperatura: ${dados?.temperatura} °C"
                    valBarraTemperatura.text = "${dados?.velocidade}°C"
                    textPressao.text = "Pressão: ${dados?.pressao} hPa"
                    valBarraPressao.text = "${dados?.velocidade}hPa"
                    textRPM.text = "Pressão: ${dados?.rpm} hPa"
                    valBarraRPM.text = "${dados?.rpm}hPa"

                    // CORRIGIDO: Atualização segura das alturas das barras
                    dados?.let {
                        // Converte os valores para Int (assumindo que são números)
                        val alturaVelocidade = it.velocidade.toInt()
                        val alturaRPM = it.rpm.toInt()
                        val alturaTemperatura = it.temperatura.toInt()
                        val alturaPressao = it.pressao.toInt()

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

                textRPM.text = "-"
                valBarraRPM.text = "-"

                textTemperatura.text = "-"
                valBarraTemperatura.text = "-"

                textPressao.text = "-"
                valBarraPressao.text = "-"

                // CORRIGIDO: Aplicação correta das alterações
                val paramsVelo = barraVelocidade.layoutParams
                paramsVelo.height = dpToPix(10)
                barraVelocidade.layoutParams = paramsVelo

                val paramsRPM = barraRPM.layoutParams
                paramsRPM.height = dpToPix(10)
                barraRPM.layoutParams = paramsRPM

                val paramsTemp = barraTemperatura.layoutParams
                paramsTemp.height = dpToPix(10)
                barraTemperatura.layoutParams = paramsTemp

                val paramsPress = barraPressao.layoutParams
                paramsPress.height = dpToPix(10)
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
        handler.removeCallbacks(updateRunnable) // limpa o loop
    }
}