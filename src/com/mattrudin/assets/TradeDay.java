package com.mattrudin.assets;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TradeDay {
    private final LocalDate date;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal last;
    private final BigDecimal volume;
    private static final int DATE_INDEX = 0;
    private static final int OPEN_INDEX = 1;
    private static final int HIGH_INDEX = 2;
    private static final int LOW_INDEX = 3;
    private static final int LAST_INDEX = 4;
    private static final int MID_INDEX = 5;
    private static final int VOLUME_INDEX = 6;

    /**
     * Will build a new TradeDay with the given values.
     *
     * @param date
     * @param open
     * @param last
     * @param high
     * @param low
     * @param volume
     */
    public TradeDay(LocalDate date, BigDecimal open, BigDecimal last, BigDecimal high, BigDecimal low, BigDecimal volume) {
        this.date = date;
        this.open = open;
        this.last = last;
        this.high = high;
        this.low = low;
        this.volume = volume;
    }

    /**
     * Will convert a String to an TradeDay. The String must comply the following convention:
     * DATE, OPEN, HIGH, LOW, LAST, MID, VOLUME
     * Please not the delimiter ",".
     *
     * @param line
     */
    public TradeDay(final String line) {
        final String[] parts = line.split(",");
        assert parts.length == 7;
        this.date = toLocalDate(parts[DATE_INDEX]);
        this.open = toBigDecimal(parts[OPEN_INDEX]);
        this.high = toBigDecimal(parts[HIGH_INDEX]);
        this.low = toBigDecimal(parts[LOW_INDEX]);
        this.last = toBigDecimal(parts[LAST_INDEX]);
        this.volume = toBigDecimal(parts[VOLUME_INDEX]);
        new TradeDay(date, open, high, low, last, volume);
    }

    private LocalDate toLocalDate(final String text) {
        return LocalDate.parse(text.strip());
    }

    private BigDecimal toBigDecimal(final String text) {
        return new BigDecimal(text.strip());
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getLast() {
        return last;
    }

    public BigDecimal getVolume() {
        return volume;
    }
}
