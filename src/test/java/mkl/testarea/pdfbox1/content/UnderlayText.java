package mkl.testarea.pdfbox1.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSStreamArray;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/32515217/how-to-overlay-two-documents-at-run-time">
 * How to Overlay two documents at run time
 * </a>
 * <p>
 * This test attempts to make the original approach of the OP work. After executing his
 * code {@link #watermarkOriginal(PDDocument, PDFont, double, double, double, String)}
 * (which paints the watermark over the existing content, not under it), we now call
 * {@link #pushUnder(PDDocument)} which pushes it under the pre-existing content.
 * </p>
 * 
 * @author mkl
 */
public class UnderlayText
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * Execute the test for sample.pdf. The results are as expected even
     * though one misses the watermark after pushing under; as there is a
     * big white rectangle drawn first in the pre-existing content, though,
     * this is correct, the watermark is hidden under that rectangle.
     */
    @Test
    public void testSample() throws IOException, COSVisitorException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("sample.pdf")   )
        {
            PDDocument document = PDDocument.loadNonSeq(resourceStream, null);
            PDFont pdfFont = PDFontFactory.createDefaultFont();

            watermarkOriginal(document, pdfFont, 45, 400, 550, "Test");
            document.save(new File(RESULT_FOLDER, "sample-underlayOriginal.pdf"));
            
            pushUnder(document);
            document.save(new File(RESULT_FOLDER, "sample-underlayPushedUnder.pdf"));

            document.close();
        }
    }

    /**
     * Execute the test for testA4.pdf. The results are as expected
     */
    @Test
    public void testTestA4() throws IOException, COSVisitorException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("testA4.pdf")   )
        {
            PDDocument document = PDDocument.loadNonSeq(resourceStream, null);
            PDFont pdfFont = PDFontFactory.createDefaultFont();

            watermarkOriginal(document, pdfFont, 45, 200, 600, "Test");
            document.save(new File(RESULT_FOLDER, "testA4-underlayOriginal.pdf"));
            
            pushUnder(document);
            document.save(new File(RESULT_FOLDER, "testA4-underlayPushedUnder.pdf"));

            document.close();
        }
    }

    /**
     * <p>
     * This is the OP's initial approach: "the problem is where ever my watermark
     * message comes it is hiding my page content."
     * </p>
     * <p>
     * I enclosed the mark drawing code in <code>saveGraphicsState</code> and
     * <code>restoreGraphicsState</code> to protect the original content from
     * being influenced by state changes by the the mark drawing code.
     * </p>
     */
    void watermarkOriginal(PDDocument document, PDFont pdfFont, double degree, double x, double y, String text) throws IOException
    {
        List<?> pages = document.getDocumentCatalog().getAllPages();
        float fontSize = 70.0f;
        for (int i = 0; i < pages.size(); i++) {
            PDPage page = (PDPage) pages.get(i);
            PDRectangle pageSize = page.findMediaBox();
            float stringWidth = pdfFont.getStringWidth(text) * fontSize
                    / 1000f;
            // calculate to center of the page
            int rotation = page.findRotation();
            boolean rotate = degree > 0;
            float pageWidth = rotate ? pageSize.getHeight() : pageSize
                    .getWidth();
            float pageHeight = rotate ? pageSize.getWidth() : pageSize
                    .getHeight();
            double centeredXPosition = rotate ? pageHeight / 2f
                    : (pageWidth - stringWidth) / 2f;
            double centeredYPosition = rotate ? (pageWidth - stringWidth) / 2f
                    : pageHeight / 2f;
            // append the content to the existing stream
            PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, true, true, true);
            contentStream.saveGraphicsState();
            contentStream.beginText();
            // set font and font size
            contentStream.setFont(pdfFont, fontSize);
            // set text color to red
            contentStream.setNonStrokingColor(240, 240, 240);
            if (rotate) {
                // rotate the text according to the page rotation
                contentStream.setTextRotation(degree, x, y);
            } else {
                contentStream.setTextTranslation(centeredXPosition,
                        centeredYPosition);
            }
            contentStream.drawString(text);
            contentStream.endText();
            contentStream.restoreGraphicsState();
            contentStream.close();
        }
    }

    /**
     * This method on each page moves the last content stream
     * to the second position.
     */
    void pushUnder(PDDocument document)
    {
        List<?> pages = document.getDocumentCatalog().getAllPages();
        float fontSize = 70.0f;
        for (int i = 0; i < pages.size(); i++) {
            PDPage page = (PDPage) pages.get(i);
            COSBase contents = page.getCOSDictionary().getDictionaryObject(COSName.CONTENTS);
            if (contents instanceof COSStreamArray)
            {
                COSStreamArray contentsArray = (COSStreamArray) contents;
                COSArray newArray = new COSArray();
                newArray.add(contentsArray.get(0));
                newArray.add(contentsArray.get(contentsArray.getStreamCount() - 1));

                for (int j = 1; j < contentsArray.getStreamCount() - 1; j++)
                {
                    newArray.add(contentsArray.get(j));
                }

                COSStreamArray newStreamArray = new COSStreamArray(newArray);
                page.getCOSDictionary().setItem(COSName.CONTENTS, newStreamArray);
            }
        }
    }
}
