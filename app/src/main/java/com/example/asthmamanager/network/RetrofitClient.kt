package com.example.asthmamanager.network

import android.util.Log // --- IMPORT ADDED ---
import com.example.asthmamanager.MyApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // --- IMPORT ADDED ---
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Lazily initialize the authInterceptor to ensure context is available.
    private val authInterceptor by lazy {
        Interceptor { chain ->
            val token = SessionManager(MyApplication.appContext).fetchAuthToken()

            // --- LOGGING ADDED ---
            // This log will show us if the token is null or valid
            Log.d("RetrofitClient", "Authorization Token: $token")
            // --- END OF LOGGING ---

            val requestBuilder = chain.request().newBuilder()

            token?.let {
                // Ensure the token isn't empty, just in case
                if (it.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                } else {
                    Log.w("RetrofitClient", "Token is blank, not adding header.")
                }
            }

            chain.proceed(requestBuilder.build())
        }
    }

    // --- LOGGING INTERCEPTOR ADDED ---
    // This will log the full request and response to Logcat
    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    // --- END OF LOGGING INTERCEPTOR ---


    // Lazily initialize httpClient to use the interceptor.
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Adds the new logger
            .addInterceptor(authInterceptor)
            .build()
    }

    // Build the Retrofit instance using the lazy-initialized client.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}