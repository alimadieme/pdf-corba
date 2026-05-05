import PdfServiceApp.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.pdmodel.encryption.*;
import org.omg.CORBA.StringHolder;
import java.io.*;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class PdfServiceImpl extends PdfServicePOA {

    @Override
    public void fusion(String fichier1, String fichier2, StringHolder resultat) {
        try {
            PDDocument doc1 = PDDocument.load(new File(fichier1));
            PDDocument doc2 = PDDocument.load(new File(fichier2));
            List<?> pages = doc2.getDocumentCatalog().getAllPages();
            for (Object p : pages) {
                doc1.addPage((PDPage) p);
            }
            String sortie = "fusion_resultat.pdf";
            doc1.save(sortie);
            doc1.close();
            doc2.close();
            resultat.value = "OK: " + sortie;
            System.out.println("[SERVEUR] Fusion OK -> " + sortie);
        } catch (Exception e) {
            resultat.value = "ERREUR fusion: " + e.getMessage();
        }
    }

    @Override
    public void decoupage(String fichier, int pageDebut, int pageFin, StringHolder resultat) {
        try {
            PDDocument doc = PDDocument.load(new File(fichier));
            PDDocument nouveau = new PDDocument();
            List<?> pages = doc.getDocumentCatalog().getAllPages();
            for (int i = pageDebut - 1; i < pageFin && i < pages.size(); i++) {
                nouveau.addPage((PDPage) pages.get(i));
            }
            String sortie = "decoupage_" + pageDebut + "_" + pageFin + ".pdf";
            nouveau.save(sortie);
            nouveau.close();
            doc.close();
            resultat.value = "OK: " + sortie;
            System.out.println("[SERVEUR] Decoupage OK -> " + sortie);
        } catch (Exception e) {
            resultat.value = "ERREUR decoupage: " + e.getMessage();
        }
    }

    @Override
    public void extrairePages(String fichier, int pageDebut, int pageFin, StringHolder resultat) {
        try {
            PDDocument doc = PDDocument.load(new File(fichier));
            PDDocument nouveau = new PDDocument();
            List<?> pages = doc.getDocumentCatalog().getAllPages();
            for (int i = pageDebut - 1; i < pageFin && i < pages.size(); i++) {
                nouveau.addPage((PDPage) pages.get(i));
            }
            String sortie = "extraction_" + pageDebut + "_" + pageFin + ".pdf";
            nouveau.save(sortie);
            nouveau.close();
            doc.close();
            resultat.value = "OK: " + sortie;
            System.out.println("[SERVEUR] Extraction pages OK -> " + sortie);
        } catch (Exception e) {
            resultat.value = "ERREUR extraction: " + e.getMessage();
        }
    }

    @Override
    public void supprimerPages(String fichier, int[] pages, StringHolder resultat) {
        try {
            PDDocument doc = PDDocument.load(new File(fichier));
            PDDocument nouveau = new PDDocument();
            List<?> toutesPages = doc.getDocumentCatalog().getAllPages();
            for (int i = 0; i < toutesPages.size(); i++) {
                boolean supprimer = false;
                for (int p : pages) {
                    if (p - 1 == i) { supprimer = true; break; }
                }
                if (!supprimer) {
                    nouveau.addPage((PDPage) toutesPages.get(i));
                }
            }
            String sortie = "sans_pages.pdf";
            nouveau.save(sortie);
            nouveau.close();
            doc.close();
            resultat.value = "OK: " + sortie;
            System.out.println("[SERVEUR] Suppression pages OK -> " + sortie);
        } catch (Exception e) {
            resultat.value = "ERREUR suppression: " + e.getMessage();
        }
    }

    @Override
    public void ajouterMotDePasse(String fichier, String motDePasse, StringHolder resultat) {
        try {
            PDDocument doc = PDDocument.load(new File(fichier));
            StandardProtectionPolicy policy =
                new StandardProtectionPolicy(motDePasse, motDePasse, new AccessPermission());
            policy.setEncryptionKeyLength(128);
            doc.protect(policy);
            String sortie = "protege.pdf";
            doc.save(sortie);
            doc.close();
            resultat.value = "OK: " + sortie;
            System.out.println("[SERVEUR] Mot de passe OK -> " + sortie);
        } catch (Exception e) {
            resultat.value = "ERREUR mot de passe: " + e.getMessage();
        }
    }

    @Override
    public void convertirEnImage(String fichier, StringHolder resultat) {
        try {
            PDDocument doc = PDDocument.load(new File(fichier));
            List<?> pages = doc.getDocumentCatalog().getAllPages();
            PDPage page = (PDPage) pages.get(0);
            BufferedImage image = page.convertToImage();
            String sortie = "page1.png";
            ImageIO.write(image, "PNG", new File(sortie));
            doc.close();
            resultat.value = "OK: " + sortie;
            System.out.println("[SERVEUR] Conversion image OK -> " + sortie);
        } catch (Exception e) {
            resultat.value = "ERREUR conversion: " + e.getMessage();
        }
    }

    @Override
    public void extraireTexte(String fichier, StringHolder texte) {
        try {
            PDDocument doc = PDDocument.load(new File(fichier));
            PDFTextStripper stripper = new PDFTextStripper();
            texte.value = stripper.getText(doc);
            doc.close();
            System.out.println("[SERVEUR] Extraction texte OK");
        } catch (Exception e) {
            texte.value = "ERREUR extraction texte: " + e.getMessage();
        }
    }

    @Override
    public void creerPdf(String contenuTexte, StringHolder resultat) {
        try {
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream content = new PDPageContentStream(doc, page);
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.moveTextPositionByAmount(50, 700);
            String[] lignes = contenuTexte.split("\\n");
            for (String ligne : lignes) {
                content.drawString(ligne);
                content.moveTextPositionByAmount(0, -20);
            }
            content.endText();
            content.close();
            String sortie = "nouveau.pdf";
            doc.save(sortie);
            doc.close();
            resultat.value = "OK: " + sortie;
            System.out.println("[SERVEUR] Creation PDF OK -> " + sortie);
        } catch (Exception e) {
            resultat.value = "ERREUR creation: " + e.getMessage();
        }
    }
}
