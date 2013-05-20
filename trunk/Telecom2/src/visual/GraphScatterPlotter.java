package visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.*;
import javax.swing.JPanel;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

public class GraphScatterPlotter extends ApplicationFrame {

    public GraphScatterPlotter(String title, String x, String y, double[][] data) {
        super(title);
        JPanel jpanel = createDemoPanel(title,x,y,data);
        jpanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(jpanel);
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    public static JPanel createDemoPanel(String title,String x, String y, double[][] data) {
    	
    	XYDataset ds = getDataset(data);
    	
        JFreeChart jfreechart = ChartFactory.createScatterPlot(title, x, y, ds, PlotOrientation.VERTICAL, false, true, false);
        Shape shape = ShapeUtilities.createDiamond(100);

        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setBaseShape(shape);
       
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);

        return new ChartPanel(jfreechart);
    }

    private static XYDataset getDataset(double[][] data) {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        XYSeries series = new XYSeries("");
        for(double[] d: data)
         series.add(d[0], d[1]);
        xySeriesCollection.addSeries(series);
        return xySeriesCollection;
    }

    public static void main(String args[]) {
    	double[][] data = new double[][]{
    			{1,2},{4,5}
    	};
    	
        GraphScatterPlotter x = new GraphScatterPlotter("Result","X","Y",data);
        
    }
}