// src/main/java/services/ServiceDossierMedical.java
package services;

import entities.DossierMedical;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceDossierMedical implements IService<DossierMedical> {
    private Connection connection;

    public ServiceDossierMedical() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(DossierMedical dossier) throws SQLException {
        String req = "INSERT INTO dossier_medical (utilisateur_id, date, fichier, unite, mesure) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, dossier.getUtilisateurId());
        ps.setDate(2, Date.valueOf(dossier.getDate()));
        ps.setString(3, dossier.getFichier());
        ps.setString(4, dossier.getUnite());
        ps.setDouble(5, dossier.getMesure());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            dossier.setId(rs.getInt(1)); // Récupère et assigne l’ID généré
            System.out.println("Dossier médical ajouté avec ID : " + dossier.getId());
        } else {
            throw new SQLException("Échec de récupération de l'ID du dossier médical");
        }
    }

    @Override
    public void modifier(DossierMedical dossier) throws SQLException {
        String req = "UPDATE dossier_medical SET utilisateur_id=?, date=?, fichier=?, unite=?, mesure=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, dossier.getUtilisateurId());
        ps.setDate(2, Date.valueOf(dossier.getDate()));
        ps.setString(3, dossier.getFichier());
        ps.setString(4, dossier.getUnite());
        ps.setDouble(5, dossier.getMesure());
        ps.setInt(6, dossier.getId());
        ps.executeUpdate();
        System.out.println("Dossier médical modifié");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM dossier_medical WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Dossier médical supprimé");
    }

    @Override
    public List<DossierMedical> afficher() throws SQLException {
        List<DossierMedical> dossiers = new ArrayList<>();
        String req = "SELECT * FROM dossier_medical";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(req);
        while (rs.next()) {
            DossierMedical dossier = new DossierMedical();
            dossier.setId(rs.getInt("id"));
            dossier.setUtilisateurId(rs.getInt("utilisateur_id"));
            dossier.setDate(rs.getDate("date").toLocalDate());
            dossier.setFichier(rs.getString("fichier"));
            dossier.setUnite(rs.getString("unite"));
            dossier.setMesure(rs.getDouble("mesure"));
            dossiers.add(dossier);
        }
        return dossiers;
    }
}