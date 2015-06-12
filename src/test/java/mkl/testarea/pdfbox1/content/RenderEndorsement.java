package mkl.testarea.pdfbox1.content;

import static mkl.testarea.pdfbox1.content.PdfRenderingEndorsementAlternative.BandColumn.Layout.leftHalfPageField;
import static mkl.testarea.pdfbox1.content.PdfRenderingEndorsementAlternative.BandColumn.Layout.rightHalfPageField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import mkl.testarea.pdfbox1.content.PdfRenderingEndorsementAlternative.BandColumn;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="http://stackoverflow.com/questions/30767643/pdfbox-issue-while-changing-page">
 * PdfBox issue while changing page
 * </a>
 * <p>
 * This test tests the rendering code in {@link PdfRenderingEndorsementAlternative} (which
 * is an alternative approach to the OP's {@link PdfRenderingEndorsement}) using the data
 * from the OP's sample file <a href="https://drive.google.com/file/d/0B-z424N1EYicR1BxUUtUSkw4V1U/view?usp=sharing">Endoso-44-17.pdf</a>.
 * </p>
 * @author mkl
 */
public class RenderEndorsement
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    public void testAlternativeQuick() throws IOException, COSVisitorException
    {
        String[] header = new String[]
        {
                "Daños - Agrícola y de animales",
                "2015-06-05 / 2016-01-31",
                "FONDO DE ASEGURAMIENTO AGRÍCOLA 21 DE OCTUBRE",
                "FNP-00004408003772000"
        };

        try (   InputStream logo = getClass().getResourceAsStream("Logo.jpg")   )
        {
            PDDocument document = new PDDocument();
            PdfRenderingEndorsementAlternative renderer = new PdfRenderingEndorsementAlternative(document, logo, header);

            renderer.render(
                    new BandColumn(leftHalfPageField, "Nombre del contrato/asegurado:", "Prueba Jesus Fac No Prop"),
                    new BandColumn(rightHalfPageField, "Nombre del contrato/asegurado:", "Prueba Jesus Fac No Prop con Endoso")
                    );

            renderer.gap(20);

            renderer.render(
                    new BandColumn(leftHalfPageField, "País:", "México"),
                    new BandColumn(rightHalfPageField, "País:", "México")
                    );

            renderer.close();
            document.save(new File(RESULT_FOLDER, "EndorsementQuick.pdf"));
        }
    }

    @Test
    public void testAlternative() throws IOException, COSVisitorException
    {
        String[] header = new String[]
        {
                "Daños - Agrícola y de animales",
                "2015-06-05 / 2016-01-31",
                "FONDO DE ASEGURAMIENTO AGRÍCOLA 21 DE OCTUBRE",
                "FNP-00004408003772000"
        };

        try (   InputStream logo = getClass().getResourceAsStream("Logo.jpg")   )
        {
            PDDocument document = new PDDocument();
            PdfRenderingEndorsementAlternative renderer = new PdfRenderingEndorsementAlternative(document, logo, header);

            for (String[] data : SAMPLE_ENDORSEMENT_DATA)
            {
                for (int i = 0; i < data.length - 2; i+=3)
                {
                    renderer.render(
                            new BandColumn(leftHalfPageField, data[i+1] != null ? data[i+0] : null, data[i+1]),
                            new BandColumn(rightHalfPageField, data[i+0], data[i+2])
                            );
                }
                renderer.gap(20);
            }

            renderer.close();
            document.save(new File(RESULT_FOLDER, "Endorsement.pdf"));
        }
    }

    static List<String[]> SAMPLE_ENDORSEMENT_DATA = Arrays.<String[]>asList(
            new String[]{"Tipo de captura:", "0", "3"},
            new String[]{"Código de riesgo:", "Bajo Riesgo", "Riesgo Expuesto"},
            new String[]{"Moneda:", "10-MONEDA NACIONAL", "21-DOLARES Y UDIS"},
            new String[]{"Nombre del contrato/asegurado:", "Prueba Jesus Fac No Prop", "Prueba Jesus Fac No Prop con Endoso"},
            new String[]{"País:", "México", "México"},
            new String[]{"Estado:", "Coahuila", "Coahuila", null, null, "Jalisco"},
            new String[]{"Municipio:", "Abasolo", "Abasolo", null, null, "Atoyac"},
            new String[]{"Cultivo/Especie:", "bbbbbbbb", "Cultivo especie"},
            new String[]{"Ciclo agrícola:", "cccccccc", "Ciclo agricola"},
            new String[]{"Unidad de riesgo:", "Hectárea", "Otros"},
            new String[]{"Unidad de riesgos (Otros):", null, "unidad de riesgo otros"},
            new String[]{"Bienes cubiertos:", "ddddddddd", "Bienes cubiertos, bienes cubiertos, bienes cubiertos, bienes cubiertos"},
            new String[]{"Riesgo(s) cubierto(s) / Cobertura(s):", "eeeeeeeee", "Riesgos cubiertos coberturas"},
            new String[]{"Otros riesgo(s) cubierto(s) / Cobertura(s):", "fffffffffffff", "Otros riesgos cubiertos coberturas"},
            new String[]{"Líder del coaseguro:", "jjjjjjjjjjjjjj", "Lider de coaseguro"},
            new String[]{"Retrocesión:", "No", "Sí"},
            new String[]{"Retrocesión (%):", null, "0.00"},
            new String[]{"Descripción Otros gastos (Cargos, etc.) %", "kkkkkkkkkkk", "Otros gastos cargos descripcion"},
            new String[]{"Siniestros punta:", "lllllllllllllllllllllllllllllll", "SINIESTROS PUNTA"},
            new String[]{"Descripción Siniestros al contado:", "mmmmmmmmmmmmm", "Siniestros al contado"},
            new String[]{"Plazo de pago para siniestros al contado:", "nnnnnnnnnnnnnnnnn", "Plazo de pago para siniestro al contado"},
            new String[]{"Rendición de estados de cuentas:", "---", "Mensual"},
            new String[]{"Plazo de recepción de cuentas:", "---", "10 Días"},
            new String[]{"Plazo de confirmación u objeción de cuentas después de recibidas las cuentas:", "---", "15 Días"},
            new String[]{"Plazo para el pago de saldos:", "---", "30 Días"},
            new String[]{"Plazo descripción:", "---", "Desde inicio de vigencia"},
            new String[]{"Moneda de liquidación de saldos:", "10-MONEDA NACIONAL", "21-DOLARES Y UDIS"},
            new String[]{"Tipo de cambio:", null, "18.00"},
            new String[]{"Fecha tipo de cambio:", null, "2015-06-08"},
            new String[]{"Texto de póliza:", null, "Texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza texto poliza"},
            new String[]{"Exclusiones adicionales a las mencionadas en las condiciones generales y especiales de la póliza:", "", "Exclusiones adicionales a las mencionadas en las condiciones generales y especiales de la pólizaExclusiones adicionales a las mencionadas en las condiciones generales y especiales de la pólizaExclusiones adicionales a las mencionadas en las condiciones generales y especiales de la póliza"},
            new String[]{"Aclaraciones:", "", "Aclaraciones Aclaraciones Aclaraciones Aclaraciones Aclaraciones Aclaraciones Aclaraciones"},
            new String[]{"Nombramiento del ajustador:", "", "Nombramiento del ajustadorNombramiento del ajustadorNombramiento del ajustadorNombramiento del ajustadorNombramiento del ajustador"},
            new String[]{"Ley aplicable:", "", "Ley aplicableLey aplicableLey aplicableLey aplicableLey aplicableLey aplicableLey aplicable"},
            new String[]{"Sede del tribunal de arbitraje:", "", "Sede del tribunal de arbitrajeSede del tribunal de arbitrajeSede del tribunal de arbitrajeSede del tribunal de arbitraje"},
            new String[]{"Cláusula en caso de falta de pago:", "", "Cláusula en caso de falta de pagoCláusula en caso de falta de pagoCláusula en caso de falta de pagoCláusula en caso de falta de pagoCláusula en caso de falta de pagoCláusula en caso de falta de pago"},
            new String[]{"Cláusulas de terminación de contrato:", "", "Cláusulas de terminación de contratoCláusulas de terminación de contratoCláusulas de terminación de contratoCláusulas de terminación de contratoCláusulas de terminación de contrato"},
            new String[]{"Otras cláusulas o condiciones:", "", "Otras cláusulas o condicionesOtras cláusulas o condicionesOtras cláusulas o condicionesOtras cláusulas o condicionesOtras cláusulas o condiciones"},
            new String[]{"Notas:", null, "Notas"}            
    );
}
