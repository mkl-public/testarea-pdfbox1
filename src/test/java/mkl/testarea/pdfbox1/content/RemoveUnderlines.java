package mkl.testarea.pdfbox1.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RemoveUnderlines
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/34720880/remove-underlines-from-text-in-pdf-file">
     * Remove underlines from text in PDF file
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/s/tkfhkb9e25eby4a/original.pdf?dl=0">
     * original.pdf
     * </a>
     * <p>
     * This test demonstrates how to remove the underlines (i.e. all blue rectangles)
     * from the document.
     * </p>
     */
    @Test
    public void testOriginal() throws IOException, COSVisitorException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("original.pdf")   )
        {
            PDDocument document = PDDocument.loadNonSeq(resourceStream, null);

            removeBlueRectangles(document);
            document.save(new File(RESULT_FOLDER, "original-noBlueRectangles.pdf"));
            
            document.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/34720880/remove-underlines-from-text-in-pdf-file">
     * Remove underlines from text in PDF file
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/s/23g54bvt781lb93/1178.pdf?dl=0">
     * 1178.pdf
     * </a>
     * <p>
     * This test demonstrates how to remove the underlines (i.e. all blue rectangles)
     * from the second sample document provided by the OP.
     * </p>
     */
    @Test
    public void test1178() throws IOException, COSVisitorException
    {
        try (   InputStream resourceStream = getClass().getResourceAsStream("1178.pdf")   )
        {
            PDDocument document = PDDocument.loadNonSeq(resourceStream, null);

            removeBlueRectangles(document);
            document.save(new File(RESULT_FOLDER, "1178-noBlueRectangles.pdf"));
            
            document.close();
        }
    }

    /**
     * This document removes all blue filled rectangles. As the sample document
     * only uses RGB colors, actually only uses "rg" to set the blue fill color,
     * the code could be somewhat simplified.
     */
    void removeBlueRectangles(PDDocument document) throws IOException
    {
        List<?> pages = document.getDocumentCatalog().getAllPages();
        for (int i = 0; i < pages.size(); i++)
        {
            PDPage page = (PDPage) pages.get(i);
            PDStream contents = page.getContents();

            PDFStreamParser parser = new PDFStreamParser(contents.getStream()); 
            parser.parse();
            List<Object> tokens = parser.getTokens();  

            Stack<Boolean> blueState = new Stack<Boolean>();
            blueState.push(false);

            for (int j = 0; j < tokens.size(); j++)  
            {  
                Object next = tokens.get(j);
                if (next instanceof PDFOperator)
                {
                    PDFOperator op = (PDFOperator) next;  
                    if (op.getOperation().equals("q"))
                    {
                        blueState.push(blueState.peek());
                    }
                    else if (op.getOperation().equals("Q"))
                    {
                        blueState.pop();
                    }
                    else if (op.getOperation().equals("rg"))
                    {
                        if (j > 2)
                        {
                            Object r = tokens.get(j-3);
                            Object g = tokens.get(j-2);
                            Object b = tokens.get(j-1);
                            if (r instanceof COSNumber && g instanceof COSNumber && b instanceof COSNumber)
                            {
                                blueState.pop();
                                blueState.push((
                                        Math.abs(((COSNumber)r).floatValue() - 0) < 0.001 &&
                                        Math.abs(((COSNumber)g).floatValue() - 0) < 0.001 &&
                                        Math.abs(((COSNumber)b).floatValue() - 1) < 0.001));
                            }
                        }
                    }
                    else if (op.getOperation().equals("f"))
                    {
                        if (blueState.peek() && j > 0)
                        {
                            Object re = tokens.get(j-1);
                            if (re instanceof PDFOperator && ((PDFOperator)re).getOperation().equals("re"))
                            {
                                tokens.set(j, PDFOperator.getOperator("n"));
                            }
                        }
                    }
                }
            }

            PDStream updatedStream = new PDStream(document);  
            OutputStream out = updatedStream.createOutputStream();  
            ContentStreamWriter tokenWriter = new ContentStreamWriter(out);  
            tokenWriter.writeTokens(tokens);  
            page.setContents(updatedStream);
        }
    }
}
