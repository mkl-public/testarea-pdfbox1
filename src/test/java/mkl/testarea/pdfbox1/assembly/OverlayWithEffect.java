package mkl.testarea.pdfbox1.assembly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.util.MapUtil;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test is about overlaying with special effect.
 * 
 * @author mkl
 */
public class OverlayWithEffect
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "assembly");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/29095822/pdfbox-overlay-fails">
     * PDFBox Overlay fails
     * </a>
     *
     * Test using the OPs files
     * https://dl.dropboxusercontent.com/u/21842502/picas/document1.pdf
     * https://dl.dropboxusercontent.com/u/21842502/picas/overlay.pdf
     */
    @Test
    public void testOverlayWithDarkenVolker() throws COSVisitorException, IOException
    {
        try (   InputStream sourceStream = getClass().getResourceAsStream("document1.pdf");
                InputStream overlayStream = getClass().getResourceAsStream("overlay.pdf")  )
        {
            final PDDocument document = PDDocument.load(sourceStream);
            final PDDocument overlay = PDDocument.load(overlayStream);
            
            overlayWithDarkenBlendMode(document, overlay);

            document.save(new File(RESULT_FOLDER, "document1-with-overlay.pdf"));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/29095822/pdfbox-overlay-fails">
     * PDFBox Overlay fails
     * </a>
     *
     * Test using other files.
     */
    @Test
    public void testOverlayWithDarkenMisc() throws COSVisitorException, IOException
    {
        try (   InputStream sourceStream = getClass().getResourceAsStream("test.pdf");
                InputStream overlayStream = getClass().getResourceAsStream("test misplaced.pdf")  )
        {
            final PDDocument document = PDDocument.load(sourceStream);
            final PDDocument overlay = PDDocument.load(overlayStream);
            
            overlayWithDarkenBlendMode(document, overlay);

            document.save(new File(RESULT_FOLDER, "test-with-overlay.pdf"));
        }
    }

    void overlayWithDarkenBlendMode(PDDocument document, PDDocument overlay) throws IOException
    {
        PDXObjectForm xobject = importAsXObject(document, (PDPage) overlay.getDocumentCatalog().getAllPages().get(0));
        PDExtendedGraphicsState darken = new PDExtendedGraphicsState();
        darken.getCOSDictionary().setName("BM", "Darken");
        
        List<PDPage> pages = document.getDocumentCatalog().getAllPages();

        for (PDPage page: pages)
        {
            Map<String, PDExtendedGraphicsState> states = page.getResources().getGraphicsStates();
            if (states == null)
                states = new HashMap<String, PDExtendedGraphicsState>();
            String darkenKey = MapUtil.getNextUniqueKey(states, "Dkn");
            states.put(darkenKey, darken);
            page.getResources().setGraphicsStates(states);

            PDPageContentStream stream = new PDPageContentStream(document, page, true, false, true);
            stream.appendRawCommands(String.format("/%s gs ", darkenKey));
            stream.drawXObject(xobject, 0, 0, 1, 1);
            stream.close();
        }
    }

    PDXObjectForm importAsXObject(PDDocument target, PDPage page) throws IOException
    {
        final PDStream xobjectStream = new PDStream(target, page.getContents().createInputStream(), false);
        final PDXObjectForm xobject = new PDXObjectForm(xobjectStream);

        xobject.setResources(page.findResources());
        xobject.setBBox(page.findCropBox());

        COSDictionary group = new COSDictionary();
        group.setName("S", "Transparency");
        group.setBoolean(COSName.getPDFName("K"), true);
        xobject.getCOSStream().setItem(COSName.getPDFName("Group"), group);

        return xobject;
    }
}
