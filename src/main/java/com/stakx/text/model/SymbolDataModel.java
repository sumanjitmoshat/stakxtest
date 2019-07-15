package com.stakx.text.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document(collection = "symboldata")
public class SymbolDataModel {

	@Id
	private String symbol;

	private Map<String, NavigableMap<BigDecimal, BigDecimal>> depthCache;

	public SymbolDataModel() {
		super();
	}

	public SymbolDataModel(String symbol, Map<String, NavigableMap<BigDecimal, BigDecimal>> depthCache) {
		super();
		this.symbol = symbol;
		this.depthCache = depthCache;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Map<String, NavigableMap<BigDecimal, BigDecimal>> getDepthCache() {
		return depthCache;
	}

	public void setDepthCache(Map<String, NavigableMap<BigDecimal, BigDecimal>> depthCache) {
		this.depthCache = depthCache;
	}

}
