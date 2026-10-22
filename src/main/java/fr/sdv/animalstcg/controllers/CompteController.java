
package fr.sdv.animalstcg.controllers;

import fr.sdv.animalstcg.models.Joueur;
import fr.sdv.animalstcg.services.JoueurService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CompteController {

    private final JoueurService joueurService;

    public CompteController(JoueurService joueurService) {
        this.joueurService = joueurService;
    }

    @GetMapping("/inscription")
    public String afficherInscription() {
        return "inscription";
    }

    @PostMapping("/inscription")
    public String inscrire(
            @RequestParam String pseudo,
            @RequestParam String motDePasse,
            Model model,
            HttpSession session) {
        try {
            Joueur joueur = joueurService.inscrire(pseudo, motDePasse);
            session.setAttribute("joueurId", joueur.getId());
            return "redirect:/profil";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            return "inscription";
        }
    }

    @GetMapping("/connexion")
    public String afficherConnexion() {
        return "connexion";
    }

    @PostMapping("/connexion")
    public String connecter(
            @RequestParam String pseudo,
            @RequestParam String motDePasse,
            HttpSession session,
            HttpServletRequest request,
            Model model) {
        try {
            Joueur joueur = joueurService.connecter(pseudo, motDePasse);
            session.invalidate();
            HttpSession nouvelleSession = request.getSession(true);
            nouvelleSession.setAttribute("joueurId", joueur.getId());
            return "redirect:/profil";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            return "connexion";
        }
    }

    @GetMapping("/deconnexion")
    public String deconnecter(HttpSession session) {
        session.invalidate();
        return "redirect:/connexion";
    }

    @GetMapping("/profil")
    public String afficherProfil(HttpSession session, Model model) {
        Long joueurId = (Long) session.getAttribute("joueurId");

        if (joueurId == null) {
            return "redirect:/connexion";
        }

        try {
            model.addAttribute("joueur", joueurService.trouverParId(joueurId));
            return "profil";
        } catch (IllegalArgumentException e) {
            session.invalidate();
            return "redirect:/connexion";
        }
    }

    @PostMapping("/profil/pseudo")
    public String modifierPseudo(
            @RequestParam String pseudo,
            HttpSession session,
            Model model) {
        Long joueurId = (Long) session.getAttribute("joueurId");
        if (joueurId == null) {
            return "redirect:/connexion";
        }

        try {
            joueurService.modifierPseudo(joueurId, pseudo);
            return "redirect:/profil";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("joueur", joueurService.trouverParId(joueurId));
            return "profil";
        }
    }

    @PostMapping("/profil/mot-de-passe")
    public String modifierMotDePasse(
            @RequestParam String ancienMotDePasse,
            @RequestParam String nouveauMotDePasse,
            HttpSession session,
            Model model) {
        Long joueurId = (Long) session.getAttribute("joueurId");
        if (joueurId == null) {
            return "redirect:/connexion";
        }

        try {
            joueurService.modifierMotDePasse(
                    joueurId, ancienMotDePasse, nouveauMotDePasse);
            model.addAttribute("message", "Mot de passe modifié.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
        }

        model.addAttribute("joueur", joueurService.trouverParId(joueurId));
        return "profil";
    }

    @PostMapping("/profil/supprimer")
    public String supprimerCompte(HttpSession session) {
        Long joueurId = (Long) session.getAttribute("joueurId");
        if (joueurId == null) {
            return "redirect:/connexion";
        }

        joueurService.supprimerCompte(joueurId);
        session.invalidate();
        return "redirect:/";
    }
}