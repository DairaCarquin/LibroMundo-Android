package com.cibertec.libromundo.model

enum class CategoriaLibro(val displayName: String) {
    FICCION("Ficción"),
    NO_FICCION("No Ficción"),
    CIENCIA("Ciencia"),
    INFANTIL("Infantil"),
    OTRO("Otro");

    companion object {
        fun valuesDisplay(): List<CategoriaLibro> = values().toList()
    }
}
