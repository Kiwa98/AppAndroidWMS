package com.example.wmsproducao.api

data class TransferenciaDTO(
    val produtoId: Long,
    val codigo: String,
    val quantidade: Int,
    val linha: String,
    val entreguePara: String,
    val data: String
)
