package com.mattrudin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        final FinanceService service = FinanceService.create();
        final List<String> prices = service.getPrice("ASHR", null, null);
        //
        final List<String> symbols = new ArrayList<>(Arrays.asList("ASHR", "GLD", "NUGT"));
        final Map<String, List<String>> morePrices = service.getPrices(symbols, null, null);
    }
}
