import PdfServiceApp.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class StartServer {
    public static void main(String args[]) {
        try {
            // Initialiser l’ORB
            ORB orb = ORB.init(args, null);

            // Créer l’implémentation
            PdfServiceImpl pdfImpl = new PdfServiceImpl();

            // Activer le POA
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("RootPOA");
            POA rootpoa = POAHelper.narrow(objRef);
            rootpoa.the_POAManager().activate();

            // Obtenir la référence CORBA
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(pdfImpl);
            PdfService href = PdfServiceHelper.narrow(ref);

            // Enregistrer dans le NameService
            org.omg.CORBA.Object objRefNS = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRefNS);

            NameComponent path[] = ncRef.to_name("PdfService");
            ncRef.rebind(path, href);

            System.out.println("SERVEUR CORBA PdfService PRÊT ET EN ATTENTE...");

            // Boucle d’attente
            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
