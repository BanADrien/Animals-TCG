
package fr.sdv.animalstcg.controllers;

import fr.sdv.animalstcg.models.Carte;
import fr.sdv.animalstcg.models.Joueur;
import fr.sdv.animalstcg.repositories.CarteRepository;
import fr.sdv.animalstcg.repositories.JoueurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Controller
public class CollectionController {

    private final CarteRepository carteRepository;
    private final JoueurRepository joueurRepository;
    private final int prixVenteDoublon;

    public CollectionController(
            CarteRepository carteRepository,
            JoueurRepository joueurRepository,
            @Value("${jeu.vente.doublon}") int prixVenteDoublon) {
        this.carteRepository = carteRepository;
        this.joueurRepository = joueurRepository;
        this.prixVenteDoublon = prixVenteDoublon;
    }

    // Affiche uniquement les cartes du joueur connecté.
    @GetMapping("/collection")
    public String collection(
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String rarete,
            @RequestParam(defaultValue = "false") boolean favoris,
            HttpSession session,
            Model model) {

        Long joueurId = (Long) session.getAttribute("joueurId");
        if (joueurId == null) {
            return "redirect:/connexion";
        }

        Optional<Joueur> resultat = joueurRepository.findById(joueurId);
        if (resultat.isEmpty()) {
            session.invalidate();
            return "redirect:/connexion";
        }

        Joueur joueur = resultat.get();

        List<Carte> cartes = carteRepository
                .findByJoueurOrderByDateObtentionDesc(joueur);

        // On applique les filtres les uns après les autres.
        if (recherche != null && !recherche.isBlank()) {
            String texte = recherche.trim().toLowerCase();
            cartes = cartes.stream()
                    .filter(c ->
                            (c.getNom() != null
                                    && c.getNom().toLowerCase().contains(texte))
                                    || (c.getNomScientifique() != null
                                    && c.getNomScientifique().toLowerCase().contains(texte)))
                    .toList();
        }

        if (type != null && !type.isBlank()) {
            cartes = cartes.stream()
                    .filter(c -> type.equals(c.getType()))
                    .toList();
        }

        if (rarete != null && !rarete.isBlank()) {
            cartes = cartes.stream()
                    .filter(c -> rarete.equals(c.getRarete()))
                    .toList();
        }

        if (favoris) {
            cartes = cartes.stream()
                    .filter(Carte::isFavori)
                    .toList();
        }

        model.addAttribute("cartes", cartes);
        model.addAttribute("total", carteRepository.countByJoueur(joueur));
        model.addAttribute("joueur", joueur);
        model.addAttribute("recherche", recherche);
        model.addAttribute("typeChoisi", type);
        model.addAttribute("rareteChoisie", rarete);
        model.addAttribute("favorisChoisis", favoris);

        return "collection";
    }

    // Affiche le détail d'une carte appartenant au joueur.
    @GetMapping("/collection/{id}")
    public String detail(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Long joueurId = (Long) session.getAttribute("joueurId");
        if (joueurId == null) {
            return "redirect:/connexion";
        }

        Carte carte = carteRepository.findById(id).orElse(null);

        if (carte == null || carte.getJoueur() == null
                || !joueurId.equals(carte.getJoueur().getId())) {
            return "redirect:/collection";
        }

        Joueur joueur = carte.getJoueur();

        boolean doublon = carte.getInatId() != null
                && carteRepository.findByJoueurAndInatId(
                joueur, carte.getInatId()).size() > 1;

        model.addAttribute("carte", carte);
        model.addAttribute("doublon", doublon);
        model.addAttribute("prixVente", prixVenteDoublon);

        return "carte";
    }

    // Ajoute ou retire une carte des favoris.
    @PostMapping("/collection/{id}/favori")
    public String changerFavori(
            @PathVariable Long id,
            HttpSession session) {

        Long joueurId = (Long) session.getAttribute("joueurId");
        if (joueurId == null) {
            return "redirect:/connexion";
        }

        Carte carte = carteRepository.findById(id).orElse(null);

        if (carte == null || carte.getJoueur() == null
                || !joueurId.equals(carte.getJoueur().getId())) {
            return "redirect:/collection";
        }

        carte.setFavori(!carte.isFavori());
        carteRepository.save(carte);

        return "redirect:/collection/" + id;
    }

    // La transaction garantit la cohérence entre la vente et les pièces.
    @Transactional
    @PostMapping("/collection/{id}/vendre")
    public String vendre(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long joueurId = (Long) session.getAttribute("joueurId");
        if (joueurId == null) {
            return "redirect:/connexion";
        }

        Carte carte = carteRepository.findById(id).orElse(null);

        if (carte == null || carte.getJoueur() == null
                || !joueurId.equals(carte.getJoueur().getId())) {
            return "redirect:/collection";
        }

        Joueur joueur = carte.getJoueur();

        // Impossible de vendre son dernier exemplaire.
        if (carte.getInatId() == null
                || carteRepository.findByJoueurAndInatId(
                joueur, carte.getInatId()).size() <= 1) {
            redirectAttributes.addFlashAttribute(
                    "erreur", "Cette carte n'est pas un doublon.");
            return "redirect:/collection/" + id;
        }

        carteRepository.delete(carte);
        joueur.setPieces(joueur.getPieces() + prixVenteDoublon);
        joueurRepository.save(joueur);

        redirectAttributes.addFlashAttribute(
                "message", "Carte vendue pour " + prixVenteDoublon + " pièces !");

        return "redirect:/collection";
    }
}