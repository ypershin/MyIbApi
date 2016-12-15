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

	private static String[] m_symbol = null;
	private double[] prc = null;

	private ArrayList<Trio> trios = null;
	int trioSize = 0;

	double[] spread = null;
	int[] pos = null;
	private long[] cnt = null;

	// private String[] m_symbol = new String[60];
	private NewContract[] m_contract = null;
	// private double m_bid, m_ask;
	private static final DateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
	private static final DecimalFormat nf = new DecimalFormat("#.00");
	private DbApi db = new DbApi();

	// private final int BATCH_SIZE = 100;

	// private ArrayList<Ticker> tickerList = new ArrayList<Ticker>();

	// see AccountSummaryTag for tags
	// private static String[] tags = { "NetLiquidation", "AvailableFunds",
	// "MaintMarginReq" };

	// private static Chart chart = new Chart(tags);
	// private static Chart chartTick = new Chart(m_symbol);
	private static Chart[] chart = null;

	// {
	// new Chart(new String[] { trio[0][0] + " - " + trio[0][1] + " - " +
	// trio[0][2], "avg-sd", "avg", "avg+sd" }),
	// new Chart(
	// new String[] { trio[1][0] + " - " + trio[1][1] + " - " + trio[1][2],
	// "avg-sd", "avg", "avg+sd" }) };

	public static void main(String[] args) {

		new CointegrationTrio().run();
	}

	@Override
	public void run() {

		trios = Utilities.getTrios("C:/Users/Ypershin/Documents/Trading/co_res3 2016-12-14 bad.csv", 2);

		// chart.activate();
		// chartTick.activate();

		ArrayList<String> symbols = new ArrayList<String>();

		int j = 0;
		for (Trio trio : trios) {
			System.out.println(trio.toString());

			boolean[] blnAdd = { true, true, true };

			// check if symbols in the array already
			for (String symbol : symbols) {
				for (int n = 0; n < 3; n++) {
					if (symbol.equals(trio.getSymbol()[n]))
						blnAdd[n] = false;
				}
			}

			// add missing symbols to array
			for (int n = 0; n < 3; n++)
				if (blnAdd[n])
					symbols.add(trio.getSymbol()[n]);

			trio.activateChart();
		}

		m_symbol = new String[symbols.size()];
		symbols.toArray(m_symbol);

		m_contract = new NewContract[m_symbol.length];
		prc = new double[m_symbol.length];

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
		// m_controller.connect("127.0.0.1", 7496, 0);
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
		// db.insertBatch(tickerList);
		db.close();

	}

	private void getMarketData(NewContract contract) {

		System.out.println(contract.localSymbol());

		m_controller.reqTopMktData(contract, "", false, new ITopMktDataHandler() {

			@Override
			public void tickPrice(NewTickType tickType, double price, int canAutoExecute) {

				if (price != 0.0) {
					// System.out.println(contract.localSymbol() + "\t" +
					// df.format(new Date())
					// + (tickType == NewTickType.BID ? "\t\t" : "\t\t\t") +
					// nf.format(price));

					// tickerList.add(new Ticker(contract.localSymbol(),
					// tickType == NewTickType.BID, price));

					for (Trio trio : trios) {
						for (int n = 0; n < 3; n++) {
							if (contract.localSymbol().equals(trio.getSymbol()[n]))
								trio.prc[n] = price;
						}

//						if (++trio.cnt > 50) {
							if (trio.prc[0] != 0 && trio.prc[1] != 0 && trio.prc[2] != 0) {

								trio.calcSpread();
								trio.addTickToChart();

							}
//						}

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
