package com.stakx.handler;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.market.AggTrade;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.TickerStatistics;
import com.stakx.test.DAL.OrderResponseDAL;
import com.stakx.test.DAL.SymbolDAL;
import com.stakx.test.wrapper.BTCWrapper;
import com.stakx.text.model.OrderResponseDataModel;
import com.stakx.text.model.SymbolDataModel;

@Component
public class DealHandler {
	@Autowired
	SymbolDAL symbolDAL;
	@Autowired
	OrderResponseDAL orderResponseDAL;
	@Autowired
	Environment env;
	BigDecimal lowestaskvalue = new BigDecimal(0);
	BigDecimal highestbidvalue = new BigDecimal(0);

	public void buyselldecider() throws InterruptedException {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("", "");
		BinanceApiRestClient client = factory.newRestClient();
		List<String> allsymbollist = client.getBookTickers().stream().map(BookTicker::getSymbol)
				.collect(Collectors.toList());
		allsymbollist.forEach(symbol -> {
			if (!symbol.equalsIgnoreCase("BTCUSDT")) {
				if (bitcoindeltachecker(client) && altcoindeltachecker(client, symbol)
						&& altcoinscheduler(client, symbol)) {
					BigDecimal avgprice = ((highestbidvalue.add(lowestaskvalue)).divide(new BigDecimal(2)));
					BigDecimal buyprice = avgprice
							.add((avgprice.multiply(new BigDecimal(.5))).divide(new BigDecimal(100)));
					Account account = client.getAccount();
					placebuyOrder(client, buyprice, symbol, account.getAssetBalance(symbol).getFree());
					try {
						afterbuysale(client, buyprice, symbol, account.getAssetBalance(symbol).getFree());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					afterbuystoploss(client, buyprice, symbol, account.getAssetBalance(symbol).getFree());
				}

			}

		});

	}

	private void afterbuystoploss(BinanceApiRestClient client, BigDecimal buyprice, String symbol, String quantity) {
		System.out.println("going for stop loss");
		BigDecimal stoplossprice = buyprice
				.subtract((buyprice.multiply(new BigDecimal(7))).divide(new BigDecimal(100)));
		NewOrder newOrder = new NewOrder(symbol, OrderSide.SELL, OrderType.STOP_LOSS_LIMIT, TimeInForce.GTC, quantity,
				stoplossprice.toString());
		NewOrderResponse newOrderResponse = client.newOrder(newOrder);
		orderResponseDAL.saveorder(new OrderResponseDataModel(newOrderResponse.getOrderId(), newOrderResponse));

	}

	public void afterbuysale(BinanceApiRestClient client, BigDecimal buyprice, String symbol, String quantity)
			throws InterruptedException {
		System.out.println("going for after buy sale");
		BigDecimal sellprice = buyprice.subtract((buyprice.multiply(new BigDecimal(5))).divide(new BigDecimal(100)));
		NewOrder newOrder = new NewOrder(symbol, OrderSide.SELL, OrderType.MARKET, null, quantity,
				sellprice.toString());
		NewOrderResponse newOrderResponse = client.newOrder(newOrder);
		orderResponseDAL.saveorder(new OrderResponseDataModel(newOrderResponse.getOrderId(), newOrderResponse));
		Thread.sleep(20000);
		BigDecimal sellafterprice = buyprice
				.subtract((buyprice.multiply(new BigDecimal(1))).divide(new BigDecimal(100)));
		NewOrder newOrderafter = new NewOrder(symbol, OrderSide.SELL, OrderType.MARKET, null, quantity,
				sellafterprice.toString());
		NewOrderResponse newOrderResponseafter = client.newOrder(newOrderafter);
		orderResponseDAL
				.saveorder(new OrderResponseDataModel(newOrderResponseafter.getOrderId(), newOrderResponseafter));
	}

	public void placebuyOrder(BinanceApiRestClient client, BigDecimal buyprice, String symbol, String quantity) {
		NewOrder newOrder = new NewOrder(symbol, OrderSide.BUY, OrderType.MARKET, null, "10", null);
		NewOrderResponse newOrderResponse = client.newOrder(newOrder);
		orderResponseDAL.saveorder(new OrderResponseDataModel(newOrderResponse.getOrderId(), newOrderResponse));
		System.out.println("going for buying");
	}

	public boolean altcoinscheduler(BinanceApiRestClient client, String symbol) {
		SymbolDataModel symbolData = symbolDAL.fetchdata(symbol);
		if (symbolData.getDepthCache().get("BIDS") == null || symbolData.getDepthCache().get("BIDS").isEmpty()
				|| symbolData.getDepthCache().get("ASKS") == null || symbolData.getDepthCache().get("ASKS").isEmpty()) {
			return false;
		}
		int stoppingtime = 10;
		boolean flag = false;
		long starttime = System.currentTimeMillis();
		do {
			if (altcoinbidconditionchecker(client, symbolData).isFlag()
					&& altcoinaskconditionchecker(client, symbolData).isFlag()) {
				if (altcoinbidconditionchecker(client, symbolData).getBTCVolume().compareTo(
						altcoinaskconditionchecker(client, symbolData).getBTCVolume().multiply(new BigDecimal(4))) == 0
						|| altcoinbidconditionchecker(client, symbolData).getBTCVolume()
								.compareTo(altcoinaskconditionchecker(client, symbolData).getBTCVolume()
										.multiply(new BigDecimal(4))) == 1) {
					flag = true;
				} else {
					flag = false;
				}
			} else {
				flag = false;
			}
		} while (System.currentTimeMillis() - starttime > 10000);
		return flag;
	}

	private BTCWrapper altcoinaskconditionchecker(BinanceApiRestClient client, SymbolDataModel symbolData) {
		lowestaskvalue = symbolData.getDepthCache().get("ASKS").firstEntry().getKey();
		BigDecimal effectivelowestaskvalue = ((symbolData.getDepthCache().get("ASKS").firstEntry().getKey()
				.multiply(new BigDecimal(103))).divide(new BigDecimal(100)));
		BigDecimal btcvolume = new BigDecimal(0);
		for (Entry entry : symbolData.getDepthCache().get("ASKS").entrySet()) {
			BigDecimal price = (BigDecimal) entry.getKey();
			BigDecimal value = (BigDecimal) entry.getValue();
			if (price.compareTo(effectivelowestaskvalue) == 0 || price.compareTo(effectivelowestaskvalue) == -1) {
				btcvolume = btcvolume.add(price.multiply(value));
			}
		}
		System.out.println("askcnditionbtcvolume" + symbolData.getDepthCache().get("ASKS") + btcvolume);
		if (btcvolume.compareTo(new BigDecimal(3)) == 0 || btcvolume.compareTo(new BigDecimal(3)) == 1) {
			return new BTCWrapper(true, btcvolume);
		}
		return new BTCWrapper(false, btcvolume);

	}

	private BTCWrapper altcoinbidconditionchecker(BinanceApiRestClient client, SymbolDataModel symbolData) {
		System.out.println("inaltcoinconditionchecker" + symbolData.getSymbol());
		highestbidvalue = symbolData.getDepthCache().get("BIDS").lastEntry().getKey();
		BigDecimal effectivefilterbidvalue = ((symbolData.getDepthCache().get("BIDS").lastEntry().getKey()
				.multiply(new BigDecimal(97))).divide(new BigDecimal(100)));
		BigDecimal btcvolume = new BigDecimal(0);
		for (Entry entry : symbolData.getDepthCache().get("BIDS").entrySet()) {
			BigDecimal price = (BigDecimal) entry.getKey();
			BigDecimal value = (BigDecimal) entry.getValue();
			if (price.compareTo(effectivefilterbidvalue) == 0 || price.compareTo(effectivefilterbidvalue) == 1) {
				btcvolume = btcvolume.add(price.multiply(value));
			}
		}
		System.out.println("bid condition btc volume" + btcvolume);
		if (btcvolume.compareTo(new BigDecimal(3)) == 0 || btcvolume.compareTo(new BigDecimal(3)) == 1) {
			return new BTCWrapper(true, btcvolume);
		}
		return new BTCWrapper(false, btcvolume);

	}

	public boolean bitcoindeltachecker(BinanceApiRestClient client) {
		Double bitcoinchangepercent = Double
				.parseDouble(client.get24HrPriceStatistics("BTCUSDT").getPriceChangePercent());
		System.out.println("bitcoindeltachangeinlast24 hours" + bitcoinchangepercent);
		if (bitcoinchangepercent > 3 || bitcoinchangepercent < -3) {
			return true;

		}

		return false;

	}

	public boolean altcoindeltachecker(BinanceApiRestClient client, String symbol) {
		List<AggTrade> aggTrades = client.getAggTrades(symbol);
		List<AggTrade> lasttenmintrades = aggTrades.stream()
				.filter(aggtrd -> aggtrd.getTradeTime() > System.currentTimeMillis() - 600000)
				.collect(Collectors.toList());
		if (lasttenmintrades.isEmpty() || lasttenmintrades.size() == 1) {
			return true;
		} else {
			Double tenminoldprize = Double.parseDouble(lasttenmintrades.get(0).getPrice());
			TickerStatistics tickerStatistics = client.get24HrPriceStatistics(symbol);
			Double latestprize = Double.parseDouble(tickerStatistics.getLastPrice());
			Double lasttenminpercentchange = ((tenminoldprize - latestprize) / latestprize) * 100;
			System.out.println("last ten min altcoin percent change" + lasttenminpercentchange);
			if (lasttenminpercentchange > 1.5 || lasttenminpercentchange < -1.5) {
				return true;
			}
		}
		return false;
	}

}
