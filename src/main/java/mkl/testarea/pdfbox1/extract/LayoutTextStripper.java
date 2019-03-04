package mkl.testarea.pdfbox1.extract;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

/**
 * <a href="https://stackoverflow.com/questions/25044272/java-pdf-file-extraction-using-pdfbox-for-tabular-data">
 * Java - PDF file extraction using PDFBOX for tabular data
 * </a>
 * <br/>
 * <a href="https://stackoverflow.com/questions/45840842/pdfbox-maintaining-pdf-structure-when-extracting-text">
 * PDFBox : Maintaining PDF structure when extracting text
 * </a>
 * <p>
 * </p>
 * 
 * @author mkl
 */
public class LayoutTextStripper extends PDFTextStripper
{
    public LayoutTextStripper() throws IOException
    {
        super();
    }

    @Override
    protected void startPage(PDPage page) throws IOException
    {
        super.startPage(page);
        cropBox = page.findCropBox();
        pageLeft = cropBox.getLowerLeftX();
        beginLine();
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException
    {
        float recentEnd = 0;
        for (TextPosition textPosition: textPositions)
        {
            String textHere = textPosition.getCharacter();
            if (textHere.trim().length() == 0)
                continue;

            float start = textPosition.getTextPos().getXPosition();
            boolean spacePresent = endsWithWS | textHere.startsWith(" ");

            if (needsWS | spacePresent | Math.abs(start - recentEnd) > 1)
            {
                int spacesToInsert = insertSpaces(chars, start, needsWS & !spacePresent);
                
                for (; spacesToInsert > 0; spacesToInsert--)
                {
                    writeString(" ");
                    chars++;
                }
            }

            writeString(textHere);
            chars += textHere.length();

            needsWS = false;
            endsWithWS = textHere.endsWith(" ");
            try
            {
                recentEnd = getEndX(textPosition);
            }
            catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
            {
                throw new IOException("Failure retrieving endX of TextPosition", e);
            }
        }
    }

    @Override
    protected void writeLineSeparator() throws IOException
    {
        super.writeLineSeparator();
        beginLine();
    }

    @Override
    protected void writeWordSeparator() throws IOException
    {
        needsWS = true;
    }

    void beginLine()
    {
        endsWithWS = true;
        needsWS = false;
        chars = 0;
    }

    int insertSpaces(int charsInLineAlready, float chunkStart, boolean spaceRequired)
    {
        int indexNow = charsInLineAlready;
        int indexToBe = (int)((chunkStart - pageLeft) / fixedCharWidth);
        int spacesToInsert = indexToBe - indexNow;
        if (spacesToInsert < 1 && spaceRequired)
            spacesToInsert = 1;

        return spacesToInsert;
    }

    float getEndX(TextPosition textPosition) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
    {
        Field field = textPosition.getClass().getDeclaredField("endX");
        field.setAccessible(true);
        return field.getFloat(textPosition);
    }

    public float fixedCharWidth = 3;

    boolean endsWithWS = true;
    boolean needsWS = false;
    int chars = 0;

    PDRectangle cropBox = null;
    float pageLeft = 0;
}
