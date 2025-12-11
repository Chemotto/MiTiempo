# Paso 1: Configuración del Proyecto y Dependencias

En este paso inicial, configuramos el entorno de desarrollo y añadimos las librerías necesarias para que la aplicación funcione correctamente.

## 1.1 - Dependencias en `build.gradle.kts`

Añadimos las siguientes librerías en el archivo `build.gradle.kts` (módulo app) para manejar la red, el parseo de datos y el ciclo de vida:

*   **Retrofit**: Para realizar peticiones HTTP a la API de AEMET.
*   **Gson Converter**: Para convertir las respuestas JSON de la API en objetos Kotlin automáticamente.
*   **OkHttp Logging Interceptor**: Para ver en el Logcat las peticiones y respuestas de red (muy útil para depurar).
*   **Lifecycle Runtime KTX**: Para usar `lifecycleScope` y manejar corrutinas de forma segura y vinculada al ciclo de vida de la Activity.

## 1.2 - Permisos

Modificamos el `AndroidManifest.xml` para añadir el permiso de acceso a internet:
`<uses-permission android:name="android.permission.INTERNET" />`

## 1.3 - Configuración de la API Key (Seguridad)

Implementamos una forma segura de manejar la clave de la API:
1.  Creamos el archivo `local.properties` (que no se sube al control de versiones) para guardar la clave: `AEMET_API_KEY=tu_clave`.
2.  Configuramos `build.gradle.kts` para leer este archivo e inyectar la clave en la clase `BuildConfig` generada automáticamente.

---

# Paso 2: Creación de Modelos de Datos (DTOs)

Como la API de AEMET devuelve respuestas complejas en formato JSON, creamos clases de datos (Data Classes) en Kotlin para representarlas.

Utilizamos la herramienta "Kotlin data class File from JSON" para generar automáticamente estas clases:
*   `PrediccionMunicipioResponse`: Para la primera llamada (obtiene la URL de datos).
*   `PrediccionMunicipioResponseItem`: Para la segunda llamada (obtiene la predicción meteorológica real).

Se ajustaron los modelos para permitir valores nulos (`?`) en campos críticos como la temperatura, evitando cierres inesperados de la app.

---

# Paso 3: Configuración de Retrofit (APIService)

Definimos una interfaz `APIService` que describe los "endpoints" de la API:

1.  `getUrlAemet`: Realiza la primera petición usando el código del municipio y la API Key.
2.  `getPrediccion`: Realiza la segunda petición a la URL dinámica obtenida en el paso anterior.

---

# Paso 4: Implementación de la Lógica (MainActivity)

En la `MainActivity`, orquestamos todo el proceso:

1.  **ViewBinding**: Activamos ViewBinding para acceder a los elementos de la interfaz de forma segura y sencilla.
2.  **Corrutinas**: Usamos `lifecycleScope.launch` y `withContext(Dispatchers.IO)` para realizar las llamadas de red en segundo plano sin bloquear la pantalla.
3.  **Flujo de Llamadas**:
    *   Llamada 1 -> Obtener URL.
    *   Pausa (`delay`) -> Requisito de AEMET.
    *   Llamada 2 -> Obtener predicción.
4.  **Manejo de Errores**: Implementamos bloques `try-catch` para capturar fallos de red o de datos y mostrarlos al usuario.

---

# Paso 5: Diseño de la Interfaz (UI)

Creamos un layout en `activity_main.xml` que incluye:

*   Un título con fondo redondeado.
*   Textos grandes para mostrar la temperatura Máxima (Rojo) y Mínima (Azul).
*   Una barra de progreso (`ProgressBar`) que se muestra mientras cargan los datos.
*   Un texto para indicar la última hora de actualización.

---

¡Y listo! Con estos pasos construimos una app funcional y robusta.