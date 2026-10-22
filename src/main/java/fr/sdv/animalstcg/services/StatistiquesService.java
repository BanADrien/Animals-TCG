package fr.sdv.animalstcg.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import fr.sdv.animalstcg.models.Carte;

// calculs sur la collection d'un joueur (en Java, pas besoin de la base ici)
@Service
public class StatistiquesService {

	// les raretes dans l'ordre, de la plus courante a la plus rare
	private static final String[] RARETES = { "Commune", "Peu commune", "Rare", "Légendaire" };

	// un Set ne garde pas les doublons : 2 cartes du meme animal = 1 espece
	public Set<Long> especesDifferentes(List<Carte> cartes) {
		Set<Long> especes = new HashSet<>();
		for (Carte carte : cartes) {
			especes.add(carte.getInatId());
		}
		return especes;
	}

	// type -> nombre de cartes (TreeMap pour avoir les types dans l'ordre alphabetique)
	public Map<String, Integer> compterParType(List<Carte> cartes) {
		Map<String, Integer> resultat = new TreeMap<>();
		for (Carte carte : cartes) {
			String type = carte.getType();
			resultat.put(type, resultat.getOrDefault(type, 0) + 1);
		}
		return resultat;
	}

	// rarete -> nombre de cartes, avec toutes les raretes meme a 0
	public Map<String, Integer> compterParRarete(List<Carte> cartes) {
		// LinkedHashMap garde l'ordre d'ajout des cles
		Map<String, Integer> resultat = new LinkedHashMap<>();
		for (String rarete : RARETES) {
			resultat.put(rarete, 0);
		}
		for (Carte carte : cartes) {
			String rarete = carte.getRarete();
			resultat.put(rarete, resultat.getOrDefault(rarete, 0) + 1);
		}
		return resultat;
	}

	// nom de l'animal -> nombre d'exemplaires, seulement pour ceux qu'on a en double
	public Map<String, Integer> doublons(List<Carte> cartes) {
		Map<String, Integer> exemplaires = new HashMap<>();
		for (Carte carte : cartes) {
			String nom = carte.getNom();
			exemplaires.put(nom, exemplaires.getOrDefault(nom, 0) + 1);
		}

		Map<String, Integer> resultat = new TreeMap<>();
		for (String nom : exemplaires.keySet()) {
			if (exemplaires.get(nom) >= 2) {
				resultat.put(nom, exemplaires.get(nom));
			}
		}
		return resultat;
	}

	// pour la longueur des barres sur la page (entre 0 et 100)
	public int pourcentage(int nombre, int total) {
		if (total == 0) {
			return 0;
		}
		return nombre * 100 / total;
	}

}
