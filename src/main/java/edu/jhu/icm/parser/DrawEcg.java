/*
 * Created on Apr 6, 2006
 *
 */
package edu.jhu.icm.parser;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

/**
 * Draw ecg sequence data
 * 
 * @author cyang
 */
public class DrawEcg {
    static Logger logger = Logger.getLogger(DrawEcg.class.getName());

    public static void plot(XYDataset xyDataset, String fileName) {
        //logger.debug("teset");

        JFreeChart chart = ChartFactory.createXYLineChart("", // chart
                // title
                "Type", // domain axis label
                "Value", // range axis label
                xyDataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
                );
        FileOutputStream fos = null;
        chart.setBackgroundPaint(Color.black);
        XYPlot plot = (XYPlot) chart.getPlot();

        plot.setBackgroundPaint(Color.BLACK);
        //plot.setOutlinePaint(Color.blue);
        // plot.setOutlineStroke(new Stroke)
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.yellow);
        if (chart == null) {
            logger.error("chart is null");
            return;
        }

        try {
            fos = new FileOutputStream(new File(fileName));
            ChartUtilities.writeChartAsPNG(fos, chart, 1200, 200);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    public static JFreeChart combinedPlot(EcgLeadData ds) {
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis(
                "Time(s)"));
        plot.setGap(5);
        plot.setOrientation(PlotOrientation.VERTICAL);
       
        XYDataset[] allDatasets = ds.getPagedXYDatasets();
        double yLowerBound = 0, yUpperBound = 0;
        
        // construct subplots and get max range
        boolean isDomainAxisSet=false;       
        for (int i = 0; i < allDatasets.length; i++) {
            XYDataset oneDataset = allDatasets[i];
            XYItemRenderer xyrender = new StandardXYItemRenderer();
            xyrender.setSeriesPaint(0, Color.YELLOW);
            NumberAxis rangeAxis = new NumberAxis("Voltage(uV)");
            XYPlot subPlot = new XYPlot(oneDataset, null, rangeAxis, xyrender);
           
            subPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            subPlot.setBackgroundPaint(Color.BLACK);
          
            if(!isDomainAxisSet){
                Range xrange=subPlot.getDataRange(subPlot.getDomainAxis());
                if(xrange.getLength()<ds.getPageWindow()){
                    double lower =  xrange.getLowerBound();
                    double upper =lower+ds.getPageWindow();
                    
                   xrange = new Range(lower, upper);
                }
                plot.getDomainAxis().setRange(xrange);
                //plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);
                logger.debug(xrange.toString());
                isDomainAxisSet=true;
            }
            
            // ValueMarker valuemarker1 = new ValueMarker(dmin, Color.GRAY, new
            // BasicStroke(1.0F));
            ValueAxis va = subPlot.getRangeAxis();
            double upperlimit = va.getUpperBound();
            double lowerlimit = va.getLowerBound();
            if (i == 0) {
                yLowerBound = lowerlimit;
                yUpperBound = upperlimit;
            } else {
                if (upperlimit > yUpperBound)
                    yUpperBound = upperlimit;
                if (lowerlimit < yLowerBound)
                    yLowerBound = lowerlimit;
            }
            plot.add(subPlot);
        }
        logger.debug("upperBound is " + yUpperBound + "; lowerBound is "
                + yLowerBound);
        List subplots = plot.getSubplots();
        String title = "ECG ";
        for (int i = 0; i < subplots.size(); i++) {
            XYPlot subPlot = (XYPlot) subplots.get(i);
            subPlot.getRangeAxis().setUpperBound(yUpperBound);
            subPlot.getRangeAxis().setLowerBound(yLowerBound);
            XYTextAnnotation annotation = new XYTextAnnotation(
                    ds.getLeadName()[i], subPlot.getDomainAxis().getLowerBound()+0.05, yUpperBound - 10);
            annotation.setTextAnchor(TextAnchor.TOP_LEFT);
            annotation.setFont(new Font("SansSerif", Font.PLAIN, 16));
            // annotation.setRotationAngle(Math.PI / 4.0);
            annotation.setPaint(Color.WHITE);
            subPlot.addAnnotation(annotation);

        }
        NumberFormat nf = new DecimalFormat("0.00");
        String startStr = nf.format(plot.getDomainAxis().getLowerBound());
        String endStr = nf.format(plot.getDomainAxis().getUpperBound());
        
        title +="("+ startStr + "s - " + endStr+"s)";
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
                plot, true);
        return chart;
    }

    public static CategoryDataset createDataset() {
        //      row keys...
        String series1 = "First";
        String series2 = "Second";
        String series3 = "Third";
        //      column keys...
        String category1 = "Category 1";
        String category2 = "Category 2";
        String category3 = "Category 3";
        String category4 = "Category 4";
        String category5 = "Category 5";
        //      create the dataset...
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, series1, category1);
        dataset.addValue(4.0, series1, category2);
        dataset.addValue(3.0, series1, category3);
        dataset.addValue(5.0, series1, category4);
        dataset.addValue(5.0, series1, category5);
        dataset.addValue(5.0, series2, category1);
        dataset.addValue(7.0, series2, category2);
        dataset.addValue(6.0, series2, category3);
        dataset.addValue(8.0, series2, category4);
        dataset.addValue(4.0, series2, category5);
        dataset.addValue(4.0, series3, category1);
        dataset.addValue(3.0, series3, category2);
        dataset.addValue(2.0, series3, category3);
        dataset.addValue(3.0, series3, category4);
        dataset.addValue(6.0, series3, category5);
        return dataset;
    }

    public static XYDataset createXYDataset() {

        XYSeries series1 = new XYSeries("First");
        series1.add(1.0, 1.0);
        series1.add(2.0, 4.0);
        series1.add(3.0, 3.0);
        series1.add(4.0, 5.0);
        series1.add(5.0, 5.0);
        series1.add(6.0, 7.0);
        series1.add(7.0, 7.0);
        series1.add(8.0, 8.0);

        XYSeries series2 = new XYSeries("Second");
        series2.add(1.0, 5.0);
        series2.add(2.0, 7.0);
        series2.add(3.0, 6.0);
        series2.add(4.0, 8.0);
        series2.add(5.0, 4.0);
        series2.add(6.0, 4.0);
        series2.add(7.0, 2.0);
        series2.add(8.0, 1.0);

        XYSeries series3 = new XYSeries("Third");
        series3.add(3.0, 4.0);
        series3.add(4.0, 3.0);
        series3.add(5.0, 2.0);
        series3.add(6.0, 3.0);
        series3.add(7.0, 6.0);
        series3.add(8.0, 3.0);
        series3.add(9.0, 4.0);
        series3.add(10.0, 3.0);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
        return dataset;
    }
}
