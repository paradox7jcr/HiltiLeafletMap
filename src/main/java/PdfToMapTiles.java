import java.awt.image.BufferedImage;
import java.io.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.tools.PDFToImage;

import javax.imageio.ImageIO;

public class PdfToMapTiles {
    public static void main(String[] args) throws IOException {
        String resourcesPath = "src/main/resources/";
        String PARTIAL_PDF = resourcesPath + "FS-1P1A - PARTIAL P1 FS FLOOR PLAN.pdf";
        String PARTIAL_JPG = resourcesPath + "FS-1P1A - PARTIAL P1 FS FLOOR PLAN1.jpg";
        String BIG_GERMAN_PDF = resourcesPath + "BIG GERMAN Floor Plan.pdf";
        String BIG_GERMAN_JPG = resourcesPath + "BIG GERMAN Floor Plan1.jpg";
        String TEST_PDF = resourcesPath + "test.pdf";
        String TEST_JPG = resourcesPath + "test1.jpg";

        float[] dataset1 = getDataForFile(BIG_GERMAN_PDF);
        float[] dataset2 = getDataForFile(PARTIAL_PDF);
        float[] dataset3 = getDataForFile(TEST_PDF);

        convertPdfToJpg(BIG_GERMAN_PDF, (int) dataset1[2]);
        convertPdfToJpg(PARTIAL_PDF, (int) dataset2[2]);
        convertPdfToJpg(TEST_PDF, (int) dataset3[2]);
        getDataForFile(BIG_GERMAN_JPG);
        getDataForFile(PARTIAL_JPG);
        getDataForFile(TEST_JPG);


        //COMMAND LINE
        //gdal_translate -a_srs WGS84 -a_ullr -180 +90 +180 -90 BIG\ GERMAN\ Floor\ Plan1.jpg BIG\ GERMAN\ Floor\ Plan.tif
        //gdal_translate -co "ZLEVEL=9" -of mbtiles BIG\ GERMAN\ Floor\ Plan.tif big_german_floor_plan.mbtiles
        //gdaladdo -r nearest big_german_floor_plan.mbtiles 2 4 8 16 32 64 (only did 5/6 levels)
    }

    private static void execDebug(String command){
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        //Wait to get exit value
        try {
            p.waitFor();
            final int exitValue = p.waitFor();
            if (exitValue == 0)
                System.out.println("Successfully executed the command: " + command);
            else {
                System.out.println("Failed to execute the following command: " + command + " due to the following error(s):");
                try (final BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String line;
                    if ((line = b.readLine()) != null)
                        System.out.println(line);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void convertPdfToJpg(String pdfPath, int dpi) throws IOException {
        PDFToImage.main(new String[] {"-dpi", Integer.toString(dpi), pdfPath});
    }

    private static float[] getDataForFile(String fileName) throws IOException {
        File file = new File(fileName);
        String extension = fileName.split("\\.")[1];
        float height = 0, width = 0;
        switch(extension){
            case "pdf":
                PDDocument document = PDDocument.load(file);
                PDPage page = document.getPage(0);
                PDRectangle cropBox = page.getCropBox();
                height = cropBox.getHeight();
                width = cropBox.getWidth();
                document.close();
                break;
            case "jpg":
                BufferedImage image = ImageIO.read(file);
                height = image.getHeight();
                width = image.getWidth();
                break;
            default:
                break;
        }
        System.out.println("File Location: " + fileName);
        System.out.println("File Size: " + getFileSizeMegaBytes(file));
        System.out.println("Height: " + height + " Width: " + width);
        int dpi = (int) (4096/Math.max(width, height)) * 100;
        System.out.println("Proposed dpi " + dpi);
        return new float[]{height, width, dpi, getFileSizeMegaBytes(file)};
    }

    private static float getFileSizeMegaBytes(File file) {
        return (float) file.length() / (1024 * 1024);
    }
}