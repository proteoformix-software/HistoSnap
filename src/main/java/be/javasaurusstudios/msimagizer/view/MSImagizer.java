package be.javasaurusstudios.msimagizer.view;

import be.javasaurusstudios.msimagizer.view.component.ProgressBarFrame;
import be.javasaurusstudios.msimagizer.view.component.SimilaritySetup;
import be.javasaurusstudios.msimagizer.control.tasks.WorkingThread;
import be.javasaurusstudios.msimagizer.control.MSiImageCache;
import be.javasaurusstudios.msimagizer.control.tasks.ImageExtractionTask;
import be.javasaurusstudios.msimagizer.model.MSScanAdduct;
import be.javasaurusstudios.msimagizer.model.image.MSiImage;
import be.javasaurusstudios.msimagizer.control.util.AnimationExporter;
import be.javasaurusstudios.msimagizer.control.util.color.ColorRange;
import be.javasaurusstudios.msimagizer.control.util.ImageUtils;
import be.javasaurusstudios.msimagizer.control.util.UILogger;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

/**
 * The main UI frame for the imagizer
 *
 * @author Dr. Kenneth Verheggen <kenneth.verheggen@proteoformix.com>
 */
public class MSImagizer extends javax.swing.JFrame {

    //The last directory that was used by the user interface
    public static File lastDirectory = new File(System.getProperty("user.home"));
    //The currently active MSI_Image
    public static MSiImage MSI_IMAGE;
    //The currently buffered image that is being displayed
    public static BufferedImage CURRENT_IMAGE;
    //The msiImageCache (to prevent 
    public static final MSiImageCache CACHE = new MSiImageCache();
    //The progress bar (can be updated by working tasks)
    private final ProgressBarFrame progressFrame;
    //The UI element for the produced images
    private static JList imageCacheList;
    //The current pixel scale
    private int currentScale = 4;
    //The current range of colors that will be applied
    private ColorRange currentRange = ColorRange.BLUE_YELLOW;
    //The current mode of reference for intensities
    private MSiImage.ImageMode currentMode = MSiImage.ImageMode.MEAN;
    private JMenuItem deleteItem;
    private JMenuItem renameItem;
    private JMenuItem saveAnimationItem;
    private JMenuItem saveFrameItem;
    private JMenuItem similarityItem;

    /**
     * Creates new form MSImagizer
     */
    public MSImagizer() {

        super.setTitle("ProteoFormiX HistoSnap");
        super.setLocationRelativeTo(null);

        try {
            Image i = ImageIO.read(getClass().getResource("/Logo.png"));
            super.setIconImage(i);
        } catch (IOException ex) {
            Logger.getLogger(MSImagizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        initComponents();

        UILogger.LOGGING_AREA = logArea;

        InitAdducts();
        InitModes();

        imageCacheList = imageList;

        progressFrame = new ProgressBarFrame();
        progressFrame.setVisible(false);

        InitSavePopup();
        InitListSelection();
        InitListPopup();

        logArea.setModel(new DefaultListModel<>());

        UILogger.Log("ProteoFormiX presents", UILogger.Level.NONE);
        UILogger.Log("HistoSnap is ready. Welcome !", UILogger.Level.NONE);
        UILogger.Log("-----------------------------", UILogger.Level.NONE);
    }

    private void InitModes() {
        //scale
        btnX1.setSelected(false);
        btnX2.setSelected(false);
        btnX4.setSelected(true);
        btnX8.setSelected(false);
        btnX16.setSelected(false);
        //mode
        btnMean.setSelected(true);
        btnMedian.setSelected(false);
        btnMin.setSelected(false);
        btnMax.setSelected(false);
        btnQ1.setSelected(false);
        btnQ2.setSelected(false);
        btn90th.setSelected(false);
        btn95th.setSelected(false);
        btn99th.setSelected(false);
    }

    private void InitAdducts() {
        BtnAllCations.setSelected(false);
        btnAnionsAll.setSelected(false);
        InitCationAdducts(false);
        InitAnionAdducts(false);
    }

    private void InitCationAdducts(boolean selected) {

        btnCatCH3CNH.setSelected(selected);
        btnCatCH3OH.setSelected(selected);
        btnCatDMSO.setSelected(selected);
        btnCatK.setSelected(selected);
        btnCatNH4.setSelected(selected);
        btnCatNa.setSelected(selected);
    }

    private void InitAnionAdducts(boolean selected) {
        btnAnionCl.setSelected(selected);
        btnAnionHCOO.setSelected(selected);
        btnAnionK.setSelected(selected);
        btnAnionNa.setSelected(selected);
        btnAnionOAc.setSelected(selected);
        btnAnionTFA.setSelected(selected);

    }

    /**
     * Shows a dialog to selected frame(s)
     */
    private void InitSavePopup() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Save...");
        menu.add(item);

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Save();
            }
        });

        lbImage.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (CURRENT_IMAGE != null && (e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

    }

    /**
     * Initializes the list of current images
     */
    private void InitListSelection() {
        imageCacheList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (imageCacheList.getSelectedValuesList().size() == 1) {
                    MSI_IMAGE = (MSiImage) imageCacheList.getSelectedValue();
                    UpdateImage();
                }
            }
        });
    }

    /**
     * Creates a popup menu with options to execute on the list selection
     */
    private void InitListPopup() {

        final MSImagizer parent = this;

        JPopupMenu menu = new JPopupMenu();
        deleteItem = new JMenuItem("Delete...");
        menu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<MSiImage> selectedImages = imageCacheList.getSelectedValuesList();
                CACHE.getImageList().removeAll(selectedImages);
                UILogger.Log("Deleted " + selectedImages.size() + " image(s)", UILogger.Level.INFO);
                UpdateCacheUI();
                if (CACHE.getImageList().size() > 0) {
                    MSI_IMAGE = CACHE.getImageList().get(0);
                    imageCacheList.setSelectedIndex(0);
                }

                UpdateImage();
            }
        });

        renameItem = new JMenuItem("Rename...");
        menu.add(renameItem);
        renameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                List<MSiImage> selectedImages = imageCacheList.getSelectedValuesList();
                if (selectedImages.size() > 1 || selectedImages.isEmpty()) {
                    System.out.println("Only works on single selections");
                    return;
                }
                MSI_IMAGE = selectedImages.get(0);
                String tmpName = MSI_IMAGE.getName();
                String newName = JOptionPane.showInputDialog(parent, "Enter a new name", MSI_IMAGE.getName());
                MSI_IMAGE.setName(newName);
                UILogger.Log("Updated " + tmpName + " to " + newName, UILogger.Level.INFO);
                UpdateImage();
            }
        });

        saveAnimationItem = new JMenuItem("Save Animation...");
        menu.add(saveAnimationItem);
        saveAnimationItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<MSiImage> selectedImages = imageCacheList.getSelectedValuesList();
                if (selectedImages.size() <= 1) {
                    return;
                }
                showSaveAnimationDialog(selectedImages);

            }
        });

        saveFrameItem = new JMenuItem("Save Frame(s)...");
        menu.add(saveFrameItem);
        saveFrameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<MSiImage> selectedImages = imageCacheList.getSelectedValuesList();
                showSaveFramesDialog(selectedImages);

            }
        });

        similarityItem = new JMenuItem("Check Similarities...");
        menu.add(similarityItem);
        similarityItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSimilaritiesDialog(imageCacheList.getSelectedValuesList());
            }
        });

        imageCacheList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (imageCacheList.getSelectedValuesList().size() >= 1 && (imageCacheList.getVisibleRowCount() > 0 & (e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)) {

                    renameItem.setEnabled(imageCacheList.getSelectedValuesList().size() >= 1);
                    deleteItem.setEnabled(imageCacheList.getSelectedValuesList().size() >= 1);
                    saveFrameItem.setEnabled(imageCacheList.getSelectedValuesList().size() >= 1);

                    similarityItem.setEnabled(imageCacheList.getSelectedValuesList().size() >= 2);
                    saveAnimationItem.setEnabled(imageCacheList.getSelectedValuesList().size() >= 2);

                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    /**
     * Shows dialog to save selected frames to an animation
     *
     * @param selectedImages The selected images
     */
    private void showSaveAnimationDialog(List<MSiImage> selectedImages) {
        final JFrame parent = this;
        JTextField timeBetweenFrames = new JTextField();
        JTextField outputFile = new JTextField();
        JButton saveAnimationLocationButton = new JButton("...");
        timeBetweenFrames.setText("" + 1000 / 60);

        saveAnimationLocationButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e
            ) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save animation...");
                fileChooser.setCurrentDirectory(lastDirectory);
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getAbsolutePath().toLowerCase().endsWith(".gif");
                    }

                    @Override
                    public String getDescription() {
                        return "Output gif animation";
                    }
                });
                int userSelection = fileChooser.showSaveDialog(parent);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    outputFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        }
        );

        final JComponent[] inputs = new JComponent[]{
            new JLabel("Time Between Frames (milliseconds)"),
            timeBetweenFrames,
            outputFile,
            new JLabel("Output File"),
            outputFile,
            saveAnimationLocationButton,};

        int result = JOptionPane.showConfirmDialog(this, inputs, "Save animation...", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            File fileToStore = new File(outputFile.getText());

            if (!fileToStore.getAbsolutePath().toLowerCase().endsWith(".gif")) {
                fileToStore = new File(fileToStore.getAbsolutePath() + ".gif");
            }

            if (fileToStore.exists()) {
                int response = JOptionPane.showConfirmDialog(this, "File already exists. Override?", "Saving...",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                int ms = Integer.parseInt(timeBetweenFrames.getText());

                BufferedImage[] images = new BufferedImage[selectedImages.size()];
                for (int i = 0; i < images.length; i++) {
                    selectedImages.get(i).CreateImage(currentMode, currentRange.getColors());
                    images[i] = selectedImages.get(i).getScaledImage(currentScale);
                }

                AnimationExporter.Save(images, fileToStore, ms, true);
                UILogger.Log("Exported animation to " + fileToStore.getAbsolutePath(), UILogger.Level.INFO);
                JOptionPane.showMessageDialog(this, "Exported animation to " + fileToStore.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not save this file : " + ex.getMessage(),
                        "Failed to export animation...",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Failed to export animation...", UILogger.Level.ERROR);
                return;
            }
        }

    }

    /**
     * Shows dialog to save selected frames as individual frames
     *
     * @param selectedImages The selected images
     */
    private void showSaveFramesDialog(List<MSiImage> selectedImages) {
        final JFrame parent = this;
        JTextField outputFile = new JTextField();
        JButton saveFramesLocationButton = new JButton("...");

        saveFramesLocationButton.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e
            ) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save frame(s)...");
                fileChooser.setCurrentDirectory(lastDirectory);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Output directory";
                    }
                });
                int userSelection = fileChooser.showSaveDialog(parent);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    outputFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        }
        );

        final JComponent[] inputs = new JComponent[]{
            outputFile,
            new JLabel("Output Folder"),
            outputFile,
            saveFramesLocationButton,};

        int result = JOptionPane.showConfirmDialog(this, inputs, "Save frames...", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            File fileToStore = new File(outputFile.getText());
            fileToStore.mkdirs();

            try {

                for (int i = 0; i < selectedImages.size(); i++) {
                    selectedImages.get(i).CreateImage(currentMode, currentRange.getColors());
                    File fileToSave = new File(fileToStore, selectedImages.get(i).getName() + ".png");
                    BufferedImage bImage = ImageUtils.SetImageTitle(selectedImages.get(i).getScaledImage(currentScale), selectedImages.get(i).getName());
                    ImageIO.write(bImage, "png", fileToSave);
                }
                UILogger.Log("Exported " + selectedImages.size() + " frames to " + fileToStore.getAbsolutePath(), UILogger.Level.INFO);
                JOptionPane.showMessageDialog(this, "Exported " + selectedImages.size() + " frames to " + fileToStore.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not save this file : " + ex.getMessage(),
                        "Failed to export frames...",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Failed to export frames...", UILogger.Level.ERROR);
                return;
            }
        }

    }

    /**
     * Shows a dialog to calculate similarities between images
     *
     * @param selectedImages The selected images
     */
    private void showSimilaritiesDialog(List<MSiImage> selectedImages) {
        if (selectedImages.size() > 1) {
            SimilaritySetup setup = new SimilaritySetup(this);
            setup.SetImages(selectedImages);
            setup.setVisible(true);
            setup.setLocationRelativeTo(this);
            this.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, "Please select at least 2 images", "Invalid selection...", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Adds a new image into the cache
     *
     * @param image the new image
     */
    public static void AddToCache(MSiImage image) {
        MSI_IMAGE = image;
        CACHE.Add(image);
        UpdateCacheUI();
    }

    /**
     * Updates the cache for the UI
     */
    private static void UpdateCacheUI() {
        if (imageCacheList != null) {
            DefaultListModel model = new DefaultListModel();
            for (MSiImage cachedImage : CACHE.getImageList()) {
                model.addElement(cachedImage);
            }
            imageCacheList.setModel(model);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu4 = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfInput = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        imageList = new javax.swing.JList<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        lbImage = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        logArea = new javax.swing.JList<>();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        btnLoad = new javax.swing.JMenuItem();
        btnSave = new javax.swing.JMenuItem();
        btnExit = new javax.swing.JMenuItem();
        menuExtract = new javax.swing.JMenu();
        btnGenerateImage = new javax.swing.JMenuItem();
        btnGenerateSequence = new javax.swing.JMenuItem();
        btnExtractMz = new javax.swing.JMenuItem();
        btnExtractRandomly = new javax.swing.JMenuItem();
        menuOptions = new javax.swing.JMenu();
        btnColor = new javax.swing.JMenu();
        btnBlueYellow = new javax.swing.JMenuItem();
        btnGreenRed = new javax.swing.JMenuItem();
        btnGreenPink = new javax.swing.JMenuItem();
        btnRedBlue = new javax.swing.JMenuItem();
        btnGrayScale = new javax.swing.JMenuItem();
        btnScale = new javax.swing.JMenu();
        btnX1 = new javax.swing.JCheckBoxMenuItem();
        btnX2 = new javax.swing.JCheckBoxMenuItem();
        btnX4 = new javax.swing.JCheckBoxMenuItem();
        btnX8 = new javax.swing.JCheckBoxMenuItem();
        btnX16 = new javax.swing.JCheckBoxMenuItem();
        btnIntensityMode = new javax.swing.JMenu();
        btnMean = new javax.swing.JCheckBoxMenuItem();
        btnMedian = new javax.swing.JCheckBoxMenuItem();
        btnMin = new javax.swing.JCheckBoxMenuItem();
        btnMax = new javax.swing.JCheckBoxMenuItem();
        btnPercentile = new javax.swing.JMenu();
        btnQ1 = new javax.swing.JCheckBoxMenuItem();
        btnQ2 = new javax.swing.JCheckBoxMenuItem();
        btn90th = new javax.swing.JCheckBoxMenuItem();
        btn95th = new javax.swing.JCheckBoxMenuItem();
        btn99th = new javax.swing.JCheckBoxMenuItem();
        btnAdducts = new javax.swing.JMenu();
        btnCations = new javax.swing.JMenu();
        BtnAllCations = new javax.swing.JCheckBoxMenuItem();
        btnCatCH3OH = new javax.swing.JCheckBoxMenuItem();
        btnCatCH3CNH = new javax.swing.JCheckBoxMenuItem();
        btnCatDMSO = new javax.swing.JCheckBoxMenuItem();
        btnCatK = new javax.swing.JCheckBoxMenuItem();
        btnCatNa = new javax.swing.JCheckBoxMenuItem();
        btnCatNH4 = new javax.swing.JCheckBoxMenuItem();
        btnAnions = new javax.swing.JMenu();
        btnAnionsAll = new javax.swing.JCheckBoxMenuItem();
        btnAnionCl = new javax.swing.JCheckBoxMenuItem();
        btnAnionHCOO = new javax.swing.JCheckBoxMenuItem();
        btnAnionK = new javax.swing.JCheckBoxMenuItem();
        btnAnionNa = new javax.swing.JCheckBoxMenuItem();
        btnAnionOAc = new javax.swing.JCheckBoxMenuItem();
        btnAnionTFA = new javax.swing.JCheckBoxMenuItem();
        btnHelp = new javax.swing.JMenu();
        btnAbout = new javax.swing.JMenuItem();
        btnHelpText = new javax.swing.JMenuItem();

        jMenu4.setText("jMenu4");

        jMenu1.setText("jMenu1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(650, 360));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Input");

        tfInput.setEditable(false);
        tfInput.setBackground(new java.awt.Color(255, 255, 255));
        tfInput.setText("-");

        jScrollPane3.setBackground(new java.awt.Color(204, 204, 204));

        jScrollPane2.setBackground(new java.awt.Color(204, 204, 204));

        imageList.setBackground(new java.awt.Color(204, 204, 204));
        jScrollPane2.setViewportView(imageList);

        jScrollPane3.setViewportView(jScrollPane2);

        jScrollPane4.setBackground(new java.awt.Color(204, 204, 204));

        jScrollPane1.setBackground(new java.awt.Color(204, 204, 204));

        lbImage.setBackground(new java.awt.Color(204, 204, 204));
        lbImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbImage.setText("<image goes here>");
        jScrollPane1.setViewportView(lbImage);

        jScrollPane4.setViewportView(jScrollPane1);

        jScrollPane5.setBackground(new java.awt.Color(204, 204, 204));

        logArea.setBackground(new java.awt.Color(204, 204, 204));
        jScrollPane5.setViewportView(logArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfInput))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE))
                    .addComponent(jScrollPane5))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addContainerGap())
        );

        menuFile.setText("File");

        btnLoad.setText("Load...");
        btnLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadActionPerformed(evt);
            }
        });
        menuFile.add(btnLoad);

        btnSave.setText("Save...");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        menuFile.add(btnSave);

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        menuFile.add(btnExit);

        jMenuBar1.add(menuFile);

        menuExtract.setText("Extract");

        btnGenerateImage.setText("Generate Image");
        btnGenerateImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateImageActionPerformed(evt);
            }
        });
        menuExtract.add(btnGenerateImage);

        btnGenerateSequence.setText("Generate Sequence");
        btnGenerateSequence.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateSequenceActionPerformed(evt);
            }
        });
        menuExtract.add(btnGenerateSequence);

        btnExtractMz.setText("Extract MZ-values");
        btnExtractMz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExtractMzActionPerformed(evt);
            }
        });
        menuExtract.add(btnExtractMz);

        btnExtractRandomly.setText("Extract Randomly");
        btnExtractRandomly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExtractRandomlyActionPerformed(evt);
            }
        });
        menuExtract.add(btnExtractRandomly);

        jMenuBar1.add(menuExtract);

        menuOptions.setText("Options");

        btnColor.setText("Color");

        btnBlueYellow.setText("Blue-Yellow");
        btnBlueYellow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBlueYellowActionPerformed(evt);
            }
        });
        btnColor.add(btnBlueYellow);

        btnGreenRed.setText("Green-Red");
        btnGreenRed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGreenRedActionPerformed(evt);
            }
        });
        btnColor.add(btnGreenRed);

        btnGreenPink.setText("Green-Pink");
        btnGreenPink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGreenPinkActionPerformed(evt);
            }
        });
        btnColor.add(btnGreenPink);

        btnRedBlue.setText("Red-Blue");
        btnRedBlue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRedBlueActionPerformed(evt);
            }
        });
        btnColor.add(btnRedBlue);

        btnGrayScale.setText("Gray Scale");
        btnGrayScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrayScaleActionPerformed(evt);
            }
        });
        btnColor.add(btnGrayScale);

        menuOptions.add(btnColor);

        btnScale.setText("Scale");

        btnX1.setSelected(true);
        btnX1.setText("x1");
        btnX1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnX1ActionPerformed(evt);
            }
        });
        btnScale.add(btnX1);

        btnX2.setSelected(true);
        btnX2.setText("x2");
        btnX2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnX2ActionPerformed(evt);
            }
        });
        btnScale.add(btnX2);

        btnX4.setSelected(true);
        btnX4.setText("x4");
        btnX4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnX4ActionPerformed(evt);
            }
        });
        btnScale.add(btnX4);

        btnX8.setSelected(true);
        btnX8.setText("x8");
        btnX8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnX8ActionPerformed(evt);
            }
        });
        btnScale.add(btnX8);

        btnX16.setSelected(true);
        btnX16.setText("x16");
        btnX16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnX16ActionPerformed(evt);
            }
        });
        btnScale.add(btnX16);

        menuOptions.add(btnScale);

        btnIntensityMode.setText("Intensity Mode");

        btnMean.setSelected(true);
        btnMean.setText("Mean");
        btnMean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMeanActionPerformed(evt);
            }
        });
        btnIntensityMode.add(btnMean);

        btnMedian.setSelected(true);
        btnMedian.setText("Median");
        btnMedian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMedianActionPerformed(evt);
            }
        });
        btnIntensityMode.add(btnMedian);

        btnMin.setSelected(true);
        btnMin.setText("Min");
        btnMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMinActionPerformed(evt);
            }
        });
        btnIntensityMode.add(btnMin);

        btnMax.setSelected(true);
        btnMax.setText("Max");
        btnMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMaxActionPerformed(evt);
            }
        });
        btnIntensityMode.add(btnMax);

        btnPercentile.setText("Percentile");

        btnQ1.setSelected(true);
        btnQ1.setText("Q1");
        btnQ1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQ1ActionPerformed(evt);
            }
        });
        btnPercentile.add(btnQ1);

        btnQ2.setSelected(true);
        btnQ2.setText("Q3");
        btnQ2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQ2ActionPerformed(evt);
            }
        });
        btnPercentile.add(btnQ2);

        btn90th.setSelected(true);
        btn90th.setText("90th percentile");
        btn90th.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn90thActionPerformed(evt);
            }
        });
        btnPercentile.add(btn90th);

        btn95th.setSelected(true);
        btn95th.setText("95th percentile");
        btn95th.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn95thActionPerformed(evt);
            }
        });
        btnPercentile.add(btn95th);

        btn99th.setSelected(true);
        btn99th.setText("99th percentile");
        btn99th.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn99thActionPerformed(evt);
            }
        });
        btnPercentile.add(btn99th);

        btnIntensityMode.add(btnPercentile);

        menuOptions.add(btnIntensityMode);

        btnAdducts.setText("Adducts");

        btnCations.setText("Cations");

        BtnAllCations.setSelected(true);
        BtnAllCations.setText("All");
        BtnAllCations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllCationsActionPerformed(evt);
            }
        });
        btnCations.add(BtnAllCations);

        btnCatCH3OH.setSelected(true);
        btnCatCH3OH.setText("CH3OH");
        btnCatCH3OH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCatCH3OHActionPerformed(evt);
            }
        });
        btnCations.add(btnCatCH3OH);

        btnCatCH3CNH.setSelected(true);
        btnCatCH3CNH.setText("CH3CNH");
        btnCatCH3CNH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCatCH3CNHActionPerformed(evt);
            }
        });
        btnCations.add(btnCatCH3CNH);

        btnCatDMSO.setSelected(true);
        btnCatDMSO.setText("DMSO");
        btnCatDMSO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCatDMSOActionPerformed(evt);
            }
        });
        btnCations.add(btnCatDMSO);

        btnCatK.setSelected(true);
        btnCatK.setText("K");
        btnCatK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCatKActionPerformed(evt);
            }
        });
        btnCations.add(btnCatK);

        btnCatNa.setSelected(true);
        btnCatNa.setText("Na");
        btnCatNa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCatNaActionPerformed(evt);
            }
        });
        btnCations.add(btnCatNa);

        btnCatNH4.setSelected(true);
        btnCatNH4.setText("NH4");
        btnCatNH4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCatNH4ActionPerformed(evt);
            }
        });
        btnCations.add(btnCatNH4);

        btnAdducts.add(btnCations);

        btnAnions.setText("Anions");

        btnAnionsAll.setSelected(true);
        btnAnionsAll.setText("All");
        btnAnionsAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnionsAllActionPerformed(evt);
            }
        });
        btnAnions.add(btnAnionsAll);

        btnAnionCl.setSelected(true);
        btnAnionCl.setText("Cl");
        btnAnionCl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnionClActionPerformed(evt);
            }
        });
        btnAnions.add(btnAnionCl);

        btnAnionHCOO.setSelected(true);
        btnAnionHCOO.setText("HCOO");
        btnAnionHCOO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnionHCOOActionPerformed(evt);
            }
        });
        btnAnions.add(btnAnionHCOO);

        btnAnionK.setSelected(true);
        btnAnionK.setText("K");
        btnAnionK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnionKActionPerformed(evt);
            }
        });
        btnAnions.add(btnAnionK);

        btnAnionNa.setSelected(true);
        btnAnionNa.setText("Na");
        btnAnionNa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnionNaActionPerformed(evt);
            }
        });
        btnAnions.add(btnAnionNa);

        btnAnionOAc.setSelected(true);
        btnAnionOAc.setText("OAc");
        btnAnionOAc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnionOAcActionPerformed(evt);
            }
        });
        btnAnions.add(btnAnionOAc);

        btnAnionTFA.setSelected(true);
        btnAnionTFA.setText("TFA");
        btnAnionTFA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnionTFAActionPerformed(evt);
            }
        });
        btnAnions.add(btnAnionTFA);

        btnAdducts.add(btnAnions);

        menuOptions.add(btnAdducts);

        jMenuBar1.add(menuOptions);

        btnHelp.setText("Help");

        btnAbout.setText("About");
        btnAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAboutActionPerformed(evt);
            }
        });
        btnHelp.add(btnAbout);

        btnHelpText.setText("Help");
        btnHelpText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHelpTextActionPerformed(evt);
            }
        });
        btnHelp.add(btnHelpText);

        jMenuBar1.add(btnHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(lastDirectory);

        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getAbsolutePath().toLowerCase().endsWith(".imzml");
            }

            @Override
            public String getDescription() {
                return "imzml image files";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            tfInput.setText(selectedFile.getAbsolutePath());
            lastDirectory = selectedFile.getParentFile();
        }
    }//GEN-LAST:event_btnLoadActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        Save();
    }//GEN-LAST:event_btnSaveActionPerformed

    /**
     * Save the images to the specified directory
     */
    public void Save() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        fileChooser.setCurrentDirectory(lastDirectory);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getAbsolutePath().toLowerCase().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "PNG image files";
            }
        });
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }

            if (fileToSave.exists()) {
                int result = JOptionPane.showConfirmDialog(this, "File already exists. Override?", "Saving...",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            try {
                ImageIO.write(CURRENT_IMAGE, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Exported image to " + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not save this file : " + ex.getMessage(),
                        "Failed to export image...",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Failed to export image...", UILogger.Level.INFO);
                return;
            }

        }
    }

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Bye bye ?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnGenerateImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateImageActionPerformed

        JTextField minMZ = new JTextField();
        JTextField maxMz = new JTextField();

        minMZ.setText("953");
        maxMz.setText("954");

        final JComponent[] inputs = new JComponent[]{
            new JLabel("Minimal MZ"),
            minMZ,
            new JLabel("Maximal Mz"),
            maxMz,};
        int result = JOptionPane.showConfirmDialog(this, inputs, "Generate Image...", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {

            UILogger.Log("Starting processing : ", UILogger.Level.INFO);
            UILogger.Log("- Scale : " + getCurrentScale(), UILogger.Level.NONE);
            UILogger.Log("- Mode  :" + getCurrentMode(), UILogger.Level.NONE);
            UILogger.Log("- Color :" + getCurrentRange(), UILogger.Level.NONE);

            ImageExtractionTask task = new ImageExtractionTask(
                    this,
                    progressFrame,
                    tfInput,
                    minMZ,
                    maxMz,
                    null,
                    lbImage,
                    currentScale,
                    currentMode,
                    currentRange,
                    false);
            new WorkingThread(this, task).execute();
        }


    }//GEN-LAST:event_btnGenerateImageActionPerformed

    private void btnGenerateSequenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateSequenceActionPerformed
        JTextField minMZ = new JTextField();
        JTextField maxMz = new JTextField();
        JTextField steps = new JTextField();

        minMZ.setText("953");
        maxMz.setText("954");
        steps.setText("5");

        final JComponent[] inputs = new JComponent[]{
            new JLabel("Minimal MZ"),
            minMZ,
            new JLabel("Maximal Mz"),
            maxMz,
            new JLabel("Steps"),
            steps,};

        int result = JOptionPane.showConfirmDialog(this, inputs, "Generate Image...", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {

            ImageExtractionTask task = new ImageExtractionTask(
                    this,
                    progressFrame,
                    tfInput,
                    minMZ,
                    maxMz,
                    steps,
                    lbImage,
                    currentScale,
                    currentMode,
                    currentRange,
                    true);
            new WorkingThread(this, task).execute();
        }
    }//GEN-LAST:event_btnGenerateSequenceActionPerformed

    private void btnGrayScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrayScaleActionPerformed
        currentRange = ColorRange.GRAY_SCALE;
        UILogger.Log("Setting color to Gray Scale", UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnGrayScaleActionPerformed

    private void btnBlueYellowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBlueYellowActionPerformed
        currentRange = ColorRange.BLUE_YELLOW;
        UILogger.Log("Setting color to Blue-Yellow", UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnBlueYellowActionPerformed

    private void btnX1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnX1ActionPerformed
        btnX1.setSelected(true);
        btnX2.setSelected(false);
        btnX4.setSelected(false);
        btnX8.setSelected(false);
        btnX16.setSelected(false);
        currentScale = 1;
        UILogger.Log("Set pixel scale to " + currentScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX1ActionPerformed

    private void btnX2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnX2ActionPerformed
        btnX1.setSelected(false);
        btnX2.setSelected(true);
        btnX4.setSelected(false);
        btnX8.setSelected(false);
        btnX16.setSelected(false);
        currentScale = 2;
        UILogger.Log("Set pixel scale to " + currentScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX2ActionPerformed

    private void btnX4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnX4ActionPerformed
        btnX1.setSelected(false);
        btnX2.setSelected(false);
        btnX4.setSelected(true);
        btnX8.setSelected(false);
        btnX16.setSelected(false);
        currentScale = 4;
        UILogger.Log("Set pixel scale to " + currentScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX4ActionPerformed

    private void btnX8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnX8ActionPerformed
        btnX1.setSelected(false);
        btnX2.setSelected(false);
        btnX4.setSelected(false);
        btnX8.setSelected(true);
        btnX16.setSelected(false);
        currentScale = 8;
        UILogger.Log("Set pixel scale to " + currentScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX8ActionPerformed

    private void btnX16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnX16ActionPerformed
        btnX1.setSelected(false);
        btnX2.setSelected(false);
        btnX4.setSelected(false);
        btnX8.setSelected(false);
        btnX16.setSelected(true);
        currentScale = 16;
        UILogger.Log("Set pixel scale to " + currentScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX16ActionPerformed

    private void btnMeanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMeanActionPerformed
        btnMean.setSelected(true);
        btnMedian.setSelected(false);
        btnMin.setSelected(false);
        btnMax.setSelected(false);
        btnQ1.setSelected(false);
        btnQ2.setSelected(false);
        btn90th.setSelected(false);
        btn95th.setSelected(false);
        btn99th.setSelected(false);
        currentMode = MSiImage.ImageMode.MEAN;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnMeanActionPerformed

    private void btnMedianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMedianActionPerformed
        btnMean.setSelected(false);
        btnMedian.setSelected(true);
        btnMin.setSelected(false);
        btnMax.setSelected(false);
        btnQ1.setSelected(false);
        btnQ2.setSelected(false);
        btn90th.setSelected(false);
        btn95th.setSelected(false);
        btn99th.setSelected(false);
        currentMode = MSiImage.ImageMode.MEDIAN;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnMedianActionPerformed

    private void btnMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMinActionPerformed
        btnMean.setSelected(false);
        btnMedian.setSelected(false);
        btnMin.setSelected(true);
        btnMax.setSelected(false);
        btnQ1.setSelected(false);
        btnQ2.setSelected(false);
        btn90th.setSelected(false);
        btn95th.setSelected(false);
        btn99th.setSelected(false);
        currentMode = MSiImage.ImageMode.MIN;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnMinActionPerformed

    private void btnMaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMaxActionPerformed
        btnMean.setSelected(false);
        btnMedian.setSelected(false);
        btnMin.setSelected(false);
        btnMax.setSelected(true);
        btnQ1.setSelected(false);
        btnQ2.setSelected(false);
        btn90th.setSelected(false);
        btn95th.setSelected(false);
        btn99th.setSelected(false);
        currentMode = MSiImage.ImageMode.MAX;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnMaxActionPerformed

    private void btnQ1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQ1ActionPerformed
        btnMean.setSelected(false);
        btnMedian.setSelected(false);
        btnMin.setSelected(false);
        btnMax.setSelected(false);
        btnQ1.setSelected(true);
        btnQ2.setSelected(false);
        btn90th.setSelected(false);
        btn95th.setSelected(false);
        btn99th.setSelected(false);
        currentMode = MSiImage.ImageMode.Q1;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnQ1ActionPerformed

    private void btnQ2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQ2ActionPerformed
        btnMean.setSelected(false);
        btnMedian.setSelected(false);
        btnMin.setSelected(false);
        btnMax.setSelected(false);
        btnQ1.setSelected(false);
        btnQ2.setSelected(true);
        btn90th.setSelected(false);
        btn95th.setSelected(false);
        btn99th.setSelected(false);
        currentMode = MSiImage.ImageMode.Q3;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnQ2ActionPerformed

    private void btn90thActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn90thActionPerformed
        btnMean.setSelected(false);
        btnMedian.setSelected(false);
        btnMin.setSelected(false);
        btnMax.setSelected(false);
        btnQ1.setSelected(false);
        btnQ2.setSelected(false);
        btn90th.setSelected(true);
        btn95th.setSelected(false);
        btn99th.setSelected(false);
        currentMode = MSiImage.ImageMode.Q90;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btn90thActionPerformed

    private void btn95thActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn95thActionPerformed
        btnMean.setSelected(false);
        btnMedian.setSelected(false);
        btnMin.setSelected(false);
        btnMax.setSelected(false);
        btnQ1.setSelected(false);
        btnQ2.setSelected(false);
        btn90th.setSelected(false);
        btn95th.setSelected(true);
        btn99th.setSelected(false);
        currentMode = MSiImage.ImageMode.Q95;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btn95thActionPerformed

    private void btn99thActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn99thActionPerformed
        btnMean.setSelected(false);
        btnMedian.setSelected(false);
        btnMin.setSelected(false);
        btnMax.setSelected(false);
        btnQ1.setSelected(false);
        btnQ2.setSelected(false);
        btn90th.setSelected(false);
        btn95th.setSelected(false);
        btn99th.setSelected(true);
        currentMode = MSiImage.ImageMode.Q99;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btn99thActionPerformed

    private void btnGreenRedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGreenRedActionPerformed
        UILogger.Log("Setting color to Green-Red", UILogger.Level.INFO);
        currentRange = ColorRange.GREEN_RED;
        UpdateImage();
    }//GEN-LAST:event_btnGreenRedActionPerformed

    private void btnGreenPinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGreenPinkActionPerformed
        UILogger.Log("Setting color to Green-Pink", UILogger.Level.INFO);
        currentRange = ColorRange.GREEN_PINK;
        UpdateImage();
    }//GEN-LAST:event_btnGreenPinkActionPerformed

    private void btnRedBlueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRedBlueActionPerformed
        UILogger.Log("Setting color to Red-Blue", UILogger.Level.INFO);
        currentRange = ColorRange.RED_BLUE;
        UpdateImage();
    }//GEN-LAST:event_btnRedBlueActionPerformed

    private void btnExtractMzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExtractMzActionPerformed

        final JFrame parent = this;

        JTextArea textArea = new JTextArea();
        textArea.setColumns(30);
        textArea.setRows(15);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);

        JScrollPane panel = new JScrollPane(textArea);
        panel.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);

        JLabel label = new JLabel("Minimal Deviation (%)");
        JTextField mzTolerance = new JTextField(".05");

        Object[] content = new Object[]{
            label, mzTolerance, panel
        };

        int result = JOptionPane.showConfirmDialog(parent, content, "Masses to scan (one per line)", JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            double toleranceValue;
            try {
                toleranceValue = Double.parseDouble(mzTolerance.getText());
                if (toleranceValue <= 0) {
                    throw new NullPointerException();
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        mzTolerance.getText() + " is an invalid entry. The tolerance is a real value > 0",
                        "Failed to calculate similarities...",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Failed to calculate similarities : invalid tolerance provided", UILogger.Level.ERROR);
                return;
            }

            String[] lines = textArea.getText().split("\\n");
            List<Float> mzValues = new ArrayList<>();
            for (String line : lines) {
                try {
                    float mz = Float.parseFloat(line.replace(",", "."));
                    if (mz <= 0) {
                        throw new NullPointerException();
                    }
                    mzValues.add(mz);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            line + " is an invalid entry. The mz values a real value > 0",
                            "Failed to calculate similarities...",
                            JOptionPane.ERROR_MESSAGE);
                    UILogger.Log("Failed to calculate similarities : invalid mz values provided", UILogger.Level.ERROR);
                    return;
                }
            }

            for (float mz : mzValues) {

                ImageExtractionTask task = new ImageExtractionTask(
                        this,
                        progressFrame,
                        tfInput,
                        mz - (float) toleranceValue,
                        mz + (float) toleranceValue,
                        1,
                        lbImage,
                        currentScale,
                        currentMode,
                        currentRange,
                        false);
                task.setNotifyWhenRead(false);
                new WorkingThread(this, task.mute()).execute();

            }

            JOptionPane.showMessageDialog(parent, "Finished extracting mz values");
        }
    }//GEN-LAST:event_btnExtractMzActionPerformed

    private void btnExtractRandomlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExtractRandomlyActionPerformed

        JTextField samples = new JTextField();

        JTextField mzTolerance = new JTextField(".05");

        JTextField lowerRangeMZ = new JTextField();
        JTextField upperRangeMZ = new JTextField();

        samples.setText("10");
        lowerRangeMZ.setText("900");
        upperRangeMZ.setText("1200");

        final JComponent[] inputs = new JComponent[]{
            new JLabel("#Samples"),
            samples,
            new JLabel("Minimal Deviation (%)"),
            mzTolerance,
            new JLabel("Minimal MZ"),
            lowerRangeMZ,
            new JLabel("Maximal Mz"),
            upperRangeMZ,};
        int result = JOptionPane.showConfirmDialog(this, inputs, "Generate Random Images...", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            int sampleCount;
            try {
                sampleCount = Integer.parseInt(samples.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        samples.getText() + " is an invalid entry. Samples should be an integer value > 0",
                        "Failed to calculate similarities...",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Failed to calculate similarities : invalid samplecount provided", UILogger.Level.ERROR);
                return;
            }

            float toleranceValue;
            try {
                toleranceValue = Float.parseFloat(mzTolerance.getText());
                if (toleranceValue <= 0) {
                    throw new NullPointerException();
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        mzTolerance.getText() + " is an invalid entry. The tolerance is a real value > 0",
                        "Failed to calculate similarities...",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Failed to calculate similarities : invalid tolerance provided", UILogger.Level.ERROR);
                return;
            }

            float lowerMzBoundary;
            try {
                lowerMzBoundary = Float.parseFloat(lowerRangeMZ.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        lowerRangeMZ.getText() + " is an invalid entry. Samples should be a real value > 0",
                        "Failed to calculate similarities...",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Failed to calculate similarities : invalid sample size provided", UILogger.Level.ERROR);
                return;
            }

            float upperMzBoundary;
            try {
                upperMzBoundary = Float.parseFloat(upperRangeMZ.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        upperRangeMZ.getText() + " is an invalid entry. Samples should be a real value > 0",
                        "Failed to calculate similarities...",
                        JOptionPane.ERROR_MESSAGE);
                UILogger.Log("Failed to calculate similarities : invalid tolerance provided", UILogger.Level.ERROR);
                return;
            }
            Random rnd = new Random();
            for (int i = 0; i < sampleCount; i++) {

                float sampledValue = lowerMzBoundary + (Math.abs(upperMzBoundary - lowerMzBoundary) * rnd.nextFloat());

                ImageExtractionTask task = new ImageExtractionTask(
                        this,
                        progressFrame,
                        tfInput,
                        sampledValue - toleranceValue,
                        sampledValue + toleranceValue,
                        1,
                        lbImage,
                        currentScale,
                        currentMode,
                        currentRange,
                        false);
                task.setNotifyWhenRead(false);
                new WorkingThread(this, task).execute();
            }
        }
    }//GEN-LAST:event_btnExtractRandomlyActionPerformed

    private void btnCatCH3OHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatCH3OHActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.CH3OH_CATION, btnCatCH3OH);
    }//GEN-LAST:event_btnCatCH3OHActionPerformed

    private void btnCatCH3CNHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatCH3CNHActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.CH3CNH_CATION, btnCatCH3CNH);
    }//GEN-LAST:event_btnCatCH3CNHActionPerformed

    private void btnCatDMSOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatDMSOActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.DMSO_CATION, btnCatDMSO);
    }//GEN-LAST:event_btnCatDMSOActionPerformed

    private void btnCatKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatKActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.K_CATION, btnCatK);
    }//GEN-LAST:event_btnCatKActionPerformed

    private void btnCatNaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatNaActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.Na_CATION, btnCatNa);
    }//GEN-LAST:event_btnCatNaActionPerformed

    private void btnCatNH4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCatNH4ActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.NH4_CATION, btnCatNH4);
    }//GEN-LAST:event_btnCatNH4ActionPerformed

    private void btnAnionClActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnionClActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.Cl_ANION, btnAnionCl);
    }//GEN-LAST:event_btnAnionClActionPerformed

    private void btnAnionHCOOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnionHCOOActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.HCOO_ANION, btnAnionHCOO);
    }//GEN-LAST:event_btnAnionHCOOActionPerformed

    private void btnAnionKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnionKActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.K_ANION, btnAnionK);
    }//GEN-LAST:event_btnAnionKActionPerformed

    private void btnAnionNaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnionNaActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.Na_ANION, btnAnionNa);
    }//GEN-LAST:event_btnAnionNaActionPerformed

    private void btnAnionOAcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnionOAcActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.OAc_ANION, btnAnionOAc);
    }//GEN-LAST:event_btnAnionOAcActionPerformed

    private void btnAnionTFAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnionTFAActionPerformed
        ToggleAdductEnabled(MSScanAdduct.ADDUCTS.TFA_ANION, btnAnionTFA);
    }//GEN-LAST:event_btnAnionTFAActionPerformed

    private void BtnAllCationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllCationsActionPerformed
        if (BtnAllCations.isSelected()) {
            BtnAllCations.setText("None");
            InitCationAdducts(true);
            MSScanAdduct.ENABLED_ADDUCTS.removeAll(MSScanAdduct.ADDUCTS.GetCations());
            MSScanAdduct.ENABLED_ADDUCTS.addAll(MSScanAdduct.ADDUCTS.GetCations());
        } else {
            BtnAllCations.setText("All");
            MSScanAdduct.ENABLED_ADDUCTS.removeAll(MSScanAdduct.ADDUCTS.GetCations());
            InitCationAdducts(false);
        }
    }//GEN-LAST:event_BtnAllCationsActionPerformed

    private void btnAnionsAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnionsAllActionPerformed
        if (btnAnionsAll.isSelected()) {
            btnAnionsAll.setText("None");
            InitAnionAdducts(true);
            MSScanAdduct.ENABLED_ADDUCTS.removeAll(MSScanAdduct.ADDUCTS.GetAnions());
            MSScanAdduct.ENABLED_ADDUCTS.addAll(MSScanAdduct.ADDUCTS.GetAnions());
        } else {
            btnAnionsAll.setText("All");
            MSScanAdduct.ENABLED_ADDUCTS.removeAll(MSScanAdduct.ADDUCTS.GetAnions());
            InitAnionAdducts(false);
        }
    }//GEN-LAST:event_btnAnionsAllActionPerformed

    private void btnAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAboutActionPerformed
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://www.proteoformix.com/"));
            } catch (URISyntaxException | IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not open the webpage. Are you online?",
                        "Could not reach website...",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnAboutActionPerformed

    private void btnHelpTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHelpTextActionPerformed

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/JavaSaurusStudios/ProteoFormiX_HistoSnap/tree/master"));
            } catch (URISyntaxException | IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not open the webpage. Are you online?",
                        "Could not reach website...",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_btnHelpTextActionPerformed

    private void ToggleAdductEnabled(MSScanAdduct.ADDUCTS adduct, JCheckBoxMenuItem button) {
        if (MSScanAdduct.ENABLED_ADDUCTS.contains(adduct)) {
            MSScanAdduct.ENABLED_ADDUCTS.remove(adduct);
        } else {
            MSScanAdduct.ENABLED_ADDUCTS.add(adduct);
        }
        button.setSelected(MSScanAdduct.ENABLED_ADDUCTS.contains(adduct));
    }

    /**
     * Updates the image on screen
     */
    private void UpdateImage() {
        ImageIcon icon = null;
        if (!CACHE.getImageList().isEmpty()) {
            MSImagizer.MSI_IMAGE.CreateImage(currentMode, currentRange.getColors());
            MSImagizer.CURRENT_IMAGE = MSImagizer.MSI_IMAGE.getScaledImage(currentScale);
            icon = new ImageIcon(MSImagizer.CURRENT_IMAGE);
        }
        lbImage.setIcon(icon);
        lbImage.setText("");
    }

    public int getCurrentScale() {
        return currentScale;
    }

    public ColorRange getCurrentRange() {
        return currentRange;
    }

    public MSiImage.ImageMode getCurrentMode() {
        return currentMode;
    }

    public ProgressBarFrame getProgressFrame() {
        return progressFrame;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem BtnAllCations;
    private javax.swing.JCheckBoxMenuItem btn90th;
    private javax.swing.JCheckBoxMenuItem btn95th;
    private javax.swing.JCheckBoxMenuItem btn99th;
    private javax.swing.JMenuItem btnAbout;
    private javax.swing.JMenu btnAdducts;
    private javax.swing.JCheckBoxMenuItem btnAnionCl;
    private javax.swing.JCheckBoxMenuItem btnAnionHCOO;
    private javax.swing.JCheckBoxMenuItem btnAnionK;
    private javax.swing.JCheckBoxMenuItem btnAnionNa;
    private javax.swing.JCheckBoxMenuItem btnAnionOAc;
    private javax.swing.JCheckBoxMenuItem btnAnionTFA;
    private javax.swing.JMenu btnAnions;
    private javax.swing.JCheckBoxMenuItem btnAnionsAll;
    private javax.swing.JMenuItem btnBlueYellow;
    private javax.swing.JCheckBoxMenuItem btnCatCH3CNH;
    private javax.swing.JCheckBoxMenuItem btnCatCH3OH;
    private javax.swing.JCheckBoxMenuItem btnCatDMSO;
    private javax.swing.JCheckBoxMenuItem btnCatK;
    private javax.swing.JCheckBoxMenuItem btnCatNH4;
    private javax.swing.JCheckBoxMenuItem btnCatNa;
    private javax.swing.JMenu btnCations;
    private javax.swing.JMenu btnColor;
    private javax.swing.JMenuItem btnExit;
    private javax.swing.JMenuItem btnExtractMz;
    private javax.swing.JMenuItem btnExtractRandomly;
    private javax.swing.JMenuItem btnGenerateImage;
    private javax.swing.JMenuItem btnGenerateSequence;
    private javax.swing.JMenuItem btnGrayScale;
    private javax.swing.JMenuItem btnGreenPink;
    private javax.swing.JMenuItem btnGreenRed;
    private javax.swing.JMenu btnHelp;
    private javax.swing.JMenuItem btnHelpText;
    private javax.swing.JMenu btnIntensityMode;
    private javax.swing.JMenuItem btnLoad;
    private javax.swing.JCheckBoxMenuItem btnMax;
    private javax.swing.JCheckBoxMenuItem btnMean;
    private javax.swing.JCheckBoxMenuItem btnMedian;
    private javax.swing.JCheckBoxMenuItem btnMin;
    private javax.swing.JMenu btnPercentile;
    private javax.swing.JCheckBoxMenuItem btnQ1;
    private javax.swing.JCheckBoxMenuItem btnQ2;
    private javax.swing.JMenuItem btnRedBlue;
    private javax.swing.JMenuItem btnSave;
    private javax.swing.JMenu btnScale;
    private javax.swing.JCheckBoxMenuItem btnX1;
    private javax.swing.JCheckBoxMenuItem btnX16;
    private javax.swing.JCheckBoxMenuItem btnX2;
    private javax.swing.JCheckBoxMenuItem btnX4;
    private javax.swing.JCheckBoxMenuItem btnX8;
    private javax.swing.JList<String> imageList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel lbImage;
    private javax.swing.JList<String> logArea;
    private javax.swing.JMenu menuExtract;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuOptions;
    private javax.swing.JTextField tfInput;
    // End of variables declaration//GEN-END:variables

}
