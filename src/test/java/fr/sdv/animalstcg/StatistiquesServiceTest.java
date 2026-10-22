package fr.sdv.animalstcg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.services.StatistiquesService;

class StatistiquesServiceTest {

	private StatistiquesService stats = new StatistiquesService();
	private List<Carte> cartes;

	// une petite collection : le renard roux en 3 exemplaires et le canard en 2
	@BeforeEach
	void preparer() {
		cartes = new ArrayList<>();
		cartes.add(creerCarte(42069L, "Renard roux", "Mammifère", "Peu commune"));
		cartes.add(creerCarte(42069L, "Renard roux", "Mammifère", "Peu commune"));
		cartes.add(creerCarte(42069L, "Renard roux", "Mammifère", "Peu commune"));
		cartes.add(creerCarte(6930L, "Canard colvert", "Oiseau", "Commune"));
		cartes.add(creerCarte(6930L, "Canard colvert", "Oiseau", "Commune"));
		cartes.add(creerCarte(40905L, "Renard volant", "Mammifère", "Légendaire"));
	}

	private Carte creerCarte(Long inatId, String nom, String type, String rarete) {
		Carte carte = new Carte();
		carte.setInatId(inatId);
		carte.setNom(nom);
		carte.setType(type);
		carte.setRarete(rarete);
		return carte;
	}

	@Test
	void especesDifferentesSansDoublon() {
		Set<Long> especes = stats.especesDifferentes(cartes);

		assertEquals(3, especes.size());
		assertTrue(especes.contains(42069L));
	}

	@Test
	void compterParType() {
		Map<String, Integer> parType = stats.compterParType(cartes);

		assertEquals(2, parType.size());
		assertEquals(4, parType.get("Mammifère"));
		assertEquals(2, parType.get("Oiseau"));
	}

	@Test
	void compterParRareteAvecToutesLesRaretes() {
		Map<String, Integer> parRarete = stats.compterParRarete(cartes);

		assertEquals(4, parRarete.size());
		assertEquals(2, parRarete.get("Commune"));
		assertEquals(3, parRarete.get("Peu commune"));
		assertEquals(0, parRarete.get("Rare"));
		assertEquals(1, parRarete.get("Légendaire"));

		// les raretes restent dans l'ordre, de Commune a Legendaire
		List<String> ordre = new ArrayList<>(parRarete.keySet());
		assertEquals("Commune", ordre.get(0));
		assertEquals("Légendaire", ordre.get(3));
	}

	@Test
	void doublonsSeulementSiDeuxExemplairesOuPlus() {
		Map<String, Integer> doublons = stats.doublons(cartes);

		assertEquals(2, doublons.size());
		assertEquals(3, doublons.get("Renard roux"));
		assertEquals(2, doublons.get("Canard colvert"));
		assertFalse(doublons.containsKey("Renard volant"));
	}

	@Test
	void collectionVide() {
		List<Carte> vide = new ArrayList<>();

		assertEquals(0, stats.especesDifferentes(vide).size());
		assertTrue(stats.compterParType(vide).isEmpty());
		assertEquals(0, stats.compterParRarete(vide).get("Commune"));
		assertTrue(stats.doublons(vide).isEmpty());
	}

	@Test
	void pourcentagePourLesBarres() {
		assertEquals(50, stats.pourcentage(3, 6));
		assertEquals(100, stats.pourcentage(6, 6));
		// pas de division par zero si la collection est vide
		assertEquals(0, stats.pourcentage(0, 0));
	}

}
