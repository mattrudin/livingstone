package com.mattrudin;

import com.mattrudin.assets.Asset;
import com.mattrudin.assets.TradeDay;
import com.mattrudin.service.FinanceServiceFactory;
import com.mattrudin.service.IFinanceService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    final IFinanceService service = FinanceServiceFactory.create();
    //    final Asset price = service.getPrice("ASHR", LocalDate.parse("2020-11-12"));
    final List<String> symbols = new ArrayList<>(Arrays.asList("ASHR", "GLD", "NUGT"));
    final List<Asset> prices = service.getPrices(symbols, LocalDate.parse("2020-11-12"));

    for (final Asset price : prices) {
      final List<TradeDay> list = price.getTradingDays();
      final TradeDay yesterday = list.get(0);
      final TradeDay today = list.get(1);
      final BigDecimal gap = BigDecimal.valueOf(1.1);
      if (today.getOpen().compareTo(yesterday.getAdjustedClose().multiply(gap)) > 0) {
        System.out.printf("%s is a up gap candidate", price.getSymbolName());
      }
    }
  }
}
