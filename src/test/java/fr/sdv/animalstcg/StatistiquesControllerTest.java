package fr.sdv.animalstcg;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.sdv.animalstcg.controllers.StatistiquesController;
import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.models.Joueur;
import fr.sdv.animalstcg.repositories.CarteRepository;
import fr.sdv.animalstcg.repositories.JoueurRepository;
import fr.sdv.animalstcg.services.StatistiquesService;

// le vrai StatistiquesService fait les calculs, seuls les repositories sont des faux
@WebMvcTest(StatistiquesController.class)
@Import(StatistiquesService.class)
class StatistiquesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CarteRepository carteRepository;

	@MockitoBean
	private JoueurRepository joueurRepository;

	private MockHttpSession session;
	private Joueur joueur;

	@BeforeEach
	void preparer() {
		session = new MockHttpSession();
		session.setAttribute("joueurId", 1L);

		joueur = new Joueur("sacha", "hache", 50);
		joueur.setId(1L);
		when(joueurRepository.findById(1L)).thenReturn(Optional.of(joueur));
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
	void sansConnexionOnVaSurLaPageConnexion() throws Exception {
		mockMvc.perform(get("/statistiques"))
				.andExpect(redirectedUrl("/connexion"));
	}

	@Test
	void collectionVide() throws Exception {
		when(carteRepository.findByJoueur(joueur)).thenReturn(new ArrayList<>());

		mockMvc.perform(get("/statistiques").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("ouvre ton premier booster")))
				.andExpect(content().string(not(containsString("Par rareté"))));
	}

	@Test
	void statistiquesDeLaCollection() throws Exception {
		List<Carte> cartes = new ArrayList<>();
		cartes.add(creerCarte(1L, "Renard roux", "Mammifère", "Peu commune"));
		cartes.add(creerCarte(1L, "Renard roux", "Mammifère", "Peu commune"));
		cartes.add(creerCarte(2L, "Canard colvert", "Oiseau", "Commune"));
		cartes.add(creerCarte(3L, "Renard volant", "Mammifère", "Légendaire"));
		when(carteRepository.findByJoueur(joueur)).thenReturn(cartes);

		mockMvc.perform(get("/statistiques").session(session))
				.andExpect(status().isOk())
				.andExpect(model().attribute("total", 4))
				.andExpect(model().attribute("especes", 3))
				// 2 cartes peu communes sur 4 = barre a moitie remplie
				.andExpect(content().string(containsString("width:50%")))
				.andExpect(content().string(containsString("Renard roux")))
				.andExpect(content().string(containsString("x2")));
	}

}
