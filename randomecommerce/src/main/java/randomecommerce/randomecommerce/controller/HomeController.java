/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package randomecommerce.randomecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import randomecommerce.randomecommerce.service.ProductoService;
import randomecommerce.randomecommerce.domain.Carrito;



@Controller
public class HomeController {
    
    private final ProductoService productoService;
    
    public HomeController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/Random")
    public String mostrarInicio(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            Model model,
            Carrito carrito) {
        
        // Validar y normalizar parámetros
        String busquedaNormalizada = (busqueda != null && !busqueda.trim().isEmpty()) 
            ? busqueda.trim() 
            : null;
        
        String categoriaValida = (categoria != null && productoService.esCategoriaValida(categoria)) 
            ? categoria 
            : null;
        
        // Usar el servicio para buscar y filtrar productos
        model.addAttribute("productos", 
            productoService.buscarYFiltrarProductos(busquedaNormalizada, categoriaValida));
        model.addAttribute("carrito", carrito);
        
        // Pasar parámetros al modelo para la vista
        model.addAttribute("busqueda", busquedaNormalizada);
        model.addAttribute("categoriaSeleccionada", categoriaValida);
        model.addAttribute("categorias", productoService.obtenerCategorias());
        
        return "index";
    }
}