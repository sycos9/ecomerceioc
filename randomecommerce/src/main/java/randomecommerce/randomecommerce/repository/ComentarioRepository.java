package randomecommerce.randomecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import randomecommerce.randomecommerce.domain.Comentario;
import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByProductoIdOrderByFechaDesc(Long productoId);
}
