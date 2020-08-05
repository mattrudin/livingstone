package com.mattrudin.assets;

import java.util.Collections;
import java.util.List;

public class Asset {
  private final String symbolName;
  private final List<TradeDay> tradingDays;

  public Asset(String symbolName, List<TradeDay> tradingDays) {
    this.symbolName = symbolName;
    this.tradingDays = tradingDays;
  }

  public String getSymbolName() {
    return symbolName;
  }

  public List<TradeDay> getTradingDays() {
    return Collections.unmodifiableList(tradingDays);
  }
}
