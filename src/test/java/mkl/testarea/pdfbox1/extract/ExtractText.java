package mkl.testarea.pdfbox1.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
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

    /**
     * <a href="http://stackoverflow.com/questions/32978179/using-pdfbox-to-get-location-of-line-of-text">
     * Using PDFBox to get location of line of text
     * </a>
     * <p>
     * This example shows how to extract text with the additional information of
     * the x coordinate at the start of line.
     * </p>
     */
    @Test
    public void testExtractLineStartFromSampleFile() throws COSVisitorException, IOException
    {
        try (   InputStream documentStream = getClass().getResourceAsStream("sampleFile.pdf");
                PDDocument document = PDDocument.load(documentStream))
        {
            String normal = extractLineStart(document);

            System.out.println("\n'sampleFile.pdf', extract with line starts:");
            System.out.println(normal);
            System.out.println("***********************************");
        }
    }
    
    String extractLineStart(PDDocument document) throws IOException
    {
        PDFTextStripper stripper = new PDFTextStripper()
        {
            @Override
            protected void writeLineSeparator() throws IOException
            {
                startOfLine = true;
                super.writeLineSeparator();
            }

            @Override
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException
            {
                if (startOfLine)
                {
                    TextPosition firstProsition = textPositions.get(0);
                    writeString(String.format("[%s]", firstProsition.getXDirAdj()));
                    startOfLine = false;
                }
                super.writeString(text, textPositions);
            }
            boolean startOfLine = true;
        };
        //stripper.setSortByPosition(true);
        return stripper.getText(document);
    }

}
