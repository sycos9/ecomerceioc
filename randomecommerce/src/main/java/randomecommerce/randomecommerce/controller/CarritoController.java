package randomecommerce.randomecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import randomecommerce.randomecommerce.domain.Carrito;
import randomecommerce.randomecommerce.domain.Producto;
import randomecommerce.randomecommerce.domain.User;
import randomecommerce.randomecommerce.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/Random/carrito")
public class CarritoController {

    private final Carrito carrito;
    private final ProductoRepository productoRepository;

    public CarritoController(Carrito carrito, ProductoRepository productoRepository) {
        this.carrito = carrito;
        this.productoRepository = productoRepository;
    }

    @PostMapping("/agregar/{id}")
    public String agregarProducto(
            @PathVariable Long id,
            @RequestParam("cantidad") int cantidad,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("usuari");
        if (user == null) {
            return "redirect:/login";
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producte no trobat: " + id));

        boolean añadidoCompleto = carrito.agregarProducto(producto, cantidad);

        if (añadidoCompleto) {
            redirectAttributes.addFlashAttribute(
                    "mensajeCarrito",
                    String.format("%s afegit al carro (%d unitats)", producto.getNombre(), cantidad)
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorCarrito",
                    String.format("No hi ha suficient stock per a %s.",
                            producto.getNombre())
            );
        }

        String destino = (redirectUrl != null && redirectUrl.startsWith("/Random")) ? redirectUrl : "/Random/productos";
        return "redirect:" + destino;
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProductoDelCarrito(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        carrito.eliminarProducto(id);
        redirectAttributes.addFlashAttribute("mensajeCarrito", "Producte eliminat del carro.");
        return "redirect:/Random/carrito";
    }

    @GetMapping
    public String verCarrito(Model model) {
        model.addAttribute("items", carrito.getItems());
        model.addAttribute("total", carrito.getTotal());
        model.addAttribute("carritoVacio", carrito.estaVacio());
        return "carrito/resumen"; // Thymeleaf template
    }
}

