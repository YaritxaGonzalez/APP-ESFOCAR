package esfocar_api.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class ConfigSeguridad {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}