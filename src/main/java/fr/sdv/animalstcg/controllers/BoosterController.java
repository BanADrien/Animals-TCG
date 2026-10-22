package fr.sdv.animalstcg.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.models.Joueur;
import fr.sdv.animalstcg.repositories.JoueurRepository;
import fr.sdv.animalstcg.services.BoosterService;
import jakarta.servlet.http.HttpSession;

@Controller
public class BoosterController {

	private BoosterService boosterService;
	private JoueurRepository joueurRepository;

	public BoosterController(BoosterService boosterService, JoueurRepository joueurRepository) {
		this.boosterService = boosterService;
		this.joueurRepository = joueurRepository;
	}

	@GetMapping("/booster")
	public String booster(HttpSession session, Model model) {
		Long joueurId = (Long) session.getAttribute("joueurId");
		if (joueurId == null) {
			return "redirect:/connexion";
		}

		Joueur joueur = joueurRepository.findById(joueurId).orElse(null);
		if (joueur == null) {
			return "redirect:/connexion";
		}

		model.addAttribute("joueur", joueur);
		model.addAttribute("prix", boosterService.getPrixBooster());
		return "booster";
	}

	@PostMapping("/booster/ouvrir")
	public String ouvrir(HttpSession session, RedirectAttributes redirectAttributes) {
		Long joueurId = (Long) session.getAttribute("joueurId");
		if (joueurId == null) {
			return "redirect:/connexion";
		}

		try {
			List<Carte> cartes = boosterService.ouvrirBooster(joueurId);
			redirectAttributes.addFlashAttribute("cartes", cartes);
			redirectAttributes.addFlashAttribute("message", "Booster ouvert : " + cartes.size() + " nouvelles cartes !");
		} catch (IllegalStateException e) {
			// pas assez de pieces, ou pas reussi a tirer les cartes
			redirectAttributes.addFlashAttribute("erreur", e.getMessage());
		} catch (Exception e) {
			System.out.println("Erreur pendant l'ouverture du booster : " + e.getMessage());
			redirectAttributes.addFlashAttribute("erreur", "Impossible de contacter l'API, réessaie plus tard.");
		}

		// on redirige pour qu'un F5 n'achete pas un 2e booster
		return "redirect:/booster";
	}

}
