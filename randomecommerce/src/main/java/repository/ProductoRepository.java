package repository;


import domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Ignasi
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
