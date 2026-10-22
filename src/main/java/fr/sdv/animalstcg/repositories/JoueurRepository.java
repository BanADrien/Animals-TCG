package fr.sdv.animalstcg.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sdv.animalstcg.models.Joueur;

public interface JoueurRepository extends JpaRepository<Joueur, Long> {

	// renvoie null si le pseudo n'existe pas
	Joueur findByPseudo(String pseudo);

	boolean existsByPseudo(String pseudo);

}
