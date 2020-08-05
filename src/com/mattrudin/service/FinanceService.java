package com.mattrudin.service;

import com.mattrudin.assets.Asset;
import com.mattrudin.assets.TradeDay;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FinanceService implements IFinanceService {
    private static String httpCrumb = "";
    private static final String QUERY_URI = "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%s&period2=%s&interval=1d&events=history&crumb=%s";
    private final HttpClient httpClient = HttpClientBuilder.create().build();

    public FinanceService() {
    }

    @Override
    public Asset getPrice(String symbolName, final LocalDate from, final LocalDate until) {
        Objects.requireNonNull(symbolName);
        Objects.requireNonNull(from);
        Objects.requireNonNull(until);
        final List<TradeDay> tradingDays = query(symbolName, from, until);
        return new Asset(symbolName, tradingDays);
    }

    @Override
    public Asset getPrice(String symbolName, LocalDate from) {
        Objects.requireNonNull(symbolName);
        Objects.requireNonNull(from);
        return getPrice(symbolName, from, LocalDate.now());
    }

    @Override
    public List<Asset> getPrices(List<String> symbolNames, final LocalDate from, final LocalDate until) {
        Objects.requireNonNull(symbolNames);
        Objects.requireNonNull(from);
        Objects.requireNonNull(until);
        final List<Asset> symbolPrices = new ArrayList<>();
        for (String symbolName : symbolNames) {
            if (symbolName != null) {
                final List<TradeDay> tradingDays = query(symbolName, from, until);
                final Asset asset = new Asset(symbolName, tradingDays);
                symbolPrices.add(asset);
            }
        }
        return symbolPrices;
    }

    @Override
    public List<Asset> getPrices(List<String> symbolNames, LocalDate from) {
        return getPrices(symbolNames, from, LocalDate.now());
    }

    private List<TradeDay> query(final String symbolName, final LocalDate from, final LocalDate until) {
        final String query = String.format(QUERY_URI, symbolName, getSeconds(from), getSeconds(until), httpCrumb);
        final HttpGet httpGet = new HttpGet(query);
        final List<TradeDay> tradingDays = new ArrayList<>();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            String line;
            // Writes each daily asset price
            if (httpResponse.getEntity() != null) {
                final HttpEntity httpEntity = httpResponse.getEntity();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.contains("Date") && !line.contains("N/A") && !line.contains("null")) {
                        final TradeDay tradeDay = new TradeDay(line);
                        tradingDays.add(tradeDay);
                    }
                }
                bufferedReader.close();
            }
        } catch (IOException ex) {
            System.out.println("Error while writing: " + ex.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
        return tradingDays;
    }

    private long getSeconds(final LocalDate date) {
        final LocalDateTime dateTime = date.atStartOfDay();
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    protected void setHttpCrumb(final String httpCrumb) {
        FinanceService.httpCrumb = httpCrumb;
    }
}
