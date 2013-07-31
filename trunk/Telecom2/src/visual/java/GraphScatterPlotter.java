package visual.java;

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

    public GraphScatterPlotter(String title, String x, String y, List<double[][]> ldata, List<String> labels) {
        super(title);
        JPanel jpanel = createScatterPlot(title,x,y,ldata,labels);
        jpanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(jpanel);
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    public static JPanel createScatterPlot(String title,String x, String y, List<double[][]> ldata, List<String> labels) {
    	
    	XYDataset ds = getDataset(ldata,labels);
    	
        JFreeChart jfreechart = ChartFactory.createScatterPlot(title, x, y, ds, PlotOrientation.VERTICAL, true, true, false);
        Shape shape = ShapeUtilities.createDiamond(100);

        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setBaseShape(shape);
       
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);

        return new ChartPanel(jfreechart);
    }

    private static XYDataset getDataset(List<double[][]> ldata, List<String> labels) {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        
        for(int i=0; i<ldata.size();i++) {
	        XYSeries series = new XYSeries(labels.get(i));
	        for(double[] d: ldata.get(i))
	         series.add(d[0], d[1]);
	        xySeriesCollection.addSeries(series);
        }
        
        return xySeriesCollection;
    }

    public static void main(String args[]) {
    	List<double[][]> ldata = new ArrayList<double[][]>();
    	ldata.add(new double[][]{{1,2},{4,5}});
    	ldata.add(new double[][]{{4,2},{3,5}});
    	ldata.add(new double[][]{{3,2},{3.5,5}});
    	
    	List<String> labels = new ArrayList<String>();
    	labels.add("data1");
    	labels.add("data2");
    	labels.add("data3");
    	
        new GraphScatterPlotter("Result","X","Y",ldata,labels);
        
    }
}