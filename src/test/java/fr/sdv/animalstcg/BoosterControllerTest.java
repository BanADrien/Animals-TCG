package fr.sdv.animalstcg;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.sdv.animalstcg.controllers.BoosterController;
import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.models.Joueur;
import fr.sdv.animalstcg.repositories.JoueurRepository;
import fr.sdv.animalstcg.services.BoosterService;

// on ne demarre que la partie web : le service et le repository sont remplaces par des faux (Mockito)
@WebMvcTest(BoosterController.class)
class BoosterControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BoosterService boosterService;

	@MockitoBean
	private JoueurRepository joueurRepository;

	// la session d'un joueur connecte (c'est ce que fait la page connexion)
	private MockHttpSession session;

	@BeforeEach
	void preparer() {
		session = new MockHttpSession();
		session.setAttribute("joueurId", 1L);

		Joueur joueur = new Joueur("sacha", "hache", 100);
		joueur.setId(1L);
		when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur));
		when(boosterService.getPrixBooster()).thenReturn(50);
	}

	private List<Carte> unBooster() {
		List<Carte> cartes = new ArrayList<>();
		String[] raretes = { "Commune", "Commune", "Peu commune", "Rare", "Légendaire" };
		for (int i = 0; i < 5; i++) {
			Carte carte = new Carte();
			carte.setInatId((long) i);
			carte.setNom("Animal " + i);
			carte.setNomScientifique("Animalus " + i);
			carte.setType("Oiseau");
			carte.setRarete(raretes[i]);
			carte.setObservations(160750);
			carte.setPhotoUrl("http://photo/" + i + ".jpg");
			cartes.add(carte);
		}
		return cartes;
	}

	@Test
	void sansConnexionOnVaSurLaPageConnexion() throws Exception {
		mockMvc.perform(get("/booster"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/connexion"));
	}

	@Test
	void pageBoosterAfficheLesPieces() throws Exception {
		mockMvc.perform(get("/booster").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("booster"))
				.andExpect(content().string(containsString("<b>100</b> pièces")));
	}

	@Test
	void ouvrirUnBoosterRedirigeAvecLesCartes() throws Exception {
		List<Carte> cartes = unBooster();
		when(boosterService.ouvrirBooster(1L)).thenReturn(cartes);

		mockMvc.perform(post("/booster/ouvrir").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/booster"))
				.andExpect(flash().attribute("cartes", cartes))
				.andExpect(flash().attribute("message", "Booster ouvert : 5 nouvelles cartes !"));
	}

	@Test
	void pasAssezDePieces() throws Exception {
		when(boosterService.ouvrirBooster(1L)).thenThrow(new IllegalStateException("Pas assez de pièces pour ouvrir un booster"));

		mockMvc.perform(post("/booster/ouvrir").session(session))
				.andExpect(redirectedUrl("/booster"))
				.andExpect(flash().attribute("erreur", "Pas assez de pièces pour ouvrir un booster"));
	}

	@Test
	void apiEnPanne() throws Exception {
		when(boosterService.ouvrirBooster(1L)).thenThrow(new RuntimeException("timeout"));

		mockMvc.perform(post("/booster/ouvrir").session(session))
				.andExpect(redirectedUrl("/booster"))
				.andExpect(flash().attribute("erreur", "Impossible de contacter l'API, réessaie plus tard."));
	}

	@Test
	void lesCartesSAffichentAvecLaCouleurDeLeurRarete() throws Exception {
		mockMvc.perform(get("/booster").session(session).flashAttr("cartes", unBooster()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Animal 4")))
				.andExpect(content().string(containsString("carte legendaire")))
				.andExpect(content().string(containsString("carte peu-commune")))
				.andExpect(content().string(containsString("160 750 observations")));
	}

}
