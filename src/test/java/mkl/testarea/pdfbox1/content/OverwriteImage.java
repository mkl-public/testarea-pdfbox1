package mkl.testarea.pdfbox1.content;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import mkl.testarea.pdfbox1.content.ImageLocator.ImageLocation;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/31009949/replacing-images-with-same-resource-in-pdfbox">
 * Replacing images with same resource in PDFBox
 * </a>
 * <p>
 * Sample code showing how to overwrite images..
 * </p>
 * 
 * @author mkl
 */
public class OverwriteImage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * Applying the code to the OP's sample file from a former question.
     * 
     * @throws IOException
     * @throws CryptographyException
     * @throws COSVisitorException
     */
    @Test
    public void testDrunkenFistSample() throws IOException, CryptographyException, COSVisitorException
    {
        try (   InputStream resource = getClass().getResourceAsStream("sample.pdf");
                InputStream left = getClass().getResourceAsStream("left.png");
                InputStream right = getClass().getResourceAsStream("right.png");
                PDDocument document = PDDocument.load(resource) )
        {
            if (document.isEncrypted())
            {
                document.decrypt("");
            }
            
            PDJpeg leftImage = new PDJpeg(document, ImageIO.read(left));
            PDJpeg rightImage = new PDJpeg(document, ImageIO.read(right));

            ImageLocator locator = new ImageLocator();
            List<?> allPages = document.getDocumentCatalog().getAllPages();
            for (int i = 0; i < allPages.size(); i++)
            {
                PDPage page = (PDPage) allPages.get(i);
                locator.processStream(page, page.findResources(), page.getContents().getStream());
            }

            for (ImageLocation location : locator.getLocations())
            {
                PDRectangle cropBox = location.getPage().findCropBox();
                float center = (cropBox.getLowerLeftX() + cropBox.getUpperRightX()) / 2.0f;
                PDJpeg image = location.getMatrix().getXPosition() < center ? leftImage : rightImage;
                AffineTransform transform = location.getMatrix().createAffineTransform();

                PDPageContentStream content = new PDPageContentStream(document, location.getPage(), true, false, true);
                content.drawXObject(image, transform);
                content.close();
            }

            document.save(new File(RESULT_FOLDER, "sample-changed.pdf"));
        }
    }

}
