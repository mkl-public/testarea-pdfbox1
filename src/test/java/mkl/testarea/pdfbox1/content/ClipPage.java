package mkl.testarea.pdfbox1.content;

import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSStreamArray;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ClipPage
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/30616220/splitting-at-a-specific-point-in-pdfbox">
     * Splitting at a specific point in PDFBox
     * </a>
     * <p>
     * One way to achieve the task, i.e. to split a page at a certain point (i.e. all content above
     * a limit to be included and everything below to be excluded) would be to prepend a clip path.
     * This is implemented in {@link #clipPage(PDDocument, PDPage, BoundingBox)}.
     * </p>
     */
	@Test
	public void testTestTop() throws IOException, COSVisitorException
	{
		try ( InputStream docStream = getClass().getResourceAsStream("/mkl/testarea/pdfbox1/sign/test.pdf") )
		{
			PDDocument document = PDDocument.load(docStream);
			PDPage page = (PDPage) document.getDocumentCatalog().getAllPages().get(0);
			PDRectangle cropBox = page.findCropBox();
			clipPage(document, page, new BoundingBox(cropBox.getLowerLeftX(), cropBox.getLowerLeftY() + 650, cropBox.getUpperRightX(), cropBox.getUpperRightY()));
			document.save(new File(RESULT_FOLDER, "test-cropTop.pdf"));
			document.close();
		}

	}

	void clipPage(PDDocument document, PDPage page, BoundingBox clipBox) throws IOException
	{
		PDPageContentStream pageContentStream = new PDPageContentStream(document, page, true, false);
		pageContentStream.addRect(clipBox.getLowerLeftX(), clipBox.getLowerLeftY(), clipBox.getWidth(), clipBox.getHeight());
		pageContentStream.clipPath(PathIterator.WIND_NON_ZERO);
		pageContentStream.close();

		COSArray newContents = new COSArray();
		COSStreamArray contents = (COSStreamArray) page.getContents().getStream();
		newContents.add(contents.get(contents.getStreamCount()-1));
		for (int i = 0; i < contents.getStreamCount()-1; i++)
		{
			newContents.add(contents.get(i));
		}
		page.setContents(new PDStream(new COSStreamArray(newContents)));
	}
}
