package be.javasaurusstudios.histosnap.control.tasks;

import be.javasaurusstudios.histosnap.control.util.UILogger;
import be.javasaurusstudios.histosnap.model.task.WorkingTask;
import be.javasaurusstudios.histosnap.view.component.ProgressBarFrame;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * This class represents a working thread for tasks in the background, while
 * updating the front-end
 *
 * @author Dr. Kenneth Verheggen <kenneth.verheggen@proteoformix.com>
 */
public class WorkingThread extends SwingWorker {

    //The parent frame
    private final JFrame parent;
    //The progress bar to update
    private final ProgressBarFrame progressFrame;
    // The current task to run
    private final WorkingTask[] tasks;
    //boolean indicating if this thread is busy
    private boolean working = false;

    /**
     * Constructor
     *
     * @param parent the parent frame
     * @param tasks the tasks to run
     */
    public WorkingThread(JFrame parent, WorkingTask... tasks) {
        this.parent = parent;
        this.progressFrame = tasks[0].getProgressBar();
        this.tasks = tasks;
    }

    /**
     * Constructor
     *
     * @param parent the parent frame
     * @param tasks the tasks to run
     * @param hotStart (enter in active status)
     */
    public WorkingThread(JFrame parent, boolean hotStart, WorkingTask... tasks) {
        this.parent = parent;
        this.progressFrame = tasks[0].getProgressBar();
        this.tasks = tasks;
        this.working = hotStart;
    }

    /**
     * A Thread to ensure the progressFrame moves with the UI, allowing the user
     * to drag and relocate the parent frame
     */
    private final Thread movingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (working) {
                if (progressFrame.isVisible()) {
                    progressFrame.setAlwaysOnTop(true);
                    progressFrame.setLocationRelativeTo(parent);
                }
            }
        }
    });

    /**
     * Runs the task in the background and reports to the progress frame
     *
     * @return the result of the provided task
     * @throws Exception
     */
    @Override
    protected Object doInBackground() throws Exception {
        working = true;
        movingThread.start();
        progressFrame.setLocationRelativeTo(parent);
        progressFrame.setVisible(true);
        for (int i = 0; i < tasks.length; i++) {
            Object call = tasks[i].call();
            tasks[i].Finish(call);
        }
        return true;
    }

    /**
     * Runs when the job has finished, example to disable the progressframe
     */
    @Override
    protected void done() {
        progressFrame.setVisible(false);
        working = false;
        movingThread.interrupt();
        UILogger.Log(tasks[0].getFinishMessage(), UILogger.Level.INFO);
        UILogger.Log("-----------------------------", UILogger.Level.NONE);
        if (tasks[0].isNotifyWhenReady()) {
            JOptionPane.showMessageDialog(parent, tasks[0].getFinishMessage());
        }
    }

    public boolean isWorking() {
        return working;
    }

}
