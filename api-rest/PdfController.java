import PdfServiceApp.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.StringHolder;
import com.sun.net.httpserver.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PdfController {
    private static PdfService service;
    private static MongoCollection<Document> historiqueCollection;
    private static MongoCollection<Document> usersCollection;
    private static final String UPLOAD_DIR = "/tmp/senpdf/";

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

    
    static byte[] lireBodyBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = is.read(chunk)) != -1) {
            buffer.write(chunk, 0, n);
        }
        return buffer.toByteArray();
    }

    static String lireBody(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String ligne;
        while ((ligne = br.readLine()) != null) sb.append(ligne).append("\n");
        return sb.toString().trim();
    }

    static void servirHTML(HttpExchange e) throws IOException {
        File f = new File("/app/index.html");
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        byte[] buf2 = new byte[4096]; int n2;
        while ((n2 = fis.read(buf2)) != -1) baos2.write(buf2, 0, n2);
        fis.close();
        byte[] bytes = baos2.toByteArray();
        e.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        e.sendResponseHeaders(200, bytes.length);
        e.getResponseBody().write(bytes);
        e.getResponseBody().close();
    }

    // Parse multipart form data
    static Map<String, java.lang.Object> parseMultipart(HttpExchange e) throws IOException {
        Map<String, java.lang.Object> result = new HashMap<>();
        String contentType = e.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("multipart/form-data")) {
            return result;
        }
        String boundary = "--" + contentType.split("boundary=")[1].trim();
        byte[] bodyBytes = lireBodyBytes(e.getRequestBody());
        String body = new String(bodyBytes, "ISO-8859-1");
        String[] parts = body.split(boundary);
        for (String part : parts) {
            if (part.trim().isEmpty() || part.equals("--\r\n")) continue;
            if (part.contains("filename=")) {
                // Fichier
                String filename = part.split("filename=\"")[1].split("\"")[0];
                int start = part.indexOf("\r\n\r\n") + 4;
                int end = part.lastIndexOf("\r\n");
                if (start > 4 && end > start) {
                    byte[] fileBytes = part.substring(start, end).getBytes("ISO-8859-1");
                    String key = part.split("name=\"")[1].split("\"")[0];
                    // Sauvegarder le fichier
                    new File(UPLOAD_DIR).mkdirs();
                    String savedPath = UPLOAD_DIR + filename;
                    Files.write(Paths.get(savedPath), fileBytes);
                    result.put(key, savedPath);
                    result.put(key + "_name", filename);
                }
            } else if (part.contains("name=")) {
                String key = part.split("name=\"")[1].split("\"")[0];
                int start = part.indexOf("\r\n\r\n") + 4;
                String value = part.substring(start).trim();
                result.put(key, value);
            }
        }
        return result;
    }

    static void sauvegarderHistorique(String operation, String fichier, String resultat) {
        try {
            if (historiqueCollection != null) {
                Document doc = new Document()
                    .append("operation", operation)
                    .append("fichier", fichier)
                    .append("resultat", resultat)
                    .append("date", new Date().toString());
                historiqueCollection.insertOne(doc);
            }
        } catch (Exception ex) {
            System.err.println("[MongoDB] Erreur historique: " + ex.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        // Créer dossier upload
        new File(UPLOAD_DIR).mkdirs();

        // Connexion MongoDB
        String mongoUri = System.getenv("MONGODB_URI");
        if (mongoUri != null) {
            try {
                MongoClient mongoClient = MongoClients.create(mongoUri);
                MongoDatabase db = mongoClient.getDatabase("senpdf");
                historiqueCollection = db.getCollection("historique");
                usersCollection = db.getCollection("users");
                System.out.println("[MongoDB] Connecte !");
            } catch (Exception ex) {
                System.err.println("[MongoDB] Erreur: " + ex.getMessage());
            }
        }

        // Connexion CORBA
        System.out.println("[API] Connexion au serveur CORBA...");
        String corbaHost = System.getenv("CORBA_HOST") != null ? System.getenv("CORBA_HOST") : "localhost";
        ORB orb = ORB.init(new String[]{"-ORBInitialPort", "1050", "-ORBInitialHost", corbaHost}, null);
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        service = PdfServiceHelper.narrow(ncRef.resolve_str("PdfService"));
        System.out.println("[API] Connecte au serveur CORBA !");

        // Serveur HTTP
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/upload", PdfController::upload);
        server.createContext("/api/fusion", PdfController::fusion);
        server.createContext("/api/extraireTexte", PdfController::extraireTexte);
        server.createContext("/api/creerPdf", PdfController::creerPdf);
        server.createContext("/api/decoupage", PdfController::decoupage);
        server.createContext("/api/ajouterMotDePasse", PdfController::ajouterMotDePasse);
        server.createContext("/api/convertirEnImage", PdfController::convertirEnImage);
        server.createContext("/api/supprimerPages", PdfController::supprimerPages);
        server.createContext("/api/extrairePages", PdfController::extrairePages);
        server.createContext("/api/historique", PdfController::historique);
        server.createContext("/api/download", PdfController::download);
        server.createContext("/api/health", e -> {
            try { repondre(e, 200, "{\"status\":\"OK\",\"mongodb\":\"" + (historiqueCollection != null ? "connected" : "disconnected") + "\"}"); }
            catch(Exception ex) { ex.printStackTrace(); }
        });
        server.createContext("/", e -> {
            try { servirHTML(e); }
            catch(Exception ex) {
                try { repondre(e, 500, "{\"error\":\"Frontend not found\"}"); }
                catch(Exception ex2) { ex2.printStackTrace(); }
            }
        });
        server.start();
        System.out.println("[API] Serveur HTTP demarre sur port 8080 !");
    }

    static void upload(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        try {
            Map<String, java.lang.Object> data = parseMultipart(e);
            String path = (String) data.get("file");
            String name = (String) data.get("file_name");
            if (path != null) {
                repondre(e, 200, "{\"path\":\"" + path + "\",\"name\":\"" + name + "\"}");
            } else {
                repondre(e, 400, "{\"error\":\"Aucun fichier recu\"}");
            }
        } catch (Exception ex) {
            repondre(e, 500, "{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    static void fusion(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String body = lireBody(e.getRequestBody());
        String[] parts = body.split("\\|");
        StringHolder r = new StringHolder();
        service.fusion(parts[0], parts[1], r);
        sauvegarderHistorique("fusion", parts[0], r.value);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void extraireTexte(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String fichier = lireBody(e.getRequestBody());
        StringHolder r = new StringHolder();
        service.extraireTexte(fichier, r);
        sauvegarderHistorique("extraireTexte", fichier, "OK");
        String texte = r.value.replace("\"", "'"  ).replace("\n", "\\n").replace("\r", "");
        repondre(e, 200, "{\"texte\":\"" + texte + "\"}");
    }

    static void creerPdf(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String contenu = lireBody(e.getRequestBody());
        StringHolder r = new StringHolder();
        service.creerPdf(contenu, r);
        sauvegarderHistorique("creerPdf", "nouveau", r.value);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void decoupage(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String body = lireBody(e.getRequestBody());
        String[] parts = body.split("\\|");
        StringHolder r = new StringHolder();
        service.decoupage(parts[0], Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()), r);
        sauvegarderHistorique("decoupage", parts[0], r.value);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void extrairePages(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String body = lireBody(e.getRequestBody());
        String[] parts = body.split("\\|");
        StringHolder r = new StringHolder();
        service.extrairePages(parts[0], Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()), r);
        sauvegarderHistorique("extrairePages", parts[0], r.value);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void ajouterMotDePasse(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String body = lireBody(e.getRequestBody());
        String[] parts = body.split("\\|");
        StringHolder r = new StringHolder();
        service.ajouterMotDePasse(parts[0], parts[1], r);
        sauvegarderHistorique("ajouterMotDePasse", parts[0], r.value);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void convertirEnImage(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String fichier = lireBody(e.getRequestBody());
        StringHolder r = new StringHolder();
        service.convertirEnImage(fichier, r);
        sauvegarderHistorique("convertirEnImage", fichier, r.value);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    static void supprimerPages(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        String body = lireBody(e.getRequestBody());
        String[] parts = body.split("\\|");
        String[] pagesStr = parts[1].split(",");
        int[] pages = new int[pagesStr.length];
        for (int i = 0; i < pagesStr.length; i++) pages[i] = Integer.parseInt(pagesStr[i].trim());
        StringHolder r = new StringHolder();
        service.supprimerPages(parts[0], pages, r);
        sauvegarderHistorique("supprimerPages", parts[0], r.value);
        repondre(e, 200, "{\"resultat\":\"" + r.value + "\"}");
    }

    
    static void download(HttpExchange e) throws IOException {
        String query = e.getRequestURI().getQuery();
        String filename = query.replace("file=", "").trim();
        filename = java.net.URLDecoder.decode(filename, "UTF-8");
        File f = new File(filename);
        if (!f.exists()) f = new File("/tmp/senpdf/" + filename);
        if (!f.exists()) { repondre(e, 404, "{\"error\":\"Fichier non trouve\"}"); return; }
        byte[] bytes;
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int n;
        while ((n = fis.read(buf)) != -1) baos.write(buf, 0, n);
        fis.close();
        bytes = baos.toByteArray();
        e.getResponseHeaders().add("Content-Type", "application/pdf");
        e.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
        e.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        e.sendResponseHeaders(200, bytes.length);
        e.getResponseBody().write(bytes);
        e.getResponseBody().close();
    }

    static void historique(HttpExchange e) throws IOException {
        if (e.getRequestMethod().equals("OPTIONS")) { repondre(e, 200, ""); return; }
        try {
            StringBuilder sb = new StringBuilder("[");
            if (historiqueCollection != null) {
                for (Document doc : historiqueCollection.find().limit(20)) {
                    sb.append("{\"operation\":\"").append(doc.getString("operation")).append("\",");
                    sb.append("\"fichier\":\"").append(doc.getString("fichier")).append("\",");
                    sb.append("\"resultat\":\"").append(doc.getString("resultat")).append("\",");
                    sb.append("\"date\":\"").append(doc.getString("date")).append("\"},");
                }
                if (sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("]");
            repondre(e, 200, sb.toString());
        } catch (Exception ex) {
            repondre(e, 500, "[]");
        }
    }
}