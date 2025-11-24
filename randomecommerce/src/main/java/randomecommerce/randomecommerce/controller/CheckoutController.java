package randomecommerce.randomecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import randomecommerce.randomecommerce.domain.Carrito;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import randomecommerce.randomecommerce.domain.Producto;
import randomecommerce.randomecommerce.service.ProductoService;

@Controller
@RequestMapping("/Random/checkout")
public class CheckoutController {

    private final Carrito carrito;
    private final ProductoService productoService;

    public CheckoutController(Carrito carrito, ProductoService productoService) {
        this.carrito = carrito;
        this.productoService = productoService;
    }

    @GetMapping("")
    public String mostrarCheckout(Model model) {
        if (carrito.estaVacio()) {
            return "redirect:/Random/carrito";
        }

        model.addAttribute("items", carrito.getItems());
        model.addAttribute("total", carrito.getTotal());
        return "checkout/checkout";
    }

    @PostMapping("/realitzar")
    public String realitzarComanda(
        @RequestParam String nomTargeta,
        @RequestParam String numeroTargeta,
        @RequestParam String dataExpiracio,
        @RequestParam String cvv,
        @RequestParam String nomClient,
        @RequestParam String direccio,
        @RequestParam String ciutat,
        @RequestParam String codipostal,
        @RequestParam String pais,
        @RequestParam String telefon,
        Model model) {

        for (Carrito.CarritoItem item : carrito.getItems()) {
            boolean actualizado = productoService.actualizarStock(item.getProductoId(), item.getCantidad());
            if (!actualizado) {
                System.out.println("No hay suficiente stock para el producto ID " + item.getProductoId());
            }
        }

        carrito.vaciar();

        model.addAttribute("missatgeComanda", "Comanda realitzada amb èxit! La teva comanda arribarà en 3/4 dies.");
        model.addAttribute("nombreCliente", nomClient);
        model.addAttribute("direccionEnvio", direccio + ", " + ciutat + " " + codipostal + ", " + pais);
        model.addAttribute("telefono", telefon);

        model.addAttribute("missatgeComanda", 
            "Comanda realitzada amb èxit! La teva comanda arribarà en 3/4 dies.");

        return "/checkout/confirmacio";
    }
}