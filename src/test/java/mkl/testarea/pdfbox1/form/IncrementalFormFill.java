package mkl.testarea.pdfbox1.form;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test class tests form filling in combination with the rudimentary
 * incremental update mechanism provided by PDFBox.
 * 
 * @author mkl
 */
public class IncrementalFormFill
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

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
     * <a href="https://www.dropbox.com/s/xf5pb0ng8k9zd4i/doc_v2.pdf?dl=0">doc_v2.pdf</a>
     * <br/>
     * <a href="https://www.dropbox.com/s/s8295tfyjpe1l4l/doc_v2_fillsigned.pdf?dl=0">doc_v2_fillsigned.pdf</a>
     * <p>
     * The cause of the problem is that somehow during the original filling and signing the fonts in the default
     * resources of the interactive form dictionary got lost.
     * </p><p>
     * In the original document doc_v2.pdf the interactive form dictionary contains entries for ZaDb and Helv
     * in the Font dictionary in the default resources DR dictionary; so form filli-in works, cf.
     * {@link #testFillWithoutFillAndSign()}.
     * </p><p>
     * In contrast in the interactive form dictionary of the filled and signed document doc_v2_fillsigned.pdf
     * the Font dictionary in the default resources DR dictionary is missing; so form fill-in fails, cf.
     * {@link #testFillAfterFillAndSign()}.
     * </p>
     */
    @Test
    public void testFillAfterFillAndSign() throws COSVisitorException, IOException
    {
        File currentDocument = new File("src/test/resources/mkl/testarea/pdfbox1/form/doc_v2_fillsigned.pdf");
        File newDocument = new File(RESULT_FOLDER, "doc_v2_fillsigned_filled.pdf");

        String fieldName = "New Emergency Contact";
        String value = "test";
        PDDocument doc = null;

        try (FileOutputStream fos = new FileOutputStream(newDocument))
        {
            Files.copy(currentDocument.toPath(), fos);

            doc = PDDocument.load(currentDocument);
            PDDocumentCatalog catalog = doc.getDocumentCatalog();

            catalog.getCOSObject().setNeedToBeUpdate(true);
            catalog.getPages().getCOSObject().setNeedToBeUpdate(true);

            PDAcroForm form = catalog.getAcroForm();

            form.getCOSObject().setNeedToBeUpdate(true);
            form.getDefaultResources().getCOSObject().setNeedToBeUpdate(true);

            PDField field = form.getField(fieldName);
            field.setValue(value); // here the exception occurs.

            // What should happen afterwards:
            field.getCOSObject().setNeedToBeUpdate(true);
            field.getAcroForm().getCOSObject().setNeedToBeUpdate(true);

            ((COSDictionary) field.getDictionary().getDictionaryObject("AP")).getDictionaryObject("N").setNeedToBeUpdate(true);

            try (FileInputStream fis = new FileInputStream(newDocument))
            {
                doc.saveIncremental(fis, fos);
            }
        }
        finally
        {
            if (null != doc)
            {
                doc.close();
                doc = null;
            }
        }
    }

    /**
     * @see #testFillAfterFillAndSign()
     */
    @Test
    public void testFillWithoutFillAndSign() throws COSVisitorException, IOException
    {
        File currentDocument = new File("src/test/resources/mkl/testarea/pdfbox1/form/doc_v2.pdf");
        File newDocument = new File(RESULT_FOLDER, "doc_v2_filled.pdf");

        String fieldName = "New Emergency Contact";
        String value = "test";
        PDDocument doc = null;

        try (FileOutputStream fos = new FileOutputStream(newDocument))
        {
            Files.copy(currentDocument.toPath(), fos);

            doc = PDDocument.load(currentDocument);
            PDDocumentCatalog catalog = doc.getDocumentCatalog();

            catalog.getCOSObject().setNeedToBeUpdate(true);
            catalog.getPages().getCOSObject().setNeedToBeUpdate(true);

            PDAcroForm form = catalog.getAcroForm();

            form.getCOSObject().setNeedToBeUpdate(true);
            form.getDefaultResources().getCOSObject().setNeedToBeUpdate(true);

            PDField field = form.getField(fieldName);
            field.setValue(value); // here the exception occurs.

            // What should happen afterwards:
            field.getCOSObject().setNeedToBeUpdate(true);
            field.getAcroForm().getCOSObject().setNeedToBeUpdate(true);

            ((COSDictionary) field.getDictionary().getDictionaryObject("AP")).getDictionaryObject("N").setNeedToBeUpdate(true);

            try (FileInputStream fis = new FileInputStream(newDocument))
            {
                doc.saveIncremental(fis, fos);
            }
        }
        finally
        {
            if (null != doc)
            {
                doc.close();
                doc = null;
            }
        }
    }
}
