package mkl.testarea.pdfbox1.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/30400728/signing-pdf-with-pdfbox-and-bouncycastle">
 * Signing PDF with PDFBox and BouncyCastle
 * </a>
 * <p>
 * This class tests the OP's original code ({@link CreateSignature} with {@link CreateSignature#fixed}
 * <code> = false</code>) and the fixed code ({@link CreateSignature} with {@link CreateSignature#fixed}
 * <code> = true</code>). 
 * </p>
 * <p>
 * It further tests using either the OP's original ({@link CreateSignature} with {@link CreateSignature#der}
 * <code> = false</code>) or improved ({@link CreateSignature} with {@link CreateSignature#der}
 * <code> = true</code>) encoding code, the latter producing a completely DER-encoded signature object.
 * </p>
 * <p>
 * The issue essentially is that {@link PDDocument#saveIncremental(java.io.InputStream, java.io.OutputStream)}
 * expects the <code>InputStream</code> to cover the PDF including preparations for saving while the OP only
 * provided an <code>InputStream</code> covering the original file. 
 * </p>
 * 
 * @author mkl
 */
public class SignLikeLoneWolf
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

	@Test
	public void testLoneWolf() throws IOException, GeneralSecurityException
	{
	    test(false, false, "test_signedLikeLoneWolf.pdf");
	}

	@Test
	public void testLoneWolfFixed() throws IOException, GeneralSecurityException
	{
        test(true, false, "test_signedLikeLoneWolfFixed.pdf");
	}

    @Test
    public void testLoneWolfDer() throws IOException, GeneralSecurityException
    {
        test(false, true, "test_signedLikeLoneWolfDer.pdf");
    }

    @Test
    public void testLoneWolfFixedDer() throws IOException, GeneralSecurityException
    {
        test(true, true, "test_signedLikeLoneWolfFixedDer.pdf");
    }

	void test(boolean fixed, boolean der, String filename) throws GeneralSecurityException, IOException
	{
        char[] password = "demo-rsa2048".toCharArray();

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream("keystores/demo-rsa2048.p12"), password);

        Enumeration<String> aliases = keystore.aliases();
        String alias;
        if (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
        } else {
            throw new KeyStoreException("Keystore is empty");
        }
        CreateSignature.privateKey = (PrivateKey) keystore.getKey(alias, password);
        Certificate[] certificateChain = keystore.getCertificateChain(alias);
        CreateSignature.certificate = certificateChain[0];

        File inFile = new File("src/test/resources/mkl/testarea/pdfbox1/sign/test.pdf ");
        File outFile = new File(RESULT_FOLDER, filename);
        CreateSignature createSignature = new CreateSignature();
        CreateSignature.fixed = fixed;
        CreateSignature.der = der;
        createSignature.signPdf(inFile, outFile);
	}
}
