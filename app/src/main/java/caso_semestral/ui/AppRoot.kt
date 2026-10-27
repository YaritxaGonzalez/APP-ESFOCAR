package com.example.caso_semestral.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.caso_semestral.VerdeInstitucional
import com.example.caso_semestral.viewmodel.AuthViewModel
import com.example.caso_semestral.viewmodel.DocumentosViewModel
import com.example.caso_semestral.viewmodel.EstadoSesion
import kotlinx.coroutines.launch

private data class Pestana(val ruta: String, val titulo: String, val icono: ImageVector)

private val pestanas = listOf(
    Pestana(Rutas.INICIO, "Inicio", Icons.Default.Home),
    Pestana(Rutas.DOCUMENTOS, "Documentos", Icons.Default.Folder),
    Pestana(Rutas.PERFIL, "Perfil", Icons.Default.Person)
)

private fun NavHostController.irAPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(Rutas.INICIO) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppRoot(
    authVm: AuthViewModel = viewModel(),
    docVm: DocumentosViewModel = viewModel()
) {
    val estado by authVm.estadoSesion.collectAsState()

    // mientras se lee la sesión guardada no se muestra ninguna pantalla (evita el parpadeo del login)
    if (estado is EstadoSesion.Cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        ContenidoApp(estado, authVm, docVm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoApp(estado: EstadoSesion, authVm: AuthViewModel, docVm: DocumentosViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val ruta = backStack?.destination?.route

    val seleccionado by docVm.seleccionado.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val alias by authVm.alias.collectAsState()
    val documentos by docVm.documentos.collectAsState()   // NUEVO: lista para la pantalla de Inicio
    val mensaje by docVm.mensaje.collectAsState()
    var confirmarCierre by remember { mutableStateOf(false) }

    val usuarioId = (estado as? EstadoSesion.ConSesion)?.usuarioId
    // se calcula una sola vez: cambiarlo después reiniciaría el NavHost
    val destinoInicial = remember { if (usuarioId != null) Rutas.INICIO else Rutas.LOGIN }

    // reacciona a iniciar o cerrar sesión
    LaunchedEffect(usuarioId) {
        docVm.establecerUsuario(usuarioId)
        val actual = nav.currentDestination?.route ?: return@LaunchedEffect
        if (usuarioId != null && actual in Rutas.publicas) {
            nav.navigate(Rutas.INICIO) { popUpTo(nav.graph.id) { inclusive = true } }
        } else if (usuarioId == null && actual !in Rutas.publicas) {
            nav.navigate(Rutas.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            docVm.mensajeMostrado()
        }
    }

    val conBarraSuperior = ruta != null && ruta !in Rutas.publicas
    val conBarraInferior = ruta in Rutas.pestanas
    val titulo = pestanas.find { it.ruta == ruta }?.titulo
        ?: seleccionado?.nombre?.substringAfter("_")
        ?: "Documento"
    ModalNavigationDrawer(
        drawerState = drawer,
        gesturesEnabled = conBarraSuperior,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "ESFOCAR Estudio",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                if (alias.isNotEmpty()) {
                    Text(
                        "Nombre: $alias",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                pestanas.forEach { p ->
                    NavigationDrawerItem(
                        label = { Text(p.titulo) },
                        icon = { Icon(p.icono, contentDescription = null) },
                        selected = ruta == p.ruta,
                        onClick = {
                            scope.launch { drawer.close() }
                            nav.irAPestana(p.ruta)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawer.close() }
                        confirmarCierre = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                if (conBarraSuperior) {
                    TopAppBar(
                        title = { Text(titulo) },
                        navigationIcon = {
                            if (ruta == Rutas.VISOR) {
                                IconButton(onClick = { nav.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawer.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = VerdeInstitucional,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                if (conBarraInferior) {
                    NavigationBar {
                        pestanas.forEach { p ->
                            NavigationBarItem(
                                selected = ruta == p.ruta,
                                onClick = { nav.irAPestana(p.ruta) },
                                icon = { Icon(p.icono, contentDescription = null) },
                                label = { Text(p.titulo) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = destinoInicial,
                modifier = Modifier.padding(padding)
            ) {
                composable(Rutas.LOGIN) {
                    LoginScreen(authVm) { nav.navigate(Rutas.REGISTRO) }
                }
                composable(Rutas.REGISTRO) {
                    RegistroScreen(authVm) { nav.popBackStack() }
                }
                // CAMBIADO: el Inicio ahora recibe los documentos y dos acciones de navegación
                composable(Rutas.INICIO) {
                    InicioScreen(
                        alias = alias,
                        documentos = documentos,
                        onIrDocumentos = { nav.irAPestana(Rutas.DOCUMENTOS) },
                        onAbrir = { id -> nav.navigate(Rutas.visor(id)) }
                    )
                }
                composable(Rutas.DOCUMENTOS) {
                    DocumentosScreen(docVm) { id -> nav.navigate(Rutas.visor(id)) }
                }
                composable(
                    Rutas.VISOR,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entrada ->
                    VisorScreen(docVm, entrada.arguments?.getInt("id") ?: -1)
                }
                composable(Rutas.PERFIL) {
                    PerfilScreen(alias) { confirmarCierre = true }
                }
            }
        }
    }

    if (confirmarCierre) {
        AlertDialog(
            onDismissRequest = { confirmarCierre = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Quieres cerrar tu sesión? Tus documentos seguirán guardados en el teléfono.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarCierre = false
                    authVm.cerrarSesion()
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCierre = false }) { Text("Cancelar") }
            }
        )
    }
}