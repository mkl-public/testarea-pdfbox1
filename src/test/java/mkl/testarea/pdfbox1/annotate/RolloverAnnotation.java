/**
 * 
 */
package mkl.testarea.pdfbox1.annotate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/28144941/in-pdfbox-how-to-create-a-link-annotation-with-rollover-mouse-over-effect">
 * <b>In PDFBox, how to create a link annotation with “rollover” / “mouse over” effects?</b>
 * </a>
 * <p>
 * {@link #testCreateRolloverAnnotation()} attempts to create a PDF with the desired
 * rollover effect using a {@link PDAnnotationLink}. Adobe viewers don't show the effect.
 * <p>
 * {@link #testCreateRolloverButton()} attempts the same using a {@link PDAnnotationWidget}
 * customized as a pushbutton. Adobe viewers do show the effect.
 * 
 * @author mkl
 */
public class RolloverAnnotation
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "annotate");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testCreateRolloverAnnotation() throws IOException, COSVisitorException
    {
        PDAnnotationLink txtLink = new PDAnnotationLink();

        PDActionURI action = new PDActionURI();
        action.setURI("http://www.pdfbox.org");
        txtLink.setAction(action);

        createRollover(txtLink, "rolloverAnnotation.pdf");
    }

    @Test
    public void testCreateRolloverButton() throws IOException, COSVisitorException
    {
        PDAnnotationWidget widget = new PDAnnotationWidget();
        COSDictionary dictionary = widget.getDictionary();
        dictionary.setName("FT", "Btn");
        dictionary.setInt("Ff", 65536);
        dictionary.setString("T", "Push");

        PDActionURI action = new PDActionURI();
        action.setURI("http://www.pdfbox.org");
        widget.setAction(action);

        createRollover(widget, "rolloverButton.pdf");
    }

    void createRollover(PDAnnotation annotation, String filename) throws IOException, COSVisitorException
    {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        List<PDAnnotation> annotations = page.getAnnotations();

        float x = 100;
        float y = 500;
        String text = "PDFBox";
        PDFont font = PDType1Font.HELVETICA_BOLD;
        float textWidth = font.getStringWidth(text) / 1000 * 18;

        PDPageContentStream contents = new PDPageContentStream(document, page);
        contents.beginText();
        contents.setFont(font, 18);
        contents.moveTextPositionByAmount(x, y);
        contents.drawString(text);
        contents.endText();
        contents.close();

        PDAppearanceDictionary appearanceDictionary = new PDAppearanceDictionary();
        PDAppearanceStream normal = createAppearanceStream(document, textWidth, font, "0.5 0.5 0.5 rg");
        PDAppearanceStream rollover = createAppearanceStream(document, textWidth, font, "1 0.7 0.5 rg");
        PDAppearanceStream down = createAppearanceStream(document, textWidth, font, "0 0 0 rg");
        appearanceDictionary.setNormalAppearance(normal);
        appearanceDictionary.setRolloverAppearance(rollover);
        appearanceDictionary.setDownAppearance(down);
        annotation.setAppearance(appearanceDictionary);

        PDRectangle position = new PDRectangle();
        position.setLowerLeftX(x);
        position.setLowerLeftY(y - 5);
        position.setUpperRightX(x + textWidth);
        position.setUpperRightY(y + 20);
        annotation.setRectangle(position);

        annotations.add(annotation);
        document.save(new File(RESULT_FOLDER, filename));
        document.close();
    }

    PDAppearanceStream createAppearanceStream(PDDocument document, float width, PDFont font, String backColorSettingOperation) throws IOException
    {
        PDResources pdResources = new PDResources();
        String fontName = pdResources.addFont(font);
        PDStream pdStream = new PDStream(document);
        OutputStream os = pdStream.createOutputStream();
        String streamToBe = backColorSettingOperation + " 0 -5 " + width + " 25 re f /" + fontName + " 18 Tf 0 g BT (PDFBox) Tj ET";
        os.write(streamToBe.getBytes());
        os.close();
        
        PDXObjectForm xobject = new PDXObjectForm(pdStream);
        xobject.setResources(pdResources);
        xobject.setBBox(new PDRectangle(new BoundingBox(0, -5, width, 20)));
        xobject.setFormType(1);
        PDAppearanceStream normal = new PDAppearanceStream(xobject.getCOSStream());
        return normal;
    }
}
