package mkl.testarea.pdfbox1.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/33295483/how-can-i-extract-image-from-button-icon-in-pdf-using-apache-pdfbox">
 * How can i extract image from button icon in PDF using Apache PDFBox
 * </a>
 * <br/>
 * <a href="http://examples.itextpdf.com/results/part2/chapter08/advertisement2.pdf">advertisement2.pdf</a>
 * <br/>
 * <a href="http://examples.itextpdf.com/results/part2/chapter08/buttons.pdf">buttons.pdf</a>
 * <p>
 * This test demonstrates how to extract images from annotations in general and,
 * therefore, also buttons in particular.
 * </p>
 * 
 * @author mkl
 */
public class ExtractAnnotationImageTest
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * Test using <a href="http://examples.itextpdf.com/results/part2/chapter08/advertisement2.pdf">advertisement2.pdf</a>
     * created by <a href="http://itextpdf.com/examples/iia.php?id=156">part2.chapter08.ReplaceIcon</a>
     * from ITEXT IN ACTION — SECOND EDITION.
     */
    @Test
    public void testAdvertisement2Pdf() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("advertisement2.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            extractAnnotationImages(document, new File(RESULT_FOLDER, "advertisement2%s.%s").toString());;
        }
    }

    /**
     * Test using <a href="http://examples.itextpdf.com/results/part2/chapter08/buttons.pdf">buttons.pdf</a>
     * created by <a href="http://itextpdf.com/examples/iia.php?id=154">part2.chapter08.Buttons</a>
     * from ITEXT IN ACTION — SECOND EDITION.
     */
    @Test
    public void testButtonsPdf() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("buttons.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            extractAnnotationImages(document, new File(RESULT_FOLDER, "buttons%s.%s").toString());;
        }
    }

    /**
     * Test using <a href="http://www.docdroid.net/TDGVQzg/imageicon.pdf.html">imageicon.pdf</a>
     * created by the OP.
     */
    @Test
    public void testImageiconPdf() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("imageicon.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            extractAnnotationImages(document, new File(RESULT_FOLDER, "imageicon%s.%s").toString());;
        }
    }

    public void extractAnnotationImages(PDDocument document, String fileNameFormat) throws IOException
    {
        List<PDPage> pages = document.getDocumentCatalog().getAllPages();
        if (pages == null)
            return;

        for (int i = 0; i < pages.size(); i++)
        {
            String pageFormat = String.format(fileNameFormat, "-" + i + "%s", "%s");
            extractAnnotationImages(pages.get(i), pageFormat);
        }
    }

    public void extractAnnotationImages(PDPage page, String pageFormat) throws IOException
    {
        List<PDAnnotation> annotations = page.getAnnotations();
        if (annotations == null)
            return;
        
        for (int i = 0; i < annotations.size(); i++)
        {
            PDAnnotation annotation = annotations.get(i);
            String annotationFormat = annotation.getAnnotationName() != null && annotation.getAnnotationName().length() > 0
                    ? String.format(pageFormat, "-" + annotation.getAnnotationName() + "%s", "%s")
                    : String.format(pageFormat, "-" + i + "%s", "%s");
            extractAnnotationImages(annotation, annotationFormat);
        }
    }

    public void extractAnnotationImages(PDAnnotation annotation, String annotationFormat) throws IOException
    {
        PDAppearanceDictionary appearance = annotation.getAppearance();
        extractAnnotationImages(appearance.getDownAppearance(), String.format(annotationFormat, "-Down%s", "%s"));
        extractAnnotationImages(appearance.getNormalAppearance(), String.format(annotationFormat, "-Normal%s", "%s"));
        extractAnnotationImages(appearance.getRolloverAppearance(), String.format(annotationFormat, "-Rollover%s", "%s"));
    }

    public void extractAnnotationImages(Map<String, PDAppearanceStream> stateAppearances, String stateFormat) throws IOException
    {
        if (stateAppearances == null)
            return;

        for (Map.Entry<String, PDAppearanceStream> entry: stateAppearances.entrySet())
        {
            String appearanceFormat = String.format(stateFormat, "-" + entry.getKey() + "%s", "%s");
            extractAnnotationImages(entry.getValue(), appearanceFormat);
        }
    }

    public void extractAnnotationImages(PDAppearanceStream appearance, String appearanceFormat) throws IOException
    {
        PDResources resources = appearance.getResources();
        if (resources == null)
            return;
        Map<String, PDXObject> xObjects = resources.getXObjects();
        if (xObjects == null)
            return;

        for (Map.Entry<String, PDXObject> entry : xObjects.entrySet())
        {
            PDXObject xObject = entry.getValue();
            String xObjectFormat = String.format(appearanceFormat, "-" + entry.getKey() + "%s", "%s");
            if (xObject instanceof PDXObjectForm)
                extractAnnotationImages((PDXObjectForm)xObject, xObjectFormat);
            else if (xObject instanceof PDXObjectImage)
                extractAnnotationImages((PDXObjectImage)xObject, xObjectFormat);
        }
    }

    public void extractAnnotationImages(PDXObjectForm form, String imageFormat) throws IOException
    {
        PDResources resources = form.getResources();
        if (resources == null)
            return;
        Map<String, PDXObject> xObjects = resources.getXObjects();
        if (xObjects == null)
            return;

        for (Map.Entry<String, PDXObject> entry : xObjects.entrySet())
        {
            PDXObject xObject = entry.getValue();
            String xObjectFormat = String.format(imageFormat, "-" + entry.getKey() + "%s", "%s");
            if (xObject instanceof PDXObjectForm)
                extractAnnotationImages((PDXObjectForm)xObject, xObjectFormat);
            else if (xObject instanceof PDXObjectImage)
                extractAnnotationImages((PDXObjectImage)xObject, xObjectFormat);
        }
    }

    public void extractAnnotationImages(PDXObjectImage image, String imageFormat) throws IOException
    {
        image.write2OutputStream(new FileOutputStream(String.format(imageFormat, "", image.getSuffix())));
    }
}
