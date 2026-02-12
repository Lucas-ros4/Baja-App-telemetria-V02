package com.example.bajateste01


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var textVelocidade: TextView
    private lateinit var textTemperatura: TextView
    private lateinit var textPressao: TextView

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
        textTemperatura = findViewById(R.id.textTemperatura)
        textPressao = findViewById(R.id.textPressao)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.158/") // IP do ESP32
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)

        // inicia a atualizacao periodica
        handler.post(updateRunnable)
    }

    private fun fetchDados() {
        api.getDados().enqueue(object : Callback<Dados> {
            override fun onResponse(call: Call<Dados>, response: Response<Dados>) {
                if (response.isSuccessful) {
                    val dados = response.body()
                    textVelocidade.text = "Velocidade: ${dados?.velocidade} km/h"
                    textTemperatura.text = "Temperatura: ${dados?.temperatura} °C"
                    textPressao.text = "Pressão: ${dados?.pressao} hPa"
                    //quando adicionar ou retirar os dados aqui modificar na classe primeiro e lembrar de atulizar os dados e a funcao callback
                }
            }

            override fun onFailure(call: Call<Dados>, t: Throwable) {
                textVelocidade.text = "Erro de conexão"
                textTemperatura.text = "-"
                textPressao.text = "-"
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable) // limpa o loop
    }
}