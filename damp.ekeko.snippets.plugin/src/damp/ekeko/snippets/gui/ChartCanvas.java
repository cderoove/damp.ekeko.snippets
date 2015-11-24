package damp.ekeko.snippets.gui;

import java.awt.Rectangle;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ChartCanvas extends Canvas implements PaintListener {
	private IDeviceRenderer idr = null;
	private Chart chart = null;
	private GeneratedChartState gcs;
	private boolean bFirstPaint = true;
	private Image imgChart;
	private GC gcImage;
	private Bounds bo;

	public ChartCanvas(Composite parent, int style) {
		super(parent, style);
		addPaintListener(this);
		// INITIALIZE THE SWT RENDERING DEVICE
		final PluginSettings ps = PluginSettings.instance();
		try {
			idr = ps.getDevice("dv.SWT");
		} catch (ChartException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * The SWT paint callback
	 */
	public void paintControl(PaintEvent pe)
	{
		
//		Composite co = (Composite) pe.getSource();
//		org.eclipse.swt.graphics.Rectangle d = co.getClientArea();
//		if ( bFirstPaint  )
//		{
//			imgChart = new Image( this.getDisplay( ), d );
//			gcImage = new GC( imgChart );
//			idr.setProperty( IDeviceRenderer.GRAPHICS_CONTEXT, gcImage );
//
//			bo = BoundsImpl.create( 0, 0, d.width, d.height );
//			bo.scale( 72d / idr.getDisplayServer( ).getDpiResolution( ) );
//		}
//
//		Generator gr = Generator.instance( );
//
//		try
//		{
//			if ( bFirstPaint ) // ++++ added this line. But then data does not
//			{
//				gcs = gr.build( idr.getDisplayServer( ), chart, bo, null, null, null );
//			}
//			gr.render( idr, gcs );
//			GC gc = pe.gc;
//			gc.drawImage( imgChart, d.x, d.y );
//		}
//		catch ( ChartException ce )
//		{
//			ce.printStackTrace( );
//		}
//
//		bFirstPaint = false;
//
//		Display.getDefault( ).timerExec( 500, new Runnable( ) {
//
//			public void run( )
//			{
//				refreshChart();
//			}
//		} );
//		
		chart.unsetDimension();
		
		
		try{
			if (chart == null) return;
			
			idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, pe.gc);

			Composite co = (Composite) pe.getSource();
			org.eclipse.swt.graphics.Rectangle re = co.getClientArea();
			Bounds bo = BoundsImpl.create(re.x, re.y, re.width, re.height);
			bo.scale(72d / idr.getDisplayServer().getDpiResolution());
			// BOUNDS MUST BE SPECIFIED IN POINTS

			// BUILD AND RENDER THE CHART
			Generator gr = Generator.instance();
			gcs = gr.build( idr.getDisplayServer( ), chart, bo, null, null, null );
//			gcs = gr.build(idr.getDisplayServer(), chart,
//					null, bo, null);
			gr.render(idr, gcs);
		} catch (ChartException e) {
			e.printStackTrace();
		}
	}

	public Chart getChart() {
		return chart;
	}

	public void setChart(Chart chart) {
		this.chart = chart;
	}
	
	public void refreshChart() {
		final Generator gr = Generator.instance( );

		try
		{
			gr.refresh( gcs );
		}
		catch ( ChartException ex )
		{
			ex.printStackTrace( );
		}
		redraw( );

	}

}
