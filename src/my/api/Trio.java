package my.api;

public class Trio {
	private String[] symbol = new String[3];
	private double weight[] = new double[3];
	private double avg = 0.0;
	private double sd = 0.0;
	private int jumps = 0;

	protected double[] prc = new double[3];
	private double spread = 0.0;
	private int pos = 0;
	protected long cnt = 0;
	// chart.activate();
	// chartTick.activate();
	private Chart chart = null;

	public Trio(String arg) {

		String[] arr = arg.split(",");

		symbol[0] = arr[0];
		symbol[1] = arr[1];
		symbol[2] = arr[2];
		weight[0] = 1.0;
		weight[1] = Double.parseDouble(arr[10]);
		weight[2] = Double.parseDouble(arr[11]);
		avg = Double.parseDouble(arr[4]);
		sd = Double.parseDouble(arr[7]);
		jumps = Integer.parseInt(arr[12]);

		chart = new Chart(new String[] { this.getSymbols(), "avg-sd", "avg", "avg+sd" });

	}

	public String[] getSymbol() {
		return symbol;
	}

	public double[] getWeight() {
		return weight;
	}

	public double getAvg() {
		return avg;
	}

	public double getSd() {
		return sd;
	}

	public void calcSpread() {
		spread = prc[0] + weight[1] * prc[1] + weight[2] * prc[2];
	}

	public void activateChart() {
		chart.activate();
	}

	public void addTickToChart() {
		chart.addDataTick(0, spread);
		chart.addDataTick(1, avg - sd);
		chart.addDataTick(2, avg);
		chart.addDataTick(3, avg + sd);
	}

	public String getSymbols() {
		return (String.format("%1$s - %2$s - %3$s", symbol[0], symbol[1], symbol[2]));
	}

	@Override
	public String toString() {
		return (String.format("%1$s-%2$s-%3$s\t%4$.3f:%5$.3f:%6$.3f\t%7$.2f[%8$.2f]\t%9$d", symbol[0], symbol[1],
				symbol[2], weight[0], weight[1], weight[2], avg, sd, jumps));
	}

}
