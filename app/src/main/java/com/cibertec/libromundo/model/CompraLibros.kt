package com.cibertec.libromundo.model

data class CompraLibros(
    val libros: List<Libro>,
    val subtotal: Double,
    val descuentoPorcentaje: Int,
    val descuentoMonto: Double,
    val total: Double,
    val cantidadTotalLibros: Int
)