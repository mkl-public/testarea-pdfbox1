package mkl.testarea.pdfbox1.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test concerning rendering pages as images.
 * 
 * @author mkl
 */
public class RenderPage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "render");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/31921228/pixel-shifting-when-converting-pdf-into-image-files">
     * Pixel shifting when converting PDF into image files
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/sh/mii7lo3dsvi0kmx/AABNWZ7lbdgHkSQw4RTm1IDoa?dl=0">
     * PDFs and images
     * </a>
     * <p>
     * {@link #testRenderPageDMengCreated()} renders the PDF OverlappingExample-pdfdev1-20150810-090109-EDT.pdf and
     * {@link #testRenderPageDMengExpected()} renders the PDF OverlappingExample_Expected.pdf which was considered
     * equal in appearance, falsely as it turned out.
     * </p>
     */
    @Test
    public void testRenderPageDMengExpected() throws IOException
    {
        testRenderPageDMeng("OverlappingExample_Expected.pdf");
    }

    /**
     * <a href="http://stackoverflow.com/questions/31921228/pixel-shifting-when-converting-pdf-into-image-files">
     * Pixel shifting when converting PDF into image files
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/sh/mii7lo3dsvi0kmx/AABNWZ7lbdgHkSQw4RTm1IDoa?dl=0">
     * PDFs and images
     * </a>
     * <p>
     * {@link #testRenderPageDMengCreated()} renders the PDF OverlappingExample-pdfdev1-20150810-090109-EDT.pdf and
     * {@link #testRenderPageDMengExpected()} renders the PDF OverlappingExample_Expected.pdf which was considered
     * equal in appearance, falsely as it turned out.
     * </p>
     */
    @Test
    public void testRenderPageDMengCreated() throws IOException
    {
        testRenderPageDMeng("OverlappingExample-pdfdev1-20150810-090109-EDT.pdf");
    }

    /**
     * <a href="http://stackoverflow.com/questions/31921228/pixel-shifting-when-converting-pdf-into-image-files">
     * Pixel shifting when converting PDF into image files
     * </a>
     * <p>
     * Essentially the original code from the question.
     * </p>
     */
    public void testRenderPageDMeng(String fileName) throws IOException
    {
        try (InputStream pdfFile = getClass().getResourceAsStream(fileName) )
        {
            PDDocument document = PDDocument.load(pdfFile);
            List<PDPage> list = document.getDocumentCatalog().getAllPages();

            for (int i = 0; i < list.size(); i++) {
                PDPage temp = list.get(i);
                BufferedImage image = temp.convertToImage(BufferedImage.TYPE_INT_RGB, 150);
                File outputfile = new File(RESULT_FOLDER, fileName + "_" + (i + 1) + ".png");
                ImageIOUtil.writeImage(image, outputfile.getAbsolutePath(), 150);
            }
        }
    }
}
