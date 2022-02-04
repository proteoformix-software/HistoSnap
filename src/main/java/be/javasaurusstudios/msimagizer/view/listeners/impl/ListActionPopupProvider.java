package be.javasaurusstudios.msimagizer.view.listeners.impl;

import be.javasaurusstudios.msimagizer.control.util.UILogger;
import be.javasaurusstudios.msimagizer.model.image.MSiImage;
import be.javasaurusstudios.msimagizer.view.listeners.ListenerProvider;
import be.javasaurusstudios.msimagizer.view.MSImagizer;
import static be.javasaurusstudios.msimagizer.view.MSImagizer.CACHE;
import static be.javasaurusstudios.msimagizer.view.MSImagizer.MSI_IMAGE;
import be.javasaurusstudios.msimagizer.view.component.ImageLabel;
import be.javasaurusstudios.msimagizer.view.prompt.impl.SaveAnimationDialog;
import be.javasaurusstudios.msimagizer.view.prompt.impl.SaveFramesDialog;
import be.javasaurusstudios.msimagizer.view.prompt.impl.SaveSimilaritiesDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 *
 * @author Dr. Kenneth Verheggen <kenneth.verheggen@proteoformix.com>
 */
public class ListActionPopupProvider implements ListenerProvider {

    private JMenuItem deleteItem;
    private JMenuItem renameItem;
    private JMenuItem saveAnimationItem;
    private JMenuItem saveFrameItem;
    private JMenuItem similarityItem;
    private final ImageLabel imgLabel;
    private JMenuItem generateCombinedImage;

    public ListActionPopupProvider(ImageLabel imgLabel) {
        this.imgLabel = imgLabel;
    }

    @Override
    public void SetUp(JComponent component) {

        final MSImagizer parent = MSImagizer.instance;

        JList imageCacheList = (JList) component;

        JPopupMenu menu = new JPopupMenu();
        deleteItem = new JMenuItem("Delete...");
        menu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<MSiImage> selectedImages = imageCacheList.getSelectedValuesList();
                CACHE.getImageList().removeAll(selectedImages);
                UILogger.Log("Deleted " + selectedImages.size() + " image(s)", UILogger.Level.INFO);
                MSImagizer.UpdateCacheUI();
                if (CACHE.getImageList().size() > 0) {
                    MSI_IMAGE = CACHE.getImageList().get(0);
                    imageCacheList.setSelectedIndex(0);
                }

                parent.UpdateImage();
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
                parent.UpdateImage();
            }
        });

        generateCombinedImage = new JMenuItem("Combine...");
        menu.add(generateCombinedImage);
        generateCombinedImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<MSiImage> selectedImages = imageCacheList.getSelectedValuesList();
                if (selectedImages.size() <= 1) {
                    return;
                }
                try {
                    UILogger.Log("Creating combined image for " + selectedImages.size() + " images...", UILogger.Level.INFO);
                    MSImagizer.instance.getProgressFrame().setVisible(true);
                    MSImagizer.instance.getProgressFrame().setText("Generating combined image...");
                    MSiImage image = MSiImage.CreateCombinedImage(selectedImages);
                        
                    MSImagizer.AddToCache(image);
                    
                    MSImagizer.MSI_IMAGE = image;
                    MSImagizer.instance.getProgressFrame().setText("Removing hotspots");
                    MSImagizer.MSI_IMAGE.RemoveHotSpots(99);
                    MSImagizer.instance.getProgressFrame().setText("Generating heatmap...");
                    MSImagizer.MSI_IMAGE.CreateImage(MSImagizer.instance.getCurrentMode(), MSImagizer.instance.getCurrentRange().getColors());
                    MSImagizer.CURRENT_IMAGE = MSImagizer.MSI_IMAGE.getScaledImage(MSImagizer.instance.getCurrentScale());
                } finally {
                    MSImagizer.instance.getProgressFrame().setVisible(false);
                }
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
                new SaveAnimationDialog(selectedImages).Show();
            }
        });

        saveFrameItem = new JMenuItem("Save Frame(s)...");
        menu.add(saveFrameItem);
        saveFrameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<MSiImage> selectedImages = imageCacheList.getSelectedValuesList();
                new SaveFramesDialog(selectedImages).Show();
            }
        });

        similarityItem = new JMenuItem("Check Similarities...");
        menu.add(similarityItem);
        similarityItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<MSiImage> selectedImages = imageCacheList.getSelectedValuesList();
                new SaveSimilaritiesDialog(imgLabel, selectedImages).Show();
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

}
