package com.example.wmsproducao.api

import retrofit2.Call
import retrofit2.http.*

interface WmsApi {

    @POST("https://wmsapi-1.onrender.com/estoque")
    fun salvarEstoque(@Body item: EstoqueDTO): Call<EstoqueDTO>

    @GET("https://wmsapi-1.onrender.com/estoque")
    fun listar(): Call<List<EstoqueDTO>>

    @GET("https://wmsapi-1.onrender.com/estoque/{codigo}")
    fun buscar(@Path("codigo") codigo: String): Call<EstoqueDTO>

    @PUT("https://wmsapi-1.onrender.com/estoque/{codigo}")
    fun atualizar(@Path("codigo") id: Long, @Body item: EstoqueDTO): Call<EstoqueDTO>

    @DELETE("estoque/{codigo}")
    fun remover(@Path("id") id: Long): Call<Void>

    // ===== TRANSFERÊNCIAS =====
    @POST("transferencias")
    fun salvarTransferencia(
        @Body transferencia: TransferenciaDTO
    ): Call<Void>

    @GET("transferencias")
    fun listarTransferencias(): Call<List<TransferenciaDTO>>
}
