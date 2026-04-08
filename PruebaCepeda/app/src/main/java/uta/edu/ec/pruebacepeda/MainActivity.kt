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

// 1. Modelo de Datos
data class Contact(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val email: String,
    val phone: String,
    val zip: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PruebaCepedaTheme {
                AddressBookApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressBookApp() {
    // Estados para el formulario
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    
    // Lista de contactos y gestión de Snackbar
    val contactList = remember { mutableStateListOf<Contact>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Recuperación de contactos eliminados
    var recentlyDeletedContact by remember { mutableStateOf<Contact?>(null) }
    var deletedContactIndex by remember { mutableStateOf(-1) }

    // LÓGICA DE VALIDACIÓN (Fase 1.1)
    val isNameValid = name.trim().isNotEmpty()
    val isEmailValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPhoneValid = phone.length == 10
    val isZipValid = zip.length == 5
    
    // El botón se habilita solo si TODO es válido
    val canSave = isNameValid && isEmailValid && isPhoneValid && isZipValid

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Address Book App") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(text = "Nuevo Contacto", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            // --- CAMPOS DE TEXTO CON VALIDACIÓN VISUAL ---
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre (Obligatorio)") },
                modifier = Modifier.fillMaxWidth(),
                isError = name.isNotEmpty() && !isNameValid,
                singleLine = true
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = email.isNotEmpty() && !isEmailValid,
                singleLine = true
            )
            
            OutlinedTextField(
                value = phone,
                // Solo permite números y máximo 10 caracteres
                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                label = { Text("Teléfono (10 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = phone.isNotEmpty() && !isPhoneValid,
                singleLine = true
            )
            
            OutlinedTextField(
                value = zip,
                // Solo permite números y máximo 5 caracteres
                onValueChange = { if (it.length <= 5 && it.all { c -> c.isDigit() }) zip = it },
                label = { Text("ZIP (5 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = zip.isNotEmpty() && !isZipValid,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (canSave) {
                        contactList.add(Contact(name = name, email = email, phone = phone, zip = zip))
                        // Limpiar campos después de guardar
                        name = ""; email = ""; phone = ""; zip = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave
            ) {
                Text("Guardar Contacto")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- LISTA DE CONTACTOS ---
            Text(text = "Lista de Contactos", style = MaterialTheme.typography.titleMedium)
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(contactList) { contact ->
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
                                Text(text = contact.name, style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Telf: ${contact.phone} | ZIP: ${contact.zip}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = {
                                // Lógica de Eliminación (Fase 1.2)
                                val index = contactList.indexOf(contact)
                                recentlyDeletedContact = contact
                                deletedContactIndex = index
                                contactList.remove(contact)

                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Contacto eliminado",
                                        actionLabel = "DESHACER",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        // Recuperar si presiona DESHACER
                                        contactList.add(deletedContactIndex, recentlyDeletedContact!!)
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}
