package be.javasaurusstudios.histosnap.control;

import be.javasaurusstudios.histosnap.control.cache.HistoSnapDBFile;
import be.javasaurusstudios.histosnap.model.image.MSiImage;
import be.javasaurusstudios.histosnap.model.image.MSiFrame;
import be.javasaurusstudios.histosnap.control.util.PythonExtractor;
import be.javasaurusstudios.histosnap.control.util.SystemUtils;
import be.javasaurusstudios.histosnap.control.util.UILogger;
import be.javasaurusstudios.histosnap.model.image.MultiMSiImage;
import be.javasaurusstudios.histosnap.view.MSImagizer;
import be.javasaurusstudios.histosnap.view.component.ProgressBar;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * A wrapper that calls the python script to extract a certain mz range from a
 * file
 *
 * @author Dr. Kenneth Verheggen <kenneth.verheggen@proteoformix.com>
 */
public class MzRangeExtractor {

    //The path to the input file
    private final String in;
    //The path to the output file
    private final String out;

    /**
     * Constructor
     *
     * @param in the input file path
     * @param out the output file path
     */
    public MzRangeExtractor(String in, String out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Extracts spectra into an MSiImage object using the python library (see
     * docs for more inf)
     *
     * @param mzMin The minimal MZ to consider
     * @param mzMax The maximal MZ to consider
     * @param progressBar The progressbar to indicate progress (can be null)
     * @return the extracted image
     * @throws IOException
     * @throws URISyntaxException
     * @throws Exception
     */
    public MSiImage extractSingleImage(float mzMin, float mzMax) throws IOException, URISyntaxException, Exception {

        if (MSImagizer.instance == null || MSImagizer.instance.isHighMemory()) {
            SystemUtils.MemoryState memoryState = SystemUtils.getMemoryState();
            DecimalFormat df = new DecimalFormat("#.##");
            String memory = df.format(SystemUtils.getMaxMemory());
            int dialogResult;
            switch (memoryState) {
                case HIGH:
                    return extractImageMem(mzMin, mzMax);
                case MEDIUM:
                    dialogResult = JOptionPane.showConfirmDialog(
                            MSImagizer.instance,
                            memory + " GB available memory was detected. This might be insufficient. Please consider System Settings > Low Memory Mode if the process times out. Do you wish to continue?",
                            "Memory Settings",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        return extractImageMem(mzMin, mzMax);
                    } else {
                        return null;
                    }
                case LOW:
                    dialogResult = JOptionPane.showConfirmDialog(
                            MSImagizer.instance,
                            memory + " GB available memory was detected. This will likely be insufficient, even for small projects. Please use System Settings > Low Memory Mode if the process times out. Do you wish to continue (not recommended)?",
                            "Memory Settings",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        return extractImageMem(mzMin, mzMax);
                    } else {
                        return null;
                    }
                default:
                    JOptionPane.showMessageDialog(
                            MSImagizer.instance,
                            "Insufficient memory (" + memory + " GB) available. Please enable System Settings > Low Memory Mode",
                            "Memory Settings",
                            JOptionPane.PLAIN_MESSAGE);
                    return null;
            }
        } else {

            return extractImageDb(mzMin, mzMax);
        }
    }

    /**
     * Extracts spectra into an MSiImage object using the python library (see
     * docs for more inf)
     *
     * @param ranges
     * @param progressBar The progressbar to indicate progress (can be null)
     * @return the extracted image
     * @throws IOException
     * @throws URISyntaxException
     * @throws Exception
     */
    public MultiMSiImage extractImageRange(List<float[]> ranges) throws IOException, URISyntaxException, Exception {

        if (MSImagizer.instance == null || MSImagizer.instance.isHighMemory()) {
            SystemUtils.MemoryState memoryState = SystemUtils.getMemoryState();
            DecimalFormat df = new DecimalFormat("#.##");
            String memory = df.format(SystemUtils.getMaxMemory());
            int dialogResult;
            switch (memoryState) {
                case HIGH:
                    return extractImageRangeMem(ranges);
                case MEDIUM:
                    dialogResult = JOptionPane.showConfirmDialog(
                            MSImagizer.instance,
                            memory + " GB available memory was detected. This might be insufficient. Please consider System Settings > Low Memory Mode if the process times out. Do you wish to continue?",
                            "Memory Settings",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        return extractImageRangeMem(ranges);
                    } else {
                        return null;
                    }
                case LOW:
                    dialogResult = JOptionPane.showConfirmDialog(
                            MSImagizer.instance,
                            memory + " GB available memory was detected. This will likely be insufficient, even for small projects. Please use System Settings > Low Memory Mode if the process times out. Do you wish to continue (not recommended)?",
                            "Memory Settings",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        return extractImageRangeMem(ranges);
                    } else {
                        return null;
                    }
                default:
                    JOptionPane.showMessageDialog(
                            MSImagizer.instance,
                            "Insufficient memory (" + memory + " GB) available. Please enable System Settings > Low Memory Mode",
                            "Memory Settings",
                            JOptionPane.PLAIN_MESSAGE);
                    return null;
            }
        } else {

            return extractImageRangeDb(ranges);
        }
    }

    ////DATABASE
    private MultiMSiImage extractImageRangeDb(List<float[]> ranges) throws Exception {

        UILogger.Log("Extracting image from hard drive...", UILogger.Level.INFO);

        float minMz = Float.MAX_VALUE;
        float maxMz = Float.MIN_VALUE;
        for (float[] range : ranges) {
            minMz = Math.min(range[0], minMz);
            maxMz = Math.max(range[1], maxMz);
        }

        long time = System.currentTimeMillis();

        File dbFile = new File(in + ".db");
        if (!dbFile.exists()) {
            MSImagizer.instance.getProgressBar().setText("Generating database file...");
            UILogger.Log("Creating database, this may take a while...", UILogger.Level.INFO);
            String pythonFile = PythonExtractor.getPythonScript("CreateDB.py").getAbsolutePath();

            String[] cmds = new String[]{"python", pythonFile, "--input", in};
            ProcessBuilder builder = new ProcessBuilder(cmds);
            Process process = builder.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            MSImagizer.instance.getProgressBar().setText(line);
                            //                        UILogger.Log(line);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(MzRangeExtractor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();

            process.waitFor();
        }
        HistoSnapDBFile file = new HistoSnapDBFile(dbFile);
        UILogger.Log("Processing between " + minMz + " and " + maxMz, UILogger.Level.INFO);

        MSiFrame frame = file.getImage(minMz, maxMz).getFrame();

        System.out.println("Completed loading file in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Future<MSiFrame>> subFrames = new LinkedList<>();
        for (float[] range : ranges) {
            subFrames.add(executor.submit(() -> {
                MSiFrame subFrame = frame.CreateSubFrame(range[0], range[1]);
                subFrame.setName(range[0] + " - " + range[1]);
                return subFrame;
            }));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.DAYS);

        List<MSiFrame> frames = new ArrayList<>();
        for (Future<MSiFrame> subFrame : subFrames) {
            frames.add(subFrame.get());
        }

        System.out.println("Completed loading file in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");

        File tmp = new File(out);
        if (tmp.exists()) {
            tmp.delete();
        }

        return MultiMSiImage.Generate(frames);

    }

    /**
     * Extracts spectra into an MSiImage object using the python library (see
     * docs for more inf)
     *
     * @param mzMin The minimal MZ to consider
     * @param mzMax The maximal MZ to consider
     * @param progressBar The progressbar to indicate progress (can be null)
     * @return the extracted image
     * @throws IOException
     * @throws URISyntaxException
     * @throws Exception
     */
    /////
    private MSiImage extractImageDb(float mzMin, float mzMax) throws Exception {

        UILogger.Log("Extracting image from hard drive...", UILogger.Level.INFO);

        long time = System.currentTimeMillis();

        File dbFile = new File(in + ".db");
        if (!dbFile.exists()) {
            MSImagizer.instance.getProgressBar().setText("Generating database file...");
            UILogger.Log("Creating database, this may take a while...", UILogger.Level.INFO);
            String pythonFile = PythonExtractor.getPythonScript("CreateDB.py").getAbsolutePath();

            String[] cmds = new String[]{"python", pythonFile, "--input", in};
            ProcessBuilder builder = new ProcessBuilder(cmds);
            Process process = builder.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            MSImagizer.instance.getProgressBar().setText(line);
                            //                        UILogger.Log(line);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(MzRangeExtractor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();

            process.waitFor();
        }

        HistoSnapDBFile file = new HistoSnapDBFile(dbFile);
        UILogger.Log("Processing between " + mzMin + " and " + mzMax, UILogger.Level.INFO);
        MSiImage image = file.getImage(mzMin, mzMax);

        System.out.println("Completed loading file in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");

        File tmp = new File(out);
        if (tmp.exists()) {
            tmp.delete();
        }
        return image;
    }

    ////MEMORY
    private MultiMSiImage extractImageRangeMem(List<float[]> ranges) throws Exception {

        if (ranges.isEmpty()) {
            JOptionPane.showMessageDialog(MSImagizer.instance, "There is no range available");
            return null;
        }

        UILogger.Log("Extracting image from memory...", UILogger.Level.INFO);

        long time = System.currentTimeMillis();

        float minMz = Float.MAX_VALUE;
        float maxMz = Float.MIN_VALUE;
        for (float[] range : ranges) {
            minMz = Math.min(range[0], minMz);
            maxMz = Math.max(range[1], maxMz);
        }

        String pythonFile = PythonExtractor.getPythonScript("Extract.py").getAbsolutePath();

        String[] cmds = new String[]{"python", pythonFile, "--mzMin", "" + minMz, "--mzMax", "" + maxMz, "--input", in, "--output", out};

        ProcessBuilder builder = new ProcessBuilder(cmds);
        //  builder.inheritIO();
        Process process = builder.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        MSImagizer.instance.getProgressBar().setText(line);
                        //                        UILogger.Log(line);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MzRangeExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();

        process.waitFor();

        MSiFrame frame = new SpectralDataImporter().ReadFile(new File(out));

        if (frame.getWidth() <= 0 || frame.getHeight() <= 0) {
            return null;
        }

        frame.setParentFile(in);

        UILogger.Log("Processing between " + frame.getMinMz() + " and " + frame.getMaxMz(), UILogger.Level.INFO);

        List<MSiFrame> frames = new ArrayList<>();
        for (float[] range : ranges) {
            MSiFrame subFrame = frame.CreateSubFrame(range[0], range[1]);
            subFrame.setName(range[0] + " - " + range[1]);
            frames.add(subFrame);
        }
        System.out.println("Completed loading file in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");

        File tmp = new File(out);
        if (tmp.exists()) {
            tmp.delete();
        }
        return MultiMSiImage.Generate(frames);
    }

    /**
     * Extracts spectra into an MSiImage object using the python library (see
     * docs for more inf)
     *
     * @param mzMin The minimal MZ to consider
     * @param mzMax The maximal MZ to consider
     * @param progressBar The progressbar to indicate progress (can be null)
     * @return the extracted image
     * @throws IOException
     * @throws URISyntaxException
     * @throws Exception
     */
    private MSiImage extractImageMem(float mzMin, float mzMax) throws IOException, URISyntaxException, Exception {

        UILogger.Log("Extracting image from memory...", UILogger.Level.INFO);

        long time = System.currentTimeMillis();

        String pythonFile = PythonExtractor.getPythonScript("Extract.py").getAbsolutePath();

        String[] cmds = new String[]{"python", pythonFile, "--mzMin", "" + mzMin, "--mzMax", "" + mzMax, "--input", in, "--output", out};

        ProcessBuilder builder = new ProcessBuilder(cmds);
        //  builder.inheritIO();
        Process process = builder.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        MSImagizer.instance.getProgressBar().setText(line);
                        //                        UILogger.Log(line);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MzRangeExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();

        process.waitFor();

        MSiFrame frame = new SpectralDataImporter().ReadFile(new File(out));

        if (frame.getWidth() <= 0 || frame.getHeight() <= 0) {
            return null;
        }

        frame.setParentFile(in);

        UILogger.Log("Processing between " + frame.getMinMz() + " and " + frame.getMaxMz(), UILogger.Level.INFO);
        MSiImage image = new MSiImage(frame);

        System.out.println("Completed loading file in " + ((System.currentTimeMillis() - time) / 1000) + " seconds");

        File tmp = new File(out);
        if (tmp.exists()) {
            tmp.delete();
        }
        return image;
    }

    /**
     * Extracts all spectra into an MSiImage Object
     *
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws Exception
     */
    public MSiImage ExtractFull() throws IOException, URISyntaxException, Exception {
        return extractSingleImage(-1, Float.MAX_VALUE);
    }

}
