/*
 * JStock - Free Stock Market Software
 * Copyright (C) 2011 Yan Cheng CHEOK <yccheok@yahoo.com>
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author yccheok
 */
public class VietStockServerFactory implements StockServerFactory {

    private VietStockServerFactory() {
        dividendServer = new YahooDividendServer();
    }
    
    public static StockServerFactory newInstance() {
        return new VietStockServerFactory();
    }

    @Override
    public StockServer getStockServer() {
        //System.out.println("DEBUG getVietStockServer");
        return stockServer;
    }

    @Override
    public StockHistoryServer getStockHistoryServer(Code code) {
        try {
            return new VietStockHistoryServer(code);
        }
        catch (StockHistoryNotFoundException exp) {
            log.error(null, exp);
            return null;
        }
    }

    @Override
    public StockHistoryServer getStockHistoryServer(Code code,org.yccheok.jstock.engine.Duration duration) {
        try {
            return new VietStockHistoryServer(code, duration);
        }
        catch (StockHistoryNotFoundException exp) {
            log.error(null, exp);
            return null;
        }
    }
    
    @Override
    public MarketServer getMarketServer() {
        return marketServer;
    }
    
    @Override
    public DividendServer getDividendServer() {
        return dividendServer;
    }
    
    private final StockServer stockServer = new VietStockServer();
    private final MarketServer marketServer = new VietMarketServer();
    private final DividendServer dividendServer;
    
    private static final Log log = LogFactory.getLog(VietStockServerFactory.class);
}
