package mkl.testarea.pdfbox1.sign;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/30549830/attachment-damages-signature-part-2">
 * Attachment damages signature part 2
 * </a>
 * <p>
 * The OP's original code is in {@link TC3}. From there the method {@link TC3#doSign()} is
 * copied here with different degrees of manipulations,
 * {@link #doSignOriginal(InputStream, InputStream, File)} being essentially the original
 * code with dynamic input streams and output file,
 * {@link #doSignRemoveType(InputStream, InputStream, File)} fixes the misleading trailer
 * Type entry resulting in valid signatures,
 * {@link #doSignTwoRevisions(InputStream, InputStream, File)} tries to use an incremental
 * update for the first change, too, but fails, and
 * {@link #doSignOneStep(InputStream, InputStream, File)} tries to add the image in the
 * same incremental update as the image but fails for cross reference table sources. 
 * </p>
 * @author mkl
 */
public class SignLikeUnOriginalToo
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * Essentially original code applied to PDF with cross reference table. OK.
     */
    @Test
    public void testTest_Orig() throws Exception
    {
        try (   InputStream source = getClass().getResourceAsStream("test.pdf");
                InputStream logo = getClass().getResourceAsStream("Willi-1.jpg") )
        {
            doSignOriginal(source, logo, new File(RESULT_FOLDER, "test-signedOrig.pdf"));
        }
    }

    /**
     * Essentially original code applied to PDF with cross reference stream. Invalid.
     */
    @Test
    public void testAcroForm_Orig() throws Exception
    {
        try (   InputStream source = getClass().getResourceAsStream("/mkl/testarea/pdfbox1/form/acroform.pdf");
                InputStream logo = getClass().getResourceAsStream("Willi-1.jpg") )
        {
            doSignOriginal(source, logo, new File(RESULT_FOLDER, "acroform-signedOrig.pdf"));
        }
    }

    /**
     * Original code changed to remove the Type entry from the trailer
     * applied to PDF with cross reference table. OK.
     */
    @Test
    public void testTest_RemoveType() throws Exception
    {
        try (   InputStream source = getClass().getResourceAsStream("test.pdf");
                InputStream logo = getClass().getResourceAsStream("Willi-1.jpg") )
        {
            doSignRemoveType(source, logo, new File(RESULT_FOLDER, "test-signedRemoveType.pdf"));
        }
    }

    /**
     * Original code changed to remove the Type entry from the trailer
     * applied to PDF with cross reference stream. OK.
     */
    @Test
    public void testAcroForm_RemoveType() throws Exception
    {
        try (   InputStream source = getClass().getResourceAsStream("/mkl/testarea/pdfbox1/form/acroform.pdf");
                InputStream logo = getClass().getResourceAsStream("Willi-1.jpg") )
        {
            doSignRemoveType(source, logo, new File(RESULT_FOLDER, "acroform-signedRemoveType.pdf"));
        }
    }

    /**
     * Original code changed to also save the first change as new revision
     * applied to PDF with cross reference table. Exception.
     */
    @Test
    public void testTest_TwoRevisions() throws Exception
    {
        try (   InputStream source = getClass().getResourceAsStream("test.pdf");
                InputStream logo = getClass().getResourceAsStream("Willi-1.jpg") )
        {
            doSignTwoRevisions(source, logo, new File(RESULT_FOLDER, "test-signedTwoRevisions.pdf"));
        }
    }

    /**
     * Original code changed to also save the first change as new revision
     * applied to PDF with cross reference stream. Exception.
     */
    @Test
    public void testAcroForm_TwoRevisions() throws Exception
    {
        try (   InputStream source = getClass().getResourceAsStream("/mkl/testarea/pdfbox1/form/acroform.pdf");
                InputStream logo = getClass().getResourceAsStream("Willi-1.jpg") )
        {
            doSignTwoRevisions(source, logo, new File(RESULT_FOLDER, "acroform-signedTwoRevisions.pdf"));
        }
    }

    /**
     * Original code changed to add the image and sign at the same time
     * applied to PDF with cross reference table. Page content issue, missing image resource.
     */
    @Test
    public void testTest_OneStep() throws Exception
    {
        try (   InputStream source = getClass().getResourceAsStream("test.pdf");
                InputStream logo = getClass().getResourceAsStream("Willi-1.jpg") )
        {
            doSignOneStep(source, logo, new File(RESULT_FOLDER, "test-signedOneStep.pdf"));
        }
    }

    /**
     * Original code changed to add the image and sign at the same time
     * applied to PDF with cross reference stream. OK.
     */
    @Test
    public void testAcroForm_OneStep() throws Exception
    {
        try (   InputStream source = getClass().getResourceAsStream("/mkl/testarea/pdfbox1/form/acroform.pdf");
                InputStream logo = getClass().getResourceAsStream("Willi-1.jpg") )
        {
            doSignOneStep(source, logo, new File(RESULT_FOLDER, "acroform-signedOneStep.pdf"));
        }
    }

    /**
     * Essentially the original code from {@link TC3#doSign()} with dynamic input streams and output file.
     */
    public void doSignOriginal(InputStream inputStream, InputStream logoStream, File outputDocument) throws Exception
    {
        byte inputBytes[] = IOUtils.toByteArray(inputStream);
        PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(inputBytes));
        PDJpeg ximage = new PDJpeg(pdDocument, ImageIO.read(logoStream));
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

        pdDocument.addSignature(signature, new TC3());

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

    /**
     * {@link #doSignOriginal(InputStream, InputStream, File)} changed to remove the Type entry from the trailer.
     */
    public void doSignRemoveType(InputStream inputStream, InputStream logoStream, File outputDocument) throws Exception
    {
        byte inputBytes[] = IOUtils.toByteArray(inputStream);
        PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(inputBytes));
        PDJpeg ximage = new PDJpeg(pdDocument, ImageIO.read(logoStream));
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
        pdDocument.getDocument().getTrailer().removeItem(COSName.TYPE);

        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("signer name");
        signature.setLocation("signer location");
        signature.setReason("reason for signature");
        signature.setSignDate(Calendar.getInstance());

        pdDocument.addSignature(signature, new TC3());

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

    /**
     * {@link #doSignOriginal(InputStream, InputStream, File)} changed to also save the first change as new revision.
     */
    public void doSignTwoRevisions(InputStream inputStream, InputStream logoStream, File outputDocument) throws Exception
    {
        FileOutputStream fos = new FileOutputStream(outputDocument);
        FileInputStream fis = new FileInputStream(outputDocument);

        byte inputBytes[] = IOUtils.toByteArray(inputStream);

        PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(inputBytes));
        PDJpeg ximage = new PDJpeg(pdDocument, ImageIO.read(logoStream));
        PDPage page = (PDPage) pdDocument.getDocumentCatalog().getAllPages().get(0);
        PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page, true, true);
        contentStream.drawXObject(ximage, 50, 50, 356, 40);
        contentStream.close();

        fos.write(inputBytes);
        pdDocument.saveIncremental(fis, fos);
        pdDocument.close();

        pdDocument = PDDocument.load(outputDocument);

        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("signer name");
        signature.setLocation("signer location");
        signature.setReason("reason for signature");
        signature.setSignDate(Calendar.getInstance());

        pdDocument.addSignature(signature, new TC3());

        fos = new FileOutputStream(outputDocument, true);
        fis = new FileInputStream(outputDocument);

        pdDocument.saveIncremental(fis, fos);
        pdDocument.close();
    }

    /**
     * {@link #doSignOriginal(InputStream, InputStream, File)} changed to add the image and sign at the same time.
     */
    public void doSignOneStep(InputStream inputStream, InputStream logoStream, File outputDocument) throws Exception
    {
        byte inputBytes[] = IOUtils.toByteArray(inputStream);
        PDDocument pdDocument = PDDocument.load(new ByteArrayInputStream(inputBytes));

        PDJpeg ximage = new PDJpeg(pdDocument, ImageIO.read(logoStream));
        PDPage page = (PDPage) pdDocument.getDocumentCatalog().getAllPages().get(0);
        PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page, true, true);
        contentStream.drawXObject(ximage, 50, 50, 356, 40);
        contentStream.close();

        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("signer name");
        signature.setLocation("signer location");
        signature.setReason("reason for signature");
        signature.setSignDate(Calendar.getInstance());

        pdDocument.addSignature(signature, new TC3());

        FileOutputStream fos = new FileOutputStream(outputDocument);
        fos.write(inputBytes);
        FileInputStream is = new FileInputStream(outputDocument);

        pdDocument.saveIncremental(is, fos);
    }
}
