import PdfServiceApp.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.StringHolder;
import java.io.*;
import java.net.*;

public class PontPyro {
    private static PdfService service;

    public static void main(String[] args) {
        try {
            System.out.println("[PONT] Connexion au serveur CORBA...");
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            service = PdfServiceHelper.narrow(ncRef.resolve_str("PdfService"));
            System.out.println("[PONT] Connecte au serveur CORBA !");
            ServerSocket ss = new ServerSocket(9998);
            System.out.println("[PONT] En attente JavaFX sur port 9998...");
            while (true) {
                Socket socket = ss.accept();
                new Thread(() -> gererClient(socket)).start();
            }
        } catch (Exception e) {
            System.err.println("[PONT] ERREUR : " + e.getMessage());
        }
    }

    private static void gererClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String ligne;
            while ((ligne = in.readLine()) != null) {
                System.out.println("[PONT] Recu : " + ligne);
                String reponse = traiterCommande(ligne);
                out.println(reponse);
            }
            socket.close();
        } catch (Exception e) {
            System.err.println("[PONT] Erreur: " + e.getMessage());
        }
    }

    private static String traiterCommande(String commande) {
        try {
            String[] parts = commande.split("[|]");
            String operation = parts[0].trim();
            StringHolder r = new StringHolder();
            switch (operation) {
                case "fusion": service.fusion(parts[1], parts[2], r); return r.value;
                case "decoupage": service.decoupage(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), r); return r.value;
                case "extrairePages": service.extrairePages(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), r); return r.value;
                case "supprimerPages":
                    String[] ps = parts[2].split(",");
                    int[] pages = new int[ps.length];
                    for (int i=0;i<ps.length;i++) pages[i]=Integer.parseInt(ps[i].trim());
                    service.supprimerPages(parts[1], pages, r); return r.value;
                case "ajouterMotDePasse": service.ajouterMotDePasse(parts[1], parts[2], r); return r.value;
                case "convertirEnImage": service.convertirEnImage(parts[1], r); return r.value;
                case "extraireTexte": service.extraireTexte(parts[1], r); return r.value;
                case "creerPdf": service.creerPdf(parts[1], r); return r.value;
                default: return "ERREUR: Operation inconnue";
            }
        } catch (Exception e) {
            return "ERREUR: " + e.getMessage();
        }
    }
}