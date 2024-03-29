package my.api;

import java.util.ArrayList;
import java.util.Vector;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.TagValue;
import com.ib.client.UnderComp;

public class GetHistData implements EWrapper {

	private EClientSocket m_client = new EClientSocket(this);

	private String[] m_symbol;

	ArrayList<HistRecord> histRecs = new ArrayList<HistRecord>();

	private DbApi db = new DbApi();

	public static void main(String[] args) {
		new GetHistData().run();
	}

	private void run() {
		// TODO Auto-generated method stub
		m_client.eConnect("127.0.0.1", 7496, 0);
		if (m_client.isConnected()) {
			// System.out.println("Connected to Tws server version " +
			// m_client.serverVersion() + " at "
			// + m_client.TwsConnectionTime());

//			m_symbol = new String[] { "XIV", "VXX", "EEM" };
			m_symbol = new String[] { "FB","AMZN" };

			// System.exit(0);

			int i = -1;
			while (++i < m_symbol.length) {
				// System.out.println(i);
				// System.out.println(m_symbol[i]);
				Contract contract = new Contract();
				// m_contract[i].symbol(m_symbol[i]);
				contract.m_symbol = m_symbol[i];
				contract.m_secType = "STK";
				contract.m_exchange = "SMART";
				// contract.m_primaryExch = "ISLAND";
				contract.m_currency = "USD";

				m_client.reqHistoricalData(i, contract, "20161202 18:00:00", "30 D", "1 min", "TRADES", 1, 1,
						new Vector<TagValue>());

			}

			// for (HistRecord r : histRecs) {
			// System.out.println(r.toString());
			// }

			// @SuppressWarnings("resource")
			// Scanner scanner = new Scanner(System.in);
			// int n = -1;
			// while (true) {
			// System.out.print("what request to cancel (0 to exit)? ");
			// n = scanner.nextInt();
			// if (n == 0)
			// break;
			// m_client.cancelHistoricalData(n);
			// }

		}
	}

	@Override
	public void error(Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(String str) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionClosed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta, double undPrice) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints,
			double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		// TODO Auto-generated method stub

	}

	@Override
	public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue,
			double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAccountTime(String timeStamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accountDownloadEnd(String accountName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void nextValidId(int orderId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contractDetailsEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execDetailsEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price,
			int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		// TODO Auto-generated method stub

	}

	@Override
	public void managedAccounts(String accountsList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		// TODO Auto-generated method stub

	}

	@Override
	public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume,
			int count, double WAP, boolean hasGaps) {
		// TODO Auto-generated method stub

		// String msg = EWrapperMsgGenerator.historicalData(reqId, date, open,
		// high, low, close, volume, count, WAP,
		// hasGaps);
		// System.out.println(msg);

		if (date.startsWith("finished")) {
			m_client.cancelHistoricalData(reqId);
			db.insertBatchHist(histRecs);
			histRecs = new ArrayList<HistRecord>();

			if (reqId == m_symbol.length - 1) {
				m_client.eDisconnect();
				System.out.println("disconnected");
			}

		} else {
			histRecs.add(new HistRecord(m_symbol[reqId], date, open, high, low, close, volume, count, WAP, hasGaps));
		}

	}

	@Override
	public void scannerParameters(String xml) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scannerDataEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume,
			double wap, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void currentTime(long time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fundamentalData(int reqId, String data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		// TODO Auto-generated method stub

	}

	@Override
	public void position(String account, Contract contract, int pos, double avgCost) {
		// TODO Auto-generated method stub

	}

	@Override
	public void positionEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accountSummaryEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void verifyMessageAPI(String apiData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void displayGroupList(int reqId, String groups) {
		// TODO Auto-generated method stub

	}

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
		// TODO Auto-generated method stub

	}

}
