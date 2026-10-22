package fr.sdv.animalstcg.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.models.Joueur;
import fr.sdv.animalstcg.repositories.CarteRepository;
import fr.sdv.animalstcg.repositories.JoueurRepository;
import fr.sdv.animalstcg.services.StatistiquesService;
import jakarta.servlet.http.HttpSession;

@Controller
public class StatistiquesController {

	private StatistiquesService statistiquesService;
	private CarteRepository carteRepository;
	private JoueurRepository joueurRepository;

	public StatistiquesController(StatistiquesService statistiquesService, CarteRepository carteRepository,
			JoueurRepository joueurRepository) {
		this.statistiquesService = statistiquesService;
		this.carteRepository = carteRepository;
		this.joueurRepository = joueurRepository;
	}

	@GetMapping("/statistiques")
	public String statistiques(HttpSession session, Model model) {
		Long joueurId = (Long) session.getAttribute("joueurId");
		if (joueurId == null) {
			return "redirect:/connexion";
		}

		Joueur joueur = joueurRepository.findById(joueurId).orElse(null);
		if (joueur == null) {
			return "redirect:/connexion";
		}

		List<Carte> cartes = carteRepository.findByJoueur(joueur);

		model.addAttribute("joueur", joueur);
		model.addAttribute("total", cartes.size());
		model.addAttribute("especes", statistiquesService.especesDifferentes(cartes).size());
		model.addAttribute("parRarete", statistiquesService.compterParRarete(cartes));
		model.addAttribute("parType", statistiquesService.compterParType(cartes));
		model.addAttribute("doublons", statistiquesService.doublons(cartes));
		return "statistiques";
	}

}
