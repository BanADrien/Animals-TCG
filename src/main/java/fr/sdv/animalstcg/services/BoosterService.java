package fr.sdv.animalstcg.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.models.Joueur;
import fr.sdv.animalstcg.repositories.CarteRepository;
import fr.sdv.animalstcg.repositories.JoueurRepository;

@Service
public class BoosterService {

	public static final int NOMBRE_CARTES = 5;

	private InaturalistService inaturalistService;
	private TirageService tirageService;
	private CarteRepository carteRepository;
	private JoueurRepository joueurRepository;
	private int prixBooster;
	private Random random = new Random();

	public BoosterService(InaturalistService inaturalistService, TirageService tirageService,
			CarteRepository carteRepository, JoueurRepository joueurRepository,
			@Value("${jeu.booster.prix}") int prixBooster) {
		this.inaturalistService = inaturalistService;
		this.tirageService = tirageService;
		this.carteRepository = carteRepository;
		this.joueurRepository = joueurRepository;
		this.prixBooster = prixBooster;
	}

	public int getPrixBooster() {
		return prixBooster;
	}

	// ouvre un booster pour le joueur : tire les cartes, les enregistre et retire les pieces
	public List<Carte> ouvrirBooster(Long joueurId) {
		Joueur joueur = joueurRepository.findById(joueurId).orElse(null);
		if (joueur == null) {
			throw new IllegalArgumentException("Joueur introuvable");
		}
		if (joueur.getPieces() < prixBooster) {
			throw new IllegalStateException("Pas assez de pièces pour ouvrir un booster");
		}

		// on tire d'abord : si l'API plante, le joueur ne perd pas ses pieces
		List<Carte> cartes = tirerCartes();
		if (cartes.size() < NOMBRE_CARTES) {
			throw new IllegalStateException("Impossible de tirer les cartes, réessaie plus tard");
		}

		for (Carte carte : cartes) {
			carte.setJoueur(joueur);
			carte.setDateObtention(LocalDateTime.now());
		}
		carteRepository.saveAll(cartes);

		joueur.setPieces(joueur.getPieces() - prixBooster);
		joueurRepository.save(joueur);

		return cartes;
	}

	// tire 5 animaux differents (sans les enregistrer)
	public List<Carte> tirerCartes() {
		List<Carte> cartes = new ArrayList<>();
		// le Set sert a ne pas avoir 2 fois le meme animal dans un booster
		Set<Long> dejaTires = new HashSet<>();

		int essais = 0;
		while (cartes.size() < NOMBRE_CARTES && essais < 20) {
			essais++;

			String rarete = tirageService.tirerRarete();
			int page = tirageService.choisirPage(rarete);
			List<Carte> animaux = inaturalistService.chercherPage(page);
			if (animaux.isEmpty()) {
				continue;
			}

			Carte carte = animaux.get(random.nextInt(animaux.size()));
			if (dejaTires.contains(carte.getInatId())) {
				continue;
			}

			dejaTires.add(carte.getInatId());
			carte.setRarete(rarete);
			cartes.add(carte);
		}
		return cartes;
	}

}
