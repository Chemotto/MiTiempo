package com.chema.mitiempo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.chema.mitiempo.data.remote.APIService
import com.chema.mitiempo.data.remote.dto.Prediccion
import com.chema.mitiempo.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val TAG = "MiTiempo"
    private val miApiKey = BuildConfig.AEMET_API_KEY
    private val urlBaseAemet = "https://opendata.aemet.es/opendata/api/"

    // 2. Declarar una única variable para el binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 3. Inflar el layout usando View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (miApiKey.isEmpty() || miApiKey == "PON_TU_API_KEY_AQUÍ") {
            Log.w(TAG, "La clave de AEMET no está configurada o es la de ejemplo.")
            showError("API Key no configurada.")
        } else {
            Log.d(TAG, "API Key configurada correctamente")
            getDatosAemet("28065") // Getafe
        }
    }

    private fun getDatosAemet(codigoMunicipio: String) {
        showLoading()
        lifecycleScope.launch {
            try {
                // Paso 1: Obtener la URL de los datos
                val retrofit = getRetrofit(urlBaseAemet)
                val api = retrofit.create(APIService::class.java)
                
                Log.d(TAG, "Solicitando URL para municipio: $codigoMunicipio")
                val responseUrl = api.getUrlAemet(codigoMunicipio, miApiKey)
                
                if (responseUrl.isSuccessful && responseUrl.body() != null) {
                    val urlDatos = responseUrl.body()!!.datos
                    Log.d(TAG, "URL de datos obtenida: $urlDatos")
                    
                    // Paso 2: Obtener la predicción usando la URL dinámica
                    val responsePrediccion = api.getPrediccion(urlDatos)
                    
                    if (responsePrediccion.isSuccessful && responsePrediccion.body() != null) {
                        val listaPredicciones = responsePrediccion.body()!!
                        if (listaPredicciones.isNotEmpty()) {
                            val prediccion = listaPredicciones[0].prediccion
                            showWeatherData(prediccion)
                        } else {
                            showError("La lista de predicciones está vacía")
                        }
                    } else {
                        Log.e(TAG, "Error en llamada 2: ${responsePrediccion.code()}")
                        showError("Error al obtener la predicción")
                    }
                } else {
                    Log.e(TAG, "Error en llamada 1: ${responseUrl.code()}")
                    showError("Error al obtener la URL de datos")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción: ${e.message}")
                showError("Error de conexión: ${e.message}")
            }
        }
    }

    // 4. Usar 'binding' para acceder a las vistas
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.weatherDataContainer.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
    }

    private fun showWeatherData(prediccion: Prediccion) {
        // Aseguramos que hay días en la predicción
        if (prediccion.dia.isNotEmpty()) {
            val hoy = prediccion.dia.first() // Tomamos el primer día de la lista
            Log.d(TAG, "Temperatura máxima hoy: ${hoy.temperatura.maxima}")

            binding.maxTempTextView.text = "Máx: ${hoy.temperatura.maxima}°C"
            binding.minTempTextView.text = "Mín: ${hoy.temperatura.minima}°C"
            binding.progressBar.visibility = View.GONE
            binding.weatherDataContainer.visibility = View.VISIBLE
            binding.errorTextView.visibility = View.GONE
        } else {
            showError("No hay datos de predicción para hoy")
        }
    }

    private fun showError(message: String) {
        binding.errorTextView.text = message
        binding.progressBar.visibility = View.GONE
        binding.weatherDataContainer.visibility = View.GONE
        binding.errorTextView.visibility = View.VISIBLE
    }

    private fun getRetrofit(baseUrl: String): Retrofit {
        val logging = HttpLoggingInterceptor().apply { 
            level = HttpLoggingInterceptor.Level.BODY 
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
            
        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}