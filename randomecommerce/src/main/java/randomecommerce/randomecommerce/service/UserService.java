package randomecommerce.randomecommerce.service;

import randomecommerce.randomecommerce.domain.User;
import randomecommerce.randomecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    //Registra un usuari nou si no existeix
    public boolean registrarUsuari(String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            return false;
        }
        User user = new User(username, password);
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
