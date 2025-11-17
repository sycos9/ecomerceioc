package randomecommerce.randomecommerce.controller;

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

@Controller
@RequestMapping("/Random")
public class ProductoController {
    
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    
    @Value("${app.upload.dir:${user.home}/randomecommerce/imagenes}")
    private String uploadDir;
    
    public ProductoController(ProductoRepository productoRepository, ProductoService productoService) {
        this.productoRepository = productoRepository;
        this.productoService = productoService;
    }
    
    // Excluir el campo imagen del binding automático (se maneja manualmente como MultipartFile)
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
    @GetMapping("/productos")
    public String listarProductos(Model model) {
        List<Producto> productos = productoRepository.findAll();
        model.addAttribute("productos", productos);
        return "productos/lista"; // templates/productos/lista.html
    }
    
    // Mostrar detalle de un producto
    @GetMapping("/productos/{id}")
    public String mostrarProducto(
            @PathVariable Long id,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            Model model) {
        Producto producto = productoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producte no trobat: " + id));
        model.addAttribute("producto", producto);
        // Pasar los parámetros de filtro para mantenerlos al volver
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            model.addAttribute("busqueda", busqueda.trim());
        }
        if (categoria != null && productoService.esCategoriaValida(categoria)) {
            model.addAttribute("categoriaSeleccionada", categoria);
        }
        return "productos/show";
    }
    
    // Mostrar formulario de nuevo producto
    @GetMapping("/productos/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", productoService.obtenerCategorias());
        return "productos/formulario"; // templates/productos/formulario.html
    }
    
    // Guardar producto
    @PostMapping("/productos")
    public String guardarProducto(
            @ModelAttribute Producto producto,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Model model) {

        init();

        // Validar que la categoría sea válida
        if (producto.getCategoria() == null || !productoService.esCategoriaValida(producto.getCategoria())) {
            model.addAttribute("error", "La categoria seleccionada no és vàlida.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", productoService.obtenerCategorias());
            return "productos/formulario";
        }

        // Buscar si existe un producto con el mismo código
        Optional<Producto> productoExistente = productoRepository.findByCodigo(producto.getCodigo());

        // Verificar si es duplicado de otro producto
        if (productoExistente.isPresent() &&
            (producto.getId() == null || !producto.getId().equals(productoExistente.get().getId()))) {
            model.addAttribute("errorCodigo", "El codi ja existeix. Utilitza un altre codi.");
            model.addAttribute("producto", producto); // Mantener datos introducidos
            model.addAttribute("categorias", productoService.obtenerCategorias());
            return "productos/formulario"; // Volver a la misma vista
        }

        // Manejar la subida de imagen
        if (imagen != null && !imagen.isEmpty()) {
            try {
                // Generar nombre único para la imagen
                String nombreOriginal = imagen.getOriginalFilename();
                String extension = "";
                if (nombreOriginal != null && nombreOriginal.contains(".")) {
                    extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
                }
                String nombreArchivo = UUID.randomUUID().toString() + extension;
                
                // Guardar el archivo
                Path rutaArchivo = Paths.get(uploadDir).resolve(nombreArchivo).normalize();
                Files.copy(imagen.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
                
                // Si es una edición y hay una imagen anterior, eliminarla
                if (producto.getId() != null) {
                    Optional<Producto> productoAnterior = productoRepository.findById(producto.getId());
                    if (productoAnterior.isPresent() && productoAnterior.get().getImagen() != null) {
                        Path imagenAnterior = Paths.get(uploadDir).resolve(productoAnterior.get().getImagen()).normalize();
                        try {
                            Files.deleteIfExists(imagenAnterior);
                        } catch (IOException e) {
                            // Log error pero continuar
                        }
                    }
                }
                
                // Guardar el nombre del archivo en el producto
                producto.setImagen(nombreArchivo);
            } catch (IOException e) {
                model.addAttribute("error", "Error al pujar la imatge: " + e.getMessage());
                model.addAttribute("producto", producto);
                model.addAttribute("categorias", productoService.obtenerCategorias());
                return "productos/formulario";
            }
        } else {
            // Si no se sube nueva imagen y es una edición, mantener la imagen existente
            if (producto.getId() != null) {
                Optional<Producto> productoAnterior = productoRepository.findById(producto.getId());
                if (productoAnterior.isPresent() && productoAnterior.get().getImagen() != null) {
                    producto.setImagen(productoAnterior.get().getImagen());
                }
            }
        }

        // Guardar producto (creación o edición)
        productoRepository.save(producto);
        model.addAttribute("mensaje", "Producte guardat exitosament");

        return "redirect:/Random/productos"; //
    }


    
    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producte no trobat: " + id));
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", productoService.obtenerCategorias());
        return "productos/formulario"; // reutilizamos el formulario
    }
    
    // Eliminar producto
    @GetMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producte no trobat: " + id));

        // Eliminar la imagen asociada si existe
        if (producto.getImagen() != null && !producto.getImagen().isEmpty()) {
            try {
                Path imagenPath = Paths.get(uploadDir).resolve(producto.getImagen()).normalize();
                Files.deleteIfExists(imagenPath);
            } catch (IOException e) {
                // Log error pero continuar con la eliminación del producto
            }
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
    
}