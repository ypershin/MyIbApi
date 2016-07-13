package my.api;

public class HistAnalysis implements Runnable {

	private DbApi db = new DbApi();
	private String[] chS = { "spread" };

	private Chart chart = new Chart(chS);

	public static void main(String[] args) {
		new HistAnalysis().run();
	}

	@Override
	public void run() {
		chart.activate();

		// read DB and add data to chart

		// Ticker[] tck = null;
		// long ts = 0;
		// if (j == 0)
		// ts = tck[j].getTimestamp_().getTime();
		// chart.addDataTickHist(i, (tck[j].getTimestamp_().getTime() - ts) /
		// 60000.0, tck[j].getPrice());

		// for (int i = 0; i < chS.length - 1; i++) {
		// tck = db.getRecords(chS[i]);
		// for (int j = 0; j < tck.length; j++) {
		// chart.addDataTickHist(i, j, tck[j].getPrice());
		// }
		// }

		double[][] p = db.getSpread(new String[] { "XIV", "VXX" });

		for (int i = 0; i < p.length; i++) {
			if (p[i][0] == 0.0 && i > 0 && p[i - 1][0] > 0)
				p[i][0] = p[i - 1][0];
			if (p[i][1] == 0.0 && i > 0 && p[i - 1][1] > 0)
				p[i][1] = p[i - 1][1];

			if (p[i][0] != 0.0 && p[i][1] != 0.0) {
				// System.out.println(i + "\t" + (p[i][0] - p[i][1]));
				chart.addDataTickHist(0, i, p[i][0] - p[i][1]);
			}
		}

	}

}
