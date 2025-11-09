/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package randomecommerce.randomecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author Ignasi
 */

@Controller
public class HomeController {

    @GetMapping("/Random")
    public String mostrarInicio() {
        // Esto devolverá el archivo templates/index.html
        return "index";
    }
}