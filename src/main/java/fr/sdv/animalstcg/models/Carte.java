package fr.sdv.animalstcg.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// une ligne = une carte obtenue par un joueur (si on tire 2 fois le meme animal ca fait 2 cartes)
@Entity
public class Carte {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// id de l'animal dans iNaturalist
	private Long inatId;

	private String nom;
	private String nomScientifique;

	// Mammifere, Oiseau, Reptile...
	private String type;

	// Commune, Peu commune, Rare ou Legendaire
	private String rarete;

	private String photoUrl;
	private int observations;
	private boolean favori;
	private LocalDateTime dateObtention;

	@ManyToOne
	@JoinColumn(name = "joueur_id")
	private Joueur joueur;

	public Carte() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getInatId() {
		return inatId;
	}

	public void setInatId(Long inatId) {
		this.inatId = inatId;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getNomScientifique() {
		return nomScientifique;
	}

	public void setNomScientifique(String nomScientifique) {
		this.nomScientifique = nomScientifique;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRarete() {
		return rarete;
	}

	public void setRarete(String rarete) {
		this.rarete = rarete;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public int getObservations() {
		return observations;
	}

	public void setObservations(int observations) {
		this.observations = observations;
	}

	public boolean isFavori() {
		return favori;
	}

	public void setFavori(boolean favori) {
		this.favori = favori;
	}

	public LocalDateTime getDateObtention() {
		return dateObtention;
	}

	public void setDateObtention(LocalDateTime dateObtention) {
		this.dateObtention = dateObtention;
	}

	public Joueur getJoueur() {
		return joueur;
	}

	public void setJoueur(Joueur joueur) {
		this.joueur = joueur;
	}

}
