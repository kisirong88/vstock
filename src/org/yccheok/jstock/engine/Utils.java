/*
 * JStock - Free Stock Market Software
 * Copyright (C) 2013 Yan Cheng CHEOK <yccheok@yahoo.com>
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

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.Gson;
import java.io.*;
/*import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;*/
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.util.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yccheok.jstock.engine.Stock.Board;
import org.yccheok.jstock.engine.Stock.Industry;
import org.yccheok.jstock.file.Statements;
import org.yccheok.jstock.gui.MainFrame;
import org.yccheok.jstock.gui.Pair;
import org.json.*;
import java.net.URL;

/**
 *
 * @author yccheok
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {
    }   


    /**
     * Generate the best online database result if possible so that it is
     * acceptable by JStock application.
     *
     * @param result result from online database
     * @return best result after rectified. null if result cannot be rectified
     */
    public static ResultType rectifyResult(ResultType result) {
        String symbolStr = result.symbol;
        String nameStr = result.name;
        if (symbolStr == null) {
            return null;
        }
        if (symbolStr.trim().isEmpty()) {
            return null;
        }
        symbolStr = symbolStr.trim().toUpperCase();
        if (nameStr == null) {
            // If name is not available, we will make it same as symbol.
            nameStr = symbolStr;
        }
        if (nameStr.trim().isEmpty()) {
            // If name is not available, we will make it same as symbol.
            nameStr = symbolStr;
        }
        nameStr = nameStr.trim();
        return result.deriveWithSymbol(symbolStr).deriveWithName(nameStr);
    }

    /**
     * Initialize HttpClient with information from system properties.
     *
     * @param httpClient HttpClient to be initialized
     */
    public static void setHttpClientProxyFromSystemProperties(HttpClient httpClient) {
        final String httpproxyHost = System.getProperties().getProperty("http.proxyHost");
        final String httpproxyPort = System.getProperties().getProperty("http.proxyPort");
        
        if (null == httpproxyHost || null == httpproxyPort) {
            HostConfiguration hostConfiguration = httpClient.getHostConfiguration();
            hostConfiguration.setProxyHost(null);
        }
        else {
            int port = -1;
            try {
                port = Integer.parseInt(httpproxyPort);
            }
            catch (NumberFormatException exp) {
            }     
            
            if (isValidPortNumber(port)) {
                HostConfiguration hostConfiguration = httpClient.getHostConfiguration();
                hostConfiguration.setProxy(httpproxyHost, port);                
            }
            else {
                HostConfiguration hostConfiguration = httpClient.getHostConfiguration();
                hostConfiguration.setProxyHost(null);
            }
        }
    } 
    
    // Refer to http://www.exampledepot.com/egs/java.util/CompDates.html
    public static long getDifferenceInDays(long timeInMillis0, long timeInMillis1) {
        long diffMillis = Math.abs(timeInMillis0 - timeInMillis1);
        // Get difference in days
        long diffDays = diffMillis/(24*60*60*1000);
        return diffDays;        
    }
    
    // Refer to http://www.exampledepot.com/egs/java.util/CompDates.html
    public static long getDifferenceInDays(Date date0, Date date1) {
        return getDifferenceInDays(date0.getTime(), date1.getTime());
    }

    // Refer to http://www.exampledepot.com/egs/java.util/CompDates.html
    public static long getDifferenceInDays(Calendar calendar0, Calendar calendar1) {
        return getDifferenceInDays(calendar0.getTimeInMillis(), calendar1.getTimeInMillis());
    }
    
    public static void resetCalendarTime(Calendar calendar) {
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int date = calendar.get(Calendar.DATE);
        calendar.set(year, month, date, 0, 0, 0);
        // Reset milli second as well.
        calendar.set(Calendar.MILLISECOND, 0);        
    }
    
    public static boolean isValidPortNumber(int portNumber) {
        return (portNumber >= 0) && (portNumber <= 65534);
    }
    
    public static boolean isValidPortNumber(String portNumber) {
        int port = -1;
        try {
            port = Integer.parseInt(portNumber);
        }
        catch(NumberFormatException exp) {
        }
        
        return isValidPortNumber(port);
    }
    
    public static File getStockInfoDatabaseFile(Country country) {
        return new File(org.yccheok.jstock.gui.Utils.getUserDataDirectory() + country + File.separator + "database" + File.separator + "stock-info-database.csv");
    }
    
    /**
     * Gets the CSV file, which will be used to construct 
     * {@code StockCodeAndSymbolDatabase} object.
     *
     * @param country The country of the stock market
     * @return Location of the stocks CSV file.
     */
    public static String getStocksCSVFileLocation(Country country) {
        // Must use lower case, as Google App Engine only support URL in lower
        // case.
        //System.out.println("DEBUG Country "+country.toString());
        if (country.toString() == "Vietnam") {
          return "http://priceboard.vietstock.vn/live-stock-quotes-hsx.aspx";
          //return "http://finance.vietstock.vn/GetCompanyList.ashx?language=en";
        } else {
          return org.yccheok.jstock.network.Utils.getJStockStaticServer() + "stocks_information/" + country.toString().toLowerCase() + "/" + "stocks.csv";
        }
    }

    /* Hai support to get Vietnam stocks */
    public static List<Stock> getVietStocks() {
      List<Stock> stocks = new ArrayList<Stock>();
      final Pattern colonQuotes = Pattern.compile(":\'");
      final Pattern quoteCommas = Pattern.compile("\',");
      final Pattern curlyCommas = Pattern.compile("\\},\\{");
      /*final Pattern openBrackets = Pattern.compile("[");
      final Pattern closeBrackets = Pattern.compile("]");*/
      final String language = MainFrame.getInstance().getJStockOptions().getLanguage();
      System.out.println("language "+language);
      String url = "";
      if ((language == null) || (language.equals("English"))) {
        url = "http://finance.vietstock.vn/GetCompanyList.ashx?language=en";
      } else {
        url = "http://finance.vietstock.vn/GetCompanyList.ashx";
      }
      final String location = url;
      System.out.println("location "+location);
      final String respond = org.yccheok.jstock.gui.Utils.getResponseBodyAsStringBasedOnProxyAuthOption(location);
      //System.out.println("respond "+respond);
      final String[] strings = respond.split("\\[|\\]");
      String string = strings[1];
      string = curlyCommas.matcher(string).replaceAll("}\n{");
      //System.out.println("string "+string);
      final String[] infos = string.split("\n");
      for (String info : infos) {
        //System.out.println("info(0) "+info.substring(0,1));
        if(info.substring(0,1).equals("{"))
          info = info.substring(1);
        if(info.substring(info.length()-1).equals("}"))
          info = info.substring(0,info.length()-1);
        //System.out.println("info "+info);
        info = quoteCommas.matcher(info).replaceAll("\'\n");
        String[] fields = info.split("\n");
        String code = "";
        String symbol = "";
        String name = "";
        Board board = Board.Unknown;
        Industry industry = Industry.Unknown;
        for (String field : fields) {
          //System.out.println("field "+field);
          field = colonQuotes.matcher(field).replaceAll("\n\'");
          String[] columns = field.split("\n");
          int size = columns.length;
          if(size < 2)
            continue;
          if(columns[0].equals("code")) {
            columns[1] = columns[1].substring(1,columns[1].length()-1);
            code = columns[1];
            //System.out.println("code "+code);
          } else if (columns[0].equals("name")) {
            columns[1] = columns[1].substring(1,columns[1].length()-1);
            symbol = columns[1];
            //System.out.println("symbol "+symbol);
          } else {
          }
        }
        final Stock stock = new Stock.Builder(Code.newInstance(code), Symbol.newInstance(symbol)).name(name).board(board).industry(industry).build();
        stocks.add(stock);
      }
      //System.out.println("DEBUG stocks size "+stocks.size());
      return stocks;
    }
    public static List<Stock> getVietStocksFromCSVFile(File file) {
        List<Stock> stocks = new ArrayList<Stock>();
        InputStream is = null;
        Reader reader = null;
        BufferedReader bufreader = null;
        String line = null;
        //System.out.println("DEBUG getVietStocksFromCSVFile");
        try {
            is = new FileInputStream(file);
            reader = new InputStreamReader(is, "UTF-8");
            bufreader = new BufferedReader(reader);
            boolean start_flag = false;
            while ((line = bufreader.readLine()) != null) {
              if (line.contains("div id=\"PriceDetail\"")) {
                start_flag = true;
                //System.out.println("DEBUG line "+line);
              }
              if (start_flag == true) {
                if (line.contains("tr id=")) {
                  //System.out.println("DEBUG line "+line);
                  int index = line.indexOf("Stock_");
                  index = index + 6;
                  int index2 = line.indexOf("\"", index);
                  String code = line.substring(index, index2);
                  //System.out.println("Get Code "+code);
                  //Go to field "Gia Ce"
                  line = bufreader.readLine();
                  line = bufreader.readLine();
                  //Go to field "Gia Fl"
                  line = bufreader.readLine();
                  line = bufreader.readLine();
                  line = bufreader.readLine();
                  //Go to field "Gia Pr"
                  line = bufreader.readLine();
                  line = bufreader.readLine();
                  line = bufreader.readLine();
                  //Go to field "title"
                  line = bufreader.readLine();
                  line = bufreader.readLine();
                  index = line.indexOf("'");
                  index2 = line.indexOf("'", index+1);
                  String symbol = line.substring(index+1, index2);
                  //System.out.println("Get Symbol "+symbol);
                  final String name = "";
                  final String _board = "Unknown";
                  final String _industry = "Unknown";
                  Board board;
                  Industry industry;
                  try {
                      board = Board.valueOf(_board);
                  } catch (IllegalArgumentException exp) {
                      log.error(null, exp);
                      board = Board.Unknown;
                  }
                  try {
                      industry = Industry.valueOf(_industry);
                  } catch (IllegalArgumentException exp) {
                      log.error(null, exp);
                      industry = Industry.Unknown;
                  }
                  final Stock stock = new Stock.Builder(Code.newInstance(code), Symbol.newInstance(symbol)).name(name).board(board).industry(industry).build();
                  stocks.add(stock);
                } else {
                }
              }
            }
        } catch (IOException ex) {
            log.error(null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    log.error(null, ex);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    log.error(null, ex);
                }
            }
            if (bufreader != null) {
                try {
                    bufreader.close();
                } catch (IOException ex) {
                    log.error(null, ex);
                }
            }
        }
        return stocks;
    }
    
    /**
     * One of the shortcomings of JStock is that, it is very difficult to get a
     * complete list of available stocks in a market. Most stock servers do not
     * provide information on complete list of available stocks. We can overcome
     * this, by reading the stock list information from a CSV file.
     *
     * @param file The CSV file
     * @return List of stocks carried by the CSV file.
     */
    public static List<Stock> getStocksFromCSVFile(File file) {
        List<Stock> stocks = new ArrayList<Stock>();
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        CSVReader csvreader = null;
        try {
            fileInputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(fileInputStream,  Charset.forName("UTF-8"));
            csvreader = new CSVReader(inputStreamReader);
            final String[] types = csvreader.readNext();
            if (types == null) {
                // Fail. Returns empty stock list.
                return stocks;
            }
            int code_index = -1;
            int symbol_index = -1;
            // Name, board and industry information is optional.
            int name_index = -1;            
            int board_index = -1;
            int industry_index = -1;
            
            boolean success_index = false;
            // Search for the indecies for code, symbol and name.
            for (int index = 0; index < types.length; index++) {
                final String type = types[index];
                if (0 == type.compareToIgnoreCase("code")) {
                    code_index = index;
                } else if (0 == type.compareToIgnoreCase("symbol")) {
                    symbol_index = index;
                } else if (0 == type.compareToIgnoreCase("name")) {
                    name_index = index;
                } else if (0 == type.compareToIgnoreCase("board")) {
                    board_index = index;
                } else if (0 == type.compareToIgnoreCase("industry")) {
                    industry_index = index;
                }

                if (code_index != -1 && symbol_index != -1 && name_index != -1 && board_index != -1 && industry_index != -1) {
                    // All found. Early quit.
                    break;
                }
            }

            // Ignore board_index, as it is optional.
            success_index = (code_index != -1 && symbol_index != -1);

            // Are we having all the indecies?
            if (false == success_index) {
                // Nope. Returns empty stock list.
                return stocks;
            }

            String [] nextLine;
            while ((nextLine = csvreader.readNext()) != null) {
                // Shall we continue to ignore, or shall we just return early to
                // flag an error?
                if (nextLine.length != types.length) {
                    // Give a warning message.
                    log.error("Incorrect CSV format. There should be exactly " + types.length + " item(s)");
                    continue;
                }
                final String code = nextLine[code_index];
                final String symbol = nextLine[symbol_index];
                final String name = name_index == -1 ? "" : nextLine[name_index];
                final String _board = board_index == -1 ? "Unknown" : nextLine[board_index];
                final String _industry = industry_index == -1 ? "Unknown" : nextLine[industry_index];
                Board board;
                Industry industry;
                try {
                    board = Board.valueOf(_board);
                } catch (IllegalArgumentException exp) {
                    log.error(null, exp);
                    board = Board.Unknown;
                }
                try {
                    industry = Industry.valueOf(_industry);
                } catch (IllegalArgumentException exp) {
                    log.error(null, exp);
                    industry = Industry.Unknown;
                }
                
                final Stock stock = new Stock.Builder(Code.newInstance(code), Symbol.newInstance(symbol)).name(name).board(board).industry(industry).build();
                stocks.add(stock);
            }
        } catch (IOException ex) {
            log.error(null, ex);
        } finally {
            if (csvreader != null) {
                try {
                    csvreader.close();
                } catch (IOException ex) {
                    log.error(null, ex);
                }
            }
            org.yccheok.jstock.gui.Utils.close(inputStreamReader);
            org.yccheok.jstock.gui.Utils.close(fileInputStream);
        }
        return stocks;
    }

    
    public static Pair<StockInfoDatabase, StockNameDatabase> toStockDatabase(List<Stock> stocks, Country country) {
        assert(false == stocks.isEmpty());
        
        // Let's make our database since we get a list of good stocks.
        //System.out.println("DEBUG CHECK1 stocks size "+stocks.size());
        StockInfoDatabase tmp_stock_info_database = new StockInfoDatabase(stocks);
        
        // StockNameDatabase is an optional item.
        //System.out.println("DEBUG CHECK2 stocks size "+stocks.size());
        StockNameDatabase tmp_name_database = null;
        if (org.yccheok.jstock.engine.Utils.isNameImmutable(country)) {
            tmp_name_database = new StockNameDatabase(stocks);
        }
        //System.out.println("DEBUG CHECK3 stocks size "+stocks.size());
        
        return Pair.create(tmp_stock_info_database, tmp_name_database);
    }
    
    public static boolean migrateXMLToCSVDatabases(String srcBaseDirectory, String destBaseDirectory) {
        boolean result = true;
        for (Country country : Country.values()) {
            final File userDefinedDatabaseXMLFile = new File(srcBaseDirectory + country + File.separator + "database" + File.separator + "user-defined-database.xml");
            final File userDefinedDatabaseCSVFile = new File(destBaseDirectory + country + File.separator + "database" + File.separator + "user-defined-database.csv");
            
            final java.util.List<Pair<Code, Symbol>> pairs = org.yccheok.jstock.gui.Utils.fromXML(java.util.List.class, userDefinedDatabaseXMLFile);            
            if (pairs != null && !pairs.isEmpty()) {
                final Statements statements = Statements.newInstanceFromUserDefinedDatabase(pairs);
                boolean r = statements.saveAsCSVFile(userDefinedDatabaseCSVFile);
                if (r) {
                    userDefinedDatabaseXMLFile.delete();
                }  
                result = r & result;
            } else {
                userDefinedDatabaseXMLFile.delete();
            }

            // Delete these old XML files. We can re-generate new CSV from database.zip.
            new File(srcBaseDirectory + country + File.separator + "database" + File.separator + "stock-name-database.xml").delete();
            new File(destBaseDirectory + country + File.separator + "database" + File.separator + "stock-info-database.xml").delete();
            new File(destBaseDirectory + country + File.separator + "database" + File.separator + "stockcodeandsymboldatabase.xml").delete();
        }
        return result;
    }
    
    private static final List<Index> australiaIndices = new ArrayList<Index>();
    private static final List<Index> austriaIndices = new ArrayList<Index>();
    private static final List<Index> belgiumIndices = new ArrayList<Index>();
    private static final List<Index> brazilIndices = new ArrayList<Index>();
    private static final List<Index> canadaIndices = new ArrayList<Index>();
    private static final List<Index> chinaIndices = new ArrayList<Index>();
    private static final List<Index> denmarkIndices = new ArrayList<Index>();
    private static final List<Index> franceIndices = new ArrayList<Index>();
    private static final List<Index> germanyIndices = new ArrayList<Index>();
    private static final List<Index> hongkongIndices = new ArrayList<Index>();
    private static final List<Index> indiaIndices = new ArrayList<Index>();
    private static final List<Index> indonesiaIndices = new ArrayList<Index>();
    private static final List<Index> israelIndices = new ArrayList<Index>();
    private static final List<Index> italyIndices = new ArrayList<Index>();
    private static final List<Index> koreaIndices = new ArrayList<Index>();
    private static final List<Index> malaysiaIndices = new ArrayList<Index>();
    private static final List<Index> netherlandsIndices = new ArrayList<Index>();
    private static final List<Index> newZealandIndices = new ArrayList<Index>();
    private static final List<Index> norwayIndices = new ArrayList<Index>();
    private static final List<Index> portugalIndices = new ArrayList<Index>();
    private static final List<Index> singaporeIndices = new ArrayList<Index>();
    private static final List<Index> spainIndices = new ArrayList<Index>();
    private static final List<Index> swedenIndices = new ArrayList<Index>();
    private static final List<Index> switzerlandIndices = new ArrayList<Index>();
    private static final List<Index> taiwanIndices = new ArrayList<Index>();
    private static final List<Index> unitedKingdomIndices = new ArrayList<Index>();
    private static final List<Index> unitedStateIndices = new ArrayList<Index>();
    private static final List<Index> vietnamIndices = new ArrayList<Index>();
    
    static
    {
        austriaIndices.add(Index.ATX);
        australiaIndices.add(Index.AORD);
        belgiumIndices.add(Index.BFX);
        brazilIndices.add(Index.BVSP);
        canadaIndices.add(Index.GSPTSE);
        chinaIndices.add(Index.SSEC);
        denmarkIndices.add(Index.OMXC20CO);
        franceIndices.add(Index.FCHI);  
        germanyIndices.add(Index.DAX);
        hongkongIndices.add(Index.HSI);
        indiaIndices.add(Index.BSESN);
        indiaIndices.add(Index.NSEI);
        indonesiaIndices.add(Index.JKSE);
        israelIndices.add(Index.TA100);
        italyIndices.add(Index.FTSEMIB);
        koreaIndices.add(Index.KS11);
        malaysiaIndices.add(Index.KLSE);
        netherlandsIndices.add(Index.AEX);
        newZealandIndices.add(Index.NZSX50);
        norwayIndices.add(Index.OSEAX);
        portugalIndices.add(Index.PSI20);
        singaporeIndices.add(Index.STI);
        spainIndices.add(Index.SMSI);
        swedenIndices.add(Index.OMX30);
        switzerlandIndices.add(Index.SSMI);
        taiwanIndices.add(Index.TWII);
        unitedKingdomIndices.add(Index.FTSE);
        unitedStateIndices.add(Index.DJI);        
        unitedStateIndices.add(Index.IXIC);        
        vietnamIndices.add(Index.VNINDEX); // Hai support to get HOSE and HNX index for Vietnam
        vietnamIndices.add(Index.HNXINDEX); // Hai support to get HOSE and HNX index for Vietnam
    }

    private static final List<String> australiaBoards = new ArrayList<String>();
    private static final List<String> austriaBoards = new ArrayList<String>();
    private static final List<String> belgiumBoards = new ArrayList<String>();
    private static final List<String> brazilBoards = new ArrayList<String>();
    private static final List<String> canadaBoards = new ArrayList<String>();
    private static final List<String> chinaBoards = new ArrayList<String>();
    private static final List<String> denmarkBoards = new ArrayList<String>();
    private static final List<String> franceBoards = new ArrayList<String>();
    private static final List<String> germanyBoards = new ArrayList<String>();
    private static final List<String> hongkongBoards = new ArrayList<String>();
    private static final List<String> indiaBoards = new ArrayList<String>();
    private static final List<String> indonesiaBoards = new ArrayList<String>();
    private static final List<String> israelBoards = new ArrayList<String>();
    private static final List<String> italyBoards = new ArrayList<String>();
    private static final List<String> koreaBoards = new ArrayList<String>();
    private static final List<String> malaysiaBoards = new ArrayList<String>();
    private static final List<String> netherlandsBoards = new ArrayList<String>();
    private static final List<String> newZealandBoards = new ArrayList<String>();
    private static final List<String> norwayBoards = new ArrayList<String>();
    private static final List<String> portugalBoards = new ArrayList<String>();
    private static final List<String> singaporeBoards = new ArrayList<String>();
    private static final List<String> spainBoards = new ArrayList<String>();
    private static final List<String> swedenBoards = new ArrayList<String>();
    private static final List<String> switzerlandBoards = new ArrayList<String>();
    private static final List<String> taiwanBoards = new ArrayList<String>();
    private static final List<String> unitedKingdomBoards = new ArrayList<String>();
    private static final List<String> unitedStateBoards = new ArrayList<String>();
    private static final List<String> vietnamBoards = new ArrayList<String>();
    
    static
    {
        austriaBoards.add("Vienna");
        australiaBoards.add("ASX");
        australiaBoards.add("Sydney");
        belgiumBoards.add("Brussels");
        brazilBoards.add("BM&F BOVESPA");
        canadaBoards.add("Toronto");
        chinaBoards.add("SSE");
        denmarkBoards.add("Copenhagen");
        franceBoards.add("Paris");  
        germanyBoards.add("Xetra");
        germanyBoards.add("XETRA");
        germanyBoards.add("Munich");
        germanyBoards.add("Stuttgart");
        germanyBoards.add("Berlin");
        germanyBoards.add("Hamburg");
        germanyBoards.add("Dusseldorf");
        germanyBoards.add("Frankfurt");
        germanyBoards.add("Hannover");
        hongkongBoards.add("HKSE");
        indiaBoards.add("Bombay");
        indiaBoards.add("NSE");
        indonesiaBoards.add("Jakarta");
        israelBoards.add("Tel Aviv Stock Exchange");
        italyBoards.add("Milan");
        koreaBoards.add("KSE");
        koreaBoards.add("KOSDAQ");
        malaysiaBoards.add("Kuala Lumpur");
        malaysiaBoards.add("Mesdaq");
        netherlandsBoards.add("Amsterdam");
        newZealandBoards.add("NZ Stock Market");
        newZealandBoards.add("NZ Debt Market");
        newZealandBoards.add("NZ Alternative Market");
        norwayBoards.add("Oslo");
        portugalBoards.add("ENX");
        portugalBoards.add("Lisbon");
        singaporeBoards.add("SES");
        spainBoards.add("Madrid");
        spainBoards.add("MCE");
        spainBoards.add("Mercado Continuo");
        swedenBoards.add("Stockholm");
        switzerlandBoards.add("VTX");
        switzerlandBoards.add("Virt-X");
        switzerlandBoards.add("Switzerland");
        taiwanBoards.add("Taiwan");
        unitedKingdomBoards.add("FSI");
        unitedKingdomBoards.add("London");
        unitedStateBoards.add("NasdaqSC");        
        unitedStateBoards.add("DJI");        
        unitedStateBoards.add("NasdaqNM");        
        unitedStateBoards.add("NYSE");        
        unitedStateBoards.add("Nasdaq");        
        unitedStateBoards.add("AMEX");        
        unitedStateBoards.add("Pink Sheet");        
        vietnamBoards.add("HOSE"); // Hai support to get HOSE and HNX index for Vietnam
        vietnamBoards.add("HNX"); // Hai support to get HOSE and HNX index for Vietnam
    }
    /**
     * Returns code in Google's format.
     * 
     * @param code the code
     * @return code in Google's format
     */
    public static Code toGoogleFormat(Code code) {
        if (isYahooIndex(code)) {
            return toGoogleIndex(code);
        }
        
        String string = code.toString().trim().toUpperCase();
        final int string_length = string.length();
        if (string.endsWith(".N") && string_length > ".N".length()) {
            return Code.newInstance("NSE:" + string.substring(0, string_length - ".N".length()));
        } else if (string.endsWith(".B") && string_length > ".B".length()) {
            return Code.newInstance("BOM:" + string.substring(0, string_length - ".B".length()));
        } else if (string.endsWith(".NS") && string_length > ".NS".length()) {
            // Resolving Yahoo server down for India NSE stock market. Note, we
            // do not support Bombay stock market at this moment, due to the
            // difficulty in converting "TATACHEM.BO" (Yahoo Finance) to 
            // "BOM:500770" (Google Finance)
            string = string.substring(0, string_length - ".NS".length());
            String googleFormat = toGoogleFormatThroughAutoComplete(string, "NSE");
            if (googleFormat != null) {
                return Code.newInstance("NSE:" + googleFormat);
            }
        } else if (string.endsWith(".SS") && string_length > ".SS".length()) {
            string = "SHA:" + string.substring(0, string_length - ".SS".length());
            return Code.newInstance(string);
        } else if (string.endsWith(".SZ") && string_length > ".SZ".length()) {
            string = "SHE:" + string.substring(0, string_length - ".SZ".length());
            return Code.newInstance(string);
        } else if (string.endsWith(".SA") && string_length > ".SA".length()) {
            string = "BVMF:" + string.substring(0, string_length - ".SA".length());
            return Code.newInstance(string);
        } else if (string.endsWith(".VI") && string_length > ".VI".length()) {
            string = "VIE:" + string.substring(0, string_length - ".VI".length());
            return Code.newInstance(string);
        } else if (string.endsWith(".L") && string_length > ".L".length()) {
            string = "LON:" + string.substring(0, string_length - ".L".length());
            return Code.newInstance(string);
        }
        return code;
    }
    
    public static Code toYahooFormat(Code code) {
        String string = code.toString().trim().toUpperCase();
        final int string_length = string.length();
        if (string.startsWith("SHA:") && string_length > "SHA:".length()) {
            string = string.substring("SHA:".length()) + ".SS";
            return Code.newInstance(string);
        } else if (string.startsWith("SHE:") && string_length > "SHE:".length()) {
            string = string.substring("SHE:".length()) + ".SZ";
            return Code.newInstance(string);
        } else if (string.startsWith("NASDAQ:") && string_length > "NASDAQ:".length()) {
            string = string.substring("NASDAQ:".length());
            return Code.newInstance(string);
        } else if (string.startsWith("NYSE:") && string_length > "NYSE:".length()) {
            string = string.substring("NYSE:".length());
            return Code.newInstance(string);
        } else if (string.startsWith("BVMF:") && string_length > "BVMF:".length()) {
            string = string.substring("BVMF:".length()) + ".SA";
            return Code.newInstance(string);
        } else if (string.startsWith("VIE:") && string_length > "VIE:".length()) {
            string = string.substring("VIE:".length()) + ".VI";
            return Code.newInstance(string);
        } else if (string.startsWith("LON:") && string_length > "LON:".length()) {
            string = string.substring("LON:".length()) + ".L";
            return Code.newInstance(string);
        }
        
        Code newCode = toYahooIndex(code);
        return newCode;
    }
    
    public static boolean isYahooIndex(Code code) {
        return code.toString().startsWith("^");
    }
    
    private static Code toYahooIndex(Code code) {
        String string = code.toString().trim().toUpperCase();
        if (string.equals("INDEXDJX:.DJI")) {
            return Code.newInstance("^DJI");
        } else if (string.equals("INDEXNASDAQ:.IXIC")) {
            return Code.newInstance("^IXIC");
        } else if (string.equals("INDEXBOM:SENSEX")) {
            return Code.newInstance("^BSESN");
        } else if (string.equals("NSE:NIFTY")) {
            return Code.newInstance("^NSEI");
        } else if (string.equals("NSE:BANKNIFTY")) {
            return Code.newInstance("^NSEBANK");
        } else if (string.equals("INDEXBVMF:IBOV")) {
            return Code.newInstance("^BVSP");
        } else if (string.equals("INDEXVIE:ATX")) {
            return Code.newInstance("^ATX");
        } else if (string.equals("INDEXFTSE:UKX")) {
            return Code.newInstance("^FTSE");
        }
        return code;        
    }
    
    public static Code toGoogleIndex(Code code) {
        String string = code.toString().trim().toUpperCase();
        if (string.equals("^DJI")) {
            return Code.newInstance("INDEXDJX:.DJI");
        } else if (string.equals("^IXIC")) {
            return Code.newInstance("INDEXNASDAQ:.IXIC");
        } else if (string.equals("^BSESN")) {
            return Code.newInstance("INDEXBOM:SENSEX");
        } else if (string.equals("^NSEI")) {
            return Code.newInstance("NSE:NIFTY");
        } else if (string.equals("^NSEBANK")) {
            return Code.newInstance("NSE:BANKNIFTY");
        } else if (string.equals("^BVSP")) {
            return Code.newInstance("INDEXBVMF:IBOV");
        } else if (string.equals("^ATX")) {
            return Code.newInstance("INDEXVIE:ATX");
        } else if (string.equals("^FTSE")) {
            return Code.newInstance("INDEXFTSE:UKX");
        }
        return code;
    }
    
    // FIXME : Make it private.
    public static String toGoogleFormatThroughAutoComplete(String code, String exchange) {
        final StringBuilder builder = new StringBuilder("https://www.google.com/finance/match?matchtype=matchall&q=");
        try {
            // Exception will be thrown from apache httpclient, if we do not
            // perform URL encoding.
            builder.append(java.net.URLEncoder.encode(code, "UTF-8"));

            final String location = builder.toString();
            final String _respond = org.yccheok.jstock.gui.Utils.getResponseBodyAsStringBasedOnProxyAuthOption(location);
            if (_respond == null) {
                return null;
            }
            final String respond = Utils.GoogleRespondToJSON(_respond);
            // Google returns "// [ { "id": ... } ]".
            // We need to turn them into "[ { "id": ... } ]".
            final List<Map> jsonArray = gson.fromJson(respond, List.class);
            
            if (jsonArray == null) {
                return null;
            }
            
            for (int i = 0, size = jsonArray.size(); i < size; i++) {
                final Map<String, String> jsonObject = jsonArray.get(i);
                if (jsonObject.containsKey("e") && jsonObject.get("e").equalsIgnoreCase(exchange)) {
                    if (jsonObject.containsKey("t")) {
                        return jsonObject.get("t");
                    }
                }
            }           
        } catch (UnsupportedEncodingException ex) {
            log.error(null, ex);
        } catch (Exception ex) {
            // Jackson library may cause runtime exception if there is error
            // in the JSON string.
            log.error(null, ex);
        }        
        return null;
    }
    
    /**
     * Returns code in non Yahoo! format, by stripping off ".KL" suffix.
     * 
     * @param code the code
     * @return code in non Yahoo! format, by stripping off ".KL" suffix.
     */
    public static Code toNonYahooFormat(Code code)
    {
        final String tmp = code.toString();
        final String TMP = tmp.toUpperCase();
        int endIndex = TMP.lastIndexOf(".KL");
        if (endIndex < 0) {
            return code;
        }
        return Code.newInstance(tmp.substring(0, endIndex));
    }
    
    /*
     * Returns best search engine based on current selected country.
     * 
     * @param list List of elements, to be inserted into search engine
     * @return Best search engine based on current selected country.
     */
    public static boolean isPinyinTSTSearchEngineRequiredForSymbol() {
        final Country country = MainFrame.getInstance().getJStockOptions().getCountry();
        return (country == Country.China || country == Country.Taiwan);
    }

    /*
     * Returns <code>true</code> if we should maintain the symbol as database's,
     * even the symbol provided by stock server is different from our database.
     * This happens when our symbol in database is Chinese, but the symbol
     * returned by stock server is in English.
     * 
     * @return <code>true</code> if we should maintain the symbol as database's.
     */
    public static boolean isSymbolImmutable() {
        final Country country = MainFrame.getInstance().getJStockOptions().getCountry();
        return (country == Country.China || country == Country.Taiwan);
    }

    /*
     * Returns <code>true</code> if we should maintain the name as database's,
     * even the name provided by stock server is different from our database.
     * This happens when our name in database is Chinese, but the name returned
     * by stock server is in English.
     *
     * @return <code>true</code> if we should maintain the name as database's.
     */
    public static boolean isNameImmutable() {
        final Country country = MainFrame.getInstance().getJStockOptions().getCountry();
        return isNameImmutable(country);
    }

    private static boolean isNameImmutable(Country country) {
        return (country == Country.China || country == Country.Taiwan);
    }
    
    /*
     * Returns <code>true</code> if we need to use red color to indicate "rise
     * above". Green color to indicate "fall below".
     * 
     * @return <code>true</code> if we need to use red color to indicate "rise
     * above". Green color to indicate "fall below".
     */
    public static boolean isFallBelowAndRiseAboveColorReverse() {
        final Country country = MainFrame.getInstance().getJStockOptions().getCountry();
        return (country == Country.China || country == Country.Taiwan);
    }

    public static List<Index> getStockIndices(Country country) {
        switch (country)
        {
            case Australia:
                return java.util.Collections.unmodifiableList(Utils.australiaIndices);
            case Austria:
                return java.util.Collections.unmodifiableList(Utils.austriaIndices);
            case Belgium:
                return java.util.Collections.unmodifiableList(Utils.belgiumIndices);
            case Brazil:
                return java.util.Collections.unmodifiableList(Utils.brazilIndices);
            case Canada:
                return java.util.Collections.unmodifiableList(Utils.canadaIndices);
            case China:
                return java.util.Collections.unmodifiableList(Utils.chinaIndices);
            case Denmark:
                return java.util.Collections.unmodifiableList(Utils.denmarkIndices);
            case France:
                return java.util.Collections.unmodifiableList(Utils.franceIndices);
            case Germany:
                return java.util.Collections.unmodifiableList(Utils.germanyIndices);
            case HongKong:
                return java.util.Collections.unmodifiableList(Utils.hongkongIndices);
            case India:
                return java.util.Collections.unmodifiableList(Utils.indiaIndices);
            case Indonesia:
                return java.util.Collections.unmodifiableList(Utils.indonesiaIndices);
            case Israel:
                return java.util.Collections.unmodifiableList(Utils.israelIndices);
            case Italy:
                return java.util.Collections.unmodifiableList(Utils.italyIndices);
            case Korea:
                return java.util.Collections.unmodifiableList(Utils.koreaIndices);
            case Malaysia:
                return java.util.Collections.unmodifiableList(Utils.malaysiaIndices);
            case Netherlands:
                return java.util.Collections.unmodifiableList(Utils.netherlandsIndices);
            case NewZealand:
                return java.util.Collections.unmodifiableList(Utils.newZealandIndices);
            case Norway:
                return java.util.Collections.unmodifiableList(Utils.norwayIndices);
            case Portugal:
                return java.util.Collections.unmodifiableList(Utils.portugalIndices);
            case Singapore:
                return java.util.Collections.unmodifiableList(Utils.singaporeIndices);
            case Spain:
                return java.util.Collections.unmodifiableList(Utils.spainIndices);
            case Sweden:
                return java.util.Collections.unmodifiableList(Utils.swedenIndices);
            case Switzerland:
                return java.util.Collections.unmodifiableList(Utils.switzerlandIndices);
            case Taiwan:
                return java.util.Collections.unmodifiableList(Utils.taiwanIndices);
            case UnitedKingdom:
                return java.util.Collections.unmodifiableList(Utils.unitedKingdomIndices);                
            case UnitedState:
                return java.util.Collections.unmodifiableList(Utils.unitedStateIndices);
            case Vietnam:
                return java.util.Collections.unmodifiableList(Utils.vietnamIndices);
        }
        
        return java.util.Collections.emptyList();
    }
    public static List<String> getBoards(Country country) {
        switch (country)
        {
            case Australia:
                return java.util.Collections.unmodifiableList(Utils.australiaBoards);
            case Austria:
                return java.util.Collections.unmodifiableList(Utils.austriaBoards);
            case Belgium:
                return java.util.Collections.unmodifiableList(Utils.belgiumBoards);
            case Brazil:
                return java.util.Collections.unmodifiableList(Utils.brazilBoards);
            case Canada:
                return java.util.Collections.unmodifiableList(Utils.canadaBoards);
            case China:
                return java.util.Collections.unmodifiableList(Utils.chinaBoards);
            case Denmark:
                return java.util.Collections.unmodifiableList(Utils.denmarkBoards);
            case France:
                return java.util.Collections.unmodifiableList(Utils.franceBoards);
            case Germany:
                return java.util.Collections.unmodifiableList(Utils.germanyBoards);
            case HongKong:
                return java.util.Collections.unmodifiableList(Utils.hongkongBoards);
            case India:
                return java.util.Collections.unmodifiableList(Utils.indiaBoards);
            case Indonesia:
                return java.util.Collections.unmodifiableList(Utils.indonesiaBoards);
            case Israel:
                return java.util.Collections.unmodifiableList(Utils.israelBoards);
            case Italy:
                return java.util.Collections.unmodifiableList(Utils.italyBoards);
            case Korea:
                return java.util.Collections.unmodifiableList(Utils.koreaBoards);
            case Malaysia:
                return java.util.Collections.unmodifiableList(Utils.malaysiaBoards);
            case Netherlands:
                return java.util.Collections.unmodifiableList(Utils.netherlandsBoards);
            case NewZealand:
                return java.util.Collections.unmodifiableList(Utils.newZealandBoards);
            case Norway:
                return java.util.Collections.unmodifiableList(Utils.norwayBoards);
            case Portugal:
                return java.util.Collections.unmodifiableList(Utils.portugalBoards);
            case Singapore:
                return java.util.Collections.unmodifiableList(Utils.singaporeBoards);
            case Spain:
                return java.util.Collections.unmodifiableList(Utils.spainBoards);
            case Sweden:
                return java.util.Collections.unmodifiableList(Utils.swedenBoards);
            case Switzerland:
                return java.util.Collections.unmodifiableList(Utils.switzerlandBoards);
            case Taiwan:
                return java.util.Collections.unmodifiableList(Utils.taiwanBoards);
            case UnitedKingdom:
                return java.util.Collections.unmodifiableList(Utils.unitedKingdomBoards);                
            case UnitedState:
                return java.util.Collections.unmodifiableList(Utils.unitedStateBoards);
            case Vietnam:
                return java.util.Collections.unmodifiableList(Utils.vietnamBoards);
        }
        
        return java.util.Collections.emptyList();
    }

    /*
     * Returns JSON string, by parsing respond from Google server.
     *
     * @param respond string returned from Google server directly
     * @return JSON string, by parsing respond from Google server
     */
    public static String GoogleRespondToJSON(String respond) {
        final int beginIndex = respond.indexOf("[");
        final int endIndex = respond.lastIndexOf("]");
        if (beginIndex < 0) {
            return "";
        }
        if (beginIndex > endIndex) {
            return "";
        }
        String string = respond.substring(beginIndex, endIndex + 1);
        // http://stackoverflow.com/questions/6067673/urldecoder-illegal-hex-characters-in-escape-pattern-for-input-string
        string = string.replaceAll("%", "%25");
        
        // http://stackoverflow.com/questions/15518340/json-returned-by-google-maps-query-contains-encoded-characters-like-x26-how-to
        // JSON returned by Google Maps Query contains encoded characters like \x26 (how to decode?)
        try {
            string = URLDecoder.decode(string.replace("\\x", "%"), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error(null, ex);
        }
        
        return string;
    }

    /*
     * Returns JSON string, by parsing respond from Yahoo server.
     *
     * @param respond string returned from Yahoo server directly
     * @return JSON string, by parsing respond from Yahoo server
     */
    public static String YahooRespondToJSON(String respond) {
        final int beginIndex = respond.indexOf("{");
        final int endIndex = respond.lastIndexOf("}");
        if (beginIndex < 0) {
            return "";
        }
        if (beginIndex > endIndex) {
            return "";
        }
        return respond.substring(beginIndex, endIndex + 1);
    }

    /*
     * Returns a new double initialized to the value represented by the
     * specified String, as performed by the valueOf method of class Double.
     * If failed, 0.0 will be returned.
     *
     * @return the double value represented by the string argument.
     */
    public static double parseDouble(String value) {
        if (value == null) {
            // This is an invalid value.
            return 0.0;
        }

        try {
            // Use String.replace, in order to turn "1,234,567%" into "1234567".
            return Double.parseDouble(value.replace(",", "").replace("%", ""));
        } catch (NumberFormatException ex) {
            log.error(null, ex);
        }
        // This is an invalid value.
        return 0.0;
    }

    /*
     * Returns a new long initialized to the value represented by the
     * specified String, as performed by the valueOf method of class Long.
     * If failed, 0L will be returned.
     *
     * @return the long value represented by the string argument.
     */
    public static long parseLong(String value) {
        if (value == null) {
            // This is an invalid value.
            return 0L;
        }
        
        try {
            // Use String.replace, in order to turn "1,234,567%" into "1234567".
            return Long.parseLong(value.replace(",", "").replace("%", ""));
        } catch (NumberFormatException ex) {
            log.error(null, ex);
        }
        // This is an invalid value.
        return 0L;
    }
    
    private static final Gson gson = new Gson();
    private static final Log log = LogFactory.getLog(Utils.class);
}
