package com.mattrudin.service;

import com.mattrudin.assets.Asset;

import java.time.LocalDate;
import java.util.List;

public interface IFinanceService {
    /**
     * Will return an Asset on daily basis to the given symbol.
     * @param symbolName
     * @param from
     * @param until
     * @return
     */
    Asset getPrice(final String symbolName, final LocalDate from, final LocalDate until);

    /**
     * Will return an Asset on daily basis to the given symbol. Starting from the given date till today.
     * @param symbolName
     * @param from
     * @return
     */
    Asset getPrice(final String symbolName, final LocalDate from);

    /**
     * Will return a list of Assets on daily basis to the given symbols.
     * @param symbolNames
     * @param from
     * @param until
     * @return
     */
    List<Asset> getPrices(final List<String> symbolNames, final LocalDate from, final LocalDate until);

    /**
     * Will return a map of Assets on daily basis to the given symbols. Starting from the given date till today.
     * @param symbolNames
     * @param from
     * @return
     */
    List<Asset> getPrices(final List<String> symbolNames, final LocalDate from);
}
