package mkl.testarea.pdfbox1.content;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;

/**
 * <a href="http://stackoverflow.com/questions/30767643/pdfbox-issue-while-changing-page">
 * PdfBox issue while changing page
 * </a>
 * <p>
 * This class serves as an example of an alternative approach to draw contract endorsement
 * differences. The original class of the OP is {@link PdfRenderingEndorsement}.
 * </p>
 * @author mkl
 */
public class PdfRenderingEndorsementAlternative implements AutoCloseable
{
    //
    // misc constants
    //
    static final int FIELD_WIDTH = 70;
    static final int HALF_WIDTH = 325;
    static final int TEXT_WIDTH = 410;
    
    static final float BOTTOM_MARGIN = 70;
    static final int LEFT_MARGIN = 50;

    //
    // rendering
    //
    public void gap(int size)
    {
        previousBandBase-=size;
    }

    public void render(BandColumn... columns) throws IOException
    {
        if (content == null)
            newPage();

        final List<Chunk> chunks = new ArrayList<Chunk>();
        for (BandColumn column : columns)
        {
            chunks.addAll(column.toChunks());
        }

        float offset = 0;
        while (!chunks.isEmpty())
        {
            float lowestAddedY = previousBandBase;
            float highestBaseBeforeNonAdded = Float.NEGATIVE_INFINITY;
            List<Chunk> added = new ArrayList<Chunk>();
            for (Chunk chunk: chunks)
            {
                float y = previousBandBase + chunk.y + offset; 
                if (y >= BOTTOM_MARGIN)
                {
                    content.beginText();
                    content.setFont(chunk.font, 9);
                    content.moveTextPositionByAmount(chunk.x, y);
                    content.drawString(chunk.text);
                    content.endText();
                    // draw
                    if (y < lowestAddedY)
                        lowestAddedY = y;
                    added.add(chunk);
                }
                else
                {
                    float baseBefore = chunk.y + chunk.space;
                    if (baseBefore > highestBaseBeforeNonAdded)
                        highestBaseBeforeNonAdded = baseBefore;
                }
            }
            chunks.removeAll(added);
            if (!chunks.isEmpty())
            {
                newPage();
                offset = -highestBaseBeforeNonAdded;
            }
            else
            {
                previousBandBase = lowestAddedY;
            }
        }
    }

    static public class BandColumn
    {
        public enum Layout
        {
            headerText(150, TEXT_WIDTH, 0, 10),
            leftHalfPageField(LEFT_MARGIN, FIELD_WIDTH, HALF_WIDTH - 2 * FIELD_WIDTH - 10, 10),
            rightHalfPageField(HALF_WIDTH, FIELD_WIDTH, HALF_WIDTH - 2 * FIELD_WIDTH - 10, 10);

            Layout(float x, int fieldWidth, int valueWidth, int space)
            {
                this.x = x;
                this.fieldWidth = fieldWidth;
                this.valueWidth = valueWidth;
                this.space = space;
            }

            final float x;
            final int fieldWidth, valueWidth, space;
        }

        public BandColumn(Layout layout, String labelField, String value)
        {
            this(layout.x, layout.space, labelField, value, layout.fieldWidth, layout.valueWidth);
        }

        public BandColumn(float x, int space, String labelField, String value, int fieldWidth, int valueWidth)
        {
            this.x = x;
            this.space = space;
            this.labelField = labelField;
            this.value = value;
            this.fieldWidth = fieldWidth;
            this.valueWidth = valueWidth;
        }

        List<Chunk> toChunks() throws IOException
        {
            final List<Chunk> result = new ArrayList<Chunk>();
            result.addAll(toChunks(0, fieldWidth, PDType1Font.TIMES_BOLD, labelField));
            result.addAll(toChunks(10 + fieldWidth, valueWidth, PDType1Font.TIMES_ROMAN, value));
            return result;
        }

        List<Chunk> toChunks(int offset, int width, PDFont font, String text) throws IOException
        {
            if (text == null || text.length() == 0)
                return Collections.emptyList();

            final List<Chunk> result = new ArrayList<Chunk>();
            float y = -space;
            List<String> rows = getRows(text, width, font);
            for (String row: rows)
            {
                result.add(new Chunk(x+offset, y, space, font, row));
                y-= space;
            }
            return result;
        }

        final float x;
        final int space, fieldWidth, valueWidth;
        final String labelField, value;
    }

    //
    // constructor
    //
    public PdfRenderingEndorsementAlternative(PDDocument doc, InputStream logo, 
            String[] header) throws IOException
    {
        this.doc = doc;
        this.header = header;
        logoImg = new PDJpeg(doc, logo);
    }

    //
    // AutoCloseable implementation
    //
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

        PDPage page = new PDPage(PDPage.PAGE_SIZE_LETTER);
        doc.addPage(page);
        content = new PDPageContentStream(doc, page);
        content.drawImage(logoImg, 50, 720);
        content.setLineWidth(.5f);
        content.setNonStrokingColor(Color.GRAY);
        content.drawLine(50, 710, 562, 710);

        previousBandBase = 770;
        render(new BandColumn(BandColumn.Layout.headerText, "ENDOSO", null));
        for (String head: header)
            render(new BandColumn(BandColumn.Layout.headerText, head, null));

        content.setNonStrokingColor(Color.BLACK);
        previousBandBase = 680;
    }

    // original method
    static List<String> getRows(String text, int width, PDFont font) throws IOException
    {
        float textWidth = font.getStringWidth(text) / 1000f * 9f;
        ArrayList<String> result = new ArrayList<String>();// Lists.newArrayList();
        if (textWidth < width)
        {
            result.add(text);
            return result;
        }

        float spaceWidth = font.getStringWidth(" ") / 1000f * 9f;
        String[] paragraphs = text.split("\n|\r\n|\r");
        for (String paragraph : paragraphs)
        {
            float pWidth = font.getStringWidth(paragraph) / 1000f * 9f;
            if (pWidth < width)
            {
                result.add(paragraph);
                continue;
            }

            float widthCount = 0f;
            String[] words = paragraph.trim().split(" ");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < words.length; j++)
            {
                if (words[j].trim().length() == 0)
                {
                    continue;
                }

                float wWidth = font.getStringWidth(words[j]) / 1000f * 9f;
                float totalWidth = widthCount + wWidth + spaceWidth;
                if (totalWidth < width + spaceWidth)
                {
                    sb.append(words[j]);
                    sb.append(" ");
                    widthCount = totalWidth;
                }
                else
                {
                    result.add(sb.toString().trim());
                    sb = new StringBuilder();
                    sb.append(words[j]);
                    sb.append(" ");
                    widthCount = totalWidth - widthCount;
                }
            }
            result.add(sb.toString().trim());
        }
        return result;
    }

    //
    // helper classes
    //
    static class Chunk
    {
        Chunk(float x, float y, int space, PDFont font, String text)
        {
            this.x = x;
            this.y = y;
            this.space = space;
            this.font = font;
            this.text = text;
        }

        final float x, y;
        final int space;
        final PDFont font;
        final String text;
    }

    //
    // members
    //
    private final PDDocument doc;
    private final PDJpeg logoImg;
    private final String[] header;

    private PDPageContentStream content = null;
    private float previousBandBase = 0;

}
