package com.runescape.worldmap;
/**
 * Class: RSImageProducer.java
 * Originally: Class3.java
 * */

import java.awt.*;
import java.awt.image.*;

public class RSImageProducer implements ImageProducer, ImageObserver {

	public void initializeDrawingArea() {
		DrawingArea.setArea(pixels, width, height);
	}

	public RSImageProducer(int width, int height, Component component) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
		colorModel = new DirectColorModel(32, 0xff0000, 65280, 255);
		image = component.createImage(this);
		prepareImage();
		component.prepareImage(image, this);
		prepareImage();
		component.prepareImage(image, this);
		prepareImage();
		component.prepareImage(image, this);
		initializeDrawingArea();
	}

	public boolean imageUpdate(Image image, int i, int j, int k, int l, int i1) {
		return true;
	}

	public void requestTopDownLeftRightResend(ImageConsumer imageconsumer) {
		System.out.println("TDLR");
	}

	public synchronized boolean isConsumer(ImageConsumer imageconsumer) {
		return imageConsumer == imageconsumer;
	}

	public synchronized void removeConsumer(ImageConsumer imageconsumer) {
		if (imageConsumer == imageconsumer)
			imageConsumer = null;
	}

	public void drawGraphics(Graphics g, int x, int y) {
		prepareImage();
		g.drawImage(image, x, y, this);
	}

	public void startProduction(ImageConsumer imageconsumer) {
		addConsumer(imageconsumer);
	}

	public synchronized void addConsumer(ImageConsumer imageConsumer) {
		this.imageConsumer = imageConsumer;
		imageConsumer.setDimensions(width, height);
		imageConsumer.setProperties(null);
		imageConsumer.setColorModel(colorModel);
		imageConsumer.setHints(14);
	}

	public synchronized void prepareImage() {
		if (imageConsumer == null) {
			return;
		} else {
			imageConsumer.setPixels(0, 0, width, height, colorModel, pixels, 0,
					width);
			imageConsumer.imageComplete(2);
			return;
		}
	}

	public int pixels[];
	public int width;
	public int height;
	public ColorModel colorModel;
	public ImageConsumer imageConsumer;
	public Image image;
}
