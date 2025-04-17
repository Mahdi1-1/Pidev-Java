package entities;

import enums.TypeVote;

public class Vote {
    private Integer id;
    private Utilisateur medecin;
    private Reponse reponse;
    private TypeVote valeur;

    // Constructors
    public Vote() {
    }

    public Vote(Utilisateur medecin, Reponse reponse, TypeVote valeur) {
        this.medecin = medecin;
        this.reponse = reponse;
        this.valeur = valeur;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Utilisateur getMedecin() {
        return medecin;
    }

    public void setMedecin(Utilisateur medecin) {
        this.medecin = medecin;
    }

    public Reponse getReponse() {
        return reponse;
    }

    public void setReponse(Reponse reponse) {
        this.reponse = reponse;
    }

    public TypeVote getValeur() {
        return valeur;
    }

    public void setValeur(TypeVote valeur) {
        this.valeur = valeur;
    }
}