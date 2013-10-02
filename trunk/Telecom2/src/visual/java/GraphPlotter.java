package visual.java;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

public class GraphPlotter {
	
	private XYSeriesCollection dataset;
	private ChartPanel chartpanel;
	
	public GraphPlotter(String title, String xlabel, String ylabel, String[] domain) {
		dataset = new XYSeriesCollection();
		dataset.setAutoWidth(true);
		JFreeChart jfreechart = ChartFactory.createXYAreaChart(title, xlabel, ylabel, dataset, PlotOrientation.VERTICAL, true, true, false);
		jfreechart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) jfreechart.getPlot();
		plot.setDomainAxis(0, new SymbolAxis(xlabel, domain));
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		chartpanel = new ChartPanel(jfreechart);
		chartpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	public void addData(String title, double[] data) {
			
		XYSeries ss = new XYSeries(title);
		for (int i = 0; i < data.length; i++)
			ss.add(i, data[i]);
			dataset.addSeries(ss);
	}
	
	public void addAnnotation(String label, double x, double y) {
		XYPlot plot = (XYPlot)chartpanel.getChart().getPlot();
		XYTextAnnotation annotation = new XYTextAnnotation(label, x,y);
		annotation.setPaint(new Color(200,0,0));
		annotation.setFont(new Font("SansSerif",Font.BOLD,12));
		plot.addAnnotation(annotation);
		
		//HighLowRenderer hlr=(HighLowRenderer)plot.getRenderer();

		XYLineAnnotation a1=new XYLineAnnotation(x, 0, x, chartpanel.getHeight());
		plot.addAnnotation(a1);
	}
	
	
	public void addAnnotation() {
		
	}

	public ChartPanel getChart() {
		return chartpanel;
	}
	
	public void save(String file) {
		try {
			Thread.sleep(1000); // avoid concurrent modification, by waiting the graph to be displayed
			ChartUtilities.saveChartAsPNG(new File(file), chartpanel.getChart(), 980, 550);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String args[]) throws Exception {
		GraphPlotter gp = drawGraph("Main Frame","Title", "s1", "xlabel", "ylabel",new String[]{"a","b","c","d"},new double[]{1,4,9,16});
		gp.addData("s2",new double[]{1,2,3,4});
		gp.addAnnotation("marco", 2.3, 2);
		//gp.save("x.png");
		
		
		List<String[]> domains = new ArrayList<String[]>();
		domains.add(new String[]{"1","2","3","4"});
		domains.add(new String[]{"1","2","3","4"});
		domains.add(new String[]{"1","2","3","4"});
		domains.add(new String[]{"1","2","3","4"});
		
		
		List<String> titles = new ArrayList<String>();
		titles.add("Title 1");
		titles.add("Title 2");
		titles.add("Title 3");
		titles.add("Title 4");
		
		List<double[]> data = new ArrayList<double[]>();
		data.add(new double[]{1,1,3,4});
		data.add(new double[]{2,2,3,4});
		data.add(new double[]{1,2,3,5});
		data.add(new double[]{1,3,3,4});
		
		
		List<String> labels = new ArrayList<String>();
		labels.add("s1");
		labels.add("s2");
		labels.add("s3");
		labels.add("s4");
		
		GraphPlotter[] gps = drawMultiGraph(2,2,"Main Frame",titles, "xlabel", "ylabel",labels, domains,data);
		gps[2].addData("s2",new double[]{1,2,3,4});
	}

	public static GraphPlotter drawGraph(String frameTitle, String title, String label, String xlabel, String ylabel, String[] domain, double[] data)  {
		JFrame frame = new JFrame(frameTitle);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GraphPlotter panel = new GraphPlotter(title,xlabel,ylabel,domain);
		panel.addData(label, data);
		frame.setContentPane(panel.getChart());
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
		return panel;
	}

	public static GraphPlotter[] drawMultiGraph(int n_panel_x, int n_panel_y, String frameTitle, List<String> titles, String xlabel, String ylabel, List<String> labels, List<String[]> domains, List<double[]> data)  {
	
		if(n_panel_x*n_panel_y != domains.size()) {
			System.err.println("GraphPlotter: n_panel_x*n_panel_y must be equal to domains.size!");
			return null;
		}
		
		JFrame frame = new JFrame(frameTitle);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel mainPanel = new JPanel(new GridLayout(n_panel_x, n_panel_y));

		GraphPlotter[] panel = new GraphPlotter[domains.size()];
		for (int i = 0; i < domains.size(); i++) {
			panel[i]  = new GraphPlotter(titles.get(i),xlabel,ylabel,domains.get(i));
			panel[i].addData(labels.get(i), data.get(i));
			mainPanel.add(panel[i].getChart());
		}
		frame.setContentPane(mainPanel);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
		return panel;
	}
}
