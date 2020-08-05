package com.mattrudin.service;

import com.mattrudin.assets.Asset;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class FinanceService implements IFinanceService {
    private static final HttpClientContext clientContext = HttpClientContext.create();
    private static final String CRUMB_STORE = "CrumbStore";
    private static String httpCrumb = "";
    private static final String QUOTE_URI = "https://finance.yahoo.com/quote/%s/?p=%s";
    private static final String QUERY_URI = "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%s&period2=%s&interval=1d&events=history&crumb=%s";
    private final HttpClient httpClient = HttpClientBuilder.create().build();


    private FinanceService() {
        // static
    }

    public static FinanceService create() {
        final FinanceService service = new FinanceService();
        return service;
    }

    private static void initToken(final FinanceService service) {
        BasicCookieStore cookieStore = new BasicCookieStore();
        clientContext.setCookieStore(cookieStore);
        httpCrumb = service.getHttpCrumb("SPY");
        if (httpCrumb == null || httpCrumb.isEmpty()) {
            handleNotExistingToken();
        }
    }

    private static void handleNotExistingToken() {
        System.out.println("Can not connect to Data Server");
    }

    private String getHttpCrumb(final String symbol) {
        final String[] htmlSourceCode = getHtmlSourceCode(symbol).split("}");
        final String crumb = Arrays.stream(htmlSourceCode)//
                .filter(line -> line.contains(CRUMB_STORE))//
                .findFirst()//
                .orElse(EMPTY);
        if (StringUtils.isNotEmpty(crumb)) {
            return StringEscapeUtils.unescapeJava(crumb.split(":")[2].replace("\"", ""));
        }
        return EMPTY;
    }

    private String getHtmlSourceCode(final String symbol) {
        String result = null;
        final String symbolUri = String.format(QUOTE_URI, symbol, symbol);
        final HttpGet httpGet = new HttpGet(symbolUri);
        try {
            final HttpResponse httpResponse = httpClient.execute(httpGet, clientContext);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
            HttpClientUtils.closeQuietly(httpResponse);
        } catch (Exception var6) {
            System.out.println("Can not connect to Server");
        }
        return result;
    }

    @Override
    public List<Asset> getPrice(String symbolName, final LocalDate from, final LocalDate until) {
        Objects.requireNonNull(symbolName);
        Objects.requireNonNull(from);
        Objects.requireNonNull(until);
        return query(symbolName, from, until);
    }

    @Override
    public List<Asset> getPrice(String symbolName, LocalDate from) {
        Objects.requireNonNull(symbolName);
        Objects.requireNonNull(from);
        return query(symbolName, from, LocalDate.now());
    }

    @Override
    public Map<String, List<Asset>> getPrices(List<String> symbolNames, final LocalDate from, final LocalDate until) {
        Objects.requireNonNull(symbolNames);
        Objects.requireNonNull(from);
        Objects.requireNonNull(until);
        final Map<String, List<Asset>> symbolPrices = new HashMap<>();
        for (String symbolName : symbolNames) {
            if (symbolName != null) {
                final List<Asset> symbolPrice = query(symbolName, from, until);
                symbolPrices.put(symbolName, symbolPrice);
            }
        }
        return symbolPrices;
    }

    @Override
    public Map<String, List<Asset>> getPrices(List<String> symbolNames, LocalDate from) {
        Objects.requireNonNull(symbolNames);
        Objects.requireNonNull(from);
        return getPrices(symbolNames, from, LocalDate.now());
    }

    private List<Asset> query(final String symbolName, final LocalDate from, final LocalDate until) {
        final String query = String.format(QUERY_URI, symbolName, getSeconds(from), getSeconds(until), httpCrumb);
        final HttpGet httpGet = new HttpGet(query);
        final List<Asset> symbolPrices = new ArrayList<>();
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
                        final Asset asset = new Asset(line);
                        symbolPrices.add(asset);
                    }
                }
                bufferedReader.close();
            }
        } catch (IOException ex) {
            System.out.println("Error while writing: " + ex.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
        return symbolPrices;
    }

    private long getSeconds(final LocalDate date) {
        final LocalDateTime dateTime = date.atStartOfDay();
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }
}
