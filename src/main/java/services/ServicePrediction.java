package services;

import entities.Prediction;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePrediction implements IService<Prediction> {
    private Connection connection;

    public ServicePrediction() throws SQLException {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Prediction prediction) throws SQLException {
        String query = "INSERT INTO prediction (dossier_id, hypertension, heart_disease, smoking_history, bmi, hb_a1c_level, blood_glucose_level, diabete) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, prediction.getDossierId());
            stmt.setBoolean(2, prediction.isHypertension());
            stmt.setBoolean(3, prediction.isheart_disease());
            stmt.setString(4, prediction.getsmoking_history());
            stmt.setFloat(5, prediction.getBmi());
            stmt.setFloat(6, prediction.gethbA1c_level());
            stmt.setFloat(7, prediction.getBloodGlucoseLevel());
            stmt.setBoolean(8, prediction.isDiabete());
            stmt.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    prediction.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(Prediction prediction) throws SQLException {
        String query = "UPDATE prediction SET dossier_id = ?, hypertension = ?, heart_disease = ?, smoking_history = ?, " +
                "bmi = ?, hba1c_level = ?, blood_glucose_level = ?, diabete = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, prediction.getDossierId());
            stmt.setBoolean(2, prediction.isHypertension());
            stmt.setBoolean(3, prediction.isheart_disease());
            stmt.setString(4, prediction.getsmoking_history());
            stmt.setFloat(5, prediction.getBmi());
            stmt.setFloat(6, prediction.gethbA1c_level());
            stmt.setFloat(7, prediction.getBloodGlucoseLevel());
            stmt.setBoolean(8, prediction.isDiabete());
            stmt.setInt(9, prediction.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM prediction WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Prediction> afficher() throws SQLException {
        List<Prediction> predictions = new ArrayList<>();
        String query = "SELECT * FROM prediction";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Prediction prediction = new Prediction();
                prediction.setId(rs.getInt("id"));
                prediction.setDossierId(rs.getInt("dossier_id"));
                prediction.setHypertension(rs.getBoolean("hypertension"));
                prediction.setheart_disease(rs.getBoolean("heart_disease"));
                prediction.setsmoking_history(rs.getString("smoking_history"));
                prediction.setBmi(rs.getFloat("bmi"));
                prediction.sethbA1c_level(rs.getFloat("hba1c_level"));
                prediction.setBloodGlucoseLevel(rs.getFloat("blood_glucose_level"));
                prediction.setDiabete(rs.getBoolean("diabete"));
                predictions.add(prediction);
            }
        }
        return predictions;
    }

    // Méthode pour récupérer une prédiction par ID
    public Prediction getById(int id) throws SQLException {
        String query = "SELECT * FROM prediction WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Prediction prediction = new Prediction();
                    prediction.setId(rs.getInt("id"));
                    prediction.setDossierId(rs.getInt("dossier_id"));
                    prediction.setHypertension(rs.getBoolean("hypertension"));
                    prediction.setheart_disease(rs.getBoolean("heart_disease"));
                    prediction.setsmoking_history(rs.getString("smoking_history"));
                    prediction.setBmi(rs.getFloat("bmi"));
                    prediction.sethbA1c_level(rs.getFloat("hba1c_level"));
                    prediction.setBloodGlucoseLevel(rs.getFloat("blood_glucose_level"));
                    prediction.setDiabete(rs.getBoolean("diabete"));
                    return prediction;
                }
            }
        }
        return null;
    }

    // Méthode pour récupérer toutes les prédictions d'un dossier
    public List<Prediction> getByDossierId(int dossierId) throws SQLException {
        List<Prediction> predictions = new ArrayList<>();
        String query = "SELECT * FROM prediction WHERE dossier_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dossierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prediction prediction = new Prediction();
                    prediction.setId(rs.getInt("id"));
                    prediction.setDossierId(rs.getInt("dossier_id"));
                    prediction.setHypertension(rs.getBoolean("hypertension"));
                    prediction.setheart_disease(rs.getBoolean("heart_disease"));
                    prediction.setsmoking_history(rs.getString("smoking_history"));
                    prediction.setBmi(rs.getFloat("bmi"));
                    prediction.sethbA1c_level(rs.getFloat("hb_a1c_level"));
                    prediction.setBloodGlucoseLevel(rs.getFloat("blood_glucose_level"));
                    prediction.setDiabete(rs.getBoolean("diabete"));
                    predictions.add(prediction);
                }
            }
        }
        return predictions;
    }
}