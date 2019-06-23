package uk.co.caprica.vlcj.javafx.test;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple component to manage hiding the mouse pointer over a {@link Node} after a period of inactivity.
 * <p>
 * This is somewhat of a crude implementation, we could do things like stopping/suspending timers if the pointer moves
 * inside/outside the managed component, don't hide the pointer if there is no video playing, and so forth, but it is
 * quite simple how it is and is good enough for demo purposes.
 */
public class CursorHandler {

    private final Node node;

    private final long timeout;

    private final Timer timer;

    private volatile long lastMouse;

    private AtomicBoolean hidden = new AtomicBoolean(false);

    public CursorHandler(Node node, long timeout) {
        this.node = node;
        this.timeout = timeout;
        this.timer = new Timer();

        node.setOnMouseMoved(mouseEvent -> showCursor());
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!hidden.get() && System.currentTimeMillis() - lastMouse > timeout) {
                    Platform.runLater(() -> hideCursor());
                }
            }
        }, 1000, 1000);
    }

    private synchronized void hideCursor() {
        hidden.set(true);
        node.setCursor(Cursor.NONE);
    }

    private synchronized void showCursor() {
        hidden.set(false);
        node.setCursor(Cursor.DEFAULT);
        lastMouse = System.currentTimeMillis();
    }
}
