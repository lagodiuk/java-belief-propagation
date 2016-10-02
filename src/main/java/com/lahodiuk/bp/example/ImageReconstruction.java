package com.lahodiuk.bp.example;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;
import com.lahodiuk.bp.Potential;

public class ImageReconstruction {

	static final String PATH_TO_SAVE_IMAGES = "target/reconstructed_images";
	static final boolean SAVE_IMAGES_ON_DISK = true;
	static final int INFERENCE_ITERATIONS = 100;

	public static void main(String[] args) throws Exception {

		BufferedImage originalImg = ImageIO.read(ImageReconstruction.class.getResourceAsStream("/input_3.png"));

		UI ui = constructUI(originalImg, 2);

		System.out.println("Building MRF");
		ImgNode[][] nodes = build_MRF_nodes(originalImg);
		List<Edge<ImgNodeStates, ImgNodeStates>> edges = build_MRF_connections(nodes);

		BufferedImage reconstructedImg = new BufferedImage(originalImg.getWidth(), originalImg.getHeight(), BufferedImage.TYPE_INT_RGB);
		prepare_before_saving_images(SAVE_IMAGES_ON_DISK);
		display_current_results(ui, nodes, reconstructedImg, SAVE_IMAGES_ON_DISK, 0);

		System.out.println("Inference");
		for (int i = 1; i < INFERENCE_ITERATIONS; i++) {
			System.out.println("\nNew inference iteration started\n");
			int edgesProcessed = 0;
			for (Edge<ImgNodeStates, ImgNodeStates> e : edges) {
				e.updateMessages();

				if ((edgesProcessed++ % 1000) == 0) {
					System.out.println(edgesProcessed + " edges processed so far...");
				}
			}
			for (Edge<ImgNodeStates, ImgNodeStates> e : edges) {
				e.refreshMessages();
			}
			display_current_results(ui, nodes, reconstructedImg, SAVE_IMAGES_ON_DISK, i);
		}

		System.out.println("End inference");
	}

	public static void prepare_before_saving_images(boolean saveImagesOnDisk) throws IOException {
		if (saveImagesOnDisk) {
			Path directoryForOutputImages = Paths.get(PATH_TO_SAVE_IMAGES);
			if (!Files.exists(directoryForOutputImages)) {
				Files.createDirectory(directoryForOutputImages);
			}
		}
	}

	public static void display_current_results(
			UI ui,
			ImgNode[][] nodes,
			BufferedImage reconstructedImg,
			boolean saveImagesOnDisk,
			int imageIndex) throws IOException {

		Graphics imageIconCanvas = ui.imageIconImage.createGraphics();
		display_MRF(nodes, reconstructedImg);
		imageIconCanvas.drawImage(reconstructedImg, 0, 0, ui.imageIconImage.getWidth(), ui.imageIconImage.getHeight(), null);
		ui.fr.repaint();
		if (saveImagesOnDisk) {
			ImageIO.write(reconstructedImg, "jpeg", new File(String.format(PATH_TO_SAVE_IMAGES + "/out_%03d.jpg", imageIndex)));
		}
	}

	static UI constructUI(BufferedImage originalImg, int displayImageScale) {
		JFrame fr = new JFrame();
		fr.setSize((originalImg.getWidth() * displayImageScale) + 20,
				(originalImg.getHeight() * displayImageScale) + 70);
		BufferedImage imageIconImage = new BufferedImage(
				originalImg.getWidth() * displayImageScale,
				originalImg.getHeight() * displayImageScale,
				BufferedImage.TYPE_INT_RGB);
		ImageIcon imageIcon = new ImageIcon(imageIconImage);
		JLabel comp = new JLabel(imageIcon);
		fr.getContentPane().add(comp);
		fr.setVisible(true);
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return new UI(fr, imageIconImage);
	}

	static class UI {
		final JFrame fr;
		final BufferedImage imageIconImage;

		public UI(JFrame fr, BufferedImage imageIconImage) {
			this.fr = fr;
			this.imageIconImage = imageIconImage;
		}
	}

	public static void display_MRF(ImgNode[][] nodes, BufferedImage bi2) {
		for (int w = 0; w < nodes.length; w++) {
			for (int h = 0; h < nodes[0].length; h++) {

				int alpha = 255;

				int red = nodes[w][h].getMostProbableState().getColor() * ImgNode.COLOR_PALETTE_SCALE;
				int green = red;
				int blue = red;

				int argb = (alpha << 24) + (red << 16) + (green << 8) + blue;

				bi2.setRGB(w, h, argb);
			}
		}
	}

	public static List<Edge<ImgNodeStates, ImgNodeStates>> build_MRF_connections(ImgNode[][] nodes) {
		ImgNodePotential potential = new ImgNodePotential();

		List<Edge<ImgNodeStates, ImgNodeStates>> edges = new ArrayList<>((nodes.length * nodes[0].length) + 100);
		for (int w = 0; w < nodes.length; w++) {
			for (int h = 0; h < nodes[0].length; h++) {
				if ((w + 1) < nodes.length) {
					edges.add(Edge.connect(nodes[w][h], nodes[w + 1][h], potential));
				}
				if ((h + 1) < nodes[0].length) {
					edges.add(Edge.connect(nodes[w][h], nodes[w][h + 1], potential));
				}
			}
		}
		return edges;
	}

	public static ImgNode[][] build_MRF_nodes(BufferedImage bi) {
		ImgNode[][] nodes = new ImgNode[bi.getWidth()][bi.getHeight()];

		for (int w = 0; w < bi.getWidth(); w++) {
			for (int h = 0; h < bi.getHeight(); h++) {
				int clr = bi.getRGB(w, h);
				int red = (clr & 0x00ff0000) >> 16;
				int green = (clr & 0x0000ff00) >> 8;
				int blue = clr & 0x000000ff;

				nodes[w][h] = new ImgNode(red);
			}
		}
		return nodes;
	}

	static class ImgNodeStates {

		final static Set<ImgNodeStates> ALL_STATES;
		static {
			ALL_STATES = new LinkedHashSet<>();
			for (int i = 0; i < (256 / ImgNode.COLOR_PALETTE_SCALE); i++) {
				ALL_STATES.add(new ImgNodeStates(i));
			}
		}

		int color;

		public ImgNodeStates(int color) {
			this.color = color;
		}

		public int getColor() {
			return this.color;
		}
	}

	static class ImgNodePotential extends Potential<ImgNodeStates, ImgNodeStates> {

		private static final int STATES_SIZE = ImgNodeStates.ALL_STATES.size();
		private static final double INV_STATES_SIZE = 1.0 / STATES_SIZE;

		@Override
		public double getValue(ImgNodeStates node1State, ImgNodeStates node2State) {
			int diff = Math.abs(node1State.getColor() - node2State.getColor());
			double diffDoubl = diff * INV_STATES_SIZE;
			diffDoubl = Math.sqrt(diffDoubl);
			diffDoubl = Math.sqrt(diffDoubl);
			diffDoubl = Math.sqrt(diffDoubl);
			return 1.0 - diffDoubl;
		}
	}

	static class ImgNode extends Node<ImgNodeStates> {

		static final double PRIOR_MATCH = 0.97;
		static final double UNIFORM_PROBABILITY = 1.0 / ImgNodeStates.ALL_STATES.size();
		static final double PRIOR_NOMATCH = (1 - PRIOR_MATCH) / ImgNodeStates.ALL_STATES.size();
		static final int COLOR_PALETTE_SCALE = 10;

		final int color;

		public ImgNode(int color) {
			this.color = color / COLOR_PALETTE_SCALE;
		}

		@Override
		public Set<ImgNodeStates> getStates() {
			return ImgNodeStates.ALL_STATES;
		}

		@Override
		public double getPriorProbablility(ImgNodeStates state) {
			if (this.color == 0) {
				return UNIFORM_PROBABILITY;
			}
			return (state.getColor() == this.color) ? PRIOR_MATCH : PRIOR_NOMATCH;
		}
	}
}
