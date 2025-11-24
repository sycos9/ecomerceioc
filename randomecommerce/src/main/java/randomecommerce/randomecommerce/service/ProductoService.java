/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package randomecommerce.randomecommerce.service;

import randomecommerce.randomecommerce.domain.Producto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import randomecommerce.randomecommerce.repository.ProductoRepository;
import org.springframework.stereotype.Service;

/**
 *
 * @author Ignasi
 */

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    
    // Lista fija de categorías disponibles
    public static final List<String> CATEGORIAS = List.of(
        "Electrónica",
        "Roba",
        "Llar",
        "Esports",
        "Llibres",
        "Joguines",
        "Alimentació",
        "Cosmètica",
        "Música",
        "Altres"
    );

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // --- Listar todos los productos ---
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    // --- Crear o actualizar un producto ---
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    // --- Eliminar un producto por ID ---
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    // --- Buscar producto por ID ---
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }
    
    // --- Buscar productos por nombre (contiene) ---
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // --- Filtrar por categoría ---
    public List<Producto> filtrarPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    // --- Filtrar por rango de precio ---
    public List<Producto> filtrarPorPrecio(double min, double max) {
        return productoRepository.findByPrecioBetween(min, max);
    }
    
    // --- Buscar y filtrar productos (combina búsqueda por nombre y filtro por categoría) ---
    public List<Producto> buscarYFiltrarProductos(String busqueda, String categoria) {
        List<Producto> productos;
        
        // Si hay búsqueda, buscar por nombre
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            productos = buscarPorNombre(busqueda.trim());
            
            // Si también hay filtro de categoría válido, filtrar los resultados de la búsqueda
            if (categoria != null && !categoria.isEmpty() && CATEGORIAS.contains(categoria)) {
                productos = productos.stream()
                    .filter(p -> p.getCategoria().equals(categoria))
                    .collect(Collectors.toList());
            }
        } else {
            // Sin búsqueda, filtrar solo por categoría si existe
            if (categoria != null && !categoria.isEmpty() && CATEGORIAS.contains(categoria)) {
                productos = filtrarPorCategoria(categoria);
            } else {
                productos = listarProductos();
            }
        }
        
        return productos;
    }
    
    // --- Validar si una categoría es válida ---
    public boolean esCategoriaValida(String categoria) {
        return categoria != null && CATEGORIAS.contains(categoria);
    }
    
    // --- Obtener lista de categorías ---
    public List<String> obtenerCategorias() {
        return CATEGORIAS;
    }

    // --- Actualizar stock de un producto (restar el stock vendido) ---
    public boolean actualizarStock(Long productoId, int cantidadVendida) {
        Optional<Producto> optionalProducto = productoRepository.findById(productoId);
        
        if (optionalProducto.isPresent()) {
            Producto producto = optionalProducto.get();
            
            if (producto.getStock() >= cantidadVendida) {
                producto.setStock(producto.getStock() - cantidadVendida);
                productoRepository.save(producto);
                return true;
            } else {
                return false; // No hay suficiente stock
            }
        }
        return false; // Producto no encontrado
    }

    // --- Guardar o actualizar stock manualmente ---
    public void guardarStock(Producto producto) {
        productoRepository.save(producto);
    }
}
