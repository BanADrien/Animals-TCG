package fr.sdv.animalstcg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import fr.sdv.animalstcg.services.TirageService;

class TirageServiceTest {

	private TirageService tirage = new TirageService();

	@Test
	void rareteSelonLeNombreTire() {
		assertEquals("Commune", tirage.tirerRarete(0));
		assertEquals("Commune", tirage.tirerRarete(59));
		assertEquals("Peu commune", tirage.tirerRarete(60));
		assertEquals("Peu commune", tirage.tirerRarete(84));
		assertEquals("Rare", tirage.tirerRarete(85));
		assertEquals("Rare", tirage.tirerRarete(96));
		assertEquals("Légendaire", tirage.tirerRarete(97));
		assertEquals("Légendaire", tirage.tirerRarete(99));
	}

	@Test
	void tirageAuHasardDonneUneRareteConnue() {
		Set<String> raretes = new HashSet<>();
		raretes.add("Commune");
		raretes.add("Peu commune");
		raretes.add("Rare");
		raretes.add("Légendaire");

		for (int i = 0; i < 100; i++) {
			assertTrue(raretes.contains(tirage.tirerRarete()));
		}
	}

	@Test
	void pageDansLaBonneTranche() {
		// on tire plein de fois pour etre sur de ne jamais sortir de la tranche
		for (int i = 0; i < 500; i++) {
			int page = tirage.choisirPage("Commune");
			assertTrue(page >= 1 && page <= 10);

			page = tirage.choisirPage("Légendaire");
			assertTrue(page >= 121 && page <= 200);
		}
	}

	@Test
	void jamaisAuDelaDeLaPage200() {
		// l'API refuse les pages apres 200 (50 x 200 = 10 000 resultats max)
		assertEquals(200, tirage.pageMax("Légendaire"));
	}

}
