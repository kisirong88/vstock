/*
 * JStock - Free Stock Market Software
 * Copyright (C) 2012 Yan Cheng CHEOK <yccheok@yahoo.com>
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.yccheok.jstock.gui.MainFrame;

/**
 *
 * @author yccheok
 */
public class VietStockServer implements StockServer {
    protected String getVietStockBasedURL() {
        return "http://finance.vietstock.vn/AjaxData/TradingResult/GetStockData.ashx?scode=";
    }
    protected String getVietMarketIndexBasedURL() {
        return "http://ptkt.vietstock.vn/GetMarketInfoData.ashx?catid=";
    }

    public VietStockServer() {
    }
    
    private boolean isToleranceAllowed(int currSize, int expectedSize) {
        if (currSize >= expectedSize) {
            return true;
        }
        if (expectedSize <= 0) {
            return true;
        }
        //double result = 100.0 - ((double)(expectedSize - currSize) / (double)expectedSize * 100.0);
        //return (result >= STABILITY_RATE);
        return currSize > 0;
    }

    @Override
    public List<Stock> getStocks(List<Code> codes) throws StockNotFoundException {
        return _getStocks(codes);
    }

    @Override
    public Stock getStock(Code code) throws StockNotFoundException {
        return _getStock(code);
    }

    private List<Stock> _getStocks(List<Code> codes) throws StockNotFoundException {
        List<Stock> stocks = new ArrayList<Stock>();

        //System.out.println("DEBUG _getStocks");
        if (codes.isEmpty()) {
            return stocks;
        }
        //System.out.println("DEBUG _getStocks2");

        /*final int time = codes.size() / MAX_STOCK_PER_ITERATION;
        final int remainder = codes.size() % MAX_STOCK_PER_ITERATION;

        for (int i = 0; i < time; i++) {
            final int start = i * MAX_STOCK_PER_ITERATION;
            final int end = start + MAX_STOCK_PER_ITERATION;
            final int endLoop = end - 1;

            final List<Code> expectedCodes = new ArrayList<Code>(codes.subList(start, end));

            for (int retry = 0; retry < NUM_OF_RETRY; retry++) {
                boolean success = true;
                for (int j = 0; j < expectedCodes.size(); j++) {
                    try {
                        System.out.println("DEBUG1 code "+expectedCodes.get(j).toString());
                        Stock stock = _getStock(expectedCodes.get(j));
                        if (stock != null) {
                          stocks.add(stock);
                          System.out.println("DEBUG1 remove code "+expectedCodes.get(j).toString());
                          expectedCodes.remove(j);
                        } else {
                          success = false;
                        }
                    } catch (StockNotFoundException ex) {
                        success = false;
                        throw new StockNotFoundException(null, ex);
                    }
                }
                if(success == true)
                  break;
            }
            if (expectedCodes.size() > 0) {
                throw new StockNotFoundException("Stock size (" + stocks.size() + ") inconsistent with code size (" + codes.size() + ")");
            }
        }

        final int start = codes.size() - remainder;
        final int end = start + remainder;
        final int endLoop = end - 1;

        final List<Code> expectedCodes = new ArrayList<Code>(codes.subList(start, end));
        
        System.out.println("DEBUG2 Codes "+codes);
        System.out.println("DEBUG2 expectedCodes "+expectedCodes);
        for (int retry = 0; retry < NUM_OF_RETRY; retry++) {
            boolean success = true;
            for (int j = 0; j < expectedCodes.size(); j++) {
                try {
                    System.out.println("DEBUG2 code "+expectedCodes.get(j).toString());
                    Stock stock = _getStock(expectedCodes.get(j));
                    //System.out.println("DEBUG CHECK StockCode "+stocks.get(0).code.toString()+", symbol "+stocks.get(0).symbol.toString()+", prevPrice "+stocks.get(0).getPrevPrice()+", highPrice "+stocks.get(0).getHighPrice());
                    if (stock != null) {
                      stocks.add(stock);
                      expectedCodes.remove(j);
                      System.out.println("DEBUG2 remove code "+expectedCodes.get(j).toString());
                    } else {
                      success = false;
                    }
                } catch (StockNotFoundException ex) {
                    success = false;
                    throw new StockNotFoundException("", ex);
                }
            }
            if(success == true)
              break;
        }*/

        final int start = 0;
        final int end = codes.size();
        final int endLoop = end - 1;

        final List<Code> expectedCodes = new ArrayList<Code>(codes.subList(start, end));
        
        //System.out.println("DEBUG2 Codes "+codes);
        //System.out.println("DEBUG2 expectedCodes "+expectedCodes);
        for (int j = 0; j < expectedCodes.size(); j++) {
            try {
                //System.out.println("DEBUG2 code "+expectedCodes.get(j).toString());
                Stock stock = _getStock(expectedCodes.get(j));
                //System.out.println("DEBUG CHECK StockCode "+stocks.get(0).code.toString()+", symbol "+stocks.get(0).symbol.toString()+", prevPrice "+stocks.get(0).getPrevPrice()+", highPrice "+stocks.get(0).getHighPrice());
                if (stock != null) {
                  stocks.add(stock);
                } else {
                }
            } catch (StockNotFoundException ex) {
                throw new StockNotFoundException("", ex);
            }
        }

        if (stocks.size() != codes.size()) {
            throw new StockNotFoundException("Stock size (" + stocks.size() + ") inconsistent with code size (" + codes.size() + ")");
        }

        return stocks;
    }

    private Stock _getStock(Code code) throws StockNotFoundException {
        String basedURL;
        if (code.toString().equals("^VNINDEX")) {
          basedURL = getVietMarketIndexBasedURL()+"1";
        } else if (code.toString().equals("^HNXINDEX")) {
          basedURL = getVietMarketIndexBasedURL()+"2";
        } else {
          basedURL = getVietStockBasedURL();
        }
        final StringBuilder stringBuilder = new StringBuilder(basedURL);

        final String _code;
        try {
            _code = java.net.URLEncoder.encode(code.toString(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new StockNotFoundException(code.toString(), ex);
        }
        //System.out.println("code "+code);

        if (!code.toString().equals("^VNINDEX") && !code.toString().equals("^HNXINDEX")) {
          final String language = MainFrame.getInstance().getJStockOptions().getLanguage();
          //System.out.println("language "+language);
          if ((language == null) || (language.equals("English"))) {
            stringBuilder.append(_code).append("&language=en");
          } else {
            org.yccheok.jstock.gui.Utils.getResponseBodyAsStringBasedOnProxyAuthOption("http://finance.vietstock.vn//SwitchLanguage.aspx?language=vi");
            stringBuilder.append(_code);
          }
        }

        final String location = stringBuilder.toString();
        //System.out.println("location "+location);

        for (int retry = 0; retry < NUM_OF_RETRY; retry++) {
            final String respond = org.yccheok.jstock.gui.Utils.getResponseBodyAsStringBasedOnProxyAuthOption(location);
            //System.out.println("DEBUG _getStock respond "+respond);
            if (respond == null) {
                continue;
            }
            final List<Stock> stocks = VietStockFormat.getInstance().parse(respond);
            //System.out.println("DEBUG CHECK StockCode "+stocks.get(0).code.toString()+", symbol "+stocks.get(0).symbol.toString()+", prevPrice "+stocks.get(0).getPrevPrice()+", highPrice "+stocks.get(0).getHighPrice());

            if (stocks.size() == 1) {
                return stocks.get(0);
            }

            break;
        }

        throw new StockNotFoundException(code.toString());
    }

    // Yahoo server limit is 200. We shorter, to avoid URL from being too long.
    // Yahoo sometimes does complain URL for being too long.
    private static final int MAX_STOCK_PER_ITERATION = 180;
    private static final int NUM_OF_RETRY = 2;
    
    // Update on 19 March 2009 : We cannot assume certain parameters will always
    // be float. They may become integer too. For example, in the case of Korea
    // Stock Market, Previous Close is in integer. We shall apply string quote
    // protection method too on them.
    //
    // Here are the index since 19 March 2009 :
    // (0) Symbol
    // (1) Name
    // (2) Stock Exchange
    // (3) Symbol
    // (4) Previous Close
    // (5) Symbol
    // (6) Open
    // (7) Symbol
    // (8) Last Trade
    // (9) Symbol
    // (10) Day's high
    // (11) Symbol
    // (12) Day's low
    // (13) Symbol
    // (14) Volume
    // (15) Symbol
    // (16) Change
    // (17) Symbol
    // (18) Change Percent
    // (19) Symbol
    // (20) Last Trade Size
    // (21) Symbol
    // (22) Bid
    // (23) Symbol
    // (24) Bid Size
    // (25) Symbol
    // (26) Ask
    // (27) Symbol
    // (28) Ask Size
    // (29) Symbol
    // (30) Last Trade Date
    // (31) Last Trade Time.
    //
    // s = Symbol
    // n = Name
    // x = Stock Exchange
    // o = Open             <-- Although we will keep this value in our stock data structure, we will not show
    //                          it to clients. As some stock servers unable to retrieve open price.
    // p = Previous Close
    // l1 = Last Trade (Price Only)
    // h = Day's high
    // g = Day's low
    // v = Volume           <-- We need to take special care on this, it may give us 1,234. This will
    //                          make us difficult to parse csv file. The only workaround is to make integer
    //                          in between two string literal (which will always contains "). By using regular
    //                          expression, we will manually remove the comma.
    // c1 = Change
    // p2 = Change Percent
    // k3 = Last Trade Size <-- We need to take special care on this, it may give us 1,234...
    // b3 = Bid (Real-time) <-- We use b = Bid previously. However, most stocks return 0.
    // b6 = Bid Size        <-- We need to take special care on this, it may give us 1,234...
    // b2 = Ask (Real-time) <-- We use a = Ask previously. However, most stocks return 0.
    // a5 = Ask Size        <-- We need to take special care on this, it may give us 1,234...
    // d1 = Last Trade Date
    // t1 = Last Trade Time
    //
    // c6k2c1p2c -> Change (Real-time), Change Percent (Real-time), Change, Change in Percent, Change & Percent Change
    // "+1400.00","N/A - +4.31%",+1400.00,"+4.31%","+1400.00 - +4.31%"
    //
    // "MAERSKB.CO","AP MOELLER-MAERS-","Copenhagen",32500.00,33700.00,34200.00,33400.00,660,"+1200.00","N/A - +3.69%",33,33500.00,54,33700.00,96,"11/10/2008","10:53am"
}
