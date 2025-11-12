package randomecommerce.randomecommerce.service;

import randomecommerce.randomecommerce.domain.User;
import randomecommerce.randomecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean registrarUsuari(String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            return false; // ja existeix
        }
        User user = new User(username, password);
        userRepository.save(user);
        return true;
    }

    public boolean validaUsuari(String username, String password) {
        User user = userRepository.findByUsername(username);
        return user != null && user.getPassword().equals(password);
    }
}
