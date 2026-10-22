package fr.sdv.animalstcg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.services.InaturalistService;

// on teste la lecture du JSON avec un exemple, sans appeler la vraie API
class InaturalistServiceTest {

	// meme format que l'API (le 2e animal n'a pas de nom francais, le 3e n'a pas de photo)
	private String json = """
			{
			  "total_results": 3,
			  "results": [
			    {
			      "id": 42069,
			      "name": "Vulpes vulpes",
			      "preferred_common_name": "Renard roux",
			      "iconic_taxon_name": "Mammalia",
			      "observations_count": 160750,
			      "default_photo": {"medium_url": "https://static.inaturalist.org/photos/265916780/medium.jpg"}
			    },
			    {
			      "id": 320680,
			      "name": "Pantherophis vulpinus",
			      "iconic_taxon_name": "Reptilia",
			      "observations_count": 6132,
			      "default_photo": {"medium_url": "https://static.inaturalist.org/photos/412617912/medium.jpg"}
			    },
			    {
			      "id": 12345,
			      "name": "Animal sans photo",
			      "preferred_common_name": "Sans photo",
			      "iconic_taxon_name": "Aves",
			      "observations_count": 5000,
			      "default_photo": null
			    }
			  ]
			}
			""";

	private InaturalistService service = new InaturalistService("http://adresse-pas-utilisee");

	@Test
	void lireJsonDonneLesCartes() {
		List<Carte> cartes = service.lireJson(json);

		// l'animal sans photo n'est pas garde
		assertEquals(2, cartes.size());

		Carte renard = cartes.get(0);
		assertEquals(42069L, renard.getInatId());
		assertEquals("Renard roux", renard.getNom());
		assertEquals("Vulpes vulpes", renard.getNomScientifique());
		assertEquals("Mammifère", renard.getType());
		assertEquals(160750, renard.getObservations());
		assertEquals("https://static.inaturalist.org/photos/265916780/medium.jpg", renard.getPhotoUrl());
	}

	@Test
	void sansNomFrancaisOnGardeLeNomScientifique() {
		Carte serpent = service.lireJson(json).get(1);

		assertEquals("Pantherophis vulpinus", serpent.getNom());
		assertEquals("Reptile", serpent.getType());
	}

	@Test
	void animalSansPhotoIgnore() {
		for (Carte carte : service.lireJson(json)) {
			assertNotEquals("Sans photo", carte.getNom());
		}
	}

	@Test
	void traduireType() {
		assertEquals("Oiseau", service.traduireType("Aves"));
		assertEquals("Poisson", service.traduireType("Actinopterygii"));
		assertEquals("Autre", service.traduireType("Animalia"));
	}

}
