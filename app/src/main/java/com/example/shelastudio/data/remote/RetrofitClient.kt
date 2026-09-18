package com.example.shelastudio.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client configured for the ShelaStudio REST API.
 * Base URL points to the Railway deployment.
 */
object RetrofitClient {

    private const val TAG = "RetrofitClient"

    /** Railway deployment URL — MUST end with a slash. */
    private const val BASE_URL = "https://shelastudio-api-production-1973.up.railway.app/"

    val api: ShelaStudioApi by lazy {
        Log.d(TAG, "Building Retrofit client for $BASE_URL")

        val logging = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ShelaStudioApi::class.java)
    }
}