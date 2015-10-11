package org.freekode.wowbot.beans.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.freekode.wowbot.beans.interfaces.Intelligence;
import org.freekode.wowbot.tools.StaticFunc;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class FishingAI extends Intelligence {
    private static final Logger logger = LogManager.getLogger(FishingAI.class);
    public static final double STANDARD_PITCH = -0.25;
    public static final int FISH_BUTTON = KeyEvent.VK_EQUALS;
    public static final int FISHING_TIME_SEC = 20;
    public static final int FAIL_TRYINGS = 5;
    public static final Rectangle SEARCH_SQUARE = new Rectangle(400, 110, 440, 390);
    // red colors
    public static final Color[] FIRST_COLORS = {
            Color.decode("#6b240e"),
            Color.decode("#4d160e"),

            Color.decode("#c62f12"),
            Color.decode("#94260b"),

            Color.decode("#49150a"),
            Color.decode("#341209"),
    };
    // blue colors
    public static final Color[] SECOND_COLORS = {
            Color.decode("#353c59"),
            Color.decode("#2f3756"),

            Color.decode("#4d5363"),
            Color.decode("#626574"),

            Color.decode("#1e2d4a"),
            Color.decode("#17263d"),
    };
    // white-yellow colors
    public static final Color[] THIRD_COLORS = {
            Color.decode("#6a5344"),
            Color.decode("#756051"),

            Color.decode("#4d4030"),
            Color.decode("#624d38"),

            Color.decode("#504d3e"),
            Color.decode("#42453a"),
    };


    @Override
    public void processing() {
        getCharacter().pitch(STANDARD_PITCH);
        getCharacter().fpv();

        logger.info("start fishing");
        for (int i = 0; i < FAIL_TRYINGS; i++) {
            logger.info("try = " + i);
            mouseOut();
            fish();

            Rectangle imageRect = StaticFunc.calculateCutSquare(getWindowArea(), SEARCH_SQUARE);
            BufferedImage image = StaticFunc.cutImage(imageRect, true, "search");
            int[] bobberPoint = findColor(image, FIRST_COLORS, 7);
            if (bobberPoint == null) {
                continue;
            }

            logger.info("first color found = " + new Color(bobberPoint[2]).toString());
            Rectangle bobberSquare = new Rectangle(bobberPoint[0] - 30, bobberPoint[1] - 20, 80, 50);
            Rectangle bobberRect = StaticFunc.calculateCutSquare(getWindowArea(),
                    StaticFunc.calculateCutSquare(SEARCH_SQUARE, bobberSquare));
            BufferedImage bobberImage = StaticFunc.cutImage(bobberRect, true, "bobber");
            int[] bobberPart = findColor(bobberImage, SECOND_COLORS, 6);
            if (bobberPart == null) {
                continue;
            }

            logger.info("second color found = " + new Color(bobberPart[2]).toString());
            int[] bobberCoordinates = findColor(bobberImage, THIRD_COLORS, 5);
            if (bobberCoordinates == null) {
                continue;
            }

            logger.info("third color found = " + new Color(bobberCoordinates[2]).toString());
            Rectangle stickSquare = new Rectangle(bobberCoordinates[0] - 10, bobberCoordinates[1] - 5, 22, 22);
            Rectangle trackRect = StaticFunc.calculateCutSquare(getWindowArea(),
                    StaticFunc.calculateCutSquare(SEARCH_SQUARE,
                            StaticFunc.calculateCutSquare(bobberSquare, stickSquare)));

            StaticFunc.cutImage(trackRect, true, "tracking");
            trackingSquare(trackRect, new Color(bobberCoordinates[2]));
            i = 0;

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void trackingSquare(Rectangle rectangle, Color color) {
        long endTime = System.currentTimeMillis() / 1000 + FISHING_TIME_SEC;

        while ((System.currentTimeMillis() / 1000) <= endTime) {
            try {
                BufferedImage image = StaticFunc.cutImage(rectangle, false, null);
                int[] trackCoordinates = findColor(image, new Color[]{color}, 8);
                if (trackCoordinates == null) {
                    StaticFunc.cutImage(rectangle, true, "wtf");
                    int x = (int) (rectangle.getX() + (rectangle.getWidth() / 2));
                    int y = (int) (rectangle.getY() + (rectangle.getHeight() / 2));
                    loot(x, y);
                    logger.info("take!");
                    break;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void fish() {
        try {
            getCharacter().getControl().pressKey(FISH_BUTTON);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int[] findColor(BufferedImage image, Color[] colors, double similarity) {
//        int[][] pixels = StaticFunc.convertTo2DWithoutUsingGetRGB(image);
        int[][] pixels = StaticFunc.convertTo2DUsingGetRGB(image);

        return StaticFunc.findColor(pixels, colors, similarity);
    }

    public void loot(int x, int y) {
        try {
            getCharacter().getControl().mouse(x, y);
            getCharacter().getControl().getRobot().keyPress(KeyEvent.VK_SHIFT);
            Thread.sleep(100);
            getCharacter().getControl().getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(200);
            getCharacter().getControl().getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            getCharacter().getControl().getRobot().keyRelease(KeyEvent.VK_SHIFT);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void mouseOut() {
        try {
            int x = (int) (getWindowArea().getX() + SEARCH_SQUARE.getX() + SEARCH_SQUARE.getWidth() + 20);
            int y = (int) (getWindowArea().getY() + SEARCH_SQUARE.getY());
            getCharacter().getControl().mouse(x, y);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
