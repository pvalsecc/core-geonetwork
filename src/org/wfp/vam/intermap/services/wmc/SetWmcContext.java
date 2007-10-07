package org.wfp.vam.intermap.services.wmc;

import java.net.URLDecoder;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl.WMCFactory;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCViewContext;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCWindow;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl.Utils;
import org.wfp.vam.intermap.services.map.MapUtil;

/**
 * Set the WMC from an URL-encoded parameter.
 *
 * @author Etj
 */
public class SetWmcContext implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String wmc = params.getChildText("wmc");
		String dec = URLDecoder.decode(wmc, "UTF-8");

//		System.out.println("DECODED\n" + dec);

		Element mapContext = Xml.loadString(dec, false);

//		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
//		System.out.println(" ============= request wmc is:\n\n" +xo.outputString(mapContext));

		// Create a new MapMerger object
		String sreplace  = params.getChildText("clearLayers");
		boolean breplace = Utils.getBooleanAttrib(sreplace, true);

		MapMerger mm = breplace?
							new MapMerger():
							MapUtil.getMapMerger(context);

		WMCViewContext vc = WMCFactory.parseViewContext(mapContext);
		WMCWindow win = vc.getGeneral().getWindow();

		String url = MapUtil.setContext(mm, vc);

		// Update the user session
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);

		return new Element("response")
			.addContent(new Element("imgUrl").setText(url))
			.addContent(new Element("scale").setText(mm.getDistScale()))
			.addContent(mm.getBoundingBox().toElement())
			.addContent(new Element("width").setText("" + win.getWidth()))
			.addContent(new Element("height").setText("" + win.getHeight()));
	}

}

//=============================================================================
