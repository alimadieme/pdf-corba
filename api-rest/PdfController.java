import PdfServiceApp.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.StringHolder;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;

public class PdfController {
    private static PdfService service;

    // Méthode pour lire le body en Java 8
    static String lireBody(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String ligne;
        while ((ligne = br.readLine()) != null) {
            sb.append(ligne);
        }
        return sb.toString();
    }

    static void repondre(HttpExchange e, int code, String body) throws IOException {
        e.getResponseHeaders().add("Content-Type", "application/json");
        e.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        e.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        e.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        byte[] bytes = body.getBytes("UTF-8");
        e.sendResponseHeaders(code, bytes.length);
        e.getResponseBody().write(bytes);
        e.getResponseBody().close();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[API] Connexion au serveur CORBA...");
        String corbHost = System.getenv("CORBA_HOST") != null ?
                          System.getenv("CORBA_HOST") : "localhost";
        ORB orb = ORB.init(new String[]{
            "-ORBInitialPort", "1050",
            "-ORBInitialHost", corbHost
        }, null);
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        service = PdfServiceHelper.narrow(ncRef.resolve_str("PdfService"));
        System.out.println("[API] Connecté au serveur CORBA !");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/fusion", PdfController::fusion);
        server.createContext("/api/extraireTexte", PdfController::extraireTexte);
        server.createContext("/api/creerPdf", PdfController::creerPdf);
        server.createContext("/api/decoupage", PdfController::decoupage);
        server.createContext("/api/ajouterMotDePasse", PdfController::ajouterMotDePasse);
        server.createContext("/api/convertirEnImage", PdfController::convertirEnImage);
        server.createContext("/api/health", e -> {
            try { repondre(e, 200, "{\"status\":\"OK\"}"); }
            catch(Exception ex) { ex.printStackTrace(); }
        });
        server.start();
        System.out.println("[API] Serveur HTTP démarré sur port 8080 !");
    }

    static void fusion(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String body = lireBody(e.getRequestBody());
        String[] parts = body.split("\\|");
        StringHolder r = new StringHolder();
        service.fusion(parts[0], parts[1], r);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void extraireTexte(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String fichier = lireBody(e.getRequestBody());
        StringHolder r = new StringHolder();
        service.extraireTexte(fichier, r);
        String texte = r.value.replace("\"", "'").replace("\n", "\\n").replace("\r", "");
        repondre(e, 200, "{\"texte\":\"" + texte + "\"}");
    }

    static void creerPdf(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String contenu = lireBody(e.getRequestBody());
        StringHolder r = new StringHolder();
        service.creerPdf(contenu, r);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void decoupage(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String body = lireBody(e.getRequestBody());
        String[] parts = body.split("\\|");
        StringHolder r = new StringHolder();
        service.decoupage(parts[0],
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]), r);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void ajouterMotDePasse(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String body = lireBody(e.getRequestBody());
        String[] parts = body.split("\\|");
        StringHolder r = new StringHolder();
        service.ajouterMotDePasse(parts[0], parts[1], r);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void convertirEnImage(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String fichier = lireBody(e.getRequestBody());
        StringHolder r = new StringHolder();
        service.convertirEnImage(fichier, r);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

// Servir le frontend
server.createContext("/", e -> {
    try {
        java.nio.file.Path path = java.nio.file.Paths.get("/app/index.html");
        byte[] bytes = java.nio.file.Files.readAllBytes(path);
        e.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        e.sendResponseHeaders(200, bytes.length);
        e.getResponseBody().write(bytes);
        e.getResponseBody().close();
    } catch(Exception ex) {
        repondre(e, 500, "{\"error\":\"Frontend not found\"}");
    }
});
}
