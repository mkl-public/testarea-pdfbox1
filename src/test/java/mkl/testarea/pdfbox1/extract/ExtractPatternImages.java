package mkl.testarea.pdfbox1.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractPatternImages
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34667268/extract-unselectable-content-from-pdf">
     * Extract unselectable content from PDF
     * </a>
     * <br>
     * <a href="https://dl.dropboxusercontent.com/u/2815529/test.pdf">
     * test.pdf
     * </a> as "testDrJorge.pdf"
     * <p>
     * This shows how to do pattern image exatrction. Unfortunately it has issues with CMYK
     * images, cf. <a href="https://issues.apache.org/jira/browse/PDFBOX-2128">PDFBOX-2128</a>.
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testtestDrJorge() throws IOException
    {
        try (InputStream resource = getClass().getResourceAsStream("testDrJorge.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            extractPatternImages(document, new File(RESULT_FOLDER, "testDrJorge%s.%s").toString());;
        }
    }

    public void extractPatternImages(PDDocument document, String fileNameFormat) throws IOException
    {
        List<PDPage> pages = document.getDocumentCatalog().getAllPages();
        if (pages == null)
            return;

        for (int i = 0; i < pages.size(); i++)
        {
            String pageFormat = String.format(fileNameFormat, "-" + i + "%s", "%s");
            extractPatternImages(pages.get(i), pageFormat);
        }
    }

    public void extractPatternImages(PDPage page, String pageFormat) throws IOException
    {
        PDResources resources = page.getResources();
        if (resources == null)
            return;
        Map<String, PDPatternResources> patterns = resources.getPatterns();
        
        for (Map.Entry<String, PDPatternResources> patternEntry : patterns.entrySet())
        {
            String patternFormat = String.format(pageFormat, "-" + patternEntry.getKey() + "%s", "%s");
            extractPatternImages(patternEntry.getValue(), patternFormat);
        }
    }
    
    public void extractPatternImages(PDPatternResources pattern, String patternFormat) throws IOException
    {
        COSDictionary resourcesDict = (COSDictionary) pattern.getCOSDictionary().getDictionaryObject(COSName.RESOURCES);
        if (resourcesDict == null)
            return;
        PDResources resources = new PDResources(resourcesDict);
        Map<String, PDXObject> xObjects = resources.getXObjects();
        if (xObjects == null)
            return;

        for (Map.Entry<String, PDXObject> entry : xObjects.entrySet())
        {
            PDXObject xObject = entry.getValue();
            String xObjectFormat = String.format(patternFormat, "-" + entry.getKey() + "%s", "%s");
            if (xObject instanceof PDXObjectForm)
                extractPatternImages((PDXObjectForm)xObject, xObjectFormat);
            else if (xObject instanceof PDXObjectImage)
                extractPatternImages((PDXObjectImage)xObject, xObjectFormat);
        }
    }

    public void extractPatternImages(PDXObjectForm form, String imageFormat) throws IOException
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
                extractPatternImages((PDXObjectForm)xObject, xObjectFormat);
            else if (xObject instanceof PDXObjectImage)
                extractPatternImages((PDXObjectImage)xObject, xObjectFormat);
        }

        Map<String, PDPatternResources> patterns = resources.getPatterns();
        
        for (Map.Entry<String, PDPatternResources> patternEntry : patterns.entrySet())
        {
            String patternFormat = String.format(imageFormat, "-" + patternEntry.getKey() + "%s", "%s");
            extractPatternImages(patternEntry.getValue(), patternFormat);
        }
    }

    public void extractPatternImages(PDXObjectImage image, String imageFormat) throws IOException
    {
        image.write2OutputStream(new FileOutputStream(String.format(imageFormat, "", image.getSuffix())));
    }
    
}
