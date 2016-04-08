/*
 * AlertDefaultDrawing.java
 *
 * Created on May 20, 2007, 1:29 PM
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Copyright (C) 2007 Cheok YanCheng <yccheok@yahoo.com>
 */

package org.yccheok.jstock.gui;

//import org.yccheok.jstock.gui.analysis.OperatorFigure;
import org.yccheok.jstock.gui.analysis.*;
import org.jhotdraw.draw.*;
import java.awt.geom.*;
import java.util.*;
import java.io.*;
import org.jhotdraw.undo.UndoRedoManager;
import org.yccheok.jstock.analysis.*;


/**
 *
 * @author yccheok
 */
public class IndicatorDefaultDrawing extends org.jhotdraw.draw.DefaultDrawing {
    /* Used to detect unsaved changes. */
    private final UndoRedoManager undoRedoManager = new UndoRedoManager();

    /** Creates a new instance of AlertDefaultDrawing */
    public IndicatorDefaultDrawing() {
        DOMStorableInputOutputFormat ioFormat =
                new DOMStorableInputOutputFormat(new IndicatorDOMFactory());
        LinkedList<InputFormat> inputFormats = new LinkedList<InputFormat>();
        inputFormats.add(ioFormat);
        this.setInputFormats(inputFormats);
        LinkedList<OutputFormat> outputFormats = new LinkedList<OutputFormat>();
        outputFormats.add(ioFormat);
        this.setOutputFormats(outputFormats);

        // To detect unsaved changes.
        this.addUndoableEditListener(undoRedoManager);
    }
    
    // Return an OperatorIndicator with name "null".
    public OperatorIndicator getOperatorIndicator() {
        OperatorIndicator operatorIndicator = new OperatorIndicator();

        List<Figure> figures = this.getChildren();
        for (Figure figure : figures) {
            if(figure instanceof OperatorFigure) {
                OperatorFigure operatorFigure = (OperatorFigure)figure;
                operatorIndicator.add(operatorFigure.getOperator());
            }
        }
        
        return operatorIndicator;
    }
    
    /**
     * Writes the project to the specified file.
     */
    public void write(String projectName, String jHotDrawFilename, String operatorIndicatorFilename) throws IOException {
        File jHotdrawFile = new File(jHotDrawFilename);
        
        OperatorIndicator operatorIndicator = getOperatorIndicator();
        operatorIndicator.setName(projectName);
        
        OutputFormat outputFormat = this.getOutputFormats().get(0);
        outputFormat.write(jHotdrawFile, this);

        org.yccheok.jstock.gui.Utils.toXML(operatorIndicator, operatorIndicatorFilename);

        // To flag no more unsaved changes in this drawing.
        undoRedoManager.setHasSignificantEdits(false);
    }

    public boolean hasSignificantEdits() {
        return undoRedoManager.hasSignificantEdits();
    }

    /**
     * Reads the project from the specified file.
     */
    public void read(String jHotDrawFilename, String operatorIndicatorFilename) throws IOException {
        File jHotdrawFile = new File(jHotDrawFilename);
        File xStreamFile = new File(operatorIndicatorFilename);
        
        //OperatorIndicator operatorIndicator = org.yccheok.jstock.gui.Utils.fromXML(OperatorIndicator.class, xStreamFile);
        OperatorIndicator operatorIndicator;
        if (operatorIndicatorFilename.indexOf("MACD Down Trend Signal local") < 0) {
          operatorIndicator = org.yccheok.jstock.gui.Utils.fromXML(OperatorIndicator.class, xStreamFile);
        } else {
        //System.out.println("DEBUG operatorIndicatorFilename "+operatorIndicatorFilename);
        //if (operatorIndicatorFilename.indexOf("MACD Down Trend Signal.xml") >= 0) {
        //if (operatorIndicatorFilename.indexOf("MACD Down Trend Signal local") >= 0) {
          System.out.println("DEBUG operatorIndicatorFilename "+operatorIndicatorFilename);
          //Constructor of operators will create automatically output connectors pointing to these operator
          //Constructor of operators will create automatically input connectors pointing to these operator
          //But constructor of operators don't create connections
          StockRelativeHistoryOperator stockRelativeHistory1Operator = new StockRelativeHistoryOperator();
          stockRelativeHistory1Operator.setFunction(StockRelativeHistoryOperator.Function.EMA);
          stockRelativeHistory1Operator.setType(StockRelativeHistoryOperator.Type.LastPrice);
          stockRelativeHistory1Operator.setDay(12);
          StockRelativeHistoryOperator stockRelativeHistory2Operator = new StockRelativeHistoryOperator();
          stockRelativeHistory2Operator.setFunction(StockRelativeHistoryOperator.Function.EMA);
          stockRelativeHistory2Operator.setType(StockRelativeHistoryOperator.Type.LastPrice);
          stockRelativeHistory2Operator.setDay(26);
          ArithmeticOperator arithmeticOperator = new ArithmeticOperator();
          arithmeticOperator.setArithmetic(ArithmeticOperator.Arithmetic.Subtraction);
          DoubleConstantOperator doubleConstantOperator = new DoubleConstantOperator();
          //Create equalityOperator
          EqualityOperator equalityOperator = new EqualityOperator();
          equalityOperator.setEquality(EqualityOperator.Equality.Lesser);
          //Create sinkOperator
          SinkOperator sinkOperator = new SinkOperator();

          //Create connection which connects equalityOperator to sinkOperator
          Connection equalityToSinkConnection = new Connection();
          //Connect to sinkOperator
          sinkOperator.addInputConnection(equalityToSinkConnection, 0);
          //Connect to equalityOperator
          equalityOperator.addOutputConnection(equalityToSinkConnection, 0);

          //Create connection which connects arithmeticOperator to equalityOperator
          Connection arithmeticToEqualityConnection = new Connection();
          //Connect to equalityOperator
          equalityOperator.addInputConnection(arithmeticToEqualityConnection, 0);
          //Connect to arithmeticOperator
          arithmeticOperator.addOutputConnection(arithmeticToEqualityConnection, 0);

          //Create connection which connects doubleConstantOperator to equalityOperator
          Connection doubleConstantToEqualityConnection = new Connection();
          //Connect to equalityOperator
          equalityOperator.addInputConnection(doubleConstantToEqualityConnection, 1);
          //Connect to doubleConstantOperator
          doubleConstantOperator.addOutputConnection(doubleConstantToEqualityConnection, 0);

          //Create connection which connects stockRelativeHistory1Operator to arithmeticOperator
          Connection stockRelativeHistory1ToArithmeticConnection = new Connection();
          //Connect to arithmeticOperator
          arithmeticOperator.addInputConnection(stockRelativeHistory1ToArithmeticConnection, 0);
          //Connect to arithmeticOperator
          stockRelativeHistory1Operator.addOutputConnection(stockRelativeHistory1ToArithmeticConnection, 0);

          //Create connection which connects stockRelativeHistory2Operator to arithmeticOperator
          Connection stockRelativeHistory2ToArithmeticConnection = new Connection();
          //Connect to arithmeticOperator
          arithmeticOperator.addInputConnection(stockRelativeHistory2ToArithmeticConnection, 1);
          //Connect to arithmeticOperator
          stockRelativeHistory2Operator.addOutputConnection(stockRelativeHistory2ToArithmeticConnection, 0);

          //Create OperatorIndicator
          operatorIndicator = new OperatorIndicator("MACD Down Trend Signal");
          operatorIndicator.add(stockRelativeHistory1Operator);
          operatorIndicator.add(stockRelativeHistory2Operator);
          operatorIndicator.add(equalityOperator);
          operatorIndicator.add(arithmeticOperator);
          operatorIndicator.add(doubleConstantOperator);
          operatorIndicator.add(sinkOperator);
          org.yccheok.jstock.gui.Utils.toXML(operatorIndicator, "hai.xml");
        }
        if (operatorIndicator == null) {
            throw new IOException();
        }
        
        int counter = 0;
        if (operatorIndicatorFilename.indexOf("MACD Down Trend Signal local") < 0) {
          InputFormat inputFormat = this.getInputFormats().get(0);
          inputFormat.read(jHotdrawFile, this);

          List<Figure> figures = this.getChildren();
          for (Figure f : figures) {
              if (f instanceof OperatorFigure) {
                  org.yccheok.jstock.gui.Utils.toXML(f, "figure"+counter+".xml");
                  final Operator operator = operatorIndicator.get(counter);
                  OperatorFigure operatorFigure = (OperatorFigure)f;
                  operatorFigure.setOperator(operator);
                  
                  // Property listener are not being serialized. We need to restore them manually.
                  ((AbstractOperator)operator).addPropertyChangeListener(operatorFigure);
                  
                  counter++;
              }
          }                
        } else {                
          double x = 0; double y = 0;
          double ex = 0; double ey = 0;
          double w = 0; double h = 0;
          //IndicatorDefaultDrawing alertDefaultDrawing = IndicatorDefaultDrawing();
          StockRelativeHistoryOperatorFigure stockRelativeHistory1OperatorFigure = new StockRelativeHistoryOperatorFigure();
          final Operator stockRelativeHistory1Operator = operatorIndicator.get(0);
          stockRelativeHistory1OperatorFigure.setOperator(stockRelativeHistory1Operator);
          ((AbstractOperator)stockRelativeHistory1Operator).addPropertyChangeListener(stockRelativeHistory1OperatorFigure);
          stockRelativeHistory1OperatorFigure.setAttribute("12d EMA LastPrice");
          w = stockRelativeHistory1OperatorFigure.getBounds().width;
          h = stockRelativeHistory1OperatorFigure.getBounds().height;
          stockRelativeHistory1OperatorFigure.setBounds(new Point2D.Double(241,252), new Point2D.Double(241+w,252+h));
          this.basicAdd(counter, stockRelativeHistory1OperatorFigure);

          counter++;
          StockRelativeHistoryOperatorFigure stockRelativeHistory2OperatorFigure = new StockRelativeHistoryOperatorFigure();
          final Operator stockRelativeHistory2Operator = operatorIndicator.get(1);
          stockRelativeHistory2OperatorFigure.setOperator(stockRelativeHistory2Operator);
          ((AbstractOperator)stockRelativeHistory2Operator).addPropertyChangeListener(stockRelativeHistory2OperatorFigure);
          stockRelativeHistory2OperatorFigure.setAttribute("12d EMA LastPrice");
          x = stockRelativeHistory2OperatorFigure.getBounds().x;
          y = stockRelativeHistory2OperatorFigure.getBounds().y;
          w = stockRelativeHistory2OperatorFigure.getBounds().width;
          h = stockRelativeHistory2OperatorFigure.getBounds().height;
          stockRelativeHistory2OperatorFigure.setBounds(new Point2D.Double(246,379), new Point2D.Double(246+w,379+h));
          this.basicAdd(counter, stockRelativeHistory2OperatorFigure);

          counter++;
          EqualityOperatorFigure equalityOperatorFigure = new EqualityOperatorFigure();
          final Operator equalityOperator = operatorIndicator.get(2);
          equalityOperatorFigure.setOperator(equalityOperator);
          ((AbstractOperator)equalityOperator).addPropertyChangeListener(equalityOperatorFigure);
          equalityOperatorFigure.setAttribute("Lesser");
          x = equalityOperatorFigure.getBounds().x;
          y = equalityOperatorFigure.getBounds().y;
          w = equalityOperatorFigure.getBounds().width;
          h = equalityOperatorFigure.getBounds().height;
          equalityOperatorFigure.setBounds(new Point2D.Double(641,318), new Point2D.Double(641+w,318+h));
          this.basicAdd(counter, equalityOperatorFigure);

          counter++;
          ArithmeticOperatorFigure arithmeticOperatorFigure = new ArithmeticOperatorFigure();
          final Operator arithmeticOperator = operatorIndicator.get(3);
          arithmeticOperatorFigure.setOperator(arithmeticOperator);
          ((AbstractOperator)arithmeticOperator).addPropertyChangeListener(arithmeticOperatorFigure);
          arithmeticOperatorFigure.setAttribute("Subtraction");
          x = arithmeticOperatorFigure.getBounds().x;
          y = arithmeticOperatorFigure.getBounds().y;
          w = arithmeticOperatorFigure.getBounds().width;
          h = arithmeticOperatorFigure.getBounds().height;
          arithmeticOperatorFigure.setBounds(new Point2D.Double(453,295), new Point2D.Double(453+w,295+h));
          this.basicAdd(counter, arithmeticOperatorFigure);

          counter++;
          DoubleConstantOperatorFigure doubleConstantOperatorFigure = new DoubleConstantOperatorFigure();
          final Operator doubleConstantOperator = operatorIndicator.get(4);
          doubleConstantOperatorFigure.setOperator(doubleConstantOperator);
          ((AbstractOperator)doubleConstantOperator).addPropertyChangeListener(doubleConstantOperatorFigure);
          doubleConstantOperatorFigure.setValue("0.0");
          x = doubleConstantOperatorFigure.getBounds().x;
          y = doubleConstantOperatorFigure.getBounds().y;
          w = doubleConstantOperatorFigure.getBounds().width;
          h = doubleConstantOperatorFigure.getBounds().height;
          doubleConstantOperatorFigure.setBounds(new Point2D.Double(483,410), new Point2D.Double(483+w,410+h));
          this.basicAdd(counter, doubleConstantOperatorFigure);

          counter++;
          SinkOperatorFigure sinkOperatorFigure = new SinkOperatorFigure();
          final Operator sinkOperator = operatorIndicator.get(5);
          sinkOperatorFigure.setOperator(sinkOperator);
          ((AbstractOperator)sinkOperator).addPropertyChangeListener(sinkOperatorFigure);
          x = sinkOperatorFigure.getBounds().x;
          y = sinkOperatorFigure.getBounds().y;
          w = sinkOperatorFigure.getBounds().width;
          h = sinkOperatorFigure.getBounds().height;
          sinkOperatorFigure.setBounds(new Point2D.Double(782,314), new Point2D.Double(782+w,314+h));
          this.basicAdd(counter, sinkOperatorFigure);

          counter++;
          DependencyFigure dep1 = new DependencyFigure();
          org.jhotdraw.draw.Connector start1 = stockRelativeHistory1OperatorFigure.getConnector(0);
          org.jhotdraw.draw.Connector end1 = arithmeticOperatorFigure.getConnector(0);
          dep1.handleConnect(start1, end1);
          dep1.setStartPoint(new Point2D.Double(406.103515625, 282.3759765625));
          dep1.setEndPoint(new Point2D.Double(453, 310.18798828125));
          this.basicAdd(counter, dep1);

          counter++;
          DependencyFigure dep2 = new DependencyFigure();
          org.jhotdraw.draw.Connector start2 = stockRelativeHistory2OperatorFigure.getConnector(0);
          org.jhotdraw.draw.Connector end2 = arithmeticOperatorFigure.getConnector(1);
          dep2.handleConnect(start2, end2);
          dep2.setStartPoint(new Point2D.Double(411.103515625, 409.3759765625));
          dep2.setEndPoint(new Point2D.Double(453, 340.56396484375));
          this.basicAdd(counter, dep2);

          counter++;
          DependencyFigure dep3 = new DependencyFigure();
          org.jhotdraw.draw.Connector start3 = arithmeticOperatorFigure.getConnector(2);
          org.jhotdraw.draw.Connector end3 = equalityOperatorFigure.getConnector(0);
          dep3.handleConnect(start3, end3);
          dep3.setStartPoint(new Point2D.Double(547.328125, 325.3759765625));
          dep3.setEndPoint(new Point2D.Double(641, 333.18798828125));
          this.basicAdd(counter, dep3);
          /*final AbstractConnector c = new AbstractConnector();
          c.setOwner(stockRelativeHistory1Operator);*/
          //org.yccheok.jstock.gui.Utils.toXML(stockRelativeHistory1OperatorFigure, "figure.xml");
          //org.yccheok.jstock.gui.Utils.toXML(this, "figure.xml");
          /*StockRelativeHistoryOperatorFigure stockRelativeHistory2OperatorFigure = new StockRelativeHistoryOperatorFigure();
          ArithmeticOperatorFigure arithmeticOperatorFigure = new ArithmeticOperatorFigure();
          DoubleConstantOperatorFigure doubleConstantOperatorFigure = new DoubleConstantOperatorFigure();
          EqualityOperatorFigure equalityOperatorFigure = new EqualityOperatorFigure();*/
        }
    }    
}
