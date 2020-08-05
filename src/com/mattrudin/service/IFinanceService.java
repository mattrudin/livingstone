package com.mattrudin.service;

import com.mattrudin.assets.Asset;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IFinanceService {
    List<Asset> getPrice(final String symbolName, final Date from, final Date until);

    Map<String, List<Asset>> getPrices(final List<String> symbolNames, final Date from, final Date until);
}
