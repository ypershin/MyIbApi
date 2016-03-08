package my.api;

import java.util.ArrayList;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewTickType;
import com.ib.controller.Types.MktDataType;
import com.ib.controller.Types.SecType;

public class GetTicker extends ConnectionHandlerAdapter implements ILogger {

	private final ApiController m_controller = new ApiController(this, this, this);
	private String[] m_symbol = { "SPY", "EEM", "AMZN", "AAPL", "MSFT", "FB", "TWTR" };
	private NewContract[] m_contract = new NewContract[m_symbol.length];
	// private double m_bid, m_ask;
	// private static final DateFormat df = new SimpleDateFormat("yyyy-mm-dd
	// HH:mm:ss.SSS");
	// private static final DecimalFormat nf = new DecimalFormat("#.00");
	private DbApi db = new DbApi();

	private final int BATCH_SIZE = 10;

	private ArrayList<Ticker> tickerList = new ArrayList<Ticker>();
	private long cnt = 0;

	public static void main(String[] args) {
		new GetTicker().run();
	}

	void run() {

		for (int i = 0; i < m_symbol.length; i++) {
			m_contract[i] = new NewContract();
			// m_contract[i].symbol(m_symbol[i]);
			m_contract[i].localSymbol(m_symbol[i]);
			m_contract[i].exchange("SMART");
			m_contract[i].secType(SecType.STK);
			m_contract[i].currency("USD");

		}

		m_controller.connect("127.0.0.1", 7496, 0);
	}

	@Override
	public void connected() {
		for (NewContract contract : m_contract) {
			getMarketData(contract);
		}

	}

	/*
	 * @Override public void disconnected() { System.out.println(
	 * "************ FINALLY *****************"); m_controller.disconnect();
	 * db.insertBatch(tickerList); db.close();
	 * 
	 * }
	 */

	private void getMarketData(NewContract contract) {

		m_controller.reqTopMktData(contract, "", false, new ITopMktDataHandler() {

			String symbol = contract.localSymbol();

			@Override
			public void tickPrice(NewTickType tickType, double price, int canAutoExecute) {

				print(symbol + "\ttickPrice\t" + price);

				if (price != 0.0) {
					// System.out.println(contract.localSymbol() + "\t" +
					// df.format(new
					// Date())
					// + (blnBid ? "\t\t" : "\t\t\t") + nf.format(price));

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
				print(symbol + "\ttickSize\t" + size);
			}

			@Override
			public void tickString(NewTickType tickType, String value) {
				print(symbol + "\ttickString\t" + value);
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

	void print(String str) {
		System.out.println(str);
	}

}
