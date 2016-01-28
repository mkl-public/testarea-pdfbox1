package mkl.testarea.pdfbox1.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.apache.pdfbox.examples.signature.CreateVisibleSignature;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class SignLikePdfboxExample
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/33011970/pdfbox-1-8-10-fill-and-sign-document-filling-again-fails">
     * PDFBox 1.8.10: Fill and Sign Document, Filling again fails
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/xf5pb0ng8k9zd4i/doc_v2.pdf?dl=0">
     * doc_v2.pdf
     * </a>
     * <p>
     * The cause of the problem is that somehow during the original filling and signing
     * the fonts in the default resources of the interactive form dictionary got lost.
     * </p>
     * <p>
     * PDFBox while filling in the form tries to access the font definition to create
     * an appearance stream. It doesn't find it and, therefore, eventually fails.
     * </p>
     * <p>
     * I simply applied the PDFBox example CreateVisibleSignature to the OP's original
     * doc_v2.pdf file. Indeed, this already removed the Font default resource dictionary.
     * </p>
     * <p>
     * Beware, this test requires older BouncyCastle versions to run, officially 1.44
     * but 1.47 seems to work, too.
     * </p>
     */
    @Test
    public void testVisiblySignDanielHeldtDoc_v2() throws IOException, COSVisitorException, SignatureException, GeneralSecurityException
    {
        File documentOrig = new File("src/test/resources/mkl/testarea/pdfbox1/form/doc_v2.pdf");
        File document = new File(RESULT_FOLDER, "doc_v2.pdf");
        Files.copy(documentOrig.toPath(), document.toPath(), StandardCopyOption.REPLACE_EXISTING);
        String signatureFieldName = "0";

        File ksFile = new File("keystores/demo-rsa2048.p12");
        char[] pin = "demo-rsa2048".toCharArray();

        try (InputStream image = getClass().getResourceAsStream("Willi-1.jpg"))
        {
            createVisibleSignature(document, ksFile, pin, image, signatureFieldName);
        }
    }

    private static BouncyCastleProvider provider = new BouncyCastleProvider();

    void createVisibleSignature(File document, File ksFile, char[] pin, InputStream image, String signatureFieldName) throws IOException, COSVisitorException, SignatureException, GeneralSecurityException
    {
        KeyStore keystore = KeyStore.getInstance("PKCS12", provider);
        keystore.load(new FileInputStream(ksFile), pin);

        CreateVisibleSignature signing = new CreateVisibleSignature(keystore, pin.clone());

        PDVisibleSignDesigner visibleSig = new PDVisibleSignDesigner(document.getAbsolutePath(), image, 1);
        visibleSig.xAxis(0).yAxis(0).zoom(-50).signatureFieldName(signatureFieldName);

        PDVisibleSigProperties signatureProperties = new PDVisibleSigProperties();

        signatureProperties.signerName("name").signerLocation("location").signatureReason("Security").preferredSize(0)
            .page(1).visualSignEnabled(true).setPdVisibleSignature(visibleSig).buildSignature();

        signing.signPDF(document, signatureProperties);
    }
}
