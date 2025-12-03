package randomecommerce.randomecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import randomecommerce.randomecommerce.domain.Rol;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Rol findByNombre(String nombre);
}