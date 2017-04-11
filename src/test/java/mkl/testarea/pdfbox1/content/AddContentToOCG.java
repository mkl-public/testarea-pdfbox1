/**
 * 
 */
package mkl.testarea.pdfbox1.content;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.pdmodel.markedcontent.PDPropertyList;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class AddContentToOCG
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/43275212/how-do-i-make-modifications-to-existing-layeroptional-content-group-in-pdf">
     * How do I make modifications to existing layer(Optional Content Group) in pdf?
     * </a>
     * <p>
     * This test show how add content to an optional content group which
     * may or may not already exist.
     * </p>
     * @see #addTextToLayer(PDDocument, int, String, float, float, String)
     */
    @Test
    public void testAddContentToNewOrExistingOCG() throws COSVisitorException, IOException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        addTextToLayer(document, 0, "MyLayer", 30, 600, "Text in new layer 'MyLayer'");
        addTextToLayer(document, 0, "MyOtherLayer", 230, 550, "Text in new layer 'MyOtherLayer'");
        addTextToLayer(document, 0, "MyLayer", 30, 500, "Text in existing layer 'MyLayer'");
        addTextToLayer(document, 0, "MyOtherLayer", 230, 450, "Text in existing layer 'MyOtherLayer'");

        document.save(new File(RESULT_FOLDER, "TextInOCGs.pdf"));
        document.close();
    }

    /**
     * @see #testAddContentToNewOrExistingOCG()
     */
    void addTextToLayer(PDDocument document, int pageNumber, String layerName, float x, float y, String text) throws IOException
    {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDOptionalContentProperties ocprops = catalog.getOCProperties();
        if (ocprops == null)
        {
            ocprops = new PDOptionalContentProperties();
            catalog.setOCProperties(ocprops);
        }
        PDOptionalContentGroup layer = null;
        if (ocprops.hasGroup(layerName))
        {
            layer = ocprops.getGroup(layerName);
        }
        else
        {
            layer = new PDOptionalContentGroup(layerName);
            ocprops.addGroup(layer);
        }

        PDPage page = (PDPage) document.getDocumentCatalog().getAllPages().get(pageNumber);

        PDResources resources = page.findResources();
        if (resources == null)
        {
            resources = new PDResources();
            page.setResources(resources);
        }
        PDPropertyList props = resources.getProperties();
        if (props == null)
        {
            props = new PDPropertyList();
            resources.setProperties(props);
        }

        //Find first free resource name with the pattern "MC<index>"
        int index = 0;
        PDOptionalContentGroup ocg;
        COSName resourceName;
        do
        {
            resourceName = COSName.getPDFName("MC" + index);
            ocg = props.getOptionalContentGroup(resourceName);
            index++;
        } while (ocg != null);
        //Put mapping for our new layer/OCG
        props.putMapping(resourceName, layer);

        PDFont font = PDType1Font.HELVETICA;

        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, true, true);
        contentStream.beginMarkedContentSequence(COSName.OC, resourceName);
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.moveTextPositionByAmount(x, y);
        contentStream.drawString(text);
        contentStream.endText();
        contentStream.endMarkedContentSequence();
        
        contentStream.close();
    }
}
