package damp.ekeko.snippets.gui;

//import org.eclipse.birt.chart.device.IDeviceRenderer;
//import org.eclipse.birt.chart.exception.ChartException;
//import org.eclipse.birt.chart.factory.GeneratedChartState;
//import org.eclipse.birt.chart.factory.Generator;
//import org.eclipse.birt.chart.model.Chart;
//import org.eclipse.birt.chart.model.attribute.Bounds;
//import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
//import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class ChartCanvas extends Canvas implements PaintListener {
//	private IDeviceRenderer idr = null;
//	private Chart chart = null;

	public ChartCanvas(Composite parent, int style) {
		super(parent, style);
		addPaintListener(this);
		// INITIALIZE THE SWT RENDERING DEVICE
//		final PluginSettings ps = PluginSettings.instance();
//		try {
//			idr = ps.getDevice("dv.SWT");
//		} catch (ChartException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * The SWT paint callback
	 */
	public void paintControl(PaintEvent pe)
	{	
//		try{
//			if (chart == null) return;
//			
//			idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, pe.gc);
//
//			Composite co = (Composite) pe.getSource();
//			org.eclipse.swt.graphics.Rectangle re = co.getClientArea();
//			Bounds bo = BoundsImpl.create(re.x, re.y, re.width, re.height);
//			bo.scale(72d / idr.getDisplayServer().getDpiResolution());
//			// BOUNDS MUST BE SPECIFIED IN POINTS
//
//			// BUILD AND RENDER THE CHART
//			Generator gr = Generator.instance();
//			GeneratedChartState gcs = gr.build( idr.getDisplayServer( ), chart, bo, null, null, null );
//			gr.render(idr, gcs);
//		} catch (ChartException e) {
//			e.printStackTrace();
//		}
	}

//	public Chart getChart() {
//		return chart;
//	}

//	public void setChart(Chart chart) {
//		this.chart = chart;
//	}
}
