package mkl.testarea.pdfbox1.content;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDExtendedGraphicsState;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class TransparentText
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/39998390/how-to-create-transparent-text-in-pdfbox-or-add-opacity-to-the-text-with-the-hel">
     * How to create Transparent text in pdfBOX or add opacity to the text with the help of pdfBOX?
     * </a>
     * <p>
     * Indeed, this code does not produce transparency... but look at
     * {@link #testTransparentTextLikeTilmanImproved()}.
     * </p>
     */
    @Test
    public void testTransparentTextLikeTilman() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(document, page);

        PDExtendedGraphicsState gs1 = new PDExtendedGraphicsState();
        gs1.setNonStrokingAlphaConstant(1f);
        PDExtendedGraphicsState gs2 = new PDExtendedGraphicsState();
        gs2.setNonStrokingAlphaConstant(0.2f);
        Map<String, PDExtendedGraphicsState> graphicsStatesMap = page.getResources().getGraphicsStates();
        if (graphicsStatesMap == null)
        {
            graphicsStatesMap = new HashMap<String, PDExtendedGraphicsState>();
        }
        graphicsStatesMap.put("gs1", gs1);
        graphicsStatesMap.put("gs2", gs2);
        page.getResources().setGraphicsStates(graphicsStatesMap);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 60);
        cs.setNonStrokingColor(0, 0, 0);
        cs.beginText();
        cs.appendRawCommands("/gs1 gs\n");
        cs.moveTextPositionByAmount(50, 600);
        cs.drawString("Apache PDFBox 1");
        cs.appendRawCommands("/gs2 gs\n");
        cs.moveTextPositionByAmount(20, 20);
        cs.drawString("Apache PDFBox 2");
        cs.endText();
        cs.close();

        cs.close();
        document.save(new File(RESULT_FOLDER, "transparentTextLikeTilman.pdf"));
        document.close();
    }

    /**
     * <a href="http://stackoverflow.com/questions/39998390/how-to-create-transparent-text-in-pdfbox-or-add-opacity-to-the-text-with-the-hel">
     * How to create Transparent text in pdfBOX or add opacity to the text with the help of pdfBOX?
     * </a>
     * <p>
     * This code (which parallels Tilman's own improved code) does produce transparency
     * in contrast to {@link #testTransparentTextLikeTilman()}.
     * </p>
     */
    @Test
    public void testTransparentTextLikeTilmanImproved() throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(document, page);

        PDExtendedGraphicsState gs1 = new PDExtendedGraphicsState();
        gs1.setNonStrokingAlphaConstant(1f);
        PDExtendedGraphicsState gs2 = new PDExtendedGraphicsState();
        gs2.setNonStrokingAlphaConstant(0.2f);
        Map<String, PDExtendedGraphicsState> graphicsStatesMap = page.getResources().getGraphicsStates();
        if (graphicsStatesMap == null)
        {
            graphicsStatesMap = new HashMap<String, PDExtendedGraphicsState>();
        }
        graphicsStatesMap.put("gs1", gs1);
        graphicsStatesMap.put("gs2", gs2);
        page.getResources().setGraphicsStates(graphicsStatesMap);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 60);
        cs.setNonStrokingColor(0, 0, 0);
        cs.appendRawCommands("/gs1 gs\n");
        cs.setNonStrokingColor(Color.green);
        cs.beginText();
        cs.moveTextPositionByAmount(50, 600);
        cs.drawString("Apache PDFBox 1");
        cs.endText();
        cs.appendRawCommands("/gs2 gs\n");
        cs.setNonStrokingColor(Color.red);
        cs.beginText();
        cs.moveTextPositionByAmount(70, 620);
        cs.drawString("Apache PDFBox 2");
        cs.endText();
        cs.close();

        cs.close();
        document.save(new File(RESULT_FOLDER, "transparentTextLikeTilmanImproved.pdf"));
        document.close();
    }
}
