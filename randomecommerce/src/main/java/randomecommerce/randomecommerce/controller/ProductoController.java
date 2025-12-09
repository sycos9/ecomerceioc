package randomecommerce.randomecommerce.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import randomecommerce.randomecommerce.domain.Producto;
import randomecommerce.randomecommerce.repository.ProductoRepository;
import randomecommerce.randomecommerce.service.ProductoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import randomecommerce.randomecommerce.domain.Carrito;
import randomecommerce.randomecommerce.domain.User;
import randomecommerce.randomecommerce.domain.Comentario;
import randomecommerce.randomecommerce.service.ComentarioService;

@Controller
@RequestMapping("/Random/productos")
public class ProductoController {
    
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    private final ComentarioService comentarioService;
    
    @Value("${app.upload.dir:${user.home}/randomecommerce/imagenes}")
    private String uploadDir;
    
    public ProductoController(ProductoRepository productoRepository, ProductoService productoService, ComentarioService comentarioService) {
        this.productoRepository = productoRepository;
        this.productoService = productoService;
        this.comentarioService = comentarioService;
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("imagen");
    }
    
    // Inicializar directorio de imágenes
    private void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("No es pot crear el directori d'imatges", e);
        }
    }
    
    // Mostrar lista de productos
    @GetMapping("")
    public String listarProductos(Model model) {
        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);
        return "productos/lista"; // templates/productos/lista.html
    }
    
    // Mostrar detalle de un producto
    @GetMapping("/{id}")
    public String mostrarProducto(
            @PathVariable Long id,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            Model model,
            Carrito carrito) {
        Producto producto = productoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producte no trobat: " + id));
        model.addAttribute("producto", producto);
        model.addAttribute("carrito", carrito);
        // Pasar los parámetros de filtro para mantenerlos al volver
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            model.addAttribute("busqueda", busqueda.trim());
        }
        if (categoria != null && productoService.esCategoriaValida(categoria)) {
            model.addAttribute("categoriaSeleccionada", categoria);
        }
        
        List<Comentario> comentarios = comentarioService.obtenerComentarios(id);
        model.addAttribute("comentarios", comentarios);
        
        return "productos/show";
    }
    
    // Mostrar formulario de nuevo producto
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model, HttpSession session) {
        User user = (User) session.getAttribute("usuari");
        if (user == null || !user.getRol().getNombre().equals("ADMIN")) {
            return "redirect:/Random"; // Redirige si no es admin
        }

        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", productoService.obtenerCategorias());
        return "productos/formulario";
    }
    
    @PostMapping("")
    public String guardarProducto(
            @ModelAttribute Producto producto,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Model model) {

        init();

        // Validar categoría
        if (producto.getCategoria() == null || !productoService.esCategoriaValida(producto.getCategoria())) {
            model.addAttribute("error", "La categoria seleccionada no és vàlida.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", productoService.obtenerCategorias());
            return "productos/formulario";
        }

        // Verificar duplicado de código
        Optional<Producto> prodConCodigo = productoRepository.findByCodigo(producto.getCodigo());
        if (prodConCodigo.isPresent() &&
            (producto.getId() == null || !producto.getId().equals(prodConCodigo.get().getId()))) {
            model.addAttribute("errorCodigo", "El codi ja existeix. Utilitza un altre codi.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", productoService.obtenerCategorias());
            return "productos/formulario";
        }

        Producto productoGuardar;
        if (producto.getId() != null) {
            // Edición: recuperar existente
            productoGuardar = productoRepository.findById(producto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Producte no trobat: " + producto.getId()));

            // Actualizar solo campos editables
            productoGuardar.setNombre(producto.getNombre());
            productoGuardar.setCodigo(producto.getCodigo());
            productoGuardar.setDescripcion(producto.getDescripcion());
            productoGuardar.setPrecio(producto.getPrecio());
            productoGuardar.setStock(producto.getStock());
            productoGuardar.setCategoria(producto.getCategoria());
        } else {
            // Creación
            productoGuardar = producto;
        }

        // Manejo de imagen
        if (imagen != null && !imagen.isEmpty()) {
            try {
                String nombreOriginal = imagen.getOriginalFilename();
                String extension = "";
                if (nombreOriginal != null && nombreOriginal.contains(".")) {
                    extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
                }
                String nombreArchivo = UUID.randomUUID().toString() + extension;

                Path rutaArchivo = Paths.get(uploadDir).resolve(nombreArchivo).normalize();
                Files.copy(imagen.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

                // Borrar imagen anterior si existe
                if (productoGuardar.getImagen() != null) {
                    Path imagenAnterior = Paths.get(uploadDir).resolve(productoGuardar.getImagen()).normalize();
                    Files.deleteIfExists(imagenAnterior);
                }

                productoGuardar.setImagen(nombreArchivo);
            } catch (IOException e) {
                model.addAttribute("error", "Error al pujar la imatge: " + e.getMessage());
                model.addAttribute("producto", producto);
                model.addAttribute("categorias", productoService.obtenerCategorias());
                return "productos/formulario";
            }
        }

        // Guardar producto actualizado o nuevo
        productoRepository.save(productoGuardar);

        return "redirect:/Random/productos";
    }


    
    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("usuari");
        if (user == null || !user.getRol().getNombre().equals("ADMIN")) {
            return "redirect:/Random"; 
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producte no trobat: " + id));
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", productoService.obtenerCategorias());
        return "productos/formulario";
    }
    
    // Eliminar producto
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        User user = (User) session.getAttribute("usuari");
        if (user == null || !user.getRol().getNombre().equals("ADMIN")) {
            return "redirect:/Random"; 
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producte no trobat: " + id));
        
        // Eliminar imagen asociada si existe
        if (producto.getImagen() != null && !producto.getImagen().isEmpty()) {
            try {
                Path imagenPath = Paths.get(uploadDir).resolve(producto.getImagen()).normalize();
                Files.deleteIfExists(imagenPath);
            } catch (IOException e) { }
        }

        productoRepository.delete(producto);
        redirectAttributes.addFlashAttribute("mensaje", "Producte eliminat exitosament");
        return "redirect:/Random/productos";
    }
    
    // Mostrar imágenes
    @GetMapping("/imagenes/{nombreArchivo:.+}")
    public ResponseEntity<Resource> mostrarImagen(@PathVariable String nombreArchivo) {
        try {
            Path rutaArchivo = Paths.get(uploadDir).resolve(nombreArchivo).normalize();
            Resource recurso = new UrlResource(rutaArchivo.toUri());
            
            if (recurso.exists() && recurso.isReadable()) {
                // Determinar el tipo de contenido según la extensión
                String contentType = "application/octet-stream";
                String nombreArchivoLower = nombreArchivo.toLowerCase();
                
                if (nombreArchivoLower.endsWith(".jpg") || nombreArchivoLower.endsWith(".jpeg")) {
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                } else if (nombreArchivoLower.endsWith(".png")) {
                    contentType = MediaType.IMAGE_PNG_VALUE;
                } else if (nombreArchivoLower.endsWith(".gif")) {
                    contentType = MediaType.IMAGE_GIF_VALUE;
                } else if (nombreArchivoLower.endsWith(".webp")) {
                    contentType = "image/webp";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
                        .body(recurso);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Guardar nuevo comentario
    @PostMapping("/{id}/comentar")
    public String guardarComentario(@PathVariable Long id, @RequestParam("contenido") String contenido, HttpSession session) {
        User user = (User) session.getAttribute("usuari");
        if (user != null) {
            Producto producto = productoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Producte no trobat: " + id));
            
            Comentario comentario = new Comentario(contenido, user, producto);
            comentarioService.guardarComentario(comentario);
        }
        return "redirect:/Random/productos/" + id;
    }

}