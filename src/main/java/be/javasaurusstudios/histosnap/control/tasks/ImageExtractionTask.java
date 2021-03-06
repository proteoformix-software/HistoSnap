package be.javasaurusstudios.histosnap.control.tasks;

import be.javasaurusstudios.histosnap.control.MzRangeExtractor;
import be.javasaurusstudios.histosnap.control.util.UILogger;
import be.javasaurusstudios.histosnap.model.MSScanAdduct;
import be.javasaurusstudios.histosnap.model.image.MSiImage;
import be.javasaurusstudios.histosnap.view.MSImagizer;
import be.javasaurusstudios.histosnap.view.component.ProgressBarFrame;
import be.javasaurusstudios.histosnap.model.task.WorkingTask;
import be.javasaurusstudios.histosnap.control.util.color.ColorRange;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * This class represents a task to extract an image given a mz range
 *
 * @author Dr. Kenneth Verheggen <kenneth.verheggen@proteoformix.com>
 */
public class ImageExtractionTask extends WorkingTask {

    ///Java Swing UI elements  
    //The parent JFrame 
    private final JFrame parent;
    //The textfield for the input file
    private final JTextField tfInput;
    //the label for the image icon
    private final JLabel imageIcon;
    ///State indicators
    //boolean to indicate if the intermediate images should be saved
    private boolean saveIntermediate = false;
    //the scale the images will be rendered to (pixel scale)
    private int pixelScale = 1;
    //The image generation mode (the reference the intensities for a pixel are compared to)
    private MSiImage.ImageMode mode = MSiImage.ImageMode.MEAN;
    //The range of colors that will be applied
    private ColorRange colorRange;
    ///Calculator values
    //The maximal mz value to consider
    private float maxMZ = -1;
    //The minimal mz value to consider
    private float minMZ = -1;
    //The stepcount (= bin count)
    private float stepCount = -1;
    //The calculated interval
    private float interval = -1;
    //The name for this image
    private String imageName = "";

    /**
     *
     * @param parent The parent JFrame
     * @param progressBar The progressbar to report to
     * @param tfInput the input file
     * @param minMZField the text field for the minimal mz
     * @param maxMZField the text for the maximal mz
     * @param stepsField the field for the amount of steps (1= none)
     * @param imageIcon the icon to load the image to
     * @param scale the pixel scale
     * @param mode the intensity mode
     * @param range the color range
     * @param saveIntermediate boolean indicating if this image has to be auto
     * saved
     */
    public ImageExtractionTask(JFrame parent, ProgressBarFrame progressBar, JTextField tfInput, JTextField minMZField, JTextField maxMZField, JTextField stepsField, JLabel imageIcon, int scale, MSiImage.ImageMode mode, ColorRange range, boolean saveIntermediate) {
        super(progressBar);
        this.parent = parent;
        this.tfInput = tfInput;
        this.imageIcon = imageIcon;
        this.pixelScale = scale;
        this.mode = mode;
        this.saveIntermediate = saveIntermediate;
        this.colorRange = range;

        try {
            minMZ = (float) (Float.parseFloat(minMZField.getText().replace(",", ".")));
            minMZField.setText("" + minMZ);
            if (minMZ < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            progressBar.setVisible(false);
            JOptionPane.showMessageDialog(parent,
                    "The minimal MZ value should be a real number >=0",
                    "Invalid mass to charge range",
                    JOptionPane.ERROR_MESSAGE);
            UILogger.Log("Invalid mass to charge range was provided.", UILogger.Level.ERROR);
        }
        try {
            maxMZ = (float) (Float.parseFloat((maxMZField.getText().replace(",", "."))));
            maxMZField.setText("" + maxMZ);
            if (maxMZ <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            progressBar.setVisible(false);
            JOptionPane.showMessageDialog(parent,
                    "The maximal MZ value should be a real number >0",
                    "Invalid mass to charge range",
                    JOptionPane.ERROR_MESSAGE);
            UILogger.Log("Invalid mass to charge range was provided.", UILogger.Level.ERROR);
        }

        if (stepsField == null) {
            stepCount = 1;
            interval = maxMZ - minMZ;
        } else {
            try {
                stepCount = Integer.parseInt(stepsField.getText());
                if (stepCount < 1) {
                    throw new NumberFormatException();
                }

                interval = (maxMZ - minMZ) / stepCount;

            } catch (NumberFormatException e) {
                progressBar.setVisible(false);
                JOptionPane.showMessageDialog(parent,
                        "The step count >=1",
                        "Invalid step size",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Invalid step size was provided.", UILogger.Level.ERROR);
            }
        }

    }

    /**
     *
     * @param parent The parent JFrame
     * @param progressBar The progressbar to report to
     * @param tfInput the input file
     * @param minMz the minimal mz
     * @param maxMz the maximal mz
     * @param steps the amount of iterations (1 = none)
     * @param imageIcon the icon to load the image to
     * @param scale the pixel scale
     * @param mode the intensity mode
     * @param range the color range
     * @param saveIntermediate boolean indicating if this image has to be auto
     * saved
     */
    public ImageExtractionTask(JFrame parent, ProgressBarFrame progressBar, JTextField tfInput, float minMz, float maxMz, int steps, JLabel imageIcon, int scale, MSiImage.ImageMode mode, ColorRange range, boolean saveIntermediate) {
        super(progressBar);
        this.parent = parent;
        this.tfInput = tfInput;

        this.minMZ = minMz;
        this.maxMZ = maxMz;
        this.stepCount = steps;

        this.imageIcon = imageIcon;
        this.pixelScale = scale;
        this.mode = mode;
        this.saveIntermediate = saveIntermediate;
        this.colorRange = range;

        this.interval = (maxMZ - minMZ) / stepCount;
    }

    @Override
    public Object call() throws Exception {
        Process(tfInput, imageIcon, pixelScale, colorRange, saveIntermediate);
        return "Done.";
    }

    /**
     * Processes the input file into an image
     *
     * @param tfInput the input file
     * @param imageIcon the icon to report to
     * @param scale the pixel scale
     * @param range the color range
     * @param autoSave boolean indicating if the image should be saved as an
     * intermediate
     * @throws Exception
     */
    protected void Process(JTextField tfInput, JLabel imageIcon, int scale, ColorRange range, boolean autoSave) throws Exception {

        if (!imageName.isEmpty()) {
            progressBar.setText("Extracting " + imageName);
        }

        if (minMZ == -1 || maxMZ == -1) {
            throw new Exception("Please check the mz range...");
        }

        imageIcon.setText("");
        try {
            String in = tfInput.getText();

            File inFile = new File(in);
            if (!inFile.exists()) {
                progressBar.setVisible(false);
                JOptionPane.showMessageDialog(parent,
                        "Please specify an input imzml file",
                        "Invalid input file",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Invalid input file", UILogger.Level.ERROR);
                return;
            }

            File idbFile = new File(inFile.getAbsolutePath().replace(".imzml", ".ibd"));
            if (!idbFile.exists()) {
                progressBar.setVisible(false);
                JOptionPane.showMessageDialog(parent,
                        "The corresponding ibd file could not be found in the provided file directory./nPlease verify that an idb file exist with the EXACT same name as the provided imzml.",
                        "Invalid input file",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Invalid input file", UILogger.Level.ERROR);
                return;
            }

            ExecuteImage(in, scale, imageName, range, 0, autoSave);

            for (MSScanAdduct.ADDUCTS adduct : MSScanAdduct.ENABLED_ADDUCTS) {
                ExecuteImage(in, scale, imageName + "_" + adduct.toString(), range, adduct.getMassDeficit(), autoSave);
            }

        } catch (Exception ex) {
            if (!imageName.isEmpty()) {
                progressBar.setText(imageName + " extraction has failed. Please verify if the requested mass range is present in the input data");
            }
            ex.printStackTrace();
        }
    }

    private void ExecuteImage(String in, int scale, String extractionName, ColorRange range, float massOffset, boolean autoSave) throws Exception {
        for (int i = 0; i < stepCount; i++) {

            float minMzTmp = Math.max(minMZ, minMZ + (i * interval));
            float minMzStep = minMzTmp + massOffset;
            float maxMzTmp = Math.min(maxMZ, minMZ + ((i + 1) * interval));
            float maxMzStep = maxMzTmp + massOffset;

            progressBar.setText("Processing from " + minMzStep + " to " + maxMzStep);

            String tmp = in + minMzStep + "to" + maxMzStep + ".tmp.txt";
            MzRangeExtractor extractor = new MzRangeExtractor(in, tmp);

            MSiImage image = extractor.ExtractImage(minMzStep, maxMzStep, progressBar);

            image.setName(minMzTmp + " - " + maxMzTmp + ((extractionName == null || extractionName.isEmpty()) ? "" : "_" + extractionName));

            MSImagizer.AddToCache(image);
            progressBar.setText("Removing hotspots");
            MSImagizer.MSI_IMAGE.RemoveHotSpots(99);
            progressBar.setText("Generating heatmap...");
            MSImagizer.MSI_IMAGE.CreateImage(mode, range.getColors());

            MSImagizer.CURRENT_IMAGE = MSImagizer.MSI_IMAGE.getScaledImage(scale);

            if (imageIcon != null) {
                ImageIcon icon = new ImageIcon(MSImagizer.CURRENT_IMAGE);
                imageIcon.setIcon(icon);
                imageIcon.setText("");
            }
            parent.repaint();

            if (autoSave) {
                File outputDir = new File(new File(tmp).getParentFile(), minMZ + "-to-" + maxMZ);
                File tmpFile = new File(tmp);
                outputDir.mkdirs();
                File outputFile = new File(outputDir, tmpFile.getName().replace(".tmp.txt", ".png"));
                progressBar.setText("Saving to " + outputFile);
                ImageIO.write(MSImagizer.CURRENT_IMAGE, "png", outputFile);
            }

        }
    }

    public void setImageName(String name) {
        this.imageName = name;
    }

    @Override
    public String getFinishMessage() {
        return "Image extraction completed.";
    }

}
