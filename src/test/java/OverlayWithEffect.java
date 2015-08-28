import java.io.File;
import java.io.FileInputStream;
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

/**
 * <a href="http://stackoverflow.com/questions/32269571/add-a-watermark-on-a-pdf-that-contains-images-using-pdfbox">
 * Add a watermark on a pdf that contains images using pdfbox
 * </a>
 * <br>
 * <a href="https://drive.google.com/file/d/0B7hG2Ap47MTKMEpqZkpudG1CYjg/view?usp=sharing">
 * pdf-test.pdf
 * </a>
 * <br>
 * <a href="https://drive.google.com/file/d/0B7hG2Ap47MTKTjdfbzl5Q3pZUk0/view?usp=sharing">
 * draft1.pdf
 * </a>
 * <p>
 * This class contains the code the OP provided in his question, differing only in the file locations.
 * Tested overlaying the OP's files, no issue observed.
 * </p>
 * 
 * @author mkl
 */
public class OverlayWithEffect
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "assembly");

    public static void overlayWithDarkenBlendMode(PDDocument document, PDDocument overlay) throws IOException
    {
        PDXObjectForm xobject = importAsXObject(document, (PDPage) overlay.getDocumentCatalog().getAllPages().get(0));
        PDExtendedGraphicsState darken = new PDExtendedGraphicsState();
        darken.getCOSDictionary().setName("BM", "Darken");

        List<PDPage> pages = document.getDocumentCatalog().getAllPages();

        for (PDPage page: pages)
        {
            if (page.getResources() == null) {
                page.setResources(page.findResources());
            }

            if (page.getResources() != null) {
                Map<String, PDExtendedGraphicsState> states = page.getResources().getGraphicsStates();
                if (states == null) {
                    states = new HashMap<String, PDExtendedGraphicsState>();
                }
                String darkenKey = MapUtil.getNextUniqueKey(states, "Dkn");
                states.put(darkenKey, darken);
                page.getResources().setGraphicsStates(states);
                PDPageContentStream stream = new PDPageContentStream(document, page, true, false, true);
                stream.appendRawCommands(String.format("/%s gs ", darkenKey));
                stream.drawXObject(xobject, 0, 0, 1, 1);
                stream.close();
            }
        }
    }

    public static PDXObjectForm importAsXObject(PDDocument target, PDPage page) throws IOException
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


    public static void main(String[] args) throws COSVisitorException, IOException
    {
        InputStream sourceStream = new FileInputStream("src/test/resources/mkl/testarea/pdfbox1/assembly/pdf-test.pdf");
        InputStream overlayStream = new FileInputStream("src/test/resources/mkl/testarea/pdfbox1/assembly/draft1.pdf");
        try {
            final PDDocument document = PDDocument.load(sourceStream);
            final PDDocument overlay = PDDocument.load(overlayStream);

            overlayWithDarkenBlendMode(document, overlay);

            document.save(new File(RESULT_FOLDER, "da-draft-5.pdf").toString());
            document.close();
        }
        finally {
            sourceStream.close();
            overlayStream.close();
        }
    }    
}