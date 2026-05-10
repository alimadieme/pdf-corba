import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.animation.*;
import javafx.util.Duration;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class SenPDF extends Application {

    private static final String NAVY  = "#0A1628";
    private static final String NAVY2 = "#112240";
    private static final String BLUE  = "#1565C0";
    private static final String BLUE2 = "#1976D2";
    private static final String GREEN = "#2E7D32";
    private static final String GREEN2= "#388E3C";
    private static final String BG    = "#F5F7FA";
    private static final String WHITE = "#FFFFFF";
    private static final String GRAY  = "#78909C";
    private static final String GRAY2 = "#ECEFF1";
    private static final String DARK  = "#1A237E";

    private File fichierSelectionne1;
    private File fichierSelectionne2;
    private Label labelFichier1;
    private Label labelFichier2;
    private Label labelResultat;
    private Label labelStatut;
    private VBox contentArea;
    private String operationActive = "fusion";

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setTop(creerHeader());
        root.setLeft(creerSidebar());
        contentArea = new VBox(20);
        contentArea.setPadding(new Insets(30));
        contentArea.setStyle("-fx-background-color: " + BG + ";");
        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG + "; -fx-border-color: transparent;");
        root.setCenter(scroll);
        root.setBottom(creerFooter());
        afficherOperation("fusion");
        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("SenPDF — La plateforme PDF du Senegal");
        stage.setScene(scene);
        stage.show();
        FadeTransition ft = new FadeTransition(Duration.millis(600), root);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private HBox creerHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + NAVY + "; -fx-padding: 0 30;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(64);
        HBox logo = new HBox(6);
        logo.setAlignment(Pos.CENTER_LEFT);
        Label t1 = new Label("Sen");
        t1.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label t2 = new Label("PDF");
        t2.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4FC3F7;");
        logo.getChildren().addAll(t1, t2);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        labelStatut = new Label("Connecte");
        labelStatut.setStyle("-fx-text-fill: #69F0AE; -fx-font-size: 12px;");
        Label user = new Label("Utilisateur");
        user.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 13px; -fx-padding: 0 0 0 20;");
        header.getChildren().addAll(logo, spacer, labelStatut, user);
        return header;
    }

    private VBox creerSidebar() {
        VBox sidebar = new VBox(2);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(16, 10, 16, 10));
        sidebar.setStyle("-fx-background-color: " + NAVY2 + ";");
        Label titre = new Label("OUTILS PDF");
        titre.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #78909C; -fx-padding: 8 12 12 12;");
        sidebar.getChildren().add(titre);
        String[][] outils = {
            {"Fusionner PDF",   "fusion"},
            {"Decouper PDF",    "decoupage"},
            {"Extraire pages",  "extrairePages"},
            {"Supprimer pages", "supprimerPages"},
            {"Proteger PDF",    "motDePasse"},
            {"PDF vers Image",  "convertirImage"},
            {"Extraire texte",  "extraireTexte"},
            {"Creer PDF",       "creerPdf"}
        };
        for (String[] o : outils) sidebar.getChildren().add(creerBoutonSidebar(o[0], o[1]));
        return sidebar;
    }

    private Button creerBoutonSidebar(String texte, String op) {
        Button btn = new Button(texte);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(11, 16, 11, 16));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #B0BEC5; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-radius: 8;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #1A3A5C; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-radius: 8;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #B0BEC5; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-radius: 8;"));
        btn.setOnAction(e -> { operationActive = op; afficherOperation(op); });
        return btn;
    }

    private HBox creerFooter() {
        HBox footer = new HBox();
        footer.setStyle("-fx-background-color: " + NAVY + "; -fx-padding: 10 30;");
        footer.setAlignment(Pos.CENTER);
        Label text = new Label("SenPDF 2026");
        text.setStyle("-fx-text-fill: #546E7A; -fx-font-size: 12px;");
        footer.getChildren().add(text);
        return footer;
    }

    private void afficherOperation(String op) {
        contentArea.getChildren().clear();
        fichierSelectionne1 = null;
        fichierSelectionne2 = null;
        String[] info = getTitreOperation(op);

        VBox titreCard = new VBox(4);
        titreCard.setPadding(new Insets(20, 24, 20, 24));
        titreCard.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12; -fx-border-color: " + GRAY2 + "; -fx-border-radius: 12; -fx-border-width: 0 0 0 4; -fx-border-color: " + BLUE + ";");
        Label titre = new Label(info[0]);
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + BLUE + ";");
        Label desc = new Label(info[1]);
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: " + GRAY + ";");
        titreCard.getChildren().addAll(titre, desc);

        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12; -fx-border-color: " + GRAY2 + "; -fx-border-radius: 12;");

        Label lblUpload1 = new Label("Fichier PDF");
        lblUpload1.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + DARK + ";");
        VBox uploadZone1 = creerZoneUpload(1);
        labelFichier1 = new Label("Aucun fichier selectionne");
        labelFichier1.setStyle("-fx-text-fill: " + GRAY + "; -fx-font-size: 12px;");
        card.getChildren().addAll(lblUpload1, uploadZone1, labelFichier1);

        switch (op) {
            case "fusion":
                Label lbl2 = new Label("Deuxieme fichier PDF");
                lbl2.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + DARK + ";");
                VBox upload2 = creerZoneUpload(2);
                labelFichier2 = new Label("Aucun fichier selectionne");
                labelFichier2.setStyle("-fx-text-fill: " + GRAY + "; -fx-font-size: 12px;");
                card.getChildren().addAll(lbl2, upload2, labelFichier2);
                break;
            case "decoupage": case "extrairePages":
                TextField pd = creerChamp("Page debut (ex: 1)"); pd.setId("pageDebut");
                TextField pf = creerChamp("Page fin (ex: 3)"); pf.setId("pageFin");
                HBox pb = new HBox(12, pd, pf);
                HBox.setHgrow(pd, Priority.ALWAYS); HBox.setHgrow(pf, Priority.ALWAYS);
                card.getChildren().add(pb);
                break;
            case "supprimerPages":
                TextField p = creerChamp("ex: 2,4,6"); p.setId("pages");
                card.getChildren().add(p);
                break;
            case "motDePasse":
                PasswordField m = new PasswordField();
                m.setPromptText("Mot de passe");
                m.setStyle(styleChamp()); m.setId("mdp");
                card.getChildren().add(m);
                break;
            case "creerPdf":
                TextArea ta = new TextArea();
                ta.setPromptText("Contenu du PDF...");
                ta.setPrefRowCount(5);
                ta.setStyle(styleChamp()); ta.setId("contenu");
                card.getChildren().add(ta);
                break;
        }

        Button btnExec = new Button("Executer");
        btnExec.setMaxWidth(Double.MAX_VALUE);
        btnExec.setPrefHeight(44);
        btnExec.setStyle("-fx-background-color: " + GREEN + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnExec.setOnMouseEntered(e -> btnExec.setStyle("-fx-background-color: " + GREEN2 + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnExec.setOnMouseExited(e -> btnExec.setStyle("-fx-background-color: " + GREEN + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnExec.setOnAction(e -> executerOperation(op));
        labelResultat = new Label("");
        labelResultat.setWrapText(true);
        labelResultat.setStyle("-fx-font-size: 13px;");
        card.getChildren().addAll(btnExec, labelResultat);
        contentArea.getChildren().addAll(titreCard, card);
    }

    private VBox creerZoneUpload(int num) {
        VBox zone = new VBox(8);
        zone.setAlignment(Pos.CENTER);
        zone.setPrefHeight(100);
        zone.setMaxWidth(Double.MAX_VALUE);
        zone.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #B0BEC5; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;");
        Label lbl = new Label("Cliquez pour selectionner un PDF");
        lbl.setStyle("-fx-text-fill: " + GRAY + "; -fx-font-size: 13px;");
        zone.getChildren().add(lbl);
        zone.setOnMouseEntered(e -> zone.setStyle("-fx-background-color: #E3F2FD; -fx-border-color: " + BLUE2 + "; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;"));
        zone.setOnMouseExited(e -> zone.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #B0BEC5; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;"));
        zone.setOnMouseClicked(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Selectionner un PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File f = fc.showOpenDialog(null);
            if (f != null) {
                if (num == 1) { fichierSelectionne1 = f; labelFichier1.setText("OK " + f.getName()); labelFichier1.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 12px;"); lbl.setText(f.getName()); }
                else { fichierSelectionne2 = f; labelFichier2.setText("OK " + f.getName()); labelFichier2.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 12px;"); lbl.setText(f.getName()); }
            }
        });
        return zone;
    }

    private void executerOperation(String op) {
        if (fichierSelectionne1 == null && !op.equals("creerPdf")) {
            labelResultat.setText("Veuillez selectionner un fichier PDF !");
            labelResultat.setStyle("-fx-text-fill: #C62828; -fx-font-size: 13px;");
            return;
        }
        labelResultat.setText("Traitement en cours...");
        labelResultat.setStyle("-fx-text-fill: " + BLUE2 + "; -fx-font-size: 13px;");

        new Thread(() -> {
            try {
                String res = envoyerFichier(op);
                Platform.runLater(() -> {
                    if (res != null && res.startsWith("OK")) {
                        labelResultat.setText("Succes: " + res);
                        labelResultat.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 13px;");
                    } else {
                        labelResultat.setText("Erreur: " + res);
                        labelResultat.setStyle("-fx-text-fill: #C62828; -fx-font-size: 13px;");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    labelResultat.setText("Erreur connexion: " + ex.getMessage());
                    labelResultat.setStyle("-fx-text-fill: #C62828; -fx-font-size: 13px;");
                });
            }
        }).start();
    }

    private String envoyerFichier(String op) throws Exception {
        try (Socket s = new Socket("localhost", 9998);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            String commande = construireCommande(op);
            out.println(commande);
            return in.readLine();
        }
    }

    private String construireCommande(String op) {
        String f1 = fichierSelectionne1 != null ? fichierSelectionne1.getAbsolutePath() : "";
        String f2 = fichierSelectionne2 != null ? fichierSelectionne2.getAbsolutePath() : "";
        switch (op) {
            case "fusion": return "fusion|" + f1 + "|" + f2;
            case "decoupage": return "decoupage|" + f1 + "|" + getVal("pageDebut") + "|" + getVal("pageFin");
            case "extrairePages": return "extrairePages|" + f1 + "|" + getVal("pageDebut") + "|" + getVal("pageFin");
            case "supprimerPages": return "supprimerPages|" + f1 + "|" + getVal("pages");
            case "motDePasse": return "ajouterMotDePasse|" + f1 + "|" + getVal("mdp");
            case "convertirImage": return "convertirEnImage|" + f1;
            case "extraireTexte": return "extraireTexte|" + f1;
            case "creerPdf": return "creerPdf|" + getVal("contenu");
            default: return op;
        }
    }

    private String getVal(String id) {
        for (javafx.scene.Node n : contentArea.lookupAll("#" + id)) {
            if (n instanceof TextField) return ((TextField) n).getText();
            if (n instanceof TextArea) return ((TextArea) n).getText();
            if (n instanceof PasswordField) return ((PasswordField) n).getText();
        }
        return "";
    }

    private String[] getTitreOperation(String op) {
        switch (op) {
            case "fusion": return new String[]{"Fusionner des PDFs", "Combinez plusieurs PDFs en un seul"};
            case "decoupage": return new String[]{"Decouper un PDF", "Extrayez une plage de pages"};
            case "extrairePages": return new String[]{"Extraire des pages", "Isolez des pages specifiques"};
            case "supprimerPages": return new String[]{"Supprimer des pages", "Retirez des pages indesirables"};
            case "motDePasse": return new String[]{"Proteger un PDF", "Ajoutez un mot de passe"};
            case "convertirImage": return new String[]{"PDF vers Image", "Convertissez en PNG"};
            case "extraireTexte": return new String[]{"Extraire le texte", "Recuperez le contenu textuel"};
            case "creerPdf": return new String[]{"Creer un PDF", "Generez un nouveau document"};
            default: return new String[]{"Outil", "Description"};
        }
    }

    private TextField creerChamp(String ph) {
        TextField tf = new TextField();
        tf.setPromptText(ph);
        tf.setStyle(styleChamp());
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private String styleChamp() {
        return "-fx-background-color: " + WHITE + "; -fx-border-color: #CFD8DC; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 13px;";
    }

    public static void main(String[] args) { launch(args); }
}