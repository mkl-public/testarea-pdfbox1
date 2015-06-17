package mkl.testarea.pdfbox1.content;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * <a href="http://stackoverflow.com/questions/30882927/how-to-generate-dyanamic-no-of-pages-using-pdfbox">
 * How to generate Dyanamic no of pages using PDFBOX
 * </a>
 * <p>
 * A simple PDF rendering class
 * </p>
 * 
 * @author mkl
 */
public class PdfRenderingSimple implements AutoCloseable
{
    //
    // rendering
    //
    public void renderText(String Info, int marginwidth) throws IOException
    {
        if (content == null || textRenderingLineY < 12)
            newPage();

        textRenderingLineY-=12;    
        System.out.print("lineno=" + textRenderingLineY);
        PDFont fontPlain = PDType1Font.HELVETICA;
        content.beginText();
        content.setFont(fontPlain, 10);
        content.moveTextPositionByAmount(marginwidth, textRenderingLineY);
        content.drawString(Info);
        content.endText();
    }

    //
    // constructor
    //
    public PdfRenderingSimple(PDDocument doc)
    {
        this.doc = doc;
    }

    //
    // AutoCloseable implementation
    //
    /**
     * Closes the current page
     */
    @Override
    public void close() throws IOException
    {
        if (content != null)
        {
            content.close();
            content = null;
        }
    }

    //
    // helper methods
    //
    void newPage() throws IOException
    {
        close();

        PDPage page = new PDPage();
        doc.addPage(page);
        content = new PDPageContentStream(doc, page);
        content.setNonStrokingColor(Color.BLACK);

        textRenderingLineY = 768;
    }

    //
    // members
    //
    final PDDocument doc;

    private PDPageContentStream content = null;
    private int textRenderingLineY = 0;
}
