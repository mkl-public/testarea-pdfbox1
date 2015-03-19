package mkl.testarea.pdfbox1.sign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test attempts to sign in memory.
 * 
 * @author mkl
 */
public class SignInMemory
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/29123436/how-to-sign-an-inputstream-from-a-pdf-file-with-pdfbox-2-0-0">
     * How to sign an InputStream from a PDF file with PDFBox 2.0.0
     * </a>
     * 
     * Test the equivalent for PDFBox 1.8.8. Works alright.
     */
    @Test
    public void testDummySignInMemory() throws IOException, COSVisitorException, SignatureException
    {
        try (   InputStream sourceStream = getClass().getResourceAsStream("/mkl/testarea/pdfbox1/assembly/document1.pdf");
                OutputStream output = new FileOutputStream(new File(RESULT_FOLDER, "document1-with-dummy-sig.pdf")))
        {
            byte[] input = IOUtils.toByteArray(sourceStream);
            output.write(input);
            signDetached(input, output, new SignatureInterface()
                    {
                        @Override
                        public byte[] sign(InputStream content) throws SignatureException, IOException
                        {
                            return "Test".getBytes();
                        }
                    });
        }
    }

    void signDetached(byte[] pdf, OutputStream output, SignatureInterface signatureInterface)throws IOException, SignatureException, COSVisitorException
    {
        PDDocument document = PDDocument.load(new ByteArrayInputStream(pdf));
        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("Example User");
        signature.setLocation("Los Angeles, CA");
        signature.setReason("Testing");
        // TODO extract the above details from the signing certificate? Reason as a parameter?

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        // register signature dictionary and sign interface
        document.addSignature(signature, signatureInterface);

        // write incremental (only for signing purpose)
        document.saveIncremental(new ByteArrayInputStream(pdf), output);
    }
}
