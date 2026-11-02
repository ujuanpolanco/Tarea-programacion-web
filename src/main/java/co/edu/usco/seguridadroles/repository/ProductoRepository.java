package co.edu.usco.seguridadroles.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.usco.seguridadroles.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
}
