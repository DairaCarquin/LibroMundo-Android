package com.cibertec.libromundo.model

data class Libro(
    val id: Long,
    val titulo: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val categoria: CategoriaLibro
) {
    fun calcularSubtotal(): Double = precioUnitario * cantidad
}