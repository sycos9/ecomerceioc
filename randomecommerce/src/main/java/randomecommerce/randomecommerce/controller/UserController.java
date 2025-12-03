package randomecommerce.randomecommerce.controller;

import randomecommerce.randomecommerce.service.UserService;
import randomecommerce.randomecommerce.domain.User;
import randomecommerce.randomecommerce.repository.UserRepository;
import randomecommerce.randomecommerce.repository.RolRepository;
import randomecommerce.randomecommerce.domain.Rol;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolRepository rolRepository;

    // Formulari de registre
    @GetMapping("/registre")
    public String mostraFormulariRegistre() {
        return "users/registre";
    }

    // Processa el registre
    @PostMapping("/registre")
    public String registraUsuari(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Les contrasenyes no coincideixen!");
            return "users/registre";
        }

        Rol rolUsuario = rolRepository.findByNombre("USER");
        if (rolUsuario == null) {
            rolUsuario = new Rol("USER");
            rolRepository.save(rolUsuario);
        }

        boolean creat = userService.registrarUsuari(username, password, rolUsuario);
        if (!creat) {
            model.addAttribute("error", "Aquest usuari ja existeix!");
            return "users/registre";
        }

        model.addAttribute("missatge", "Usuari registrat correctament!");
        return "users/login"; 
    }

    // Formulari de login
    @GetMapping("/login")
    public String mostraLogin() {
        return "users/login";
    }

    // Processa login
    @PostMapping("/login")
    public String iniciaSessio(@RequestParam String username,
                               @RequestParam String password,
                               Model model,
                               HttpSession sessio) {

        boolean valid = userService.validaUsuari(username, password);

        if (!valid) {
            model.addAttribute("error", "Usuari o contrasenya incorrectes!");
            return "users/login";
        }

        // Carregar el user complet des de la BBDD
        User user = userRepository.findByUsername(username);

        // Guardar usuari a la sessió
        sessio.setAttribute("usuari", user);

        return "redirect:/Random";
    }

    // Logout
    @GetMapping("/logout")
    public String tancarSessio(HttpSession sessio) {
        sessio.invalidate();
        return "redirect:/";
    }
}
