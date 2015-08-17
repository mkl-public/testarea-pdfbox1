package mkl.testarea.pdfbox1.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author mkl
 */
public class ExtractText
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/32014589/how-to-read-data-from-table-structured-pdf-using-itextsharp">
     * How to read data from table-structured PDF using itextsharp?
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/jwsuu6mz9ez84ss/sampleFile.pdf?dl=0">
     * sampleFile.pdf
     * </a>
     * <p>
     * The extraction behavior of PDFBox as used here, i.e. with <code>SortByPosition</code>
     * being <code>false</code>, can be emulated with iText(Sharp) by explicitly using the
     * {@link com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy}.
     * </p>
     * 
     * @see mkl.testarea.itext5.extract.TextExtraction
     */
    @Test
    public void testExtractFromSampleFile() throws COSVisitorException, IOException
    {
        try (   InputStream documentStream = getClass().getResourceAsStream("sampleFile.pdf");
                PDDocument document = PDDocument.load(documentStream))
        {
            String normal = extractNormal(document);

            System.out.println("\n'sampleFile.pdf', extract normally:");
            System.out.println(normal);
            System.out.println("***********************************");
        }
    }

    // extract WITHOUT SortByPosition
    String extractNormal(PDDocument document) throws IOException
    {
        PDFTextStripper stripper = new PDFTextStripper();
        //stripper.setSortByPosition(true);
        return stripper.getText(document);
    }
}
