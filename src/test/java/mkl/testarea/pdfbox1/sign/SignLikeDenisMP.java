package mkl.testarea.pdfbox1.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import org.apache.pdfbox.examples.signature.CreateVisibleSignature;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/31571055/how-can-i-get-pdvisiblesigproperties-to-write-the-signature-on-the-3-page-into-t">
 * How can I get PDVisibleSigProperties to write the signature on the 3 page into the signature box
 * </a>
 * <br>
 * <a href="http://www.ors.od.nih.gov/ser/dpsac/bgchecks/Documents/hhs-745.pdf">
 * hhs-745.pdf
 * </a>
 * <p>
 * The method {@link #testOriginalA()} essentially represents the code of the author's
 * first code version
 * </p>
 * <p>
 * Running it that method indeed showed that there are some issues in the PDFBox
 * visible signature code. Especially it seems to essentially ignore the signature
 * field name set using <code>.signatureFieldName("ApplicantSignature")</code>.
 * Internally it looks like that name is used once to set the signature dictionary
 * <b>Name</b> value but even that seems to get lost.
 * </p>
 * <p>
 * Beware, this test requires older BouncyCastle versions to run, officially 1.44
 * but 1.47 seems to work, too.
 * </p>
 * 
 * @author mkl
 */
public class SignLikeDenisMP
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    static File document;

    final static char[] password = "demo-rsa2048".toCharArray();
    static KeyStore keystore;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        document = new File(RESULT_FOLDER, "hhs-745.pdf");
        try (   InputStream documentIs = SignLikeDenisMP.class.getResourceAsStream("hhs-745.pdf");
                OutputStream documentOs = new FileOutputStream(document))
        {
            IOUtils.copy(documentIs, documentOs);
        }

        keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream("keystores/demo-rsa2048.p12"), password);
    }

    @Test
    public void testOriginalA() throws IOException, COSVisitorException, SignatureException
    {
        CreateVisibleSignature signing = new CreateVisibleSignature(
                keystore, password.clone());
        InputStream image = getClass().getResourceAsStream("Willi-1.jpg");
        PDVisibleSignDesigner visibleSig = new PDVisibleSignDesigner(
                document.toString(), image, 1);
        visibleSig.xAxis(0).yAxis(0).zoom(-75)
                .signatureFieldName("ApplicantSignature"); // ("topmostSubform[0].Page3[0].SignatureField1[0]")
        PDVisibleSigProperties signatureProperties = new PDVisibleSigProperties();
        signatureProperties.signerName("name").signerLocation("location")
                .signatureReason("Security").preferredSize(0).page(3)
                .visualSignEnabled(true).setPdVisibleSignature(visibleSig)
                .buildSignature();
        signing.signPDF(document, signatureProperties);
    }

}
