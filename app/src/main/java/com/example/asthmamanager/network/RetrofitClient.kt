package com.example.asthmamanager.network

import com.example.asthmamanager.MyApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Lazily initialize the authInterceptor to ensure context is available.
    private val authInterceptor by lazy {
        Interceptor { chain ->
            val token = SessionManager(MyApplication.appContext).fetchAuthToken()
            val requestBuilder = chain.request().newBuilder()

            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            chain.proceed(requestBuilder.build())
        }
    }

    // Lazily initialize httpClient to use the interceptor.
    private val httpClient by lazy {
        OkHttpClient.Builder()
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
