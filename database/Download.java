import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.net.MalformedURLException;
/*import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;*/
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.util.*;
import org.apache.commons.io.IOUtils;
/*import org.apache.commons.httpclient.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yccheok.jstock.engine.Stock.Board;
import org.yccheok.jstock.engine.Stock.Industry;
import org.yccheok.jstock.file.Statements;
import org.yccheok.jstock.gui.MainFrame;
import org.yccheok.jstock.gui.Pair;
import org.json.*;*/
import java.net.URL;

public class Download {
   /*public static String getResponseBodyAsStringBasedOnProxyAuthOption(String request) {
       return _getResponseBodyAsStringBasedOnProxyAuthOption(httpClient, request);
   }*/


   public static void main(String[] args) {
      String dir = "";
      for (int i = 0; i < args.length; i++) {
          if((args[i].equals("-outdir")) && (i < (args.length-1))) {
            dir = args[i+1];
            i++;
          }
      }

      final Pattern colonQuotes = Pattern.compile(":\'");
      final Pattern quoteCommas = Pattern.compile("\',");
      final Pattern curlyCommas = Pattern.compile("\\},\\{");
      /*final Pattern openBrackets = Pattern.compile("[");
      final Pattern closeBrackets = Pattern.compile("]");*/
      final String location = "http://finance.vietstock.vn/GetCompanyList.ashx?language=en";
      System.out.println("list company location "+location);
      String respond = "";
      try {
        URL url = new URL(location);
        try {
          InputStream in = url.openStream();
          try {
            respond = IOUtils.toString(in);
          } finally {
            IOUtils.closeQuietly(in);
          }
        } catch (IOException e) {
        }
      } catch (MalformedURLException e) {
      }

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

            //Get history database
            symbol = "";
            try {
                symbol = java.net.URLEncoder.encode(code, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
            }
            final String historyFile = dir + File.separator + symbol + ".xml";
            System.out.println("DEBUG historyFile "+historyFile);

            //final boolean isExist = org.yccheok.jstock.gui.Utils.isFileOrDirectoryExist(historyFile);
            final StringBuilder stringBuilder = new StringBuilder("http://finance.vietstock.vn/Controls/TradingResult/Matching_Hose_Result.aspx?scode=");
            stringBuilder.append(symbol);
            final StringBuilder columnBuilder = new StringBuilder("&lcol=TKLGD%2CTGTGD%2CCN%2CTN%2CGD1%2CGD2%2CGD3%2C");
            stringBuilder.append(columnBuilder);
            final StringBuilder formatBuilder = new StringBuilder("&sort=Time&dir=desc&page=1&psize=0");
            stringBuilder.append(formatBuilder);

            final Calendar calendar = Calendar.getInstance();
            final int endMonth = calendar.get(Calendar.MONTH)+1; //Hai fix bug, havent found root cause
            final int endDate = calendar.get(Calendar.DATE);
            final int endYear = calendar.get(Calendar.YEAR)-2000;
            calendar.add(Calendar.YEAR, -4);
            final int startMonth = calendar.get(Calendar.MONTH)+1; //Hai fix bug, havent found root cause
            final int startDate = calendar.get(Calendar.DATE);
            final int startYear = calendar.get(Calendar.YEAR)-2000;
            final StringBuilder startDateBuilder = new StringBuilder("&fdate=");
            startDateBuilder.append(startMonth).append("%2F").append(startDate).append("%2F").append(startYear);
            final StringBuilder endDateBuilder = new StringBuilder("&tdate=");
            endDateBuilder.append(endMonth).append("%2F").append(endDate).append("%2F").append(endYear);
            stringBuilder.append(startDateBuilder).append(endDateBuilder).append("&exp=xml");

            final String historyLocation = stringBuilder.toString();
            System.out.println("DEBUG download history location "+historyLocation);
            String historyRespond = "";
            try {
              URL url = new URL(location);
              try {
                InputStream historyIn = url.openStream();
                try {
                  historyRespond = IOUtils.toString(historyIn);
                } finally {
                  IOUtils.closeQuietly(historyIn);
                }
              } catch (IOException e) {
              }
            } catch (MalformedURLException e) {
            }

            try {
                //PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(historyFile, true)));
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(historyFile)));
                out.println(historyRespond);
                out.close();
            } catch (IOException e) {
              //exception handling left as an exercise for the reader
            }
          } else if (columns[0].equals("name")) {
            columns[1] = columns[1].substring(1,columns[1].length()-1);
            //symbol = columns[1];
            //System.out.println("symbol "+symbol);
          } else {
          }
        }
      }
   }
}
