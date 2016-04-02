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

import au.com.bytecode.opencsv.CSVParser;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author yccheok
 */
public class VietStockFormat implements StockFormat {

    private VietStockFormat() {}

    // This function is used to resolve, random corrupted data returned from
    // Yahoo! server. Once a while, we will receive complain from users as in
    // http://sourceforge.net/projects/jstock/forums/forum/723855/topic/4611584
    // 1048576 is just a random picked number. I assume in this world, there
    // should be no stock's last price larger than 1048576.
    // Note that, this is a very hacking way, and not reliable at all!
    private boolean isCorruptedData(double price) {
        return price > 1048576 || price < 0;
    }

    // This function is used to resolve, random corrupted data returned from
    // Yahoo! server. Once a while, we will receive complain from users as in
    // http://sourceforge.net/projects/jstock/forums/forum/723855/topic/4647070
    // 13 days is just a random picked number. I assume a stock should not be
    // older than 13 days. If not, it is just too old.
    private static long now = 0;
    private boolean isTooOldTimestamp(long timestamp) {
        if (timestamp == 0) {
            return false;
        }
        
        // Ensure we have a correct "now" value.
        if (now == 0) {
            long localNow = org.yccheok.jstock.gui.Utils.getGoogleServerTimestamp();
            if (localNow != 0) {
                now = localNow;
            } else {
                now = System.currentTimeMillis();
            }
        }

        // If more than 13 days old stock, we consider it as corrupted stock.
        return (Utils.getDifferenceInDays(timestamp, now) > 13);
    }

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
    // b = Bid
    // b6 = Bid Size        <-- We need to take special care on this, it may give us 1,234...
    // a = Ask
    // a5 = Ask Size        <-- We need to take special care on this, it may give us 1,234...
    // d1 = Last Trade Date
    // t1 = Last Trade Time
    //
    // c6k2c1p2c -> Change (Real-time), Change Percent (Real-time), Change, Change in Percent, Change & Percent Change
    // "+1400.00","N/A - +4.31%",+1400.00,"+4.31%","+1400.00 - +4.31%"
    //
    // "MAERSKB.CO","AP MOELLER-MAERS-","Copenhagen",32500.00,33700.00,34200.00,33400.00,660,"+1200.00","N/A - +3.69%",33,33500.00,54,33700.00,96,"11/10/2008","10:53am"    
    @Override
    public List<Stock> parse(String source) {
        List<Stock> stocks = new ArrayList<Stock>();
        
        if (source == null) {
            return stocks;
        }                         
        
        final String[] strings = source.split("\r\n|\r|\n");
        for (String string : strings) {
          Code code = null;
          Symbol symbol = null;
          String name = null;
          //Stock.Board board = null;
          Stock.Board board = Stock.Board.HOSE;
          Stock.Industry industry = null;
          double prevPrice = 0.0;
          double openPrice = 0.0;
          double lastPrice = 0.0;    
          double highPrice = 0.0;  
          double lowPrice = 0.0;
          // TODO: CRITICAL LONG BUG REVISED NEEDED.
          long volume = 0;
          double changePrice = 0.0;
          double changePricePercentage = 0.0;
          int lastVolume = 0;    
          double buyPrice = 0.0;
          int buyQuantity = 0;    
          double sellPrice = 0.0;
          int sellQuantity = 0; 
          double secondBuyPrice = 0.0;
          int secondBuyQuantity = 0;
          double secondSellPrice = 0.0;
          int secondSellQuantity = 0;
          double thirdBuyPrice = 0.0;
          int thirdBuyQuantity = 0;
          double thirdSellPrice = 0.0;
          int thirdSellQuantity = 0;
          long timestamp = 0;

          //System.out.println("DEBUG string "+string);
          String substr = string;
          int length = substr.length();
          //System.out.println("DEBUG substr "+substr);
          //System.out.println("DEBUG substr(0) "+substr.substring(0,1)+", substr(end) "+substr.substring(length-1));
          //System.out.println("DEBUG substr(0) "+substr.substring(0,0)+", substr(end) "+substr.substring(length-1,length-1));
          if ((substr.substring(0,1).equals("[")) && (substr.substring(length-1).equals("]"))) {
            substr = substr.substring(1,length-1);
            //System.out.println("DEBUG new substr "+substr);
          }
          length = substr.length();
          //if ((substr.substring(0,1) == "{") && (substr.substring(length-1) == "}"))
          if ((substr.substring(0,1).equals("{")) && (substr.substring(length-1).equals("}"))) {
            substr = substr.substring(1,length-1);
          }
          //System.out.println("DEBUG substr "+substr);
          String[] fields = substr.split(",");
          for (String field : fields) {
            field = colonQuotes.matcher(field).replaceAll("\"\n");
            //System.out.println("DEBUG field "+field);
            String[] columns = field.split("\n");
            //System.out.println("DEBUG column[0] "+columns[0]+"; column[1]"+columns[1]);
            int size = columns.length;
            if(size < 2)
              continue;
            if(columns[0].equals("\"StockCode\"")) {
              //System.out.println("DEBUG StockCode "+columns[1].substring(1,columns[1].length()-2));
              code = Code.newInstance(columns[1].substring(1,columns[1].length()-1));
            } else if (columns[0].equals("\"CatID\"")) {
              int id = 0;
              id = Integer.parseInt(columns[1]);
              if(id == 1) {
                code = Code.newInstance("^VNINDEX");
                name = "VN Index";
                symbol = Symbol.newInstance(name.toString());
              } else if (id == 2) {
                code = Code.newInstance("^HNXINDEX");
                name = "HNX Index";
                symbol = Symbol.newInstance(name.toString());
              }
            }
            if(columns[0].equals("\"URL\"")) {
              //System.out.println("DEBUG StockCode "+columns[1].substring(1,columns[1].length()-2));
              //name = columns[1];
              name = columns[1].substring(1,columns[1].length()-1);
              //System.out.println("DEBUG name: "+name.substring(0,28));
              if(name.substring(0,28).equals("http://finance.vietstock.vn/"))
              {
                name = name.substring(28);
                Pattern codePattern = Pattern.compile(code.toString());
                name = codePattern.matcher(name).replaceAll("");
                name = minusPattern.matcher(name).replaceAll(" ");
                if(name.substring(0,1).equals(" ")) 
                  name = name.substring(1);
                if(name.substring(name.length()-4).equals(".htm")) 
                  name = name.substring(0,name.length()-4);
                name = WordUtils.capitalize(name);
                symbol = Symbol.newInstance(name.toString());
                //System.out.println("DEBUG name: "+name);
              }
            }
            if(columns[0].equals("\"PriorClosePrice\"")) //Previous close
              //prevPrice = Double.parseDouble(columns[1].substring(1,columns[1].length()-1));
              prevPrice = Double.parseDouble(columns[1]);
            if(columns[0].equals("\"Change\"")) //Change from Previous close 
              changePrice = Double.parseDouble(columns[1]);
            if(columns[0].equals("\"OpenPrice\"") || columns[0].equals("\"OpenIndex\"")) //Day's open
              //openPrice = Double.parseDouble(columns[1].substring(1,columns[1].length()-1));
              openPrice = Double.parseDouble(columns[1]);
            if(columns[0].equals("\"ClosePrice\"") || columns[0].equals("\"CloseIndex\"")) //Day's close = Last price
              //lastPrice = Double.parseDouble(columns[1].substring(1,columns[1].length()-1));
              lastPrice = Double.parseDouble(columns[1]);
            if(columns[0].equals("\"Highest\"")) //Day's high
              //highPrice = Double.parseDouble(columns[1].substring(1,columns[1].length()-1));
              highPrice = Double.parseDouble(columns[1]);
            if(columns[0].equals("\"Lowest\"")) //Day's Low
              //lowPrice = Double.parseDouble(columns[1].substring(1,columns[1].length()-1));
              lowPrice = Double.parseDouble(columns[1]);
            if(columns[0].equals("\"TradingVolume\"")) //Day's trading volume
              //volume = Long.parseLong(columns[1].substring(1,columns[1].length()-1));
              volume = Long.parseLong(columns[1]);
            if(columns[0].equals("\"Oscillate\"")) //Change ???
              //changePrice = Double.parseDouble(columns[1].substring(1,columns[1].length()-1));
              changePrice = Double.parseDouble(columns[1]);
            if(columns[0].equals("\"PercentOscillate\"")) //Change percent ???
              //changePricePercentage = Double.parseDouble(columns[1].substring(1,columns[1].length()-1));
              changePricePercentage = Double.parseDouble(columns[1]);
            //Dont have last volume
          }
          if (prevPrice == 0) {
            prevPrice = lastPrice - changePrice;
          }
          //System.out.println("DEBUG StockCode "+code.toString()+", prevPrice "+prevPrice+", highPrice "+highPrice);
          if (timestamp == 0) timestamp = System.currentTimeMillis();
          //System.out.println("DEBUG2 StockCode "+code.toString()+", prevPrice "+prevPrice+", highPrice "+highPrice);
          Stock stock = new Stock(
                  code,
                  symbol,
                  name,
                  board,
                  industry,
                  prevPrice,
                  openPrice,
                  lastPrice,
                  highPrice,
                  lowPrice,
                  volume,
                  changePrice,
                  changePricePercentage,
                  lastVolume,
                  buyPrice,
                  buyQuantity,
                  sellPrice,
                  sellQuantity,
                  secondBuyPrice,
                  secondBuyQuantity,
                  secondSellPrice,
                  secondSellQuantity,
                  thirdBuyPrice,
                  thirdBuyQuantity,
                  thirdSellPrice,
                  thirdSellQuantity,
                  timestamp                                        
                  );
          //System.out.println("DEBUG HAI StockCode "+stock.code.toString()+", symbol "+stock.symbol.toString()+", prevPrice "+stock.getPrevPrice()+", highPrice "+stock.getHighPrice());
          stocks.add(stock);            
        }                         
        //System.out.println("DEBUG CHECK StockCode "+stocks.get(0).code.toString()+", symbol "+stocks.get(0).symbol.toString()+", prevPrice "+stocks.get(0).getPrevPrice()+", highPrice "+stocks.get(0).getHighPrice());
        
        return stocks;
    }

    public static StockFormat getInstance() {
        return stockFormat;
    }
    
    private static final StockFormat stockFormat = new VietStockFormat();
    
    // ",123,456,"   -> ",123456,"
    // ","abc,def"," -> ","abcdef","
    // Please refer http://stackoverflow.com/questions/15692458/different-regular-expression-result-in-java-se-and-android-platform for more details.
    //
    // The idea is : If a comma doesn't have double quote on its left AND on its right, replace it with empty string.
    // http://www.regular-expressions.info/lookaround.html    
    private static final Pattern commaNotBetweenQuotes = Pattern.compile("(?<!\"),(?!\")");
    private static final Pattern colonQuotes = Pattern.compile("\":");
    private static final Pattern minusPattern = Pattern.compile("-");

    private static final Pattern quotePattern = Pattern.compile("\"");
    private static final Pattern percentagePattern = Pattern.compile("%");
    
    private static final Log log = LogFactory.getLog(VietStockFormat.class);
}
