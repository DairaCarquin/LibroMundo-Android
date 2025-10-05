package com.cibertec.libromundo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibertec.libromundo.model.CategoriaLibro
import com.cibertec.libromundo.model.Libro
import com.cibertec.libromundo.ui.theme.LIbroMundoTheme
import com.cibertec.libromundo.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LIbroMundoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel = vm)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    val titulo by viewModel.titulo
    val precio by viewModel.precio
    val cantidad by viewModel.cantidad
    val categoria by viewModel.categoriaSeleccionada
    val libros = viewModel.libros

    var showValidationDialog by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var showConfirmClear by remember { mutableStateOf(false) }
    var libroToDelete by remember { mutableStateOf<Libro?>(null) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var showResumen by remember { mutableStateOf(false) }

    var snackColor by remember { mutableStateOf(colorScheme.surfaceVariant) }

    val snackEvent by viewModel.snackbarEvent.collectAsState()
    LaunchedEffect(snackEvent) {
        snackEvent?.let { (msg, colorKey) ->
            snackColor = when (colorKey) {
                MainViewModel.SnackbarColor.GRIS -> colorScheme.onSurface.copy(alpha = 0.12f)
                MainViewModel.SnackbarColor.VERDE -> colorScheme.primary.copy(alpha = 0.12f)
                MainViewModel.SnackbarColor.AZUL -> colorScheme.secondary.copy(alpha = 0.12f)
                MainViewModel.SnackbarColor.DORADO -> colorScheme.tertiary.copy(alpha = 0.12f)
                MainViewModel.SnackbarColor.EXITO -> colorScheme.primary
                MainViewModel.SnackbarColor.ADVERTENCIA -> colorScheme.error
                MainViewModel.SnackbarColor.INFO -> colorScheme.inversePrimary
            }
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearSnackbarEvent()
            }
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        "LibroMundo - Carrito de\nCompras",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFFFDFDFE)
            )
        )
    }, snackbarHost = {
        SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = snackColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    }, floatingActionButton = {
        if (viewModel.resumenHabilitado.value) {
            FloatingActionButton(
                onClick = { showResumen = true },
                containerColor = Color(0xFF1193D4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.List, contentDescription = "Mostrar Resumen")
            }
        }
    }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF6F7F8))
                    .padding(16.dp)
            ) {
                Text(
                    "Agregar Libro",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(16.dp))


                OutlinedTextField(
                    value = titulo,
                    onValueChange = { viewModel.titulo.value = it },
                    label = { Text("Título del Libro", color = Color(0xFF828282)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 15.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = precio,
                    onValueChange = { viewModel.precio.value = it },
                    label = { Text("Precio Unitario", color = Color(0xFF828282)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 15.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { viewModel.cantidad.value = it },
                    label = { Text("Cantidad", color = Color(0xFF828282)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 15.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedCategoria, onExpandedChange = {}) {
                    OutlinedTextField(
                        value = categoria?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            Text(
                                "Categoría", fontSize = 16.sp, color = Color(0xFF828282)
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                        trailingIcon = {
                            IconButton(onClick = { expandedCategoria = !expandedCategoria }) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategoria,
                        onDismissRequest = { expandedCategoria = false }) {
                        CategoriaLibro.valuesDisplay().forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.displayName) }, onClick = {
                                viewModel.categoriaSeleccionada.value = cat
                                expandedCategoria = false
                            })
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        when {
                            viewModel.titulo.value.trim().isEmpty() -> showValidationDialog =
                                "Ingrese el título del libro" to true

                            viewModel.precio.value.toDoubleOrNull() == null || (viewModel.precio.value.toDoubleOrNull()
                                ?: 0.0) <= 0.0 -> showValidationDialog =
                                "Precio debe ser mayor a 0" to true

                            viewModel.cantidad.value.toIntOrNull() == null || (viewModel.cantidad.value.toIntOrNull()
                                ?: 0) <= 0 -> showValidationDialog =
                                "Cantidad debe ser mayor a 0" to true

                            else -> {
                                val ok = viewModel.agregarLibro()
                                if (!ok) showValidationDialog = "Error al agregar libro" to true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC7E2F0), contentColor = Color(0xFF1094D4)
                    )
                ) {
                    Text("Agregar Libro")
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Libros en el carrito",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items = libros, key = { it.id }) { libro ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        libro.titulo,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                    IconButton(onClick = { libroToDelete = libro }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar libro"
                                        )
                                    }
                                }
                                Text(
                                    "Precio: S/. ${"%.2f".format(libro.precioUnitario)},   Cantidad: ${libro.cantidad}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF828282), fontSize = 14.sp
                                    )
                                )
                                Text(
                                    "Subtotal: S/. ${"%.2f".format(libro.precioUnitario * libro.cantidad)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF828282),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

            }
            Surface(
                color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", color = Color.Black)
                        Text(
                            "S/. ${"%.2f".format(viewModel.subtotalTotal.value)}",
                            color = Color(0xFF828282),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Descuento (${viewModel.descuentoPorcentaje.value}%):",
                            color = Color(0xFF828282),
                        )
                        Text(
                            "S/. ${"%.2f".format(viewModel.descuentoMonto.value)}",
                            color = Color.Black
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                        Text(
                            "S/. ${"%.2f".format(viewModel.totalPagar.value)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showConfirmClear = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFCEE8F5), contentColor = Color(0xFF1193D4)
                            )
                        ) {
                            Text("Limpiar")
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                if (viewModel.libros.isEmpty()) {
                                    showValidationDialog =
                                        "Debe haber al menos 1 libro en el carrito" to true
                                } else {
                                    viewModel.calcularTotal()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1193D4), contentColor = Color.White
                            )
                        ) {
                            Text("Calcular")
                        }
                    }
                }
            }
        }

        showValidationDialog?.let { (msg, _) ->
            AlertDialog(
                onDismissRequest = { showValidationDialog = null },
                text = { Text(msg, color = Color.Black) },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showValidationDialog = null },
                            modifier = Modifier
                                .height(50.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1193D4), contentColor = Color.White
                            )
                        ) {
                            Text("Aceptar")
                        }
                    }
                })
        }

        if (showConfirmClear) {
            AlertDialog(
                onDismissRequest = { showConfirmClear = false },
                title = { Text("Confirmación", fontWeight = FontWeight.Bold, color = Color.Black) },
                text = { Text("¿Está seguro de limpiar el carrito?", color = Color.Black) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.limpiarCarrito()
                            showConfirmClear = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1193D4), contentColor = Color.White
                        )
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showConfirmClear = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCEE8F5), contentColor = Color(0xFF1193D4)
                        )
                    ) {
                        Text("No")
                    }
                })
        }

        if (showResumen) {
            AlertDialog(onDismissRequest = { showResumen = false }, title = {
                Text(
                    "Resumen de Compra", fontWeight = FontWeight.Bold, color = Color.Black
                )
            }, text = {
                Column {
                    Text(
                        "Subtotal: S/. ${"%.2f".format(viewModel.subtotalTotal.value)}",
                        color = Color.Black
                    )
                    Text(
                        "Descuento: ${viewModel.descuentoPorcentaje.value}%", color = Color.Black
                    )
                    Text(
                        "Ahorro: S/. ${"%.2f".format(viewModel.descuentoMonto.value)}",
                        color = Color.Black
                    )
                    Text(
                        "Total a pagar: S/. ${"%.2f".format(viewModel.totalPagar.value)}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Cantidad de libros: ${viewModel.cantidadTotalLibros.value}",
                        color = Color.Black
                    )
                }
            }, confirmButton = {
                Button(
                    onClick = { showResumen = false },
                    modifier = Modifier
                        .height(50.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1193D4), contentColor = Color.White
                    )
                ) {
                    Text("Cerrar")
                }
            })
        }

        libroToDelete?.let { libro ->
            AlertDialog(
                onDismissRequest = { libroToDelete = null },
                title = {
                    Text(
                        "Confirmación",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                text = {
                    Text(
                        "¿Eliminar '${libro.titulo}' del carrito?",
                        color = Color.Black
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.eliminarLibro(libro)
                            libroToDelete = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF1193D4),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { libroToDelete = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFFCEE8F5),
                            contentColor = Color(0xFF1193D4)
                        )
                    ) {
                        Text("No")
                    }
                })
        }

    }
}
