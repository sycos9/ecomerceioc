package randomecommerce.randomecommerce.repository;


import randomecommerce.randomecommerce.domain.Producto;
import java.util.List;
import java.util.Optional;
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
    
    Optional<Producto> findByCodigo(String codigo);

}
