package randomecommerce.randomecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import randomecommerce.randomecommerce.domain.Producto;
import randomecommerce.randomecommerce.repository.ProductoRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/Random")
public class ProductoController {
    
    private final ProductoRepository productoRepository;
    
    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }
    
    // Mostrar lista de productos
    @GetMapping("/productos")
    public String listarProductos(Model model) {
        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);
        return "productos/lista"; // templates/productos/lista.html
    }
    
    // Mostrar formulario de nuevo producto
    @GetMapping("/productos/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/formulario"; // templates/productos/formulario.html
    }
    
    // Guardar producto
    @PostMapping("/productos")
    public String guardarProducto(
            @ModelAttribute Producto producto,
            Model model) {

        // Buscar si existe un producto con el mismo código
        Optional<Producto> productoExistente = productoRepository.findByCodigo(producto.getCodigo());

        // Verificar si es duplicado de otro producto
        if (productoExistente.isPresent() &&
            (producto.getId() == null || !producto.getId().equals(productoExistente.get().getId()))) {
            model.addAttribute("errorCodigo", "El código ya existe. Por favor, usa otro código.");
            model.addAttribute("producto", producto); // Mantener datos introducidos
            return "productos/formulario"; // Volver a la misma vista
        }

        // Guardar producto (creación o edición)
        productoRepository.save(producto);
        model.addAttribute("mensaje", "Producto guardado exitosamente");

        return "redirect:/Random/productos";
    }


    
    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        model.addAttribute("producto", producto);
        return "productos/formulario"; // reutilizamos el formulario
    }
    
    // Eliminar producto
    @GetMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));

        productoRepository.delete(producto);
        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
        return "redirect:/Random/productos";
    }
    
}