package randomecommerce.randomecommerce.service;

import org.springframework.stereotype.Service;
import randomecommerce.randomecommerce.domain.Comentario;
import randomecommerce.randomecommerce.repository.ComentarioRepository;

import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository) {
        this.comentarioRepository = comentarioRepository;
    }

    public List<Comentario> obtenerComentarios(Long productoId) {
        return comentarioRepository.findByProductoIdOrderByFechaDesc(productoId);
    }

    public void guardarComentario(Comentario comentario) {
        comentarioRepository.save(comentario);
    }
}
