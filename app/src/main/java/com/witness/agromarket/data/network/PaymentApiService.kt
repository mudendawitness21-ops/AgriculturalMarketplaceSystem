package com.witness.agromarket.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class DepositRequest(
    val phone: String,
    val amount: Double,
    val provider: String,
    val reference: String
)

data class WithdrawRequest(
    val phone: String,
    val amount: Double,
    val provider: String,
    val accountName: String,
    val reference: String
)

data class PaymentApiResponse(
    val status: Boolean,
    val message: String,
    val reference: String? = null,
    val lencoReference: String? = null,
    val paymentStatus: String? = null,
    val amount: Double? = null
)

interface PaymentApiService {
    @POST("api/wallet/deposit")
    suspend fun initiateDeposit(@Body request: DepositRequest): PaymentApiResponse

    @GET("api/wallet/verify/{reference}")
    suspend fun verifyDeposit(@Path("reference") reference: String): PaymentApiResponse

    @POST("api/wallet/withdraw")
    suspend fun initiateWithdraw(@Body request: WithdrawRequest): PaymentApiResponse

    companion object {
        private const val BASE_URL = "http://10.15.15.245:3000/"

        fun create(): PaymentApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PaymentApiService::class.java)
        }
    }
}
