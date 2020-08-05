package com.mattrudin.service;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class FinanceServiceFactory {
    private static final HttpClientContext clientContext = HttpClientContext.create();
    private static final String CRUMB_STORE = "CrumbStore";
    private static final String QUOTE_URI = "https://finance.yahoo.com/quote/%s/?p=%s";
    private static final HttpClient httpClient = HttpClientBuilder.create().build();

    private FinanceServiceFactory() {
        // static
    }

    public static IFinanceService create() {
        final FinanceService service = new FinanceService();
        initToken(service);
        return service;
    }

    private static void initToken(final FinanceService service) {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        clientContext.setCookieStore(cookieStore);
        final String httpCrumb = getHttpCrumb("SPY");
        if (httpCrumb == null || httpCrumb.isEmpty()) {
            handleNotExistingToken();
        } else {
            service.setHttpCrumb(httpCrumb);
        }
    }

    private static void handleNotExistingToken() {
        System.out.println("Can not connect to Data Server");
    }

    private static String getHttpCrumb(final String symbol) {
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

    private static String getHtmlSourceCode(final String symbol) {
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
}
