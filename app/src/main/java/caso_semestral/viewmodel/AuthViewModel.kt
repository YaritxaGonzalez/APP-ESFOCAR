package com.example.caso_semestral.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.caso_semestral.data.AuthRepository
import com.example.caso_semestral.data.SesionManager
import com.example.caso_semestral.data.local.AppDatabase
import com.example.caso_semestral.validarLogin
import com.example.caso_semestral.validarRegistro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estado de la sesión: evita mostrar el login un instante mientras se lee DataStore
sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class ConSesion(val usuarioId: Int) : EstadoSesion
}

data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(AppDatabase.obtener(app).usuarioDao())
    private val sesion = SesionManager(app)

    val estadoSesion: StateFlow<EstadoSesion> = sesion.usuarioId
        .map { id -> if (id == null) EstadoSesion.SinSesion else EstadoSesion.ConSesion(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    // alias del usuario activo (para mostrarlo en el menú o el perfil)
    val alias: StateFlow<String> = sesion.usuarioId
        .flatMapLatest { id -> if (id == null) flowOf("") else flowOf(repo.obtenerAlias(id) ?: "") }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun iniciarSesion(alias: String, password: String) {
        if (!validarLogin(alias, password)) return
        viewModelScope.launch {
            _ui.value = AuthUiState(cargando = true)
            val id = repo.iniciarSesion(alias, password)
            if (id == null) {
                _ui.value = AuthUiState(error = "Usuario o contraseña incorrectos")
            } else {
                sesion.iniciar(id)
                _ui.value = AuthUiState()
            }
        }
    }

    fun registrar(alias: String, password: String, repetir: String) {
        if (!validarRegistro(alias, password, repetir)) {
            _ui.value = AuthUiState(error = "Revisa los datos ingresados")
            return
        }
        viewModelScope.launch {
            _ui.value = AuthUiState(cargando = true)
            val id = repo.registrar(alias, password)
            if (id == null) {
                _ui.value = AuthUiState(error = "Ese alias ya está en uso")
            } else {
                sesion.iniciar(id)   // el registro inicia sesión automáticamente
                _ui.value = AuthUiState()
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch { sesion.cerrar() }
    }

    fun limpiarError() {
        _ui.value = _ui.value.copy(error = null)
    }
}