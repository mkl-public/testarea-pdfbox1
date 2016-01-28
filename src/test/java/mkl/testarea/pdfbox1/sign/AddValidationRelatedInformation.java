package mkl.testarea.pdfbox1.sign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/30617875/add-dss-dictionary">
 * Add dss dictionary
 * </a>
 * <p>
 * You can add validation related information to a Document Security Store
 * like this using PDFBox
 * </p>
 * @author mkl
 */
public class AddValidationRelatedInformation
{
    static private BouncyCastleProvider provider = new BouncyCastleProvider();

    static private char[] pin = "demo-rsa2048".toCharArray();
    static private PrivateKey privKey;
    static private Certificate[] cert;

    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        Security.addProvider(provider);
        KeyStore keystore = KeyStore.getInstance("PKCS12", provider);
        keystore.load(new FileInputStream(new File("keystores/demo-rsa2048.p12")), pin.clone());
        String alias = keystore.aliases().nextElement();
        privKey = (PrivateKey) keystore.getKey(alias, pin);
        cert = keystore.getCertificateChain(alias);
    }

    @Test
    public void testTestWithImageAndSignature() throws IOException, COSVisitorException, CertificateEncodingException
    {
        String resourceName = "testWithImageAndSignature.pdf";
        File resultFile = new File(RESULT_FOLDER, "testWithImageAndSignature-dss.pdf");

        test(resourceName, resultFile);
    }

    @Test
    public void test2gFixCertified() throws IOException, COSVisitorException, CertificateEncodingException
    {
        String resourceName = "2g-fix-certified.pdf";
        File resultFile = new File(RESULT_FOLDER, "2g-fix-certified-dss.pdf");

        test(resourceName, resultFile);
    }

    void test(String resourceName, File resultFile) throws IOException, COSVisitorException, CertificateEncodingException
    {
        try (   InputStream source = getClass().getResourceAsStream(resourceName);
                FileOutputStream fos = new FileOutputStream(resultFile);
                FileInputStream fis = new FileInputStream(resultFile);
                )
        {
            List<byte[]> certificates = new ArrayList<byte[]>();
            for (int i = 0; i < cert.length; i++)
                certificates.add(cert[i].getEncoded());
            COSDictionary dss = createDssDictionary(certificates, null, null);

            byte inputBytes[] = IOUtils.toByteArray(source);

            PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(inputBytes));
            PDDocumentCatalog catalog = pdDocument.getDocumentCatalog();
            catalog.getCOSObject().setNeedToBeUpdate(true);
            catalog.getCOSDictionary().setItem(COSName.getPDFName("DSS"), dss);

            fos.write(inputBytes);
            pdDocument.saveIncremental(fis, fos);
            pdDocument.close();
        }
    }

    COSDictionary createDssDictionary(Iterable<byte[]> certifiates, Iterable<byte[]> crls, Iterable<byte[]> ocspResponses) throws IOException
    {
        final COSDictionary dssDictionary = new COSDictionary();
        dssDictionary.setNeedToBeUpdate(true);
        dssDictionary.setName(COSName.TYPE, "DSS");

        if (certifiates != null)
            dssDictionary.setItem(COSName.getPDFName("Certs"), createArray(certifiates));
        if (crls != null)
            dssDictionary.setItem(COSName.getPDFName("CRLs"), createArray(crls));
        if (ocspResponses != null)
            dssDictionary.setItem(COSName.getPDFName("OCSPs"), createArray(ocspResponses));

        return dssDictionary;
    }

    COSArray createArray(Iterable<byte[]> datas) throws IOException
    {
        COSArray array = new COSArray();
        array.setNeedToBeUpdate(true);
        
        if (datas != null)
        {
            for (byte[] data: datas)
                array.add(createStream(data));
        }

        return array;
    }

    COSStream createStream(byte[] data) throws IOException
    {
        RandomAccessBuffer storage = new RandomAccessBuffer();
        COSStream stream = new COSStream(storage);
        stream.setNeedToBeUpdate(true);
        final OutputStream unfilteredStream = stream.createUnfilteredStream();
        unfilteredStream.write(data);
        unfilteredStream.flush();
        return stream;
    }
}
