# MiTiempo 🌤️

**MiTiempo** es una aplicación de ejemplo desarrollada para Android (Kotlin) que demuestra cómo interactuar con una API gubernamental real, en este caso, la de **AEMET OpenData**, para obtener la previsión meteorológica de un municipio (Getafe).

El proyecto está diseñado con fines educativos para enseñar buenas prácticas, arquitectura moderna y manejo robusto de redes.

---

## 📱 Características Principales

*   **Conexión API en Dos Pasos**: Implementa el flujo complejo de AEMET (Petición de URL -> Petición de Datos).
*   **Interfaz Robusta**: Manejo de estados de carga (loading), éxito (data) y error.
*   **Datos en Tiempo Real**: Muestra la temperatura máxima y mínima para el día actual.
*   **Información de Actualización**: Indica al usuario la fecha y hora exacta de la última consulta.
*   **Seguridad**: Uso de `local.properties` y `BuildConfig` para proteger la API Key.

## 🛠️ Tecnologías y Librerías

*   **[Kotlin](https://kotlinlang.org/)**: Lenguaje principal.
*   **[Retrofit 2](https://square.github.io/retrofit/)**: Cliente HTTP para las peticiones a la API.
*   **[OkHttp + Logging Interceptor](https://square.github.io/okhttp/)**: Para interceptar y depurar las llamadas de red.
*   **[Gson](https://github.com/google/gson)**: Para el parseo de JSON a objetos Kotlin.
*   **[Corrutinas (Kotlin Coroutines)](https://developer.android.com/kotlin/coroutines)**: Gestión de hilos y operaciones asíncronas.
*   **[Lifecycle KTX](https://developer.android.com/jetpack/androidx/releases/lifecycle)**: Integración de corrutinas con el ciclo de vida de la Activity (`lifecycleScope`).
*   **ViewBinding**: Vinculación segura de vistas sin `findViewById`.

## 🚀 Configuración e Instalación

1.  **Clona el repositorio**:
    ```bash
    git clone https://github.com/tu-usuario/MiTiempo.git
    ```
2.  **Consigue tu API Key**:
    Regístrate en [AEMET OpenData](https://opendata.aemet.es/) para obtener tu clave gratuita.

3.  **Configura la clave**:
    Crea un archivo llamado `local.properties` en la raíz del proyecto (si no existe) y añade la siguiente línea:
    ```properties
    AEMET_API_KEY=TU_CLAVE_API_AQUI_SIN_COMILLAS
    ```
    *(El sistema se encargará de inyectarla de forma segura en la compilación).*

4.  **Ejecuta**:
    Abre el proyecto en Android Studio y dale al botón de **Run**.

## 🏗️ Estructura del Proyecto

*   `data/remote`: Contiene la interfaz `APIService` y los modelos de datos (DTOs) generados a partir de las respuestas JSON.
*   `MainActivity.kt`: `Activity` principal que orquesta la lógica de negocio, las llamadas de red y la actualización de la UI.

## 📝 Notas de Desarrollo

Este proyecto resuelve el reto de APIs que requieren autenticación y llamadas encadenadas, gestionando errores comunes como:
*   Fallo en la red.
*   Respuestas nulas o incompletas (gestión de `null-safety` en temperaturas).
*   Bloqueo del hilo principal (uso de `Dispatchers.IO`).

---
*Desarrollado con ❤️ para aprender Android.*