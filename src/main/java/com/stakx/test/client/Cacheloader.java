package com.stakx.test.client;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.BookTicker;
import com.stakx.handler.DealHandler;
import com.stakx.test.DAL.SymbolDAL;
import com.stakx.text.model.SymbolDataModel;

@Component
public class Cacheloader implements CommandLineRunner {
	@Autowired
	SymbolDAL symbolDataDAL;
	@Autowired
	DealHandler dealHandler;

	@Override
	public void run(String... args) throws Exception {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("", "");
		BinanceApiRestClient client = factory.newRestClient();
		long serverTime = client.getServerTime();
		System.out.println(serverTime);
		BinanceApiWebSocketClient websocketclient = BinanceApiClientFactory.newInstance().newWebSocketClient();

		Thread t1 = new Thread() {
			public void run() {
				try {
					Thread.sleep(180000);
					dealHandler.buyselldecider();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t1.start();
		Thread t2 = new Thread() {
			public void run() {
				datacahcing(websocketclient, client);
			}
		};
		t2.start();

	}

	public void datacahcing(BinanceApiWebSocketClient websocketclient, BinanceApiRestClient client) {
		List<String> allsymbollist = client.getBookTickers().stream().map(BookTicker::getSymbol)
				.collect(Collectors.toList());
		System.out.println("total existing coin count : " + allsymbollist.size());
		allsymbollist.forEach(sym -> {
			System.out.println("current loading symbole name : " + sym);
			DepthCacheLoader depthCacheLoader = new DepthCacheLoader(sym);
			symbolDataDAL.savedata(new SymbolDataModel(sym, depthCacheLoader.startDepthEventStreaming(sym)));
			System.out.println("before initializing cache");
			depthCacheLoader.initializeDepthCache(sym);
			symbolDataDAL.savedata(new SymbolDataModel(sym, depthCacheLoader.getDepthCache()));

		});
	}
}
