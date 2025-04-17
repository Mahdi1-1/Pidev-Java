package utils;

public class UtilisateurStatique {
    private static int utilisateurId = -1; // Valeur par défaut : -1 (aucun utilisateur)

    public static void setUtilisateurId(int id) {
        utilisateurId = id;
    }

    public static int getUtilisateurId() {
        return utilisateurId;
    }
}