package be.javasaurusstudios.histosnap.view.listeners.imagelist.impl;

import be.javasaurusstudios.histosnap.model.image.MSiFrame;
import be.javasaurusstudios.histosnap.model.image.MultiMSiImage;
import be.javasaurusstudios.histosnap.view.listeners.imagelist.ListenerProvider;
import be.javasaurusstudios.histosnap.view.HistoSnap;
import static be.javasaurusstudios.histosnap.view.HistoSnap.MSI_IMAGE;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Dr. Kenneth Verheggen <kenneth.verheggen@proteoformix.com>
 */
public class ListSelectionUpdateProvider implements ListenerProvider {

    @Override
    public void setUp(JComponent component) {

        if (!(component instanceof JList)) {
            return;
        }

        JList imageCacheList = (JList) component;

        HistoSnap main = HistoSnap.instance;

        imageCacheList.addListSelectionListener((ListSelectionEvent e) -> {
            if (imageCacheList.getSelectedValuesList().size() == 1) {
                MSI_IMAGE = HistoSnap.CACHE.getImage((String) imageCacheList.getSelectedValue());
                main.updateImage();
            } else {
                if (imageCacheList.getSelectedValuesList().size() > 1) {
                    List<MSiFrame> frames = new ArrayList<>();
                    for (Object value : imageCacheList.getSelectedValuesList()) {
                        frames.add(HistoSnap.CACHE.getImage((String) value).getFrame());
                    }
                    if (!frames.isEmpty()) {
                        MSI_IMAGE = MultiMSiImage.generate(frames);
                        main.updateImage();
                    }
                }
            }
        });
    }

}
