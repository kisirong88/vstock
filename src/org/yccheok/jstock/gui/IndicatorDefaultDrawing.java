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

import org.yccheok.jstock.gui.analysis.OperatorFigure;
import org.jhotdraw.draw.*;
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
        
        InputFormat inputFormat = this.getInputFormats().get(0);
        inputFormat.read(jHotdrawFile, this);
        if (operatorIndicatorFilename.indexOf("MACD Down Trend Signal.xml") >= 0) {
          //IndicatorDefaultDrawing alertDefaultDrawing = IndicatorDefaultDrawing();
          /*StockRelativeHistoryOperatorFigure stockRelativeHistory1OperatorFigure = new StockRelativeHistoryOperatorFigure();
          StockRelativeHistoryOperatorFigure stockRelativeHistory2OperatorFigure = new StockRelativeHistoryOperatorFigure();
          ArithmeticOperatorFigure arithmeticOperatorFigure = new ArithmeticOperatorFigure();
          DoubleConstantOperatorFigure doubleConstantOperatorFigure = new DoubleConstantOperatorFigure();
          EqualityOperatorFigure equalityOperatorFigure = new EqualityOperatorFigure();*/
        }

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
        
        List<Figure> figures = this.getChildren();
        int counter = 0;
        for (Figure f : figures) {
            if (f instanceof OperatorFigure) {
                final Operator operator = operatorIndicator.get(counter);
                OperatorFigure operatorFigure = (OperatorFigure)f;
                operatorFigure.setOperator(operator);
                
                // Property listener are not being serialized. We need to restore them manually.
                ((AbstractOperator)operator).addPropertyChangeListener(operatorFigure);
                
                counter++;
            }
        }                
    }    
}
