package fr.sdv.animalstcg;

import java.util.ArrayList;
import java.util.List;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.services.InaturalistService;

// faux service pour les tests : renvoie des animaux inventes au lieu d'appeler l'API
public class FauxInaturalistService extends InaturalistService {

	private boolean toujoursLeMeme;
	private int nombreAppels = 0;

	// toujoursLeMeme = true : chaque page ne contient qu'un seul animal, toujours le meme
	public FauxInaturalistService(boolean toujoursLeMeme) {
		super("http://adresse-pas-utilisee");
		this.toujoursLeMeme = toujoursLeMeme;
	}

	@Override
	public List<Carte> chercherPage(int page) {
		nombreAppels++;
		List<Carte> animaux = new ArrayList<>();

		if (toujoursLeMeme) {
			animaux.add(creerAnimal(1L, "Toujours le meme"));
			return animaux;
		}

		// 10 animaux differents par page
		for (int i = 0; i < 10; i++) {
			animaux.add(creerAnimal((long) (page * 100 + i), "Animal " + page + "-" + i));
		}
		return animaux;
	}

	public int getNombreAppels() {
		return nombreAppels;
	}

	private Carte creerAnimal(Long inatId, String nom) {
		Carte carte = new Carte();
		carte.setInatId(inatId);
		carte.setNom(nom);
		carte.setType("Oiseau");
		carte.setPhotoUrl("http://photo");
		return carte;
	}

}
