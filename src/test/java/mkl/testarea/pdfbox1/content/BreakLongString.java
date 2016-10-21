package mkl.testarea.pdfbox1.content;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class BreakLongString
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/19635275/how-to-generate-multiple-lines-in-pdf-using-apache-pdfbox">
     * How to generate multiple lines in PDF using Apache pdfbox
     * </a>
     * <p>
     * This test shows how to split a long string into multiple lines.
     * </p>
     */
    @Test
    public void testBreakString() throws COSVisitorException, IOException
    {
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);

            PDFont pdfFont = PDType1Font.HELVETICA;
            float fontSize = 25;
            float leading = 1.5f * fontSize;

            PDRectangle mediabox = page.findMediaBox();
            float margin = 72;
            float width = mediabox.getWidth() - 2*margin;
            float startX = mediabox.getLowerLeftX() + margin;
            float startY = mediabox.getUpperRightY() - margin;

            String text = "I am trying to create a PDF file with a lot of text contents in the document. I am using PDFBox"; 
            List<String> lines = new ArrayList<String>();
            int lastSpace = -1;
            while (text.length() > 0)
            {
                int spaceIndex = text.indexOf(' ', lastSpace + 1);
                if (spaceIndex < 0)
                    spaceIndex = text.length();
                String subString = text.substring(0, spaceIndex);
                float size = fontSize * pdfFont.getStringWidth(subString) / 1000;
                System.out.printf("'%s' - %f of %f\n", subString, size, width);
                if (size > width)
                {
                    if (lastSpace < 0)
                        lastSpace = spaceIndex;
                    subString = text.substring(0, lastSpace);
                    lines.add(subString);
                    text = text.substring(lastSpace).trim();
                    System.out.printf("'%s' is line\n", subString);
                    lastSpace = -1;
                }
                else if (spaceIndex == text.length())
                {
                    lines.add(text);
                    System.out.printf("'%s' is line\n", text);
                    text = "";
                }
                else
                {
                    lastSpace = spaceIndex;
                }
            }

            contentStream.beginText();
            contentStream.setFont(pdfFont, fontSize);
            contentStream.moveTextPositionByAmount(startX, startY);
            for (String line: lines)
            {
                contentStream.drawString(line);
                contentStream.moveTextPositionByAmount(0, -leading);
            }
            contentStream.endText(); 
            contentStream.close();

            doc.save(new File(RESULT_FOLDER, "break-long-string.pdf"));
        }
        finally
        {
            if (doc != null)
            {
                doc.close();
            }
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/20680430/is-it-possible-to-justify-text-in-pdfbox">
     * Is it possible to justify text in PDFBOX?
     * </a>
     * <p>
     * This test shows how to split a long string into multiple lines with justification.
     * </p>
     */
    @Test
    public void testBreakStringJustified() throws COSVisitorException, IOException
    {
        PDDocument doc = null;
        try
        {
            doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);

            PDFont pdfFont = PDType1Font.HELVETICA;
            float fontSize = 25;
            float leading = 1.5f * fontSize;

            PDRectangle mediabox = page.findMediaBox();
            float margin = 72;
            float width = mediabox.getWidth() - 2*margin;
            float startX = mediabox.getLowerLeftX() + margin;
            float startY = mediabox.getUpperRightY() - margin - pdfFont.getFontBoundingBox().getUpperRightY() * fontSize / 1000 ;

            String text = "I am trying to create a PDF file with a lot of text contents in the document. I am using PDFBox"; 
            List<String> lines = new ArrayList<String>();
            int lastSpace = -1;
            while (text.length() > 0)
            {
                int spaceIndex = text.indexOf(' ', lastSpace + 1);
                if (spaceIndex < 0)
                    spaceIndex = text.length();
                String subString = text.substring(0, spaceIndex);
                float size = fontSize * pdfFont.getStringWidth(subString) / 1000;
                System.out.printf("'%s' - %f of %f\n", subString, size, width);
                if (size > width)
                {
                    if (lastSpace < 0)
                        lastSpace = spaceIndex;
                    subString = text.substring(0, lastSpace);
                    lines.add(subString);
                    text = text.substring(lastSpace).trim();
                    System.out.printf("'%s' is line\n", subString);
                    lastSpace = -1;
                }
                else if (spaceIndex == text.length())
                {
                    lines.add(text);
                    System.out.printf("'%s' is line\n", text);
                    text = "";
                }
                else
                {
                    lastSpace = spaceIndex;
                }
            }
            
            contentStream.beginText();
            contentStream.setFont(pdfFont, fontSize);
            contentStream.moveTextPositionByAmount(startX, startY);
            for (String line: lines)
            {
                float charSpacing = 0;
                if (line.length() > 1)
                {
                    float size = fontSize * pdfFont.getStringWidth(line) / 1000;
                    float free = width - size;
                    if (free > 0)
                    {
                        charSpacing = free / (line.length() - 1);
                    }
                }
                contentStream.appendRawCommands(String.format("%f Tc\n", charSpacing).replace(',', '.'));
                
                contentStream.drawString(line);
                contentStream.moveTextPositionByAmount(0, -leading);
            }
            contentStream.endText(); 
            contentStream.close();

            doc.save(new File(RESULT_FOLDER, "break-long-string-justified.pdf"));
        }
        finally
        {
            if (doc != null)
            {
                doc.close();
            }
        }
    }

}
