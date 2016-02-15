package mkl.testarea.pdfbox1.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractPaths
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35409283/how-to-find-table-border-lines-in-pdf-using-pdfbox">
     * How to find table border lines in pdf using PDFBox?
     * </a>
     * <br/>
     * <a href="http://stats.bls.gov/news.release/pdf/empsit.pdf">empsit.pdf</a>
     * <p>
     * This test uses the {@link PrintPaths} class to print the lines from page 8
     * of the sample document.
     * </p>
     */
    @Test
    public void testPrintPathsEmpsit8() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("empsit.pdf") )
        {
            PDDocument document = PDDocument.load(resource);
            List<?> allPages = document.getDocumentCatalog().getAllPages();
            int i = 7; // page 8

            System.out.println("\n\nPage " + (i+1));
            PrintPaths printPaths = new PrintPaths();

            PDPage page = (PDPage) allPages.get(i);
            PDStream contents = page.getContents();
            if (contents != null)
            {
                printPaths.processStream(page, page.findResources(), page.getContents().getStream());
            }
        }
    }
}
