package my.api;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Chart {

	private JFrame window = null;
	private JFreeChart chart = null;
	private XYSeriesCollection dataset = null;
	private XYSeries[] series = null;
	private long startTime = System.nanoTime();
	private double[] hlVal = { 0.0, 0.0 };

	public Chart(String[] arr) {
		// create and configure the window
		window = new JFrame();
		window.setTitle("IB API");
		window.setSize(800, 600);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create button
		// JButton startButton = new JButton("Start");
		// JPanel topPanel = new JPanel();
		// topPanel.add(startButton);
		// window.add(topPanel, BorderLayout.NORTH);

		// create the line graph
		dataset = new XYSeriesCollection();
		series = new XYSeries[arr.length];

		for (int i = 0; i < arr.length; i++) {
			series[i] = new XYSeries(arr[i]);
			dataset.addSeries(series[i]);
		}

		chart = ChartFactory.createXYLineChart(new SimpleDateFormat("MMM dd  HH:mm:ss").format(new Date()), "min", "", dataset);

		window.add(new ChartPanel(chart), BorderLayout.CENTER);

	}

	public void activate() {

		// show the window
		window.setVisible(true);
	}

	public void addData(int i, double y) {
		double x = (System.nanoTime() - startTime) / 1e9 / 60;

		double ty = Math.floor(y / 1000) * 1000.0;
		hlVal[0] = (hlVal[0] == 0) ? ty : Math.min(hlVal[0], ty);
		ty = Math.ceil(y / 1000) * 1000.0;
		hlVal[1] = (hlVal[1] == 0) ? ty : Math.max(hlVal[1], ty);

		chart.getXYPlot().getRangeAxis().setRange(hlVal[0], hlVal[1]);

		series[i].add(x, y);
	}

}
