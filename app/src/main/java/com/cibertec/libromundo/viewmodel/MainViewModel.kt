package com.cibertec.libromundo.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cibertec.libromundo.model.CategoriaLibro
import com.cibertec.libromundo.model.Libro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.round

class MainViewModel : ViewModel() {

    private val idCounter = AtomicLong(1)

    val libros = mutableStateListOf<Libro>()

    val titulo = mutableStateOf("")
    val precio = mutableStateOf("")
    val cantidad = mutableStateOf("")
    val categoriaSeleccionada = mutableStateOf<CategoriaLibro?>(null)

    val subtotalTotal = mutableStateOf(0.0)
    val descuentoPorcentaje = mutableStateOf(0)
    val descuentoMonto = mutableStateOf(0.0)
    val totalPagar = mutableStateOf(0.0)
    val cantidadTotalLibros = mutableStateOf(0)

    val resumenHabilitado = mutableStateOf(false)

    private val _snackbarEvent = MutableStateFlow<Pair<String, SnackbarColor>?>(null)
    val snackbarEvent: StateFlow<Pair<String, SnackbarColor>?> = _snackbarEvent

    fun triggerSnackbar(message: String, color: SnackbarColor) {
        _snackbarEvent.value = message to color
    }

    fun clearSnackbarEvent() {
        _snackbarEvent.value = null
    }

    fun agregarLibro(): Boolean {
        if (titulo.value.trim().isEmpty()) return false
        val precioD = precio.value.toDoubleOrNull() ?: return false
        val cantidadI = cantidad.value.toIntOrNull() ?: return false
        if (precioD <= 0.0 || cantidadI <= 0) return false
        val categoria = categoriaSeleccionada.value ?: return false

        val libro = Libro(
            id = idCounter.getAndIncrement(),
            titulo = titulo.value.trim(),
            precioUnitario = precioD,
            cantidad = cantidadI,
            categoria = categoria
        )
        libros.add(libro)

        titulo.value = ""
        precio.value = ""
        cantidad.value = ""

        triggerSnackbar("Libro agregado al carrito", SnackbarColor.EXITO)
        return true
    }

    fun eliminarLibro(libro: Libro) {
        libros.remove(libro)
        recalcularAutomatico()
        triggerSnackbar("Libro eliminado del carrito", SnackbarColor.ADVERTENCIA)
    }

    fun limpiarCarrito() {
        libros.clear()
        titulo.value = ""
        precio.value = ""
        cantidad.value = ""
        subtotalTotal.value = 0.0
        descuentoPorcentaje.value = 0
        descuentoMonto.value = 0.0
        totalPagar.value = 0.0
        cantidadTotalLibros.value = 0
        resumenHabilitado.value = false
        triggerSnackbar("Carrito limpiado", SnackbarColor.INFO)
    }

    private fun redondear2Decimales(value: Double): Double {
        return (round(value * 100.0) / 100.0)
    }

    private fun calcularPorcentajeDescuento(totalLibros: Int): Int {
        return when {
            totalLibros in 1..2 -> 0
            totalLibros in 3..5 -> 10
            totalLibros in 6..10 -> 15
            totalLibros >= 11 -> 20
            else -> 0
        }
    }

    fun calcularTotal(): Boolean {
        if (libros.isEmpty()) return false

        val subtotal = libros.sumOf { it.calcularSubtotal() }
        val cantidadTotal = libros.sumOf { it.cantidad }
        val porcentaje = calcularPorcentajeDescuento(cantidadTotal)
        val descuento = subtotal * porcentaje / 100.0
        val total = subtotal - descuento

        subtotalTotal.value = redondear2Decimales(subtotal)
        descuentoPorcentaje.value = porcentaje
        descuentoMonto.value = redondear2Decimales(descuento)
        totalPagar.value = redondear2Decimales(total)
        cantidadTotalLibros.value = cantidadTotal
        resumenHabilitado.value = true

        when (porcentaje) {
            0 -> triggerSnackbar("No hay descuento aplicado", SnackbarColor.GRIS)
            10 -> triggerSnackbar(
                "¡Genial! Ahorraste S/. ${"%.2f".format(descuentoMonto.value)}", SnackbarColor.VERDE
            )

            15 -> triggerSnackbar(
                "¡Excelente! Ahorraste S/. ${"%.2f".format(descuentoMonto.value)}",
                SnackbarColor.AZUL
            )

            20 -> triggerSnackbar(
                "¡Increíble! Ahorraste S/. ${"%.2f".format(descuentoMonto.value)}",
                SnackbarColor.DORADO
            )
        }
        return true
    }

    private fun recalcularAutomatico() {
        if (libros.isEmpty()) {
            subtotalTotal.value = 0.0
            descuentoPorcentaje.value = 0
            descuentoMonto.value = 0.0
            totalPagar.value = 0.0
            cantidadTotalLibros.value = 0
            resumenHabilitado.value = false
            return
        }
        calcularTotal()
    }

    enum class SnackbarColor { GRIS, VERDE, AZUL, DORADO, EXITO, ADVERTENCIA, INFO }
}