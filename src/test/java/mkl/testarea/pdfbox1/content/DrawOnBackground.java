package mkl.testarea.pdfbox1.content;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.EncodingManager;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class DrawOnBackground
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/33440450/pdfbox-how-to-draw-text-on-top-of-a-filled-rectangle">
     * PDFBox: How to draw text on top of a filled rectangle?
     * </a>
     * <p>
     * The OP's code works. Thus, the cause for his issue lies beyond the code he provided.
     * </p>
     */
    @Test
    public void testDrawOnBackgroundLikeDave823() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream content = new PDPageContentStream(document, page);
        PDFont font = PDType1Font.HELVETICA_BOLD;

        int cursorX = 70;
        int cursorY = 500;

        //draw rectangle
        content.setNonStrokingColor(200, 200, 200); //gray background
        content.fillRect(cursorX, cursorY, 100, 50);

        //draw text
        content.setNonStrokingColor(0, 0, 0); //black text
        content.beginText();
        content.setFont(font, 12);
        content.moveTextPositionByAmount(cursorX, cursorY);
        content.drawString("Test Data");
        content.endText();

        content.close();
        document.save(new File(RESULT_FOLDER, "textOnBackground.pdf"));
        document.close();
    }

}
