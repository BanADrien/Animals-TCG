package fr.sdv.animalstcg.services;

import java.util.Random;

import org.springframework.stereotype.Service;

// le hasard du booster : d'abord la rarete, puis la page de l'API ou on va piocher
@Service
public class TirageService {

	private Random random = new Random();

	// tire un nombre entre 0 et 99 et en deduit la rarete
	public String tirerRarete() {
		return tirerRarete(random.nextInt(100));
	}

	// 60% Commune, 25% Peu commune, 12% Rare, 3% Legendaire
	public String tirerRarete(int nombre) {
		if (nombre < 60) {
			return "Commune";
		} else if (nombre < 85) {
			return "Peu commune";
		} else if (nombre < 97) {
			return "Rare";
		} else {
			return "Légendaire";
		}
	}

	// les animaux de l'API sont tries du plus observe au moins observe (50 par page, 200 pages max)
	// donc plus la page est loin, plus l'animal est rare
	public int pageMin(String rarete) {
		switch (rarete) {
			case "Commune":
				return 1;
			case "Peu commune":
				return 11;
			case "Rare":
				return 51;
			default:
				return 121;
		}
	}

	public int pageMax(String rarete) {
		switch (rarete) {
			case "Commune":
				return 10;
			case "Peu commune":
				return 50;
			case "Rare":
				return 120;
			default:
				return 200;
		}
	}

	// une page au hasard entre pageMin et pageMax (inclus)
	public int choisirPage(String rarete) {
		int min = pageMin(rarete);
		int max = pageMax(rarete);
		return min + random.nextInt(max - min + 1);
	}

}
