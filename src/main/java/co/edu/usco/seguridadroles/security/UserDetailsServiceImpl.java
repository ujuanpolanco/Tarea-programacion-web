package co.edu.usco.seguridadroles.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.usco.seguridadroles.model.Role;
import co.edu.usco.seguridadroles.model.User;
import co.edu.usco.seguridadroles.repository.UserRepository;

/**
 * "Lectura desde DB": Spring Security llama a este servicio cuando alguien
 * envia el formulario de login. Aqui buscamos el usuario en MySQL y le
 * devolvemos a Spring su contrasena (hash), si esta habilitado y sus roles.
 *
 * Spring es el que compara la contrasena escrita con el hash usando el
 * PasswordEncoder (BCrypt) definido en WebSecurityConfig.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario: " + username));

        // Cada fila de roles se convierte en una autoridad con el mismo nombre
        // (USER, CREATOR, EDITOR, ADMIN). Por eso en la configuracion se usa
        // hasAuthority / hasAnyAuthority y no hasRole (que exige el prefijo ROLE_).
        String[] autoridades = user.getRoles().stream()
                .map(Role::getName)
                .toArray(String[]::new);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(autoridades)
                .build();
    }
}
