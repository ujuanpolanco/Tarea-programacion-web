package co.edu.usco.seguridadroles.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion de Spring Security del ejercicio.
 *
 * Las reglas son las mismas de la diapositiva. La diferencia es de sintaxis:
 * la diapositiva usa antMatchers() y and(), que son de Spring Security 5
 * (clase WebSecurityConfigurerAdapter). En Spring Boot 3 / Spring Security 6
 * esos metodos ya no existen: se usa requestMatchers() y el estilo con
 * lambdas, donde cada bloque (formLogin, logout, ...) va en su propio metodo
 * y por eso ya no hace falta .and().
 *
 * Diapositiva (Spring Security 5)          Este proyecto (Spring Security 6)
 * --------------------------------         ---------------------------------
 * http.authorizeRequests()                 http.authorizeHttpRequests(auth -> auth
 *   .antMatchers("/")...                       .requestMatchers("/")...
 *   .and().formLogin().permitAll()         .formLogin(form -> form...permitAll())
 *   .and().logout().permitAll()            .logout(logout -> logout.permitAll())
 *   .and().exceptionHandling()             .exceptionHandling(ex -> ex
 *        .accessDeniedPage("/403")              .accessDeniedPage("/403"))
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Las contrasenas en la tabla users estan cifradas con BCrypt.
     * Spring encuentra este bean y lo usa automaticamente junto con
     * UserDetailsServiceImpl para validar el login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Los estilos deben cargar tambien en la pagina de login
                .requestMatchers("/css/**").permitAll()

                // ---- Reglas de la diapositiva ----
                .requestMatchers("/").hasAnyAuthority("USER", "CREATOR", "EDITOR", "ADMIN")
                .requestMatchers("/new").hasAnyAuthority("ADMIN", "CREATOR")
                .requestMatchers("/edit/**").hasAnyAuthority("ADMIN", "EDITOR")
                .requestMatchers("/delete/**").hasAuthority("ADMIN")
                .anyRequest().authenticated())

            // Pagina de login personalizada (templates/login.html)
            .formLogin(form -> form
                .loginPage("/login")
                // Si el usuario existe pero enabled = 0, mostramos un mensaje distinto
                .failureHandler((request, response, exception) -> {
                    String motivo = exception instanceof DisabledException ? "deshabilitado" : "error";
                    response.sendRedirect(request.getContextPath() + "/login?" + motivo);
                })
                .permitAll())

            // POST /logout cierra la sesion y vuelve a /login?logout
            .logout(logout -> logout.permitAll())

            // Pagina personalizada cuando el usuario no tiene el rol necesario
            .exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }
}
