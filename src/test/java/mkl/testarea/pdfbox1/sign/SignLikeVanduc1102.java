package mkl.testarea.pdfbox1.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.Calendar;

import org.apache.pdfbox.examples.signature.CreateVisibleSignature;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test deals with signing related questions of the stackoverflow user
 * <a href="http://stackoverflow.com/users/1681903/vanduc1102">vanduc1102</a>.
 * 
 * @author mkl
 */
public class SignLikeVanduc1102
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    static File document;

    final static char[] password = "demo-rsa2048".toCharArray();
    static KeyStore keystore;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        document = new File(RESULT_FOLDER, "Vanduc1102-test.pdf");
        try (   InputStream documentIs = SignLikeDenisMP.class.getResourceAsStream("Vanduc1102-test.pdf");
                OutputStream documentOs = new FileOutputStream(document))
        {
            IOUtils.copy(documentIs, documentOs);
        }

        keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream("keystores/demo-rsa2048.p12"), password);
    }

    /**
     * <a href="http://stackoverflow.com/questions/34125145/pdfbox-document-has-been-altered-or-corrupted-since-it-was-signed">
     * pdfbox - document has been altered or corrupted since it was signed
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0BwLd99D2Ui9VQjhuQTNXRzQ5bkk/view?usp=sharing">
     * duc-test.pdf
     * </a>
     * <p>
     * The resource Vanduc1102-test.pdf is the original (unsigned) PDF extracted from duc-test.pdf. 
     * </p>
     * <p>
     * The OP's code indeed produces a file with a wrong hash value. The fix is easy, though,
     * cf. {@link #testCorruptFixed()}.
     * </p>
     */
    @Test
    public void testCorruptOriginal() throws COSVisitorException, IOException, SignatureException
    {
        File result = new File(RESULT_FOLDER, "Vanduc1102-test-signed-corrupt.pdf");
        CreateSignatureVanduc1Corrupt creator = new CreateSignatureVanduc1Corrupt(keystore, password, false);
        creator.signDetached(document.toString(), result.toString(), "src/test/resources/mkl/testarea/pdfbox1/sign/Willi-1.jpg");
    }

    /**
     * <a href="http://stackoverflow.com/questions/34125145/pdfbox-document-has-been-altered-or-corrupted-since-it-was-signed">
     * pdfbox - document has been altered or corrupted since it was signed
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0BwLd99D2Ui9VQjhuQTNXRzQ5bkk/view?usp=sharing">
     * duc-test.pdf
     * </a>
     * <p>
     * The resource Vanduc1102-test.pdf is the original (unsigned) PDF extracted from duc-test.pdf. 
     * </p>
     * <p>
     * The OP's code indeed produces a file with a wrong hash value,
     * cf. {@link #testCorruptOriginal()}. . The fix is easy, though,
     * the OP uses the wrong {@link InputStream} parameter for
     * {@link PDDocument#saveIncremental(InputStream, OutputStream)},
     * cf. the fix in
     * {@link CreateSignatureVanduc1Corrupt#signDetached(String, String, String)}.
     * </p>
     */
    @Test
    public void testCorruptFixed() throws COSVisitorException, IOException, SignatureException
    {
        File result = new File(RESULT_FOLDER, "Vanduc1102-test-signed-corrupt-fixed.pdf");
        CreateSignatureVanduc1Corrupt creator = new CreateSignatureVanduc1Corrupt(keystore, password, true);
        creator.signDetached(document.toString(), result.toString(), "src/test/resources/mkl/testarea/pdfbox1/sign/Willi-1.jpg");
    }

}

/**
 * <a href="http://stackoverflow.com/questions/34125145/pdfbox-document-has-been-altered-or-corrupted-since-it-was-signed">
 * pdfbox - document has been altered or corrupted since it was signed
 * </a>
 * <p>
 * As vanduc1102 stated in <a href="http://stackoverflow.com/questions/34125145/pdfbox-document-has-been-altered-or-corrupted-since-it-was-signed/34310486#comment56418021_34310486">this comment</a>,
 * his code was based on {@link CreateVisibleSignature}. With some minor changes it indeed could be made runnable therein.
 * </p>
 * <p>
 * The fix can be activated via constructor parameter.
 * </p>
 */
class CreateSignatureVanduc1Corrupt extends CreateVisibleSignature
{
    public CreateSignatureVanduc1Corrupt(KeyStore keystore, char[] pin, boolean fixed)
    {
        super(keystore, pin);
        this.fixed = fixed;
    }

    public void signDetached(String inputFilePath, String outputFilePath, String signatureImagePath/*, Sign signProperties*/) throws IOException, COSVisitorException, SignatureException {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        PDDocument document = null;
        InputStream signImageStream = null;

        try {
            /*setTsaClient(null);*/
            document = PDDocument.load(inputFilePath);
            // create signature dictionary
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("VANDUC1102");
            signature.setLocation(null);
            String displayName = "Hello World, Document signed by VANDUC1102";
            String reason = /*reasonText*/"REASON" + " " + displayName;
            signature.setReason(reason);

            // the signing date, needed for valid signature
            signature.setSignDate(Calendar.getInstance());            
            int signatureInPage = /*signProperties.getPageNumber()*/0 + 1;
            signImageStream = new FileInputStream(new File(signatureImagePath));
            PDVisibleSignDesigner visibleSig = new PDVisibleSignDesigner(inputFilePath, signImageStream, signatureInPage);

            float xAxis = convertPixel2Point(/*signProperties.getX()*/100) ;
            float yAxis = convertPixel2Point(/*signProperties.getY()*/100);               
            float signImageHeight = convertPixel2Point(/*signImageHeight*/100);    
            float signImageWidth = convertPixel2Point(/*signImageWidth*/100);

            visibleSig.xAxis(xAxis)
                    .yAxis(yAxis)
                    .zoom(0)
                    .signatureFieldName("Signature")
                    .height(signImageHeight)
                    .width(signImageWidth);
            PDVisibleSigProperties signatureProperties = new PDVisibleSigProperties();

            signatureProperties.signerName(/*eiUser.getName()*/ "eiUserName")
                     .signerLocation(null)
                     .signatureReason(reason)
                     .preferredSize(0)
                     .page(/*signProperties.getPageNumber()*/0)
                     .visualSignEnabled(true)
                     .setPdVisibleSignature(visibleSig)
                     .buildSignature();
             // register signature dictionary and sign interface
            SignatureOptions signatureOptions = new SignatureOptions();
            signatureOptions.setVisualSignature(signatureProperties);
            signatureOptions.setPage(signatureInPage);
            document.addSignature(signature, this, signatureOptions);

            File outputFile = new File(outputFilePath);
            outputStream = new FileOutputStream(outputFile);
            inputStream = new FileInputStream(inputFilePath);
            IOUtils.copy(inputStream, outputStream);
            // vvv FIX: InputStream parameter of document.saveIncremental
            // vvv      must cover the whole, enhanced document
            if (fixed)
                inputStream = new FileInputStream(outputFile);
            // =========
            document.saveIncremental(inputStream, outputStream);
            outputStream.flush();
//        } catch (COSVisitorException | SignatureException | IOException ex) {
//            log.error("signDetached ", ex);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(outputStream);
            org.apache.commons.io.IOUtils.closeQuietly(inputStream);
            org.apache.commons.io.IOUtils.closeQuietly(signImageStream);
            document.close();
        }
    }

    private float convertPixel2Point(float pixel){
        return pixel * (float) 72/96;
    }

    final boolean fixed;
}


