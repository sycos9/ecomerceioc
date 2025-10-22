/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import domain.Producto;
import java.util.List;
import repository.ProductoRepository;
import org.springframework.stereotype.Service;

/**
 *
 * @author Ignasi
 */

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

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
}
