package co.edu.usco.seguridadroles.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.usco.seguridadroles.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring Data genera la consulta a partir del nombre del metodo:
    // SELECT ... FROM users WHERE username = ?
    Optional<User> findByUsername(String username);
}
