package mkl.testarea.pdfbox1.edit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSString;
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
 * This test is about naive test editing attempts.
 * 
 * @author mkl
 */
public class NaiveContentEdit
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "edit");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/29220165/editing-content-in-pdf-using-pdfbox-removes-last-line-from-pdf">
     * Editing content in pdf using PDFBox removes last line from pdf
     * </a>
     * 
     * Reproducing the issue.
     */
    @Test
    public void testDrunkenfistOriginal() throws IOException, COSVisitorException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("Original.pdf") )
        {
            PDDocument doc = PDDocument.load(originalStream);
            PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
            PDStream contents = page.getContents();
            PDFStreamParser parser = new PDFStreamParser(contents.getStream());
            parser.parse();
            List<Object> tokens = parser.getTokens();
            for (int j = 0; j < tokens.size(); j++) {
                Object next = tokens.get(j);
                if (next instanceof PDFOperator) {
                    PDFOperator op = (PDFOperator) next;
                    if (op.getOperation().equals("Tj")) {
                        COSString previous = (COSString) tokens.get(j - 1);
                        String string = previous.getString();

                        string = string.replace("@ordnum&", "-ORDERNR-");
                        string = string.replace("@shipid&", "-SHIPMENTID-");
                        string = string.replace("@customer&", "-CUSTOMERNR-");
                        string = string.replace("@fromname&", "-FROMNAME-");

                        tokens.set(j - 1, new COSString(string.trim()));
                    }
                }
            }

            PDStream updatedStream = new PDStream(doc);  
            OutputStream out = updatedStream.createOutputStream();  
            ContentStreamWriter tokenWriter = new ContentStreamWriter(out);  
            tokenWriter.writeTokens(tokens);  
            page.setContents(updatedStream);
            
            doc.save(new File(RESULT_FOLDER, "Original-edited.pdf"));
            doc.close();
        }
    }
}
