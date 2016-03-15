// $Id$
package mkl.testarea.pdfbox1.merge;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageNode;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 *
 */
public class InsertPages
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/35964628/pdfbox-split-into-3-at-specified-page-numbers-to-insert-pdf">
     * PDFBox Split into 3 at specified page numbers (to insert pdf)
     * </a>
     * <p>
     * This test shows how to insert the same single page into another document multiple times.
     * </p>
     * <p>
     * Beware, this code assumes a flat page tree. In case of deeper page trees one has to
     * walk the page list differently, respect the count of contained {@link PDPageNode}
     * objects and recurse if appropriate. 
     * </p>
     */
    @Test
    public void testInsertPages() throws IOException, COSVisitorException
    {
        PDDocument document = create100Pages();
        PDDocument singlePageDocument = create1Page("A");
        PDPage singlePage = (PDPage) singlePageDocument.getDocumentCatalog().getAllPages().get(0);
        
        PDPageNode rootPages = document.getDocumentCatalog().getPages();
        rootPages.getKids().add(3-1, singlePage);
        singlePage.setParent(rootPages);
        singlePage = new PDPage(new COSDictionary(singlePage.getCOSDictionary()));
        rootPages.getKids().add(7-1, singlePage);
        singlePage = new PDPage(new COSDictionary(singlePage.getCOSDictionary()));
        rootPages.getKids().add(10-1, singlePage);
        rootPages.updateCount();
        
        document.save(new File(RESULT_FOLDER, "100-A-at-3.pdf"));
    }

    public PDDocument create100Pages() throws IOException
    {
        PDDocument document = new PDDocument();
        
        for (int i = 0; i < 100; i++)
        {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 100);
            content.moveTextPositionByAmount(100, 300);
            content.drawString(String.format("-%s-", i + 1));
            content.endText();
            content.close();
        }
        
        return document;
    }

    public PDDocument create1Page(String mark) throws IOException
    {
        PDDocument document = new PDDocument();
        
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream content = new PDPageContentStream(document, page);
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 100);
        content.moveTextPositionByAmount(100, 300);
        content.drawString(mark);
        content.endText();
        content.close();
        
        return document;
    }
}
