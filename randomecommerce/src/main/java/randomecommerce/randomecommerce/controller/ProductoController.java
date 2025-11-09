/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package randomecommerce.randomecommerce.controller;


import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import randomecommerce.randomecommerce.domain.Producto;
import randomecommerce.randomecommerce.repository.ProductoRepository;

/**
 *
 * @author Ignasi
 */
@Controller
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // POST para guardar producto
    @PostMapping("/productos")
    public void guardarProducto(
            @RequestParam String codigo,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam double precio,
            @RequestParam int stock,
            @RequestParam String categoria,
            HttpServletResponse response
    ) throws IOException {
        if (productoRepository.findByCodigo(codigo).isPresent()) {
            // Código duplicado → mostrar mensaje de error
            String htmlError = "<p style='color:red;'>El código ya existe. Por favor, usa otro código.</p>" +
                               "<a href='/Random/productos/nuevo'>Volver al formulario</a>";
            response.setContentType("text/html");
            response.getWriter().write(htmlError);
            return;
        }
        Producto producto = new Producto(codigo, nombre, descripcion, precio, stock, categoria);
        productoRepository.save(producto);
        // Redirige a lista de productos
        response.sendRedirect("/Random/productos");

    }

    // GET para redirigir /productos/nuevo al HTML estático
    @GetMapping("Random/productos/nuevo")
    public void mostrarFormulario(HttpServletResponse response) throws IOException {
        // Redirigir internamente sin cambiar la URL
        InputStream htmlFile = new ClassPathResource("static/productoForm.html").getInputStream();
        String html = new String(htmlFile.readAllBytes(), StandardCharsets.UTF_8);
        response.setContentType("text/html");
        response.getWriter().write(html);
    }   
    
    @GetMapping("Random/productos")
    public void mostrarLista(HttpServletResponse response) throws IOException {
        // Cargar el HTML desde static
        InputStream htmlFile = new ClassPathResource("static/productoList.html").getInputStream();
        String html = new String(htmlFile.readAllBytes(), StandardCharsets.UTF_8);
        // Enviar al navegador
        response.setContentType("text/html");
        response.getWriter().write(html);
    }
    
    @GetMapping("/productos/data")
    @ResponseBody
    public List<Producto> obtenerProductos() {
        return productoRepository.findAll();
    }
}