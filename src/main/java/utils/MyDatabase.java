// src/main/java/utils/MyDatabase.java
package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    final String URL = "jdbc:mysql://127.0.0.1:3306/tbibi_integration";
    final String USERNAME = "root";
    final String PASSWORD = ""; // Pas de mot de passe selon votre URL
    private Connection connection;
    private static MyDatabase instance;

    private MyDatabase() {
        try {
            // Forcer le chargement du pilote JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion établie à MySQL");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            throw new RuntimeException("Impossible de se connecter à la base de données. Vérifiez que le serveur MySQL est en cours d'exécution.", e);
        } catch (ClassNotFoundException e) {
            System.err.println("Pilote JDBC introuvable : " + e.getMessage());
            throw new RuntimeException("Pilote JDBC MySQL introuvable. Vérifiez vos dépendances.", e);
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Vérifier si la connexion est fermée ou invalide
            if (connection == null || connection.isClosed()) {
                // Tenter de rétablir la connexion
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Reconnexion établie à MySQL");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification/reconnexion à la base de données : " + e.getMessage());
            throw new RuntimeException("Impossible de se connecter à la base de données. Vérifiez que le serveur MySQL est en cours d'exécution.", e);
        }
        return connection;
    }
}