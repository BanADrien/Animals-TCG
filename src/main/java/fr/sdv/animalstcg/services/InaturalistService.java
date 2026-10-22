package fr.sdv.animalstcg.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import fr.sdv.animalstcg.models.Carte;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// va chercher les animaux sur l'API iNaturalist
@Service
public class InaturalistService {

	private String urlApi;
	private RestClient restClient = RestClient.create();
	private ObjectMapper objectMapper = new ObjectMapper();

	public InaturalistService(@Value("${api.inaturalist.url}") String urlApi) {
		this.urlApi = urlApi;
	}

	// une page de 50 especes animales, triees de la plus observee a la moins observee
	// (taxon_id=1 = seulement les animaux, locale=fr = noms en francais)
	public List<Carte> chercherPage(int page) {
		String url = urlApi + "?taxon_id=1&rank=species&locale=fr&per_page=50"
				+ "&order_by=observations_count&order=desc&page=" + page;

		String json = restClient.get()
				.uri(url)
				.retrieve()
				.body(String.class);
		return lireJson(json);
	}

	// transforme le JSON de l'API en liste de cartes (pas encore liees a un joueur)
	public List<Carte> lireJson(String json) {
		List<Carte> cartes = new ArrayList<>();
		JsonNode resultats = objectMapper.readTree(json).get("results");

		for (JsonNode animal : resultats) {
			// pas de photo = pas de carte
			JsonNode photo = animal.get("default_photo");
			if (photo == null || photo.isNull()) {
				continue;
			}

			Carte carte = new Carte();
			carte.setPhotoUrl(photo.get("medium_url").asString());
			carte.setInatId(animal.get("id").asLong());
			carte.setNomScientifique(animal.get("name").asString());
			carte.setObservations(animal.get("observations_count").asInt());

			// certains animaux n'ont pas de nom francais : on garde le nom scientifique
			if (animal.has("preferred_common_name") && !animal.get("preferred_common_name").isNull()) {
				carte.setNom(animal.get("preferred_common_name").asString());
			} else {
				carte.setNom(carte.getNomScientifique());
			}

			String classe = "";
			if (animal.has("iconic_taxon_name") && !animal.get("iconic_taxon_name").isNull()) {
				classe = animal.get("iconic_taxon_name").asString();
			}
			carte.setType(traduireType(classe));

			cartes.add(carte);
		}
		return cartes;
	}

	// l'API donne la classe en latin
	public String traduireType(String classe) {
		switch (classe) {
			case "Mammalia":
				return "Mammifère";
			case "Aves":
				return "Oiseau";
			case "Reptilia":
				return "Reptile";
			case "Amphibia":
				return "Amphibien";
			case "Actinopterygii":
				return "Poisson";
			case "Insecta":
				return "Insecte";
			case "Arachnida":
				return "Arachnide";
			case "Mollusca":
				return "Mollusque";
			default:
				return "Autre";
		}
	}

}
