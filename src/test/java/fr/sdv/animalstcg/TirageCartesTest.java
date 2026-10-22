package fr.sdv.animalstcg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.services.BoosterService;
import fr.sdv.animalstcg.services.TirageService;

// tests unitaires du tirage des cartes : pas de Spring, pas de base, pas de reseau
class TirageCartesTest {

	@Test
	void cinqCartesDifferentesAvecUneRarete() {
		BoosterService booster = new BoosterService(new FauxInaturalistService(false), new TirageService(), null, null, 50);

		List<Carte> cartes = booster.tirerCartes();

		assertEquals(BoosterService.NOMBRE_CARTES, cartes.size());
		Set<Long> ids = new HashSet<>();
		for (Carte carte : cartes) {
			ids.add(carte.getInatId());
			assertNotNull(carte.getRarete());
		}
		assertEquals(5, ids.size());
	}

	@Test
	void lesCartesNeSontPasEncoreAUnJoueur() {
		BoosterService booster = new BoosterService(new FauxInaturalistService(false), new TirageService(), null, null, 50);

		for (Carte carte : booster.tirerCartes()) {
			assertEquals(null, carte.getJoueur());
		}
	}

	@Test
	void pasDeBoucleInfinieSiLApiDonneToujoursLeMemeAnimal() {
		FauxInaturalistService api = new FauxInaturalistService(true);
		BoosterService booster = new BoosterService(api, new TirageService(), null, null, 50);

		List<Carte> cartes = booster.tirerCartes();

		// le Set refuse les doublons : une seule carte, et on s'arrete apres 20 essais
		assertEquals(1, cartes.size());
		assertEquals(20, api.getNombreAppels());
	}

	@Test
	void unAppelParCarteQuandToutSePasseBien() {
		FauxInaturalistService api = new FauxInaturalistService(false);
		BoosterService booster = new BoosterService(api, new TirageService(), null, null, 50);

		booster.tirerCartes();

		// au moins 5 appels (un par carte), un peu plus si on tombe 2 fois sur le meme animal
		assertTrue(api.getNombreAppels() >= 5 && api.getNombreAppels() <= 20);
	}

}
