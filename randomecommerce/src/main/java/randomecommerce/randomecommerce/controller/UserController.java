package randomecommerce.randomecommerce.controller;

import randomecommerce.randomecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

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

        boolean creat = userService.registrarUsuari(username, password);
        if (!creat) {
            model.addAttribute("error", "Aquest usuari ja existeix!");
            return "users/registre";
        }

        model.addAttribute("missatge", "Usuari registrat correctament!");
        return "users/login"; // Redirigeix a login
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
                               Model model) {
        boolean valid = userService.validaUsuari(username, password);

        if (!valid) {
            model.addAttribute("error", "Usuari o contrasenya incorrectes!");
            return "users/login";
        }

        model.addAttribute("usuari", username);
        return "redirect:/Random";
    }
}