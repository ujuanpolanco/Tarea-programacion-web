package co.edu.usco.seguridadroles.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import co.edu.usco.seguridadroles.model.Producto;
import co.edu.usco.seguridadroles.repository.ProductoRepository;

/**
 * CRUD de productos. Las URLs coinciden con las de la diapositiva para que
 * las reglas de WebSecurityConfig las protejan:
 *
 *   /             listar          USER, CREATOR, EDITOR, ADMIN
 *   /new          crear           ADMIN, CREATOR
 *   /edit/{id}    editar          ADMIN, EDITOR
 *   /delete/{id}  eliminar        ADMIN
 *
 * Los formularios se envian (POST) a la misma URL que los muestra (GET).
 * Asi la misma regla protege mostrar el formulario y guardar los datos, y
 * no queda ninguna URL de "guardar" sin proteger.
 */
@Controller
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @GetMapping("/")
    public String listar(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "index";
    }

    @GetMapping("/new")
    public String formularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "producto_form";
    }

    @PostMapping("/new")
    public String crear(@ModelAttribute Producto producto) {
        producto.setId(null); // siempre es un registro nuevo
        productoRepository.save(producto);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String formularioEditar(@PathVariable Integer id, Model model) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto " + id));
        model.addAttribute("producto", producto);
        return "producto_form";
    }

    @PostMapping("/edit/{id}")
    public String editar(@PathVariable Integer id, @ModelAttribute Producto producto) {
        producto.setId(id); // el id sale de la URL, no del formulario
        productoRepository.save(producto);
        return "redirect:/";
    }

    // Eliminar es POST (boton dentro de un formulario) y no un enlace GET,
    // porque una accion que borra datos no deberia ejecutarse solo con abrir una URL.
    @PostMapping("/delete/{id}")
    public String eliminar(@PathVariable Integer id) {
        productoRepository.deleteById(id);
        return "redirect:/";
    }
}
