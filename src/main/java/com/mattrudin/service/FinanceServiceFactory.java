package com.mattrudin.service;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class FinanceServiceFactory {
    private static final HttpClientContext clientContext = HttpClientContext.create();
    private static final String CRUMB_STORE = "CrumbStore";
    private static final String QUOTE_URI = "https://finance.yahoo.com/quote/%s/?p=%s";
    private static final String SYMBOL = "SPY";
    private static final HttpClient httpClient = HttpClientBuilder.create().build();
    private static final Log log = LogFactory.getLog(FinanceServiceFactory.class);

    private FinanceServiceFactory() {
        // static
    }

    public static IFinanceService create() {
        final String httpCrumb = getHttpCrumb();
        return new FinanceService(httpCrumb);
    }

    private static String getHttpCrumb() {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        clientContext.setCookieStore(cookieStore);
        final String httpCrumb = getHttpCrumbFromSource();
        if (httpCrumb == null || httpCrumb.isEmpty()) {
            handleNotExistingToken();
        }
        return httpCrumb;
    }

    private static void handleNotExistingToken() {
        log.error("Token does not exist.");
    }

    private static String getHttpCrumbFromSource() {
        final String[] htmlSourceCode = getHtmlSourceCode().split("}");
        final String crumb = Arrays.stream(htmlSourceCode)//
                .filter(line -> line.contains(CRUMB_STORE))//
                .findFirst()//
                .orElse(EMPTY);
        if (StringUtils.isNotEmpty(crumb)) {
            return StringEscapeUtils.unescapeJava(crumb.split(":")[2].replace("\"", ""));
        }
        return EMPTY;
    }

    private static String getHtmlSourceCode() {
        String result = null;
        final String symbolUri = String.format(QUOTE_URI, SYMBOL, SYMBOL);
        final HttpGet httpGet = new HttpGet(symbolUri);
        BufferedReader bufferedReader = null;
        try {
            final HttpResponse httpResponse = httpClient.execute(httpGet, clientContext);
            bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
            HttpClientUtils.closeQuietly(httpResponse);
        } catch (final Exception exception) {
            log.error("Could not connect to Server", exception);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException ioException) {
                    log.error("Could not close BufferedReader", ioException);
                }
            }
        }
        return result;
    }
}
