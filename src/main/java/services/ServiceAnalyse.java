// src/main/java/services/ServiceAnalyse.java
package services;

import entities.Analyse;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceAnalyse implements IService<Analyse> {
    private Connection connection;

    public ServiceAnalyse() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Analyse analyse) throws SQLException {
        String req = "INSERT INTO analyse (dossier_id, type, dateanalyse, donnees_analyse, diagnostic) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, analyse.getDossierId()); // Assurez-vous que dossierId est valide
        ps.setString(2, analyse.getType());
        ps.setDate(3, Date.valueOf(analyse.getDateAnalyse()));
        ps.setString(4, analyse.getDonneesAnalyse());
        ps.setString(5, analyse.getDiagnostic());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            analyse.setId(rs.getInt(1));
            System.out.println("Analyse ajoutée avec ID : " + analyse.getId());
        } else {
            throw new SQLException("Échec de récupération de l'ID de l'analyse");
        }
    }

    @Override
    public void modifier(Analyse analyse) throws SQLException {
        String req = "UPDATE analyse SET dossier_id=?, type=?, dateanalyse=?, donnees_analyse=?, diagnostic=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, analyse.getDossierId());
        ps.setString(2, analyse.getType());
        ps.setDate(3, Date.valueOf(analyse.getDateAnalyse()));
        ps.setString(4, analyse.getDonneesAnalyse());
        ps.setString(5, analyse.getDiagnostic());
        ps.setInt(6, analyse.getId());
        ps.executeUpdate();
        System.out.println("Analyse modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM analyse WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Analyse supprimée");
    }

    @Override
    public List<Analyse> afficher() throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String req = "SELECT * FROM analyse";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(req);
        while (rs.next()) {
            Analyse analyse = new Analyse();
            analyse.setId(rs.getInt("id"));
            analyse.setDossierId(rs.getInt("dossier_id"));
            analyse.setType(rs.getString("type"));
            analyse.setDateAnalyse(rs.getDate("dateanalyse").toLocalDate());
            analyse.setDonneesAnalyse(rs.getString("donnees_analyse"));
            analyse.setDiagnostic(rs.getString("diagnostic"));
            analyses.add(analyse);
        }
        return analyses;
    }
}