package mkl.testarea.pdfbox1.extract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/29554400/apache-pdfbox-remove-spaces-between-characters">
 * Apache PDFBox Remove Spaces between characters
 * </a>
 * <p>
 * The issue at hand turns out to be due to overlapping space characters in the same position
 * as regular characters. This can be circumvented by removing all spaces.
 * </p>
 * 
 * @author mkl
 */
public class ExtractWithoutExtraSpaces
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testExtractFromTestDocument() throws COSVisitorException, IOException
    {
        byte[] documentBytes = createTestDocument();
        Files.write(new File(RESULT_FOLDER, "TestDocument.pdf").toPath(), documentBytes);

        try (   InputStream documentStream = new ByteArrayInputStream(documentBytes);
                PDDocument document = PDDocument.load(documentStream))
        {
            String normal = extractNormal(document);
            String noSpaces = extractNoSpaces(document);

            System.out.println("\nTestDocument, extract normally:");
            System.out.println("> " + normal);
            System.out.println("\nTestDocument, extract no-spaces:");
            System.out.println("> " + noSpaces);
        }
    }

    String extractNormal(PDDocument document) throws IOException
    {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        return stripper.getText(document);
    }

    String extractNoSpaces(PDDocument document) throws IOException
    {
        PDFTextStripper stripper = new PDFTextStripper()
        {
            @Override
            protected void processTextPosition(TextPosition text)
            {
                String character = text.getCharacter();
                if (character != null && character.trim().length() != 0)
                    super.processTextPosition(text);
            }
        };
        stripper.setSortByPosition(true);
        return stripper.getText(document);
    }

    byte[] createTestDocument() throws IOException, COSVisitorException
    {
        try (   ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PDDocument doc = new PDDocument()   )
        {
            PDPage page = new PDPage(new PDRectangle(792, 612));
            doc.addPage(page);
            
            PDFont font = PDType1Font.COURIER;

            PDPageContentStream contents = new PDPageContentStream(doc, page);
            contents.beginText();
            contents.setFont(font, 9);
            contents.moveTextPositionByAmount(100, 500);
            contents.drawString("             2                                                                  Netto        5,00 EUR 3,00");
            contents.moveTextPositionByAmount(0, 0);
            contents.drawString("                2882892  ENERGIZE LR6 Industrial                     2,50 EUR 1");
            contents.endText();
            contents.close();
            
            doc.save(baos);
            
            return baos.toByteArray();
        }
    }
}
