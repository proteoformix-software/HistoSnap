package be.javasaurusstudios.histosnap.view;

import be.javasaurusstudios.histosnap.view.component.ProgressBarFrame;
import be.javasaurusstudios.histosnap.control.tasks.WorkingThread;
import be.javasaurusstudios.histosnap.control.MSiImageCache;
import be.javasaurusstudios.histosnap.control.tasks.imaging.ImageExtractionTask;
import be.javasaurusstudios.histosnap.control.tasks.housekeeping.SessionLoadingTask;
import be.javasaurusstudios.histosnap.control.tasks.housekeeping.SessionSavingTask;
import be.javasaurusstudios.histosnap.model.MSScanAdduct;
import be.javasaurusstudios.histosnap.model.image.MSiImage;
import be.javasaurusstudios.histosnap.control.util.color.ColorRange;
import be.javasaurusstudios.histosnap.control.util.UILogger;
import be.javasaurusstudios.histosnap.model.task.WorkingTask;
import be.javasaurusstudios.histosnap.view.handlers.DHBBackgroundExtractionHandler;
import be.javasaurusstudios.histosnap.view.handlers.RandomBackgroundExtractor;
import be.javasaurusstudios.histosnap.view.listeners.imagelist.impl.ImageHighlightProvider;
import be.javasaurusstudios.histosnap.view.listeners.imagelist.impl.ListActionPopupProvider;
import be.javasaurusstudios.histosnap.view.listeners.imagelist.impl.ListSavePopupProvider;
import be.javasaurusstudios.histosnap.view.listeners.imagelist.impl.ListSelectionUpdateProvider;
import be.javasaurusstudios.histosnap.view.listeners.mouse.ScaleScrollListener;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 * The main UI frame for the imagizer
 *
 * @author Dr. Kenneth Verheggen <kenneth.verheggen@proteoformix.com>
 */
public class MSImagizer extends javax.swing.JFrame {

    //The singleton instance
    public static MSImagizer instance;
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
    public static JList imageCacheList;
    //The current pixel scale to export
    private int exportScale = 4;
    //The actual current pixel scale
    private int currentScale;
    //The current range of colors that will be applied
    private ColorRange currentRange = ColorRange.BLUE_YELLOW;
    //The current mode of reference for intensities
    private MSiImage.ImageMode currentMode = MSiImage.ImageMode.MEAN;
    //Boolean indicating if this machine has enough memory or not
    private boolean useHighMemory = false;

    ///list of buttons with checkboxes attached to select modes
    private List<JCheckBoxMenuItem> modeButtons;

    /**
     * Creates new form MSImagizer
     */
    public MSImagizer() {

        super.setTitle("ProteoFormiX HistoSnap");
        super.setLocationRelativeTo(null);

        instance = this;
        currentScale=exportScale;
        
        try {
            Image i = ImageIO.read(getClass().getResource("/icon.png"));
            super.setIconImage(i);
        } catch (IOException ex) {
            Logger.getLogger(MSImagizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        initComponents();

        imageCacheList = imageList;

        progressFrame = new ProgressBarFrame();
        progressFrame.setVisible(false);

        logArea.setModel(new DefaultListModel<>());

        UILogger.LOGGING_AREA = logArea;

        InitListeners();
        InitAdducts();
        InitModes();

        UILogger.Log("ProteoFormiX presents", UILogger.Level.NONE);
        UILogger.Log("HistoSnap is ready. Welcome !", UILogger.Level.NONE);
        UILogger.Log("-----------------------------", UILogger.Level.NONE);

        BtnLowMemory.setSelected(true);
        btnHighMemory.setSelected(false);

    }

    //Handle Scaling
    public void increaseCurrentScale() {
        if (this.currentScale < 16) {
            this.currentScale *= 2;
            if (this.currentScale > 16) {
                this.currentScale = 16;
            }
            UpdateImage();
        }
    }

    public void decreaseCurrentScale() {
        if (this.currentScale > 1) {
            this.currentScale /= 2;
            UpdateImage();
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
        lbImage = new be.javasaurusstudios.histosnap.view.component.ImageLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        logArea = new javax.swing.JList<>();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        fileMenu = new javax.swing.JMenu();
        btnLoad = new javax.swing.JMenuItem();
        btnSave = new javax.swing.JMenuItem();
        sessionsMenu = new javax.swing.JMenu();
        btnLoadSession = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        btnExit = new javax.swing.JMenuItem();
        menuExtract = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        btnExtractRandomly = new javax.swing.JMenuItem();
        btnExtractDHBMatrix = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        btnGenerateImage = new javax.swing.JMenuItem();
        btnGenerateSequence = new javax.swing.JMenuItem();
        btnExtractMz = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        btnIntensityMode = new javax.swing.JMenu();
        btnTIC = new javax.swing.JCheckBoxMenuItem();
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
        menuOptions = new javax.swing.JMenu();
        BtnLowMemory = new javax.swing.JCheckBoxMenuItem();
        btnHighMemory = new javax.swing.JCheckBoxMenuItem();
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
        tfInput.setBackground(new java.awt.Color(255, 102, 102));
        tfInput.setText("-");

        jScrollPane3.setBackground(new java.awt.Color(204, 204, 204));

        jScrollPane2.setBackground(new java.awt.Color(204, 204, 204));

        imageList.setBackground(new java.awt.Color(204, 204, 204));
        jScrollPane2.setViewportView(imageList);

        jScrollPane3.setViewportView(jScrollPane2);

        jScrollPane4.setBackground(new java.awt.Color(204, 204, 204));

        jScrollPane1.setBackground(new java.awt.Color(204, 204, 204));

        lbImage.setBackground(new java.awt.Color(204, 204, 204));
        lbImage.setForeground(new java.awt.Color(0, 0, 0));
        lbImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbImage.setToolTipText("");
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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfInput))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 629, Short.MAX_VALUE))
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

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        menuFile.setText("File");

        fileMenu.setText("File");

        btnLoad.setText("Load...");
        btnLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadActionPerformed(evt);
            }
        });
        fileMenu.add(btnLoad);

        btnSave.setText("Save...");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        fileMenu.add(btnSave);

        menuFile.add(fileMenu);

        sessionsMenu.setText("Session");

        btnLoadSession.setText("Load Session...");
        btnLoadSession.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadSessionActionPerformed(evt);
            }
        });
        sessionsMenu.add(btnLoadSession);

        jMenuItem1.setText("Save Session...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        sessionsMenu.add(jMenuItem1);

        menuFile.add(sessionsMenu);

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        menuFile.add(btnExit);

        jMenuBar1.add(menuFile);

        menuExtract.setText("Extract");

        jMenu3.setText("Background");

        btnExtractRandomly.setText("Random Background");
        btnExtractRandomly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExtractRandomlyActionPerformed(evt);
            }
        });
        jMenu3.add(btnExtractRandomly);

        btnExtractDHBMatrix.setText("DHB Background");
        btnExtractDHBMatrix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExtractDHBMatrixActionPerformed(evt);
            }
        });
        jMenu3.add(btnExtractDHBMatrix);

        menuExtract.add(jMenu3);

        jMenu5.setText("Image");

        btnGenerateImage.setText("From value");
        btnGenerateImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateImageActionPerformed(evt);
            }
        });
        jMenu5.add(btnGenerateImage);

        btnGenerateSequence.setText("From range");
        btnGenerateSequence.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateSequenceActionPerformed(evt);
            }
        });
        jMenu5.add(btnGenerateSequence);

        btnExtractMz.setText("From list");
        btnExtractMz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExtractMzActionPerformed(evt);
            }
        });
        jMenu5.add(btnExtractMz);

        menuExtract.add(jMenu5);

        jMenuBar1.add(menuExtract);

        jMenu6.setText("Visuals");

        btnIntensityMode.setText("Compare To");

        btnTIC.setSelected(true);
        btnTIC.setText("Total Ion Current");
        btnTIC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTICActionPerformed(evt);
            }
        });
        btnIntensityMode.add(btnTIC);

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

        jMenu6.add(btnIntensityMode);

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

        jMenu6.add(btnColor);

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

        jMenu6.add(btnScale);

        jMenuBar1.add(jMenu6);

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

        jMenuBar1.add(btnAdducts);

        menuOptions.setText("Options");

        BtnLowMemory.setSelected(true);
        BtnLowMemory.setText("Db Mode (slow)");
        BtnLowMemory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnLowMemoryActionPerformed(evt);
            }
        });
        menuOptions.add(BtnLowMemory);

        btnHighMemory.setSelected(true);
        btnHighMemory.setText("Memory Mode");
        btnHighMemory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHighMemoryActionPerformed(evt);
            }
        });
        menuOptions.add(btnHighMemory);

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
        Load();
    }//GEN-LAST:event_btnLoadActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        Save();
    }//GEN-LAST:event_btnSaveActionPerformed

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
                    exportScale,
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
                    exportScale,
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
        exportScale = 1;
        currentScale = exportScale;
        UILogger.Log("Set pixel scale to " + exportScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX1ActionPerformed

    private void btnX2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnX2ActionPerformed
        btnX1.setSelected(false);
        btnX2.setSelected(true);
        btnX4.setSelected(false);
        btnX8.setSelected(false);
        btnX16.setSelected(false);
        exportScale = 2;
        currentScale = exportScale;
        UILogger.Log("Set pixel scale to " + exportScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX2ActionPerformed

    private void btnX4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnX4ActionPerformed
        btnX1.setSelected(false);
        btnX2.setSelected(false);
        btnX4.setSelected(true);
        btnX8.setSelected(false);
        btnX16.setSelected(false);
        exportScale = 4;
        currentScale = exportScale;
        UILogger.Log("Set pixel scale to " + exportScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX4ActionPerformed

    private void btnX8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnX8ActionPerformed
        btnX1.setSelected(false);
        btnX2.setSelected(false);
        btnX4.setSelected(false);
        btnX8.setSelected(true);
        btnX16.setSelected(false);
        exportScale = 8;
        currentScale = exportScale;
        UILogger.Log("Set pixel scale to " + exportScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX8ActionPerformed

    private void btnX16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnX16ActionPerformed
        btnX1.setSelected(false);
        btnX2.setSelected(false);
        btnX4.setSelected(false);
        btnX8.setSelected(false);
        btnX16.setSelected(true);
        exportScale = 16;
        currentScale = exportScale;
        UILogger.Log("Set pixel scale to " + exportScale, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnX16ActionPerformed

    private void btnMeanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMeanActionPerformed
        SetIntensityMode(btnMean);
        currentMode = MSiImage.ImageMode.MEAN;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnMeanActionPerformed

    private void btnMedianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMedianActionPerformed
        SetIntensityMode(btnMedian);
        currentMode = MSiImage.ImageMode.MEDIAN;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnMedianActionPerformed

    private void btnMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMinActionPerformed
        SetIntensityMode(btnMin);
        currentMode = MSiImage.ImageMode.MIN;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnMinActionPerformed

    private void btnMaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMaxActionPerformed
        SetIntensityMode(btnMax);
        currentMode = MSiImage.ImageMode.MAX;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnMaxActionPerformed

    private void btnQ1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQ1ActionPerformed
        SetIntensityMode(btnQ1);
        currentMode = MSiImage.ImageMode.Q1;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnQ1ActionPerformed

    private void btnQ2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQ2ActionPerformed
        SetIntensityMode(btnQ2);
        currentMode = MSiImage.ImageMode.Q3;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnQ2ActionPerformed

    private void btn90thActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn90thActionPerformed
        SetIntensityMode(btn90th);
        currentMode = MSiImage.ImageMode.Q90;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btn90thActionPerformed

    private void btn95thActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn95thActionPerformed
        SetIntensityMode(btn95th);
        currentMode = MSiImage.ImageMode.Q95;
        UILogger.Log("Set pixel scale to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btn95thActionPerformed

    private void btn99thActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn99thActionPerformed
        SetIntensityMode(btn99th);
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
        textArea.setColumns(100);
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
            HashMap<String, Float> mzValues = new HashMap<>();

            ArrayList<WorkingTask> tasks = new ArrayList<>();

            for (String line : lines) {

                String[] parts = line.split("\t");
                String name = parts.length > 1 ? parts[1] : parts[0];

                try {
                    float mz = Float.parseFloat(parts[0].replace(",", "."));
                    if (mz <= 0) {
                        throw new NullPointerException();
                    }
                    mzValues.put(name, mz);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            line + " is an invalid entry. The mz values a real value > 0",
                            "Failed to calculate similarities...",
                            JOptionPane.ERROR_MESSAGE);
                    UILogger.Log("Failed to calculate similarities : invalid mz values provided", UILogger.Level.ERROR);
                    return;
                }
            }

            progressFrame.setVisible(true);
            progressFrame.setText("Starting extraction process...");
            for (Entry<String, Float> entry : mzValues.entrySet()) {
                progressFrame.setVisible(true);
                ImageExtractionTask task = new ImageExtractionTask(
                        this,
                        progressFrame,
                        tfInput,
                        entry.getValue() - (float) toleranceValue,
                        entry.getValue() + (float) toleranceValue,
                        1,
                        lbImage,
                        exportScale,
                        currentMode,
                        currentRange,
                        false);

                task.setImageName(entry.getKey());
                task.setNotifyWhenRead(false);

                tasks.add(task.mute());

            }

            tasks.get(0).setNotifyWhenRead(true);
            WorkingThread worker = new WorkingThread(this, true, tasks.toArray(new WorkingTask[tasks.size()]));
            worker.execute();

            progressFrame.setVisible(false);
            //  JOptionPane.showMessageDialog(parent, "Finished extracting mz values");
            //  UILogger.Log("Finished extracting", UILogger.Level.INFO);
        }
    }//GEN-LAST:event_btnExtractMzActionPerformed

    private void btnExtractRandomlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExtractRandomlyActionPerformed
        new RandomBackgroundExtractor(this, progressFrame, tfInput, lbImage, currentScale, currentRange, currentMode).Show(true);
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

    private void BtnLowMemoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnLowMemoryActionPerformed
        useHighMemory = false;
        BtnLowMemory.setSelected(true);
        btnHighMemory.setSelected(false);
        UILogger.Log("HistoSnap is now running in " + (useHighMemory ? "High Memory Mode" : "Low Memory Mode"), UILogger.Level.INFO);
    }//GEN-LAST:event_BtnLowMemoryActionPerformed

    private void btnHighMemoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHighMemoryActionPerformed
        useHighMemory = true;
        BtnLowMemory.setSelected(false);
        btnHighMemory.setSelected(true);
        UILogger.Log("HistoSnap is now running in " + (useHighMemory ? "High Memory Mode" : "Low Memory Mode"), UILogger.Level.INFO);
    }//GEN-LAST:event_btnHighMemoryActionPerformed

    private void btnTICActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTICActionPerformed
        SetIntensityMode(btnTIC);
        currentMode = MSiImage.ImageMode.TOTAL_ION_CURRENT;
        UILogger.Log("Set image mode to " + currentMode, UILogger.Level.INFO);
        UpdateImage();
    }//GEN-LAST:event_btnTICActionPerformed

    private void btnLoadSessionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadSessionActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File(".")); // start at application current directory
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File yourFolder = fc.getSelectedFile();
            UILogger.Log("Changing sessions...", UILogger.Level.INFO);

            progressFrame.setVisible(true);
            WorkingTask task = new SessionLoadingTask(progressFrame, yourFolder, tfInput);
            new WorkingThread(this, task).execute();
        }

    }//GEN-LAST:event_btnLoadSessionActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File(".")); // start at application current directory
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File yourFolder = fc.getSelectedFile();
            UILogger.Log("Saving sessions...", UILogger.Level.INFO);
            progressFrame.setVisible(true);
            WorkingTask task = new SessionSavingTask(progressFrame, yourFolder, false);
            new WorkingThread(this, task).execute();

        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void btnExtractDHBMatrixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExtractDHBMatrixActionPerformed
        new DHBBackgroundExtractionHandler(this, progressFrame, tfInput, lbImage, currentScale, currentRange, currentMode).Show(false);
    }//GEN-LAST:event_btnExtractDHBMatrixActionPerformed

    /**
     * Adds a new image into the cache
     *
     * @param image the new image
     */
    public static void AddToCache(MSiImage image) {
        MSI_IMAGE=(image);
        CACHE.add(image);
        UpdateCacheUI();
    }

    /**
     * Updates the cache for the UI
     */
    public static void UpdateCacheUI() {
        if (imageCacheList != null) {
            DefaultListModel model = new DefaultListModel();
            for (String cachedImage : CACHE) {
                model.addElement(cachedImage);
            }
            imageCacheList.setModel(model);
        }
    }

    private void InitListeners() {
        new ListSavePopupProvider().SetUp(lbImage);
        new ListSelectionUpdateProvider().SetUp(imageCacheList);
        new ListActionPopupProvider(lbImage).SetUp(imageCacheList);
        new ImageHighlightProvider().SetUp(lbImage);

        ScaleScrollListener scaleScrollListener = new ScaleScrollListener(this, lbImage);

    }

    private void InitModes() {
        //scale
        btnX1.setSelected(false);
        btnX2.setSelected(false);
        btnX4.setSelected(true);
        btnX8.setSelected(false);
        btnX16.setSelected(false);
        //mode
        currentMode = MSiImage.ImageMode.TOTAL_ION_CURRENT;
        modeButtons = new ArrayList<>();
        modeButtons.add(btnTIC);
        modeButtons.add(btnMean);
        modeButtons.add(btnMedian);
        modeButtons.add(btnMin);
        modeButtons.add(btnMax);
        modeButtons.add(btnQ1);
        modeButtons.add(btnQ2);
        modeButtons.add(btn90th);
        modeButtons.add(btn95th);
        modeButtons.add(btn99th);

        modeButtons.get(0).setSelected(true);
        for (int i = 1; i < modeButtons.size(); i++) {
            modeButtons.get(i).setSelected(false);
        }

    }

    private void SetIntensityMode(JCheckBoxMenuItem item) {
        for (int i = 0; i < modeButtons.size(); i++) {
            modeButtons.get(i).setSelected(modeButtons.get(i).equals(item));
        }
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
    public void UpdateImage() {
        ImageIcon icon = null;
        if (!CACHE.isEmpty()) {
            MSImagizer.MSI_IMAGE.CreateImage(currentMode, currentRange.getColors());
            MSImagizer.CURRENT_IMAGE = MSImagizer.MSI_IMAGE.getScaledImage(currentScale);
            icon = new ImageIcon(MSImagizer.CURRENT_IMAGE);
        }
        lbImage.setIcon(icon);
        lbImage.setText("");
    }

    public boolean isHighMemory() {
        return useHighMemory;
    }

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

    /**
     * Loads a file
     */
    public void Load() {
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
            if (selectedFile.exists()) {
                tfInput.setBackground(Color.green);
            }
        }
    }

    public int getExportScale() {
        return exportScale;
    }

    public int getCurrentScale(){
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

    @Override
    public void paint(Graphics g) {
        super.paint(g); //To change body of generated methods, choose Tools | Templates.
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem BtnAllCations;
    private javax.swing.JCheckBoxMenuItem BtnLowMemory;
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
    private javax.swing.JMenuItem btnExtractDHBMatrix;
    private javax.swing.JMenuItem btnExtractMz;
    private javax.swing.JMenuItem btnExtractRandomly;
    private javax.swing.JMenuItem btnGenerateImage;
    private javax.swing.JMenuItem btnGenerateSequence;
    private javax.swing.JMenuItem btnGrayScale;
    private javax.swing.JMenuItem btnGreenPink;
    private javax.swing.JMenuItem btnGreenRed;
    private javax.swing.JMenu btnHelp;
    private javax.swing.JMenuItem btnHelpText;
    private javax.swing.JCheckBoxMenuItem btnHighMemory;
    private javax.swing.JMenu btnIntensityMode;
    private javax.swing.JMenuItem btnLoad;
    private javax.swing.JMenuItem btnLoadSession;
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
    private javax.swing.JCheckBoxMenuItem btnTIC;
    private javax.swing.JCheckBoxMenuItem btnX1;
    private javax.swing.JCheckBoxMenuItem btnX16;
    private javax.swing.JCheckBoxMenuItem btnX2;
    private javax.swing.JCheckBoxMenuItem btnX4;
    private javax.swing.JCheckBoxMenuItem btnX8;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JList<String> imageList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private be.javasaurusstudios.histosnap.view.component.ImageLabel lbImage;
    private javax.swing.JList<String> logArea;
    private javax.swing.JMenu menuExtract;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuOptions;
    private javax.swing.JMenu sessionsMenu;
    private javax.swing.JTextField tfInput;
    // End of variables declaration//GEN-END:variables

}
