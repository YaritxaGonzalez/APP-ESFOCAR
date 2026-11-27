package com.example.caso_semestral.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Lo que se envía y lo que responde el backend (coincide con AuthController)
data class LoginRequest(val alias: String, val password: String)

data class UsuarioResponse(
    val id: Long,
    val alias: String,
    val rol: String,
    val seccionId: Long?
)

interface EsfocarApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioResponse>
}