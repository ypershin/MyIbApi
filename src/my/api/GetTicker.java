package my.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ib.controller.AccountSummaryTag;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IAccountSummaryHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewTickType;
import com.ib.controller.Types.MktDataType;
import com.ib.controller.Types.SecType;

public class GetTicker extends ConnectionHandlerAdapter implements ILogger, Runnable {

	private final ApiController m_controller = new ApiController(this, this, this);

	private static String[] m_symbol = { "EEM", "XIV" };
	// private String[] m_symbol = new String[60];
	private NewContract[] m_contract = null;
	// private double m_bid, m_ask;
	private static final DateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
	private static final DecimalFormat nf = new DecimalFormat("#.00");
	private DbApi db = new DbApi();

	private final int BATCH_SIZE = 50;

	private ArrayList<Ticker> tickerList = new ArrayList<Ticker>();
	private long cnt = 0;

	private static String[] tags = { "NetLiquidation", "AvailableFunds", "MaintMarginReq" };
	private static Chart chart = new Chart(tags);

	public static void main(String[] args) {
		chart.activate();

		new GetTicker().run();
	}

	@Override
	public void run() {

		// m_symbol =
		// getSymbols("C:\\Users\\Ypershin\\Documents\\TSX60_components.csv");
		// m_contract = new NewContract[m_symbol.length];
		//
		// // System.exit(0);
		//
		// for (int i = 0; i < m_symbol.length; i++) {
		// // System.out.println(m_symbol[i]);
		// m_contract[i] = new NewContract();
		// // m_contract[i].symbol(m_symbol[i]);
		// m_contract[i].localSymbol(m_symbol[i]);
		// m_contract[i].exchange("SMART");
		// m_contract[i].secType(SecType.STK);
		// m_contract[i].currency("USD");
		//
		// }

		m_controller.connect("127.0.0.1", 7496, 0);

	}

	@Override
	public void connected() {

		// }

		// for (NewContract contract : m_contract) {
		// getMarketData(contract);
		// }

		getAccountSummary();

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
				System.out.println(tag + "\t" + value + "\t" + df.format(new Date()));
				switch (tag) {
				case NetLiquidation:
					chart.addData(0, Double.parseDouble(value));
					break;
				case AvailableFunds:
					chart.addData(1, Double.parseDouble(value));
					break;
				case MaintMarginReq:
					chart.addData(2, Double.parseDouble(value));
					break;
				default:
					break;
				}

				// try {
				// Thread.sleep(1000);
				// } catch (InterruptedException e) {
				// }

			}
		});

	}

	private void getMarketData(NewContract contract) {

		System.out.println(contract.localSymbol());

		m_controller.reqTopMktData(contract, "", false, new ITopMktDataHandler() {

			@Override
			public void tickPrice(NewTickType tickType, double price, int canAutoExecute) {

				if (price != 0.0) {
					System.out.println(contract.localSymbol() + "\t" + df.format(new Date())
							+ (tickType == NewTickType.BID ? "\t\t" : "\t\t\t") + nf.format(price));

					tickerList.add(new Ticker(contract.localSymbol(), tickType == NewTickType.BID, price));

					if (++cnt % BATCH_SIZE == 0) {
						db.insertBatch(tickerList);
						tickerList = new ArrayList<Ticker>();
					}

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
