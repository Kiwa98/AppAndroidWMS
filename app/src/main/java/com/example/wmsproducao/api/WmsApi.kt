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

    @PUT("estoque/{codigo}")
    fun atualizar(@Path("id") id: Long, @Body item: EstoqueDTO): Call<EstoqueDTO>

    @DELETE("estoque/{id}")
    fun remover(@Path("id") id: Long): Call<Void>
}
