package services;

import entities.DossierMedical;
import entities.Utilisateur;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class ServiceDossierMedical implements IService<DossierMedical> {
    private Connection connection;

    public ServiceDossierMedical() throws SQLException {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(DossierMedical dossierMedical) throws SQLException {
        String query = "INSERT INTO dossier_medical (utilisateur_id, date, fichier, unite, mesure) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dossierMedical.getUtilisateurId());
            stmt.setDate(2, Date.valueOf(dossierMedical.getDate()));
            stmt.setString(3, dossierMedical.getFichier());
            stmt.setString(4, dossierMedical.getUnite());
            stmt.setDouble(5, dossierMedical.getMesure());
            stmt.executeUpdate();
        }
    }

    @Override
    public void modifier(DossierMedical dossierMedical) throws SQLException {
        String query = "UPDATE dossier_medical SET utilisateur_id = ?, date = ?, fichier = ?, unite = ?, mesure = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dossierMedical.getUtilisateurId());
            stmt.setDate(2, Date.valueOf(dossierMedical.getDate()));
            stmt.setString(3, dossierMedical.getFichier());
            stmt.setString(4, dossierMedical.getUnite());
            stmt.setDouble(5, dossierMedical.getMesure());
            stmt.setInt(6, dossierMedical.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM dossier_medical WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<DossierMedical> afficher() throws SQLException {
        List<DossierMedical> dossiers = new ArrayList<>();
        String query = "SELECT * FROM dossier_medical";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
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
        }
        return dossiers;
    }

    public DossierMedical getById(int id) throws SQLException {
        String query = "SELECT * FROM dossier_medical WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DossierMedical dossier = new DossierMedical();
                    dossier.setId(rs.getInt("id"));
                    dossier.setUtilisateurId(rs.getInt("utilisateur_id"));
                    dossier.setDate(rs.getDate("date").toLocalDate());
                    dossier.setFichier(rs.getString("fichier"));
                    dossier.setUnite(rs.getString("unite"));
                    dossier.setMesure(rs.getDouble("mesure"));
                    return dossier;
                }
            }
        }
        return null;
    }

    public DossierMedical getByUtilisateurId(int utilisateurId) throws SQLException {
        String query = "SELECT * FROM dossier_medical WHERE utilisateur_id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, utilisateurId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DossierMedical dossier = new DossierMedical();
                    dossier.setId(rs.getInt("id"));
                    dossier.setUtilisateurId(rs.getInt("utilisateur_id"));
                    dossier.setDate(rs.getDate("date").toLocalDate());
                    dossier.setFichier(rs.getString("fichier"));
                    dossier.setUnite(rs.getString("unite"));
                    dossier.setMesure(rs.getDouble("mesure"));
                    return dossier;
                }
            }
        }
        return null;
    }

    public Utilisateur getUtilisateurByDossierId(int dossierId) throws SQLException {
        System.out.println("Fetching Utilisateur for dossierId: " + dossierId);
        String query = "SELECT u.* FROM utilisateur u JOIN dossier_medical dm ON u.id = dm.utilisateur_id WHERE dm.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dossierId);
            System.out.println("Executing query: " + query + " with dossierId = " + dossierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int rawId = rs.getInt("id");
                    System.out.println("Raw ID from database: " + rawId);
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId(rawId);
                    utilisateur.setNom(rs.getString("nom"));
                    utilisateur.setPrenom(rs.getString("prenom"));
                    utilisateur.setEmail(rs.getString("email"));
                    utilisateur.setRoles(rs.getString("roles"));
                    utilisateur.setTelephone(rs.getInt("telephone"));
                    utilisateur.setAdresse(rs.getString("adresse"));
                    utilisateur.setDateNaissance(rs.getDate("date_naissance") != null ? rs.getDate("date_naissance").toLocalDate() : null);
                    utilisateur.setSexe(rs.getString("sexe"));
                    System.out.println("Utilisateur object after setting fields: " + utilisateur);
                    return utilisateur;
                } else {
                    System.out.println("No Utilisateur found for dossierId: " + dossierId);
                    return null;
                }
            }
        }
    }

    // Méthode modifiée pour récupérer les emails du médecin et du patient
    public String[] getDoctorAndPatientEmails(int dossierId, String doctorEmail) throws SQLException {
        String[] emails = new String[2]; // [0] pour l'email du patient, [1] pour l'email du médecin
        // Récupérer l'email du patient à partir de l'Utilisateur associé au DossierMedical
        String query = "SELECT u.email AS patient_email " +
                "FROM dossier_medical dm " +
                "JOIN utilisateur u ON dm.utilisateur_id = u.id " +
                "WHERE dm.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dossierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    emails[0] = rs.getString("patient_email"); // Email du patient
                    emails[1] = doctorEmail; // Email du médecin (utilisateur connecté)
                }
            }
        }
        return emails;
    }

    public int calculateAge(LocalDate dateNaissance) throws IllegalArgumentException {
        if (dateNaissance == null) {
            throw new IllegalArgumentException("La date de naissance ne peut pas être null.");
        }
        if (dateNaissance.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date de naissance ne peut pas être dans le futur : " + dateNaissance);
        }
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }
}