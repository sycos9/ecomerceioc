package repository;


import domain.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Ignasi
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // --- Buscar productos por nombre (contiene) ---
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // --- Filtrar por categoría ---
    List<Producto> findByCategoria(String categoria);

    // --- Filtrar por rango de precios ---
    List<Producto> findByPrecioBetween(double min, double max);
}
