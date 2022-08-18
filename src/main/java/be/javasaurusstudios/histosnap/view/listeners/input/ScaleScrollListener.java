package be.javasaurusstudios.histosnap.view.listeners.input;

import be.javasaurusstudios.histosnap.view.HistoSnap;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JLabel;

/**
 *
 * @author Dr. Kenneth Verheggen <kenneth.verheggen@proteoformix.com>
 */
public class ScaleScrollListener implements MouseWheelListener, KeyListener {

    /**
     * The parent for the scroll listener
     */
    private final HistoSnap parent;
    /**
     * Boolean indicating if left ctrl is held down before scrolling
     */
    @Deprecated
    private boolean ctrlHeld;

    public ScaleScrollListener(HistoSnap parent, JLabel imgLabel) {
        this.parent = parent;
        imgLabel.addMouseWheelListener(this);
        this.parent.addKeyListener(this);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // if (ctrlHeld) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            parent.increaseCurrentScale();
        } else if (notches > 0) {
            parent.decreaseCurrentScale();
        }
        //    }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!ctrlHeld && (e.getKeyCode() == KeyEvent.VK_CONTROL)) {
            ctrlHeld = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        if (ctrlHeld && (e.getKeyCode() == KeyEvent.VK_CONTROL)) {
            ctrlHeld = false;
        }

    }
}
