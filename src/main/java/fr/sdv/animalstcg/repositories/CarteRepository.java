package fr.sdv.animalstcg.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.models.Joueur;

public interface CarteRepository extends JpaRepository<Carte, Long> {

	List<Carte> findByJoueur(Joueur joueur);

	// la collection, les dernieres cartes obtenues en premier
	List<Carte> findByJoueurOrderByDateObtentionDesc(Joueur joueur);

	List<Carte> findByJoueurAndNomContainingIgnoreCase(Joueur joueur, String nom);

	List<Carte> findByJoueurAndType(Joueur joueur, String type);

	List<Carte> findByJoueurAndRarete(Joueur joueur, String rarete);

	List<Carte> findByJoueurAndFavoriTrue(Joueur joueur);

	// toutes les cartes du meme animal (pour savoir si c'est un doublon)
	List<Carte> findByJoueurAndInatId(Joueur joueur, Long inatId);

	long countByJoueur(Joueur joueur);

	// cherche dans le nom francais et dans le nom scientifique
	@Query("SELECT c FROM Carte c WHERE c.joueur = :joueur AND (LOWER(c.nom) LIKE LOWER(CONCAT('%', :texte, '%')) OR LOWER(c.nomScientifique) LIKE LOWER(CONCAT('%', :texte, '%')))")
	List<Carte> chercherParNom(@Param("joueur") Joueur joueur, @Param("texte") String texte);

}
