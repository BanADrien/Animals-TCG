
package fr.sdv.animalstcg.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccueilController {

    // Affiche la page d'accueil lorsqu'on accède à "/"
    @GetMapping("/")
    public String accueil() {
        return "accueil";
    }
}