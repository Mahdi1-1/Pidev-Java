package tests.Patient;

import entities.Ordonnance;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PatientOrdonnanceDetailsController implements Initializable {

    @FXML private Label labelId;
    @FXML private Label labelDate;
    @FXML private Label labelMedecin;
    @FXML private TextArea textDescription;
    @FXML private ImageView imgSignature;
    @FXML private Button btnDownloadPdf;
    @FXML private Button btnClose;
    
    private Ordonnance ordonnance;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Nothing specific to initialize here
    }
    
    public void setOrdonnance(Ordonnance ordonnance) {
        this.ordonnance = ordonnance;
        
        if (ordonnance != null) {
            updateUI();
        }
    }
    
    private void updateUI() {
        labelId.setText(ordonnance.getId().toString());
        
        // Format date from consultation
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        labelDate.setText(ordonnance.getConsultation().getDateC().format(formatter));
        
        // Set medecin info from consultation
        labelMedecin.setText(
            ordonnance.getConsultation().getMedecin().getNom() + " " + 
            ordonnance.getConsultation().getMedecin().getPrenom()
        );
        
        // Set prescription description
        textDescription.setText(ordonnance.getDescription());
        textDescription.setEditable(false);
        
        // Set signature image if available
        if (ordonnance.getSignature() != null && !ordonnance.getSignature().isEmpty()) {
            try {
                Image signatureImage = new Image(ordonnance.getSignature());
                imgSignature.setImage(signatureImage);
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de la signature: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleDownloadPdf() {
        // Create content for printing
        VBox contentToPrint = createPrintContent();
        
        // Create a PrinterJob
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            // Show printer dialog
            boolean proceed = job.showPrintDialog(btnDownloadPdf.getScene().getWindow());
            
            if (proceed) {
                // Print the node
                boolean printed = job.printPage(contentToPrint);
                
                if (printed) {
                    job.endJob();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Impression réussie", 
                        "L'ordonnance a été envoyée à l'imprimante avec succès.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Échec de l'impression", 
                        "L'impression a échoué. Veuillez réessayer.");
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                "Imprimante non disponible", 
                "Aucune imprimante n'est disponible sur votre système.");
        }
    }
    
    private VBox createPrintContent() {
        VBox content = new VBox(20);
        content.setPrefWidth(595); // A4 width in points
        content.setPrefHeight(842); // A4 height in points
        content.setStyle("-fx-padding: 50; -fx-background-color: white;");
        
        // Title
        Text title = new Text("ORDONNANCE MÉDICALE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        content.getChildren().add(title);
        
        // Separator line
        Separator separator1 = new Separator();
        content.getChildren().add(separator1);
        
        // Doctor info
        Text doctorInfo = new Text("Docteur: " + ordonnance.getConsultation().getMedecin().getNom() + 
                                  " " + ordonnance.getConsultation().getMedecin().getPrenom());
        doctorInfo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        content.getChildren().add(doctorInfo);
        
        // Date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Text dateText = new Text("Date: " + ordonnance.getConsultation().getDateC().format(formatter));
        dateText.setFont(Font.font("Arial", 14));
        content.getChildren().add(dateText);
        
        // Patient info
        Text patientInfo = new Text("Patient: " + ordonnance.getConsultation().getPatient().getNom() + 
                                   " " + ordonnance.getConsultation().getPatient().getPrenom());
        patientInfo.setFont(Font.font("Arial", 14));
        content.getChildren().add(patientInfo);
        
        // Separator line
        Separator separator2 = new Separator();
        content.getChildren().add(separator2);
        
        // Prescription content
        Text prescriptionTitle = new Text("Prescription:");
        prescriptionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        content.getChildren().add(prescriptionTitle);
        
        TextArea prescriptionText = new TextArea(ordonnance.getDescription());
        prescriptionText.setWrapText(true);
        prescriptionText.setEditable(false);
        prescriptionText.setPrefHeight(300);
        prescriptionText.setStyle("-fx-font-family: Arial; -fx-font-size: 14;");
        content.getChildren().add(prescriptionText);
        
        // Signature
        Text signatureText = new Text("Signature du médecin");
        signatureText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        signatureText.setTranslateX(350);
        signatureText.setTranslateY(50);
        content.getChildren().add(signatureText);
        
        // Footer
        Text footer = new Text("Ordonnance générée par l'application TBibi - ID: " + ordonnance.getId());
        footer.setFont(Font.font("Arial", 12));
        footer.setTranslateY(50);
        content.getChildren().add(footer);
        
        return content;
    }
    
    @FXML
    private void handleClose() {
        ((Stage) btnClose.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 