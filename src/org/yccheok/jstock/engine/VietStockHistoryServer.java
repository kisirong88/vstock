/*
 * JStock - Free Stock Market Software
 * Copyright (C) 2009 Yan Cheng CHEOK <yccheok@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.yccheok.jstock.engine;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author yccheok
 */
public class VietStockHistoryServer implements StockHistoryServer {        
    // Use ThreadLocal to ensure thread safety.
    private static final ThreadLocal <SimpleDateFormat> simpleDateFormatThreadLocal = new ThreadLocal <SimpleDateFormat>() {
        @Override protected SimpleDateFormat initialValue() {
            return new java.text.SimpleDateFormat("yyyy-MM-dd");
        }
    };
    
    protected StockServer getStockServer() {
        // Don't return member variable, as NPE might occur. We do have case 
        // where constructor calls abstract method.
        // http://stackoverflow.com/questions/15327417/is-it-ok-to-call-abstract-method-from-constructor-in-java        
        return new VietStockServer();
    }
    
    public VietStockHistoryServer(Code code) throws StockHistoryNotFoundException
    {
        this(code, DEFAULT_HISTORY_DURATION);
    }

    public VietStockHistoryServer(Code code, Duration duration) throws StockHistoryNotFoundException
    {
        if (code == null || duration == null)
        {
            throw new IllegalArgumentException("Code or duration cannot be null");
        }

        this.code = code;
        this.duration = duration;
        try {
            buildHistory(this.code);
        }
        catch (java.lang.OutOfMemoryError exp) {
            // Thrown from method.getResponseBodyAsString
            log.error(null, exp);
            throw new StockHistoryNotFoundException("Out of memory", exp);
        }
    }

    private boolean parse(String respond)
    {
        historyDatabase.clear();
        timestamps.clear();

        long timestamp = 0;

        String[] stockDatas = respond.split("</item>");

        // There must be at least two lines : header information and history information.
        final int length = stockDatas.length;

        if (length <= 1) {
            return false;
        }

        Symbol symbol = Symbol.newInstance(code.toString());
        String name = symbol.toString();
        Stock.Board board = Stock.Board.Unknown;
        Stock.Industry industry = Stock.Industry.Unknown;

        try {
            Stock stock = getStockServer().getStock(code);
            symbol = stock.symbol;
            name = stock.getName();
            board = stock.getBoard();
            industry = stock.getIndustry();
        }
        catch (StockNotFoundException exp) {
            log.error(null, exp);
        }

        double previousClosePrice = Double.MAX_VALUE;

        //for (int i = length - 1; i > 0; i--)
        for (int i = 0; i < length; i++) //The order date of VietStock is backward to Yahoo
        {
            System.out.println("DEBUG stockDatas"+stockDatas[i]);
            // Use > instead of >=, to avoid header information (Date,Open,High,Low,Close,Volume,Adj Close)
            // Date,Open,High,Low,Close,Volume,Adj Close
            if (stockDatas[i].substring(0, 3).equals("<t>"))
              stockDatas[i] = stockDatas[i].substring(3);
            String[] fields = stockDatas[i].split("</([^>]+)>");
            if (fields.length < 8) {
                continue;
            }
            //Time stamp
            fields[0] = fields[0].substring(19); //Remove "<item><TradingDate>"
            //Total volume
            fields[1] = fields[1].substring(13); //Remove "<TotalVolume>"
            //Total value
            fields[2] = fields[2].substring(12); //Remove "<TotalValue>"
            //Highest
            fields[3] = fields[3].substring(9); //Remove "<Highest>"
            //Lowest
            fields[4] = fields[4].substring(8); //Remove "<Lowest>"
            //Price1
            fields[5] = fields[5].substring(8); //Remove "<Price1>"
            //Price2
            fields[6] = fields[6].substring(8); //Remove "<Price2>"
            //Price
            fields[7] = fields[7].substring(7); //Remove "<Price>"
            //for (int j = 0; j < fields.length; j++) {
            //  System.out.println("DEBUG field"+fields[j]);
            //}

            String[] date = fields[0].split(" ");
            String[] element = date[0].split("/");
            if (element[0].length() == 1)
              element[0] = "0"+element[0];
            if (element[1].length() == 1)
              element[1] = "0"+element[1];
            String day = element[2]+"-"+element[0]+"-"+element[1];
            //System.out.println("DEBUG day "+day);
            try {
                timestamp = simpleDateFormatThreadLocal.get().parse(day).getTime();
            } catch (ParseException ex) {
                log.error(null, ex);
                continue;
            }

            double prevPrice = 0.0;
            double openPrice = 0.0;
            double highPrice = 0.0;
            double lowPrice = 0.0;
            double closePrice = 0.0;
            // TODO: CRITICAL LONG BUG REVISED NEEDED.
            long volume = 0;
            //double adjustedClosePrice = 0.0;

            try {
                prevPrice = (previousClosePrice == Double.MAX_VALUE) ? 0 : previousClosePrice;
                openPrice = Double.parseDouble(fields[5]);
                highPrice = Double.parseDouble(fields[3]);
                lowPrice = Double.parseDouble(fields[4]);
                closePrice = Double.parseDouble(fields[7]);
                // TODO: CRITICAL LONG BUG REVISED NEEDED.
                volume = Long.parseLong(fields[1]);
                //adjustedClosePrice = Double.parseDouble(fields[6]);
            }
            catch (NumberFormatException exp) {
                log.error(null, exp);
            }

            double changePrice = (previousClosePrice == Double.MAX_VALUE) ? 0 : closePrice - previousClosePrice;
            double changePricePercentage = ((previousClosePrice == Double.MAX_VALUE) || (previousClosePrice == 0.0)) ? 0 : changePrice / previousClosePrice * 100.0;

            Stock stock = new Stock(
                    code,
                    symbol,
                    name,
                    board,
                    industry,
                    prevPrice,
                    openPrice,
                    closePrice, /* Last Price. */
                    highPrice,
                    lowPrice,
                    volume,
                    changePrice,
                    changePricePercentage,
                    0,
                    0.0,
                    0,
                    0.0,
                    0,
                    0.0,
                    0,
                    0.0,
                    0,
                    0.0,
                    0,
                    0.0,
                    0,
                    timestamp
                    );

            historyDatabase.put(timestamp, stock);
            timestamps.add(timestamp);
            previousClosePrice = closePrice;
        }

        return (historyDatabase.size() > 0);
    }

    private String getVietStockHistoryBasedURL() {
        return "http://finance.vietstock.vn/Controls/TradingResult/Matching_Hose_Result.aspx?scode=";
    }

    private void buildHistory(Code code) throws StockHistoryNotFoundException
    {
        final StringBuilder stringBuilder = new StringBuilder(getVietStockHistoryBasedURL());

        //Indicate stock code in request to server
        final String symbol;
        try {
            symbol = java.net.URLEncoder.encode(code.toString(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new StockHistoryNotFoundException("code.toString()=" + code.toString(), ex);
        }

        stringBuilder.append(symbol);

        //Indicate columns in xml file will be gotten from server
        final StringBuilder columnBuilder = new StringBuilder("&lcol=TKLGD%2CTGTGD%2CCN%2CTN%2CGD1%2CGD2%2CGD3%2C");
        stringBuilder.append(columnBuilder);
        final StringBuilder formatBuilder = new StringBuilder("&sort=Time&dir=desc&page=1&psize=0");
        stringBuilder.append(formatBuilder);

        //Indicate date in request to server
        final int endMonth = duration.getEndDate().getMonth()+1; //Hai fix bug, havent found root cause
        final int endDate = duration.getEndDate().getDate();
        final int endYear = duration.getEndDate().getYear()-2000;
        final int startMonth = duration.getStartDate().getMonth()+1;
        final int startDate = duration.getStartDate().getDate();
        final int startYear = duration.getStartDate().getYear()-2000;

        final StringBuilder startDateBuilder = new StringBuilder("&fdate=");
        System.out.println("DEBUG start date: "+startMonth+"/"+startDate+"/"+startYear);
        System.out.println("DEBUG end date: "+endMonth+"/"+endDate+"/"+endYear);
        startDateBuilder.append(startMonth).append("%2F").append(startDate).append("%2F").append(startYear);
        final StringBuilder endDateBuilder = new StringBuilder("&tdate=");
        endDateBuilder.append(endMonth).append("%2F").append(endDate).append("%2F").append(endYear);

        stringBuilder.append(startDateBuilder).append(endDateBuilder).append("&exp=xml");
        final String location = stringBuilder.toString();

        boolean success = false;

        for (int retry = 0; retry < NUM_OF_RETRY; retry++) {
            System.out.println("DEBUG location "+location);
            final String respond = org.yccheok.jstock.gui.Utils.getResponseBodyAsStringBasedOnProxyAuthOption(location);
            //System.out.println("DEBUG respond "+respond);

            if (respond == null) {
                continue;
            }

            success = parse(respond);

            if (success) {
                break;
            }
        }

        if (success == false) {
            throw new StockHistoryNotFoundException(code.toString());
        }
    }

    @Override
    public Stock getStock(long timestamp) {
        return historyDatabase.get(timestamp);
    }

    @Override
    public long getTimestamp(int index) {
        return timestamps.get(index);
    }

    @Override
    public int size() {
        return timestamps.size();
    }

    @Override
    public long getSharesIssued() {
        return 0;
    }

    @Override
    public long getMarketCapital() {
        return 0;
    }

    public Duration getDuration() {
        return duration;
    }

    // http://ichart.yahoo.com/table.csv?s=JAVA&d=10&e=14&f=2008&g=d&a=2&b=11&c=1987&ignore=.csv
    // d = end month (0-11)
    // e = end date
    // f = end year
    // g = daily?
    // a = start month (0-11)
    // b = start date
    // c = start year
    //
    // Date,Open,High,Low,Close,Volume,Adj Close
    // 2008-11-07,4.32,4.41,4.12,4.20,10882100,4.20
    // 2008-11-06,4.57,4.60,4.25,4.25,10717900,4.25
    // 2008-11-05,4.83,4.90,4.62,4.62,9250800,4.62

    private static final int NUM_OF_RETRY = 2;
    private static final Duration DEFAULT_HISTORY_DURATION =  Duration.getTodayDurationByYears(10);

    private final java.util.Map<Long, Stock> historyDatabase = new HashMap<Long, Stock>();
    private final java.util.List<Long> timestamps = new ArrayList<Long>();

    private final Code code;
    private final Duration duration;

    private static final Log log = LogFactory.getLog(VietStockHistoryServer.class);
}
