import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MedianImage {

    // really should be Mode not median

    public static final String TEST_FOLDER = "test_images";
    public static double MERGE_THRESHOLD = 42;


    public static void main(String... args) throws IOException {

        BufferedImage out = medianImage(TEST_FOLDER);
        ImageIO.write(out, "png", new File("out.png"));

    }

    public static BufferedImage[] loadImagesFromFolder(String folderPath) throws IOException {
        File f = new File(folderPath);

        File[] subFiles = f.listFiles();
        BufferedImage[] images = new BufferedImage[subFiles.length];

        for(int i = 0; i < subFiles.length; i++){
            images[i] = ImageIO.read(subFiles[i]);
        }
        return images;
    }

    public static Integer getMode(List<Integer> colorsAtSpot){
        Map<Integer, Integer> colorToCount = new HashMap<>();
        int maxColor = -1;
        int maxCount = -1;
        for(int color : colorsAtSpot){
            colorToCount.put(color, colorToCount.getOrDefault(color, 0) + 1);
            if(colorToCount.get(color) > maxCount){
                maxColor = color;
                maxCount = colorToCount.get(color);
            }
        }
        return maxColor;
    }

    public static double colorDistance(Integer c1, Integer c2){
        Color c1c = new Color(c1);
        Color c2c = new Color(c2);
        return Math.sqrt(Math.pow(c1c.getRed() - c2c.getRed(), 2) +
                         Math.pow(c1c.getGreen() - c2c.getGreen(), 2) +
                         Math.pow(c1c.getBlue() - c2c.getBlue(), 2));
    }

    public static Integer averageColors(List<Integer> colors){
        double redSum = 0;
        double greenSum = 0;
        double blueSum = 0;

        for(Integer i : colors){
            Color c = new Color(i);
            redSum += c.getRed();
            greenSum += c.getGreen();
            blueSum += c.getBlue();
        }

        Color avg = new Color(Math.toIntExact(Math.round(redSum / colors.size())),
                              Math.toIntExact(Math.round(greenSum / colors.size())),
                              Math.toIntExact(Math.round(blueSum / colors.size())));
        return avg.getRGB();
    }

    public static List<Integer> mergeColors(List<Integer> colors){
        Map<Integer, ArrayList<Integer>> colorToSimilar = new HashMap<>();

        for(Integer c : colors){
            double minDistance = Double.MAX_VALUE;
            Integer mostSimilarColor = null;
            for(Integer similarColor : colorToSimilar.keySet()){
                if(colorDistance(c, similarColor) < minDistance){
                    minDistance = colorDistance(c, similarColor);
                    mostSimilarColor = similarColor;
                }
            }

            if (minDistance < MERGE_THRESHOLD) {
                ArrayList<Integer> similarColors = colorToSimilar.get(mostSimilarColor);
                similarColors.add(c);
                colorToSimilar.put(mostSimilarColor, similarColors);
            } else {
                colorToSimilar.put(c, new ArrayList<>(Collections.singletonList(c)));
            }
        }



        ArrayList<Integer> finalColors = new ArrayList<>();
        for(Integer key : colorToSimilar.keySet()){
            List<Integer> similars = colorToSimilar.get(key);
            Integer avg = averageColors(similars);
            for(int i = 0; i < similars.size(); i++){
                finalColors.add(avg);
            }
        }

        return finalColors;
    }

    public static BufferedImage medianImage(String folderPath) throws IOException {
        BufferedImage[] images = loadImagesFromFolder(folderPath);
        BufferedImage out = new BufferedImage(images[0].getWidth(), images[0].getHeight(), BufferedImage.TYPE_INT_ARGB);

        for(int x = 0; x < images[0].getWidth(); x++){
            for (int y = 0; y < images[0].getHeight(); y++) {
                List<Integer> colorsAtSpot = new ArrayList<>();
                for(BufferedImage image : images){
                    colorsAtSpot.add(image.getRGB(x, y));
                }
                colorsAtSpot = mergeColors(colorsAtSpot);
                out.setRGB(x, y, getMode(colorsAtSpot));
            }
        }

        return out;
    }


}
