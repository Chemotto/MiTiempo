package com.chema.mitiempo

import android.graphics.Color
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    /**
     * Orquesta el proceso de obtención de datos del tiempo desde la AEMET.
     * Utiliza `lifecycleScope` para lanzar una corrutina de forma segura.
     *
     * @param codigoPoblacion El código del municipio para el cual se solicita la predicción.
     */
    private fun getDatosAemet(codigoPoblacion: String) {
        showLoading()

        // `lifecycleScope` es la forma correcta y segura de lanzar corrutinas en una Activity.
        // Se cancela automáticamente si la pantalla se destruye, evitando errores y fugas de memoria.
        lifecycleScope.launch {
            try {
                // `withContext(Dispatchers.IO)` ejecuta la lógica de red en un hilo secundario
                // para no bloquear la interfaz de usuario. Al finalizar, devuelve el resultado.
                val prediccionFinal = withContext(Dispatchers.IO) {
                    Log.d(TAG, "Iniciando llamadas de red en hilo secundario...")

                    // --- PASO 1: Obtener la URL de los datos ---
                    val retrofitPaso1 = getRetrofit(urlBaseAemet)
                    val servicePaso1 = retrofitPaso1.create(APIService::class.java)
                    val responseUrl = servicePaso1.getUrlAemet(codigoPoblacion, miApiKey)

                    // Si la llamada falla o el estado no es 200, lanzamos un error que será
                    // capturado por el bloque 'catch'.
                    if (!responseUrl.isSuccessful || responseUrl.body()?.estado != 200) {
                        throw Exception("Error al obtener la URL de datos: ${responseUrl.body()?.descripcion ?: "Desconocido"}")
                    }

                    val urlDatosFinales = responseUrl.body()!!.datos
                    Log.d(TAG, "URL de datos obtenida: $urlDatosFinales")

                    // La API de AEMET requiere una pausa entre la primera y la segunda llamada.
                    delay(2000)

                    // --- PASO 2: Obtener la predicción del tiempo ---
                    val retrofitPaso2 = getRetrofit("https://opendata.aemet.es/")
                    val servicePaso2 = retrofitPaso2.create(APIService::class.java)
                    val responsePrediccion = servicePaso2.getPrediccion(urlDatosFinales)

                    if (!responsePrediccion.isSuccessful) {
                        throw Exception("Error al obtener la predicción final.")
                    }

                    // Se devuelve el primer elemento de la respuesta, que será el resultado de `withContext`.
                    // Este será el objeto `PrediccionMunicipioResponseItem` que esperamos.
                    responsePrediccion.body()!!.first()
                }

                // Al salir de `withContext`, el código continúa en el hilo principal (Main).
                // Aquí podemos actualizar la UI de forma segura con el resultado obtenido.
                Log.d(TAG, "Llamadas de red completadas. Actualizando UI...")
                // Usamos el operador safe call (?.) en caso de que alguna propiedad sea null en tiempo de ejecución
                // aunque ya lo controlamos en el DTO, es buena práctica al loguear.
                Log.d(TAG, "Temperatura máxima: ${prediccionFinal.prediccion.dia.firstOrNull()?.temperatura?.maxima}ªC")
                Log.d(TAG, "Temperatura mínima: ${prediccionFinal.prediccion.dia.firstOrNull()?.temperatura?.minima}ªC")

                showWeatherData(prediccionFinal.prediccion)

            } catch (e: Exception) {
                // El bloque `catch` maneja cualquier excepción lanzada en el `try`,
                // incluyendo errores de red o las excepciones que lanzamos manualmente.
                // Como este bloque se ejecuta en el hilo principal, podemos actualizar la UI.
                Log.e(TAG, "Excepción en getDatosAemet: ${e.message}", e)
                showError("Fallo al obtener los datos: ${e.message}")
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
            
            // Gestionamos los nulos: si no hay dato, mostramos "--"
            val maxTemp = hoy.temperatura.maxima?.toString() ?: "--"
            val minTemp = hoy.temperatura.minima?.toString() ?: "--"
            
            Log.d(TAG, "Temperatura máxima hoy: $maxTemp")

            binding.maxTempTextView.text = "$maxTemp°C" // Quitamos el texto "Máx:"
            binding.maxTempTextView.setTextColor(Color.RED)

            binding.minTempTextView.text = "$minTemp°C" // Quitamos el texto "Mín:"
            binding.minTempTextView.setTextColor(Color.BLUE)
            
            // Formatear y mostrar la fecha actual
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaActual = sdf.format(Date())
            binding.lastUpdateTextView.text = "Actualizado: $fechaActual"

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