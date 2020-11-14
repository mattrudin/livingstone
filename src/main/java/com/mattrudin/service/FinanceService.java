package com.mattrudin.service;

import com.mattrudin.assets.Asset;
import com.mattrudin.assets.TradeDay;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import pl.zankowski.iextrading4j.api.stocks.Earning;
import pl.zankowski.iextrading4j.api.stocks.Earnings;
import pl.zankowski.iextrading4j.client.IEXCloudClient;
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder;
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.request.stocks.EarningsRequestBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FinanceService implements IFinanceService {
  private final String httpCrumb;
  private static final String QUERY_URI =
      "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%s&period2=%s&interval=1d&events=history&crumb=%s";
  private static final String PUBLIC_KEY = "pk_36c2d705c37a4381a9caad94fb74f8f5";
  private final HttpClient httpClient = HttpClientBuilder.create().build();
  private static final Log log = LogFactory.getLog(FinanceService.class);
  private final LocalDate tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS);
  private final IEXCloudClient cloudClient;

  public FinanceService(final String httpCrumb) {
    this.httpCrumb = httpCrumb;
    cloudClient =
        IEXTradingClient.create(
            IEXTradingApiVersion.IEX_CLOUD_V1,
            new IEXCloudTokenBuilder().withPublishableToken(PUBLIC_KEY).build());
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
    return getPrice(symbolName, from, tomorrow);
  }

  @Override
  public List<Asset> getPrices(
      List<String> symbolNames, final LocalDate from, final LocalDate until) {
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
    return getPrices(symbolNames, from, tomorrow);
  }

  @Override
  public Earnings getEarnings(String symbolName) {
    Objects.requireNonNull(symbolName);
    return cloudClient.executeRequest(new EarningsRequestBuilder().withSymbol(symbolName).build());
  }

  @Override
  public LocalDate getLastEarningDate(String symbolName) {
    final Earnings asset = getEarnings(symbolName);
    final List<Earning> earnings = asset.getEarnings();
    // The free IEX Account offers only the last earning date, hence we have only one Earning-Object
    return earnings.get(0).getEPSReportDate();
  }

  private List<TradeDay> query(
      final String symbolName, final LocalDate from, final LocalDate until) {
    final String query =
        String.format(QUERY_URI, symbolName, getSeconds(from), getSeconds(until), httpCrumb);
    final HttpGet httpGet = new HttpGet(query);
    final List<TradeDay> tradingDays = new ArrayList<>();
    HttpResponse httpResponse = null;
    BufferedReader bufferedReader = null;
    log.info(String.format("Getting symbol %s", symbolName));
    try {
      httpResponse = httpClient.execute(httpGet);
      String line;
      // Writes each daily asset price
      if (httpResponse.getEntity() != null) {
        final HttpEntity httpEntity = httpResponse.getEntity();
        bufferedReader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
        while ((line = bufferedReader.readLine()) != null) {
          if (isValid(line)) {
            final TradeDay tradeDay = new TradeDay(line);
            tradingDays.add(tradeDay);
          }
        }
      }
    } catch (IOException ioException) {
      log.error("Error while writing", ioException);
    } finally {
      HttpClientUtils.closeQuietly(httpResponse);
      try {
        if (bufferedReader != null) {
          bufferedReader.close();
        }
      } catch (IOException ioException) {
        log.error("Could not close BufferedReader", ioException);
      }
    }
    return tradingDays;
  }

  private boolean isValid(final String line) {
    return !line.contains("Date")
        && !line.contains("N/A")
        && !line.contains("null")
        && !line.contains("404 Not Found");
  }

  private long getSeconds(final LocalDate date) {
    final LocalDateTime dateTime = date.atStartOfDay();
    return dateTime.toEpochSecond(ZoneOffset.UTC);
  }
}
