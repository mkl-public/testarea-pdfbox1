package mkl.testarea.pdfbox1.sign;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Calendar;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/**
 * <a href="http://stackoverflow.com/questions/30549830/attachment-damages-signature-part-2">
 * Attachment damages signature part 2
 * </a>
 * <p>
 * This is the test class presented by the OP of the question qith slight changes
 * in the signature creation code to account for different BouncyCastle versions used.
 * The class {@link CMSProcessableInputStream} embedded in this file has been copied
 * from inaccessible PDFBox code. 
 * </p>
 * <p>
 * The actual tests executed for this issue in {@link SignLikeUnOriginalToo} actually copy
 * the method {@link #doSign()} and use {@link TC3} merely as {@link SignatureInterface}
 * implementation.
 * </p>
 * 
 * @author UnOriginalToo
 */
public class TC3 implements SignatureInterface
{

    private char[] pin = "demo-rsa2048".toCharArray();
    private BouncyCastleProvider provider = new BouncyCastleProvider();
    private PrivateKey privKey;
    private Certificate[] cert;

    public TC3() throws Exception
    {
        Security.addProvider(provider);
        KeyStore keystore = KeyStore.getInstance("PKCS12", provider);
        keystore.load(new FileInputStream(new File("keystores/demo-rsa2048.p12")), pin.clone());
        String alias = keystore.aliases().nextElement();
        privKey = (PrivateKey) keystore.getKey(alias, pin);
        cert = keystore.getCertificateChain(alias);
    }

    public void doSign() throws Exception
    {
        byte inputBytes[] = IOUtils.toByteArray(new FileInputStream("resources/rooster.pdf"));
        PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(inputBytes));
        PDJpeg ximage = new PDJpeg(pdDocument, ImageIO.read(new File("resources/logo.jpg")));
        PDPage page = (PDPage) pdDocument.getDocumentCatalog().getAllPages().get(0);
        PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page, true, true);
        contentStream.drawXObject(ximage, 50, 50, 356, 40);
        contentStream.close();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        pdDocument.save(os);
        os.flush();
        pdDocument.close();

        inputBytes = os.toByteArray();
        pdDocument = PDDocument.load(new ByteArrayInputStream(inputBytes));

        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("signer name");
        signature.setLocation("signer location");
        signature.setReason("reason for signature");
        signature.setSignDate(Calendar.getInstance());

        pdDocument.addSignature(signature, this);

        File outputDocument = new File("resources/signed.pdf");
        ByteArrayInputStream fis = new ByteArrayInputStream(inputBytes);
        FileOutputStream fos = new FileOutputStream(outputDocument);
        byte[] buffer = new byte[8 * 1024];
        int c;
        while ((c = fis.read(buffer)) != -1)
        {
            fos.write(buffer, 0, c);
        }
        fis.close();
        FileInputStream is = new FileInputStream(outputDocument);

        pdDocument.saveIncremental(is, fos);
        pdDocument.close();
    }

    public byte[] sign(InputStream content)
    {
// Original code for older BC version.
//      CMSProcessableInputStream input = new CMSProcessableInputStream(content);
//      CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
//      List<Certificate> certList = Arrays.asList(cert);
//      CertStore certStore = null;
//      try{
//          certStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), provider);
//          gen.addSigner(privKey, (X509Certificate) certList.get(0), CMSSignedGenerator.DIGEST_SHA256);
//          gen.addCertificatesAndCRLs(certStore);
//          CMSSignedData signedData = gen.generate(input, false, provider);
//          return signedData.getEncoded();
//      }catch (Exception e){}
//      return null;
// Replacement code adapted from CreateSignature
        try {
            BouncyCastleProvider BC = new BouncyCastleProvider();
            Store<?> certStore = new JcaCertStore(Collections.singletonList(cert[0]));

            CMSTypedDataInputStream input = new CMSTypedDataInputStream(content);
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            ContentSigner sha512Signer = new JcaContentSignerBuilder("SHA256WithRSA").setProvider(BC).build(privKey);

            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider(BC).build()).build(sha512Signer, new X509CertificateHolder(cert[0].getEncoded())
            ));
            gen.addCertificates(certStore);
            CMSSignedData signedData = gen.generate(input, false);

            if (true)
            { // DER-encode signature container
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DEROutputStream dos = new DEROutputStream(baos);
                dos.writeObject(signedData.toASN1Structure());
                return baos.toByteArray();
            }
            else
                return signedData.getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception
    {
        new TC3().doSign();
    }
}

/**
 * Wrap a InputStream into a CMSProcessable object for bouncy castle. It's an
 * alternative to the CMSProcessableByteArray.
 * 
 * @author Thomas Chojecki
 * 
 */
class CMSProcessableInputStream implements CMSProcessable
{
  InputStream in;

  public CMSProcessableInputStream(InputStream is)
  {
    in = is;
  }

  public Object getContent()
  {
    return in;
  }

  public void write(OutputStream out) throws IOException, CMSException
  {
    // read the content only one time
    byte[] buffer = new byte[8 * 1024];
    int read;
    while ((read = in.read(buffer)) != -1)
    {
      out.write(buffer, 0, read);
    }
    in.close();
  }
}
