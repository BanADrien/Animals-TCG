
package fr.sdv.animalstcg.services;

import fr.sdv.animalstcg.models.Joueur;
import fr.sdv.animalstcg.repositories.CarteRepository;
import fr.sdv.animalstcg.repositories.JoueurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JoueurService {

    private final JoueurRepository joueurRepository;
    private final CarteRepository carteRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final int piecesDepart;

    // Spring fournit automatiquement les repositories et la configuration.
    public JoueurService(
            JoueurRepository joueurRepository,
            CarteRepository carteRepository,
            @Value("${jeu.pieces.depart}") int piecesDepart) {
        this.joueurRepository = joueurRepository;
        this.carteRepository = carteRepository;
        this.piecesDepart = piecesDepart;
    }

    // Crée un compte avec un pseudo unique et un mot de passe haché.
    public Joueur inscrire(String pseudo, String motDePasse) {
        verifierChamps(pseudo, motDePasse);
        pseudo = pseudo.trim();

        if (joueurRepository.existsByPseudo(pseudo)) {
            throw new IllegalArgumentException("Ce pseudo est déjà utilisé.");
        }

        String hash = encoder.encode(motDePasse);
        Joueur joueur = new Joueur(pseudo, hash, piecesDepart);

        return joueurRepository.save(joueur);
    }

    // Renvoie le joueur si ses identifiants sont corrects.
    public Joueur connecter(String pseudo, String motDePasse) {
        verifierChamps(pseudo, motDePasse);

        Joueur joueur = joueurRepository.findByPseudo(pseudo.trim());

        if (joueur == null ||
                !encoder.matches(motDePasse, joueur.getMotDePasse())) {
            throw new IllegalArgumentException(
                    "Pseudo ou mot de passe incorrect.");
        }

        return joueur;
    }

    public Joueur trouverParId(Long id) {
        return joueurRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Joueur introuvable."));
    }

    // Modifie le pseudo en vérifiant qu'il n'est pas déjà pris.
    public Joueur modifierPseudo(Long id, String nouveauPseudo) {
        if (nouveauPseudo == null || nouveauPseudo.isBlank()) {
            throw new IllegalArgumentException("Le pseudo est obligatoire.");
        }

        Joueur joueur = trouverParId(id);
        nouveauPseudo = nouveauPseudo.trim();

        if (!joueur.getPseudo().equals(nouveauPseudo)
                && joueurRepository.existsByPseudo(nouveauPseudo)) {
            throw new IllegalArgumentException("Ce pseudo est déjà utilisé.");
        }

        joueur.setPseudo(nouveauPseudo);
        return joueurRepository.save(joueur);
    }

    // Vérifie l'ancien mot de passe avant de le remplacer.
    public void modifierMotDePasse(
            Long id, String ancienMotDePasse, String nouveauMotDePasse) {

        verifierChamps(ancienMotDePasse, nouveauMotDePasse);

        Joueur joueur = trouverParId(id);

        if (!encoder.matches(ancienMotDePasse, joueur.getMotDePasse())) {
            throw new IllegalArgumentException(
                    "L'ancien mot de passe est incorrect.");
        }

        joueur.setMotDePasse(encoder.encode(nouveauMotDePasse));
        joueurRepository.save(joueur);
    }

    // Supprime les cartes avant de supprimer leur propriétaire.
    @Transactional
    public void supprimerCompte(Long id) {
        Joueur joueur = trouverParId(id);
        carteRepository.deleteAll(carteRepository.findByJoueur(joueur));
        joueurRepository.delete(joueur);
    }

    private void verifierChamps(String pseudo, String motDePasse) {
        if (pseudo == null || pseudo.isBlank()
                || motDePasse == null || motDePasse.isBlank()) {
            throw new IllegalArgumentException(
                    "Tous les champs sont obligatoires.");
        }
    }
}