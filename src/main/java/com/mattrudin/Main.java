package com.mattrudin;

import com.mattrudin.assets.Asset;
import com.mattrudin.service.FinanceServiceFactory;
import com.mattrudin.service.IFinanceService;
import pl.zankowski.iextrading4j.api.stocks.Earnings;

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
    final Earnings earning = service.getEarnings("AAPL");
  }
}
