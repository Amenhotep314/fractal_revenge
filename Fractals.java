import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Fractals {
    public static void main(String[] args) {
        double xMin = -2.0; // Define the range of the x- and y-axes
        double xMax = 1.0;
        double yMin = -1.0;
        double yMax = 1.0;
        int width = 1080; // Define the resolution of the image in pixels, and don't bother enforcing the aspect ratio
        int height = 720;

        // These are just parameters for the render
        double exponent = 1.0;
        double exponentIncrement = 0.01;

        // A parallel computing fiesta! Originally this was running linearly in Python, which was gonna take days.
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

        // For every frame, generate a Mandelbrot set with a different exponent and save it to an image.
        for (int i = 0; i < 720; i++) {
            int[][] mandelbrotSet = generateMandelbrotSet(width, height, xMin, xMax, yMin, yMax, exponent);
            exponent += exponentIncrement;

            String filename = String.format("%03d", Integer.valueOf(i)) + ".png";
            System.out.println("Saved image: " + filename);
            executor.submit(() -> saveImage(mandelbrotSet, width, height, filename));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int[][] generateMandelbrotSet(int width, int height, double xMin, double xMax, double yMin, double yMax, double exponent) {
        int[][] mandelbrotSet = new int[width][height];
        double realScale = (xMax - xMin) / (double)width;
        double imaginaryScale = (yMax - yMin) / (double)height;

        ExecutorService mandelbrotter = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        for (int x = 0; x < width; x++) {
            final int xFinal = x;
            for (int y = 0; y < height; y++) {
                final int yFinal = y;
                mandelbrotter.submit(() -> {
                    double real = xMin + xFinal * realScale;
                    double imaginary = yMin + yFinal * imaginaryScale;
                    Complex c = new Complex(real, imaginary);
                    Complex z = new Complex(0, 0);

                    int depth = isInMandelbrotSet(z, c, exponent, 0);
                    mandelbrotSet[xFinal][yFinal] = depth;
                });
            }
        }
        mandelbrotter.shutdown();
        try {
            mandelbrotter.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mandelbrotSet;
    }

    public static int isInMandelbrotSet(Complex z, Complex c, double exponent, int depth) {
        Complex result = z.pow(exponent).add(c);

        if (z.real() * z.real() + z.im() * z.im() <= 4 && depth < 900) {
            return isInMandelbrotSet(result, c, exponent, depth + 1);
        } else {
            if (depth < 900) {
                return depth;
            } else {
                return -1;
            }
        }
    }

    public static void saveImage(int[][] image, int width, int height, String filePath) {

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int maxDepth = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (image[x][y] > maxDepth) {
                    maxDepth = image[x][y];
                }
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (image[x][y] == -1) {
                    bufferedImage.setRGB(x, y, 0x000000);
                    continue;
                }
                int[] rgbColor = calculateShade(image[x][y], maxDepth);
                int r = rgbColor[0];
                int g = rgbColor[1];
                int b = rgbColor[2];

                if (r > 255) { r = 255; }
                if (g > 255) { g = 255; }
                if (b > 255) { b = 255; }
                int rgb = (r << 16) | (g << 8) | b;
                bufferedImage.setRGB(x, y, rgb);
            }
        }

        try {
            File outputFile = new File(filePath);
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] calculateShade(int depth, int maxDepth) {
        int color = (int)((-255 / (maxDepth * maxDepth)) * ((depth - maxDepth) * (depth - maxDepth)) + 255);

        int red = 255-color;
        int green = 128;
        int blue = color;

        return new int[]{red, green, blue};
    }
}