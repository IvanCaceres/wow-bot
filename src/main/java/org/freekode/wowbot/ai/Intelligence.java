package org.freekode.wowbot.ai;

import com.sun.jna.platform.win32.WinUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.freekode.wowbot.controller.Controller;
import org.freekode.wowbot.tools.ConfigKeys;
import org.freekode.wowbot.tools.StaticFunc;

import javax.swing.*;
import java.awt.*;

public abstract class Intelligence<T> extends SwingWorker<Boolean, Void> {
    private static final Logger logger = LogManager.getLogger(Intelligence.class);

    private Rectangle windowArea;
    private Controller controller;


    @Override
    public Boolean doInBackground() {
        logger.info("start");
        try {
            windowArea = findWindow();
            init();

            return processing();
        } catch (Exception e) {
            logger.info("Intelligence exception: " + e.getMessage());
            return true;
        }
    }

    public Rectangle findWindow() throws Exception {
        WinUser.WINDOWINFO windowCoordinates = StaticFunc.upWindow(ConfigKeys.WINDOW_CLASS, ConfigKeys.WINDOW_NAME);

        if (windowCoordinates == null) {
            throw new Exception("there is no window");
        }

        return windowCoordinates.rcClient.toRectangle();
    }

    public void init() throws InterruptedException {
        controller = new Controller(windowArea);
    }

    public void send(T object) {
        firePropertyChange("custom", null, object);
    }

    public void send(T object, String command) {
        firePropertyChange(command, null, object);
    }

    public void kill() {
        if (!isDone() || !isCancelled()) {
            logger.info("kill");
            terminating();
            cancel(true);
        }
    }

    public Boolean processing() throws InterruptedException {
        return true;
    }

    public void terminating() {
    }

    public Rectangle getWindowArea() {
        return windowArea;
    }

    public Controller getController() {
        return controller;
    }
}
