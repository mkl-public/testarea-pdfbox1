package mkl.testarea.pdfbox1.content;

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
 * This test focuses on drawing special characters, something the PDFBox method
 * {@link PDPageContentStream#drawString(String)} is not really good at-
 * 
 * @author mkl
 */
public class DrawSpecialCharacters
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/22260344/pdfbox-encode-symbol-currency-euro">
     * PdfBox encode symbol currency euro
     * </a>
     * <p>
     * Three ways of trying to draw a '€' symbol, the first one fails.
     * </p>
     */
    @Test
    public void testDrawEuro() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contents = new PDPageContentStream(document, page);
        PDFont font = PDType1Font.HELVETICA_BOLD;
        contents.beginText();
        contents.setFont(font, 18);
        contents.moveTextPositionByAmount(30, 600);
        contents.drawString("€");
        contents.moveTextPositionByAmount(0, -30);
        contents.drawString(String.valueOf(Character.toChars(EncodingManager.INSTANCE.getEncoding(COSName.WIN_ANSI_ENCODING).getCode("Euro"))));
        contents.moveTextPositionByAmount(0, -30);
        byte[] commands = "(x) Tj ".getBytes();
        commands[1] = (byte) 128;
        contents.appendRawCommands(commands);
        contents.endText();
        contents.close();
        document.save(new File(RESULT_FOLDER, "Euro.pdf"));
        document.close();
    }

    /**
     * <a href="http://stackoverflow.com/questions/30619974/pdfbox-unable-to-write-superscripted-characters">
     * PDFBox unable to write superscripted characters
     * </a>
     * <p>
     * {@link #testDrawTmSignBroken()} represents the observation the OP made,
     * {@link #testDrawTmSignCustomDraw()} is a solution using custom drawing operations, and
     * {@link #testDrawTmSignLetters()} is a solution drawing the trademark symbol using smaller letters with text rise.
     * </p>
     */
	@Test
	public void testDrawTmSignBroken() throws IOException, COSVisitorException
	{
		PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contents = new PDPageContentStream(document, page);
        PDFont font = PDType1Font.HELVETICA_BOLD;
        contents.beginText();
        contents.setFont(font, 18);
        contents.moveTextPositionByAmount(30, 600);
        contents.drawString("90000039-PREDISOL ® C YELLOW 13 SNDOT™M");
        contents.endText();
        contents.close();
        document.save(new File(RESULT_FOLDER, "TM_naive.pdf"));
        document.close();
	}

	@Test
	public void testDrawTmSignCustomDraw() throws IOException, COSVisitorException
	{
		PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contents = new PDPageContentStream(document, page);
        PDFont font = PDType1Font.HELVETICA_BOLD;
        contents.beginText();
        contents.setFont(font, 18);
        contents.moveTextPositionByAmount(30, 600);
        contents.drawString("90000039-PREDISOL ® C YELLOW 13 SNDOT");
        byte[] commands = "(x) Tj ".getBytes();
        commands[1] = (byte) 0231;
        contents.appendRawCommands(commands);
        contents.drawString("M");
        contents.endText();
        contents.close();
        document.save(new File(RESULT_FOLDER, "TM_customDraw.pdf"));
        document.close();
	}

	@Test
	public void testDrawTmSignLetters() throws IOException, COSVisitorException
	{
		PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contents = new PDPageContentStream(document, page);
        PDFont font = PDType1Font.HELVETICA_BOLD;
        contents.beginText();
        contents.setFont(font, 18);
        contents.moveTextPositionByAmount(30, 600);
        contents.drawString("90000039-PREDISOL ® C YELLOW 13 SNDOT");
        contents.appendRawCommands("\n6 Ts\n".getBytes());
        contents.setFont(font, 10);
        contents.drawString("TM");
        contents.appendRawCommands("\n0 Ts\n".getBytes());
        contents.setFont(font, 18);
        contents.drawString("M");
        contents.endText();
        contents.close();
        document.save(new File(RESULT_FOLDER, "TM_letters.pdf"));
        document.close();
	}

}
