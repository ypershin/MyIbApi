package my.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ib.controller.AccountSummaryTag;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IAccountSummaryHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewTickType;
import com.ib.controller.Types.MktDataType;
import com.ib.controller.Types.SecType;

public class CointegrationTrio extends ConnectionHandlerAdapter implements ILogger, Runnable {

	private final ApiController m_controller = new ApiController(this, this, this);

	private static String[] m_symbol = { "AA", "AAPL", "GSK", "BHI", "CDNS", "CELG" };

	private static String[][] trio = { { m_symbol[0], m_symbol[1], m_symbol[2] },
			{ m_symbol[3], m_symbol[4], m_symbol[5] } };

	private static double[][] weight = { { 1.0, 2.13, -7.723 }, { 1.0, -4.844, 0.746 } };
	private static double[][] stats = { { -18.95, 0.44 }, { 26.75, 0.20 } };
	double[] spread = new double[trio.length];
	int[] pos = new int[trio.length];

	// private String[] m_symbol = new String[60];
	private NewContract[] m_contract = null;
	// private double m_bid, m_ask;
	private static final DateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
	private static final DecimalFormat nf = new DecimalFormat("#.00");
	private DbApi db = new DbApi();

	private final int BATCH_SIZE = 100;

	private ArrayList<Ticker> tickerList = new ArrayList<Ticker>();
	private long[] cnt = new long[trio.length];

	private double[] prc = new double[m_symbol.length];

	// see AccountSummaryTag for tags
	private static String[] tags = { "NetLiquidation", "AvailableFunds", "MaintMarginReq" };

	// private static Chart chart = new Chart(tags);
	// private static Chart chartTick = new Chart(m_symbol);
	private static Chart[] chartSpread = {
			new Chart(new String[] { trio[0][0] + " - " + trio[0][1] + " - " + trio[0][2], "avg-sd", "avg", "avg+sd" }),
			new Chart(
					new String[] { trio[1][0] + " - " + trio[1][1] + " - " + trio[1][2], "avg-sd", "avg", "avg+sd" }) };

	public static void main(String[] args) {

		new CointegrationTrio().run();
	}

	@Override
	public void run() {
		// chart.activate();
		// chartTick.activate();
		chartSpread[0].activate();
		chartSpread[1].activate();

		// m_symbol =
		// getSymbols("C:\\Users\\Ypershin\\Documents\\TSX60_components.csv");
		m_contract = new NewContract[m_symbol.length];

		// System.exit(0);

		for (int i = 0; i < m_symbol.length; i++) {
			// System.out.println(m_symbol[i]);
			m_contract[i] = new NewContract();
			// m_contract[i].symbol(m_symbol[i]);
			m_contract[i].localSymbol(m_symbol[i]);
			m_contract[i].exchange("SMART");
			m_contract[i].secType(SecType.STK);
			m_contract[i].currency("USD");
			// m_contract[i].currency("CAD");
		}
		m_controller.connect("127.0.0.1", 7496, 0);
	}

	@Override
	public void connected() {

		// }

		// getAccountSummary();

		for (NewContract contract : m_contract) {
			getMarketData(contract);
		}

	}

	@Override
	public void disconnected() {
		System.out.println("************ FINALLY *****************");
		m_controller.disconnect();
		db.insertBatch(tickerList);
		db.close();

	}

	private void getAccountSummary() {
		// AccountSummaryTag[] tag = { AccountSummaryTag.BuyingPower,
		// AccountSummaryTag.NetLiquidation };
		AccountSummaryTag[] tag = { AccountSummaryTag.NetLiquidation, AccountSummaryTag.AvailableFunds,
				AccountSummaryTag.MaintMarginReq };

		// while (true) {
		m_controller.reqAccountSummary("All", tag, new IAccountSummaryHandler() {

			@Override
			public void accountSummaryEnd() {
				// TODO Auto-generated method stub

			}

			@Override
			public void accountSummary(String account, AccountSummaryTag tag, String value, String currency) {
				// TODO Auto-generated method stub

				// System.out.println(tag + "\t" + value + "\t" + df.format(new
				// Date()));
				// switch (tag) {
				// case NetLiquidation:
				// chart.addDataAcc(0, Double.parseDouble(value));
				// break;
				// case AvailableFunds:
				// chart.addDataAcc(1, Double.parseDouble(value));
				// break;
				// case MaintMarginReq:
				// chart.addDataAcc(2, Double.parseDouble(value));
				// break;
				// default:
				// break;
				// }

				// try {
				// Thread.sleep(1000);
				// } catch (InterruptedException e) {
				// }

			}
		});

	}

	private void getMarketData(NewContract contract) {

		System.out.println(contract.localSymbol());
		int ml = m_symbol.length;

		m_controller.reqTopMktData(contract, "", false, new ITopMktDataHandler() {

			@Override
			public void tickPrice(NewTickType tickType, double price, int canAutoExecute) {

				if (price != 0.0) {
					// System.out.println(contract.localSymbol() + "\t" +
					// df.format(new Date())
					// + (tickType == NewTickType.BID ? "\t\t" : "\t\t\t") +
					// nf.format(price));

					tickerList.add(new Ticker(contract.localSymbol(), tickType == NewTickType.BID, price));

					for (int j = 0; j < ml; j++) {
						if (contract.localSymbol().equals(m_symbol[j])) {
							// chartTick.addDataTick(j, price);
							if (j < prc.length)
								prc[j] = price;
							break;
						}
					}

					for (int j = 0; j < trio.length; j++) {
						if (++cnt[j] > 50) {
							if (prc[0 + 3 * j] != 0 && prc[1 + 3 * j] != 0 && prc[2 + 3 * j] != 0) {
								spread[j] = prc[0 + 3 * j] + weight[j][1] * prc[1 + 3 * j]
										+ weight[j][2] * prc[2 + 3 * j];

								chartSpread[j].addDataTick(0, spread[j]);
								chartSpread[j].addDataTick(1, stats[j][0] - stats[j][1]);
								chartSpread[j].addDataTick(2, stats[j][0]);
								chartSpread[j].addDataTick(3, stats[j][0] + stats[j][1]);

								if (pos[j] == 0 && (spread[j] < stats[j][0] - stats[j][1]
										|| spread[j] > stats[j][0] + stats[j][1])) {
									pos[j]++;
									// System.out.println(String.format("%1$.2f\t%2$.2f\t%3$.2f\t%4$.2f",
									// prc[0 + 3 * j],
									// prc[1 + 3 * j], prc[2 + 3 * j],
									// spread[j]));
									System.out.println(String.format("%1$s-%2$s-%3$s\t%4$.2f\t%5$.2f\t%6$.2f\t%7$.2f",
											trio[j][0], trio[j][1], trio[j][2], prc[0 + 3 * j], prc[1 + 3 * j],
											prc[2 + 3 * j], spread[j]));
								}
							}
						}

					}

					// if (++cnt % BATCH_SIZE == 0) {
					// db.insertBatch(tickerList);
					// tickerList = new ArrayList<Ticker>();
					// }

				}

				// checkPrices(this);
			}

			@Override
			public void tickSize(NewTickType tickType, int size) {
				// print("tickSize\t" + size);
			}

			@Override
			public void tickString(NewTickType tickType, String value) {
				// print(symbol + "\ttickString\t" + value);
			}

			@Override
			public void tickSnapshotEnd() {
				// TODO Auto-generated method stub

			}

			@Override
			public void marketDataType(MktDataType marketDataType) {
				// TODO Auto-generated method stub

			}
		});

	}

	@Override
	public void log(String valueOf) {
		// TODO Auto-generated method stub

	}

	private String[] getSymbols(String fileName) {

		ArrayList<String> arr = new ArrayList<String>();

		try {
			FileReader f = new FileReader(fileName);

			try {
				@SuppressWarnings("resource")
				BufferedReader br = new BufferedReader(f);
				String line = "";
				int cnt = 0;
				while (true) {
					line = br.readLine();
					if (line == null || line.equals(""))
						break;

					if (++cnt > 1) {
						// System.out.println(line);
						arr.add(line.split(",")[1]);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return arr.toArray(new String[arr.size()]);
	}

	void print(String str) {
		System.out.println(str);
	}

}
