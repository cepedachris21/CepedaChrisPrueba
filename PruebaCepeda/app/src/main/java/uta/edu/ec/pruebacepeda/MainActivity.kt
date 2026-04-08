package uta.edu.ec.pruebacepeda

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uta.edu.ec.pruebacepeda.ui.theme.PruebaCepedaTheme

// 1. Modelo de Datos en Español
data class Contacto(
    val id: Long = System.currentTimeMillis(),
    val nombre: String,
    val correo: String,
    val telefono: String,
    val codigoPostal: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PruebaCepedaTheme {
                AppLibretaContactos()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLibretaContactos() {
    // --- ESTADOS DEL FORMULARIO (En Español) ---
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    
    // --- GESTIÓN DE LISTA Y SNACKBAR ---
    val listaContactos = remember { mutableStateListOf<Contacto>() }
    val estadoSnackbar = remember { SnackbarHostState() }
    val alcanceCorrutina = rememberCoroutineScope()
    
    // --- RESPALDO PARA "DESHACER" ---
    var contactoEliminadoRecientemente by remember { mutableStateOf<Contacto?>(null) }
    var indiceContactoEliminado by remember { mutableStateOf(-1) }

    // --- LÓGICA DE VALIDACIÓN (FASE 1.1) ---
    val esNombreValido = nombre.trim().isNotEmpty()

    val esCorreoValido = correo.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(correo).matches()

    val esTelefonoValido = telefono.length == 10

    val esCodigoPostalValido = codigoPostal.length == 5
    

    val puedeGuardar = esNombreValido && esCorreoValido && esTelefonoValido && esCodigoPostalValido

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = estadoSnackbar) },
        topBar = {
            // CAMBIO EN VIVO: Cambiar el título de la barra superior aquí
            CenterAlignedTopAppBar(title = { Text("Libreta de Contactos - Prueba") })
        }
    ) { rellenoInterno ->
        Column(
            modifier = Modifier
                .padding(rellenoInterno)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(text = "Nuevo Contacto", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            // --- CAMPOS CON VALIDACIÓN Y SOPORTE VISUAL ---
            
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre (Obligatorio)") },
                modifier = Modifier.fillMaxWidth(),
                isError = nombre.isNotEmpty() && !esNombreValido,
                supportingText = {
                    if (nombre.isNotEmpty() && !esNombreValido) {
                        Text("Requerido", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )
            
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = correo.isNotEmpty() && !esCorreoValido,
                supportingText = {
                    if (correo.isNotEmpty() && !esCorreoValido) {

                        Text("Correo inválido", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )
            
            OutlinedTextField(
                value = telefono,
                //  Si cambiaste la longitud a 9 arriba, cámbiala también aquí (it.length <= 9)
                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) telefono = it },
                label = { Text("Teléfono (10 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = telefono.isNotEmpty() && !esTelefonoValido,
                supportingText = {
                    if (telefono.isNotEmpty() && !esTelefonoValido) {
                        // Mostrar cuántos dígitos lleva el usuario
                        Text("Faltan dígitos (${telefono.length}/10)", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )
            
            OutlinedTextField(
                value = codigoPostal,
                onValueChange = { if (it.length <= 5 && it.all { c -> c.isDigit() }) codigoPostal = it },
                label = { Text("ZIP / Código Postal (5 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = codigoPostal.isNotEmpty() && !esCodigoPostalValido,
                supportingText = {
                    if (codigoPostal.isNotEmpty() && !esCodigoPostalValido) {
                        Text("Deben ser 5 dígitos", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (puedeGuardar) {
                        listaContactos.add(Contacto(nombre = nombre, correo = correo, telefono = telefono, codigoPostal = codigoPostal))
                        nombre = ""; correo = ""; telefono = ""; codigoPostal = "" // Limpiar campos
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = puedeGuardar
            ) {
                Text("Guardar Contacto")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- LISTA DE CONTACTOS ---
            Text(text = "Lista de Contactos", style = MaterialTheme.typography.titleMedium)
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(listaContactos) { contacto ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = contacto.nombre, style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Telf: ${contacto.telefono} | ZIP: ${contacto.codigoPostal}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = {
                                // --- LÓGICA DE ELIMINACIÓN (FASE 1.2) ---
                                val indice = listaContactos.indexOf(contacto)
                                contactoEliminadoRecientemente = contacto
                                indiceContactoEliminado = indice
                                listaContactos.remove(contacto)

                                alcanceCorrutina.launch {
                                    val resultado = estadoSnackbar.showSnackbar(
                                        message = "Contacto eliminado",
                                        actionLabel = "DESHACER",
                                        //  Duración (Short = 2s, Long = 4s)
                                        duration = SnackbarDuration.Short
                                    )
                                    // Si presiona DESHACER, se restaura en su posición original
                                    if (resultado == SnackbarResult.ActionPerformed) {
                                        listaContactos.add(indiceContactoEliminado, contactoEliminadoRecientemente!!)
                                    }
                                }
                            }) {
                                // Cambiar color del icono de eliminar
                                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}
