package com.mattrudin;

import com.mattrudin.assets.Asset;
import com.mattrudin.service.FinanceService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        final FinanceService service = FinanceService.create();
        final List<Asset> prices = service.getPrice("ASHR", LocalDate.parse("2020-01-01"));
        //
        final List<String> symbols = new ArrayList<>(Arrays.asList("ASHR", "GLD", "NUGT"));
        final Map<String, List<Asset>> morePrices = service.getPrices(symbols, LocalDate.parse("2020-01-01"), LocalDate.parse("2020-03-30"));
    }
}
