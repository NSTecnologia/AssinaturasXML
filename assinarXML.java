import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLSigner {

    public static X509Certificate buscaCertificado(String cnpj) {
        try {
            KeyStore ks = KeyStore.getInstance("Windows-MY");
            ks.load(null, null);

            Enumeration<String> aliases = ks.aliases();
            X509Certificate cert = null;

            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate currentCert = (X509Certificate) ks.getCertificate(alias);

                if (currentCert.getSubjectDN().getName().contains(cnpj)) {
                    cert = currentCert;
                    break;
                }
            }

            return cert;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String assinaXML(String XMLString, String RefUri, X509Certificate cert, PrivateKey privateKey) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(XMLString.getBytes()));

            int qtdeRefUri = doc.getElementsByTagName(RefUri).getLength();

            if (qtdeRefUri == 0) {
                throw new Exception("A tag de assinatura " + RefUri + " inexiste");
            } else if (qtdeRefUri > 1) {
                throw new Exception("A tag de assinatura " + RefUri + " não é única");
            } else {
                XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

                Reference ref = fac.newReference("#" + RefUri, fac.newDigestMethod(DigestMethod.SHA1, null));

                SignedInfo si = fac.newSignedInfo(
                        fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                        fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                        fac.newReferences(Collections.singletonList(ref)));

                KeyInfoFactory kif = fac.getKeyInfoFactory();
                X509Data x509d = kif.newX509Data(Collections.singletonList(cert));
                KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509d));

                XMLSignature signature = fac.newXMLSignature(si, ki);

                DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());
                signature.sign(dsc);

                return convertDocumentToString(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String convertDocumentToString(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String cnpj = "seu cnpj";
        X509Certificate cert = buscaCertificado(cnpj);

        if (cert != null) {
            String XMLString = "xml a ser assinado";
            String RefUri = "infNFe";
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(cert.getSubjectDN().getName(), null);

            String signedXML = assinaXML(XMLString, RefUri, cert, privateKey);
            System.out.println(signedXML);
        } else {
            System.out.println("Certificado não encontrado");
        }
    }
}
