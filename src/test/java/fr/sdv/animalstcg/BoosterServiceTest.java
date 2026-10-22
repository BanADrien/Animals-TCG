package fr.sdv.animalstcg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.models.Joueur;
import fr.sdv.animalstcg.repositories.CarteRepository;
import fr.sdv.animalstcg.repositories.JoueurRepository;
import fr.sdv.animalstcg.services.BoosterService;
import fr.sdv.animalstcg.services.TirageService;

// base H2 en memoire + un faux service iNaturalist, pour ne pas appeler la vraie API
@DataJpaTest
class BoosterServiceTest {

	@Autowired
	private CarteRepository carteRepository;

	@Autowired
	private JoueurRepository joueurRepository;

	private BoosterService boosterService;
	private Joueur joueur;

	@BeforeEach
	void preparer() {
		boosterService = new BoosterService(new FauxInaturalistService(false), new TirageService(),
				carteRepository, joueurRepository, 50);
		joueur = joueurRepository.save(new Joueur("testeur", "motdepassehache", 100));
	}

	@Test
	void unBoosterDonneCinqCartesDifferentes() {
		List<Carte> cartes = boosterService.ouvrirBooster(joueur.getId());

		assertEquals(5, cartes.size());

		Set<Long> ids = new HashSet<>();
		for (Carte carte : cartes) {
			ids.add(carte.getInatId());
			assertNotNull(carte.getRarete());
		}
		assertEquals(5, ids.size());
	}

	@Test
	void lesCartesSontEnregistreesPourLeJoueur() {
		boosterService.ouvrirBooster(joueur.getId());

		assertEquals(5, carteRepository.countByJoueur(joueur));
	}

	@Test
	void leBoosterCoute50Pieces() {
		boosterService.ouvrirBooster(joueur.getId());

		Joueur apres = joueurRepository.findById(joueur.getId()).get();
		assertEquals(50, apres.getPieces());
	}

	@Test
	void pasAssezDePieces() {
		joueur.setPieces(20);
		joueurRepository.save(joueur);

		assertThrows(IllegalStateException.class, () -> boosterService.ouvrirBooster(joueur.getId()));
		// rien n'a ete enregistre et les pieces n'ont pas bouge
		assertEquals(0, carteRepository.countByJoueur(joueur));
		assertEquals(20, joueurRepository.findById(joueur.getId()).get().getPieces());
	}

}
