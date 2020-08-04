package com.mattrudin;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IFinanceService {
    List<String> getPrice(final String symbolName, final Date from, final Date until);

    Map<String, List<String>> getPrices(final List<String> symbolNames, final Date from, final Date until);
}
