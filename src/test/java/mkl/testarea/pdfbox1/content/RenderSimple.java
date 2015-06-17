package mkl.testarea.pdfbox1.content;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/30882927/how-to-generate-dyanamic-no-of-pages-using-pdfbox">
 * How to generate Dyanamic no of pages using PDFBOX 
 * </a>
 * <p>
 * Using the simple PDF rendering class {@link PdfRenderingSimple}.
 * </p>
 * 
 * @author mklink
 */
public class RenderSimple
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testSimple() throws IOException, COSVisitorException
    {
        PDDocument doc = new PDDocument();

        PdfRenderingSimple renderer = new PdfRenderingSimple(doc);
        for (int i = 0; i < 2000; i++)
        {
            renderer.renderText("hello" + i, 60);
        }
        renderer.close();

        doc.save(new File(RESULT_FOLDER, "renderSimple.pdf"));
        doc.close();
    }

}
