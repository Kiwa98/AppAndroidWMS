package com.example.wmsproducao.api

data class EstoqueDTO(
    val id: Long? = null,
    val nome: String,
    val ean: String,
    val totvs: String,
    val quantidade: Int,
    val localizacao: String
)
