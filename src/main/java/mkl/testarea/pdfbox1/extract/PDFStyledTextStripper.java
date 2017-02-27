package mkl.testarea.pdfbox1.extract;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * <a href="http://stackoverflow.com/questions/39962563/detect-bold-italic-and-strike-through-text-using-pdfbox-with-vb-net">
 * Detect Bold, Italic and Strike Through text using PDFBox with VB.NET
 * </a>
 * <br/>
 * <a href="http://www.filedropper.com/exampledocument">
 * Example Document.pdf
 * </a>
 * <p>
 * This {@link PDFTextStripper} subclass shows how to extract text with the styles
 * used in the example document.
 * </p>
 * <p>
 * BEWARE: This merely is a proof of concept! In particular the tests in
 * {@link TransformedRectangle#underlines(TextPosition)} and
 * {@link TransformedRectangle#strikesThrough(TextPosition)} only work
 * with the restriction described in the methods themselves.
 * </p>
 * <a href="https://github.com/mkl-public/testarea-pdfbox1/issues/1">
 * PDFStyledTextStripper StrikeThrough Bug #1
 * </a>
 * <br/>
 * <a href="https://www.dropbox.com/s/5chty5f4yotkb7s/style-test.pdf?dl=1">
 * style-test.pdf
 * </a>
 * <p>
 * Indeed, the 's' of 'stroked' was not recognized as style strikethrough.
 * This appears due to a border case where float / double arithmetics give
 * you a headache. To make this work, the tolerance of the {@link TransformedRectangle}
 * method <code>strikesThrough</code> (and also <code>underlines</code>)
 * has been changed.
 * </p>
 * @author mkl
 */
public class PDFStyledTextStripper extends PDFTextStripper
{
    public PDFStyledTextStripper() throws IOException
    {
        super();
        registerOperatorProcessor("re", new AppendRectangleToPath());
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException
    {
        for (TextPosition textPosition : textPositions)
        {
            Set<String> style = determineStyle(textPosition);
            if (!style.equals(currentStyle))
            {
                output.write(style.toString());
                currentStyle = style;
            }
            output.write(textPosition.getCharacter());
        }
    }

    Set<String> determineStyle(TextPosition textPosition)
    {
        Set<String> result = new HashSet<>();

        if (textPosition.getFont().getBaseFont().toLowerCase().contains("bold"))
            result.add("Bold");

        if (textPosition.getFont().getBaseFont().toLowerCase().contains("italic"))
            result.add("Italic");

        if (rectangles.stream().anyMatch(r -> r.underlines(textPosition)))
            result.add("Underline");

        if (rectangles.stream().anyMatch(r -> r.strikesThrough(textPosition)))
            result.add("StrikeThrough");

        return result;
    }

    class AppendRectangleToPath extends OperatorProcessor
    {
        public void process(PDFOperator operator, List<COSBase> arguments)
        {
            COSNumber x = (COSNumber) arguments.get(0);
            COSNumber y = (COSNumber) arguments.get(1);
            COSNumber w = (COSNumber) arguments.get(2);
            COSNumber h = (COSNumber) arguments.get(3);

            double x1 = x.doubleValue();
            double y1 = y.doubleValue();

            // create a pair of coordinates for the transformation
            double x2 = w.doubleValue() + x1;
            double y2 = h.doubleValue() + y1;

            Point2D p0 = transformedPoint(x1, y1);
            Point2D p1 = transformedPoint(x2, y1);
            Point2D p2 = transformedPoint(x2, y2);
            Point2D p3 = transformedPoint(x1, y2);

            rectangles.add(new TransformedRectangle(p0, p1, p2, p3));
        }

        Point2D.Double transformedPoint(double x, double y)
        {
            double[] position = {x,y}; 
            getGraphicsState().getCurrentTransformationMatrix().createAffineTransform().transform(
                    position, 0, position, 0, 1);
            return new Point2D.Double(position[0],position[1]);
        }
    }

    static class TransformedRectangle
    {
        public TransformedRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3)
        {
            this.p0 = p0;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }

        boolean strikesThrough(TextPosition textPosition)
        {
            Matrix matrix = textPosition.getTextPos();
            // TODO: This is a very simplistic implementation only working for horizontal text without page rotation
            // and horizontal rectangular strikeThroughs with p0 at the left bottom and p2 at the right top

            // Check if rectangle horizontally matches (at least) the text
            if (p0.getX() > matrix.getXPosition() + textPosition.getWidth() * .1f || p2.getX() < matrix.getXPosition() + textPosition.getWidth() * .9f)
                return false;
            // Check whether rectangle vertically is at the right height to underline
            double vertDiff = p0.getY() - matrix.getYPosition();
            if (vertDiff < 0 || vertDiff > textPosition.getFont().getFontDescriptor().getAscent() * textPosition.getFontSizeInPt() / 1000.0)
                return false;
            // Check whether rectangle is small enough to be a line
            return Math.abs(p2.getY() - p0.getY()) < 2;
        }

        boolean underlines(TextPosition textPosition)
        {
            Matrix matrix = textPosition.getTextPos();
            // TODO: This is a very simplistic implementation only working for horizontal text without page rotation
            // and horizontal rectangular underlines with p0 at the left bottom and p2 at the right top

            // Check if rectangle horizontally matches (at least) the text
            if (p0.getX() > matrix.getXPosition() + textPosition.getWidth() * .1f || p2.getX() < matrix.getXPosition() + textPosition.getWidth() * .9f)
                return false;
            // Check whether rectangle vertically is at the right height to underline
            double vertDiff = p0.getY() - matrix.getYPosition();
            if (vertDiff > 0 || vertDiff < textPosition.getFont().getFontDescriptor().getDescent() * textPosition.getFontSizeInPt() / 500.0)
                return false;
            // Check whether rectangle is small enough to be a line
            return Math.abs(p2.getY() - p0.getY()) < 2;
        }

        final Point2D p0, p1, p2, p3;
    }

    final List<TransformedRectangle> rectangles = new ArrayList<>();
    Set<String> currentStyle = Collections.singleton("Undefined");
}
