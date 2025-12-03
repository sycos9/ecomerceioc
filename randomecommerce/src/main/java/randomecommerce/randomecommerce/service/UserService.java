package randomecommerce.randomecommerce.service;

import randomecommerce.randomecommerce.domain.User;
import randomecommerce.randomecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import randomecommerce.randomecommerce.domain.Rol;
import randomecommerce.randomecommerce.repository.RolRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolRepository rolRepository;

    //Registra un usuari nou si no existeix
    public boolean registrarUsuari(String username, String password, Rol rolUsuario) {
    if (userRepository.findByUsername(username) != null) {
        return false;
    }

    // Si no se pasa un rol válido, asigna USER por defecto
    if (rolUsuario == null) {
        rolUsuario = rolRepository.findByNombre("USER");
        if (rolUsuario == null) {
            rolUsuario = rolRepository.save(new Rol("USER"));
        }
    }

    User user = new User(username, password);
    user.setRol(rolUsuario); // asignamos el rol al usuario
    userRepository.save(user);
    return true;
}

    //Valida si l'usuari existeix i la contrasenya es correcta
    public boolean validaUsuari(String username, String password) {
        User user = userRepository.findByUsername(username);
        return user != null && user.getPassword().equals(password);
    }
    
    //Obtenir usuari per el seu nom
    public User getUser(String username) {
        return userRepository.findByUsername(username);
    }
}
