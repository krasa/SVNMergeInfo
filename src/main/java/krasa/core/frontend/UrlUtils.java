package krasa.core.frontend;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.SharedResourceReference;

/**
 * @author Vojtech Krasa
 */
public class UrlUtils {
	public static String getAbsoluteUrl(SharedResourceReference reference) {
		RequestCycle requestCycle = RequestCycle.get();
		assert requestCycle != null;
		CharSequence resetUrl = requestCycle.urlFor(reference, null);
		assert resetUrl != null;
		String abs = RequestUtils.toAbsolutePath("/", "../intellijPlugin");
		Url url = Url.parse(abs);
		return requestCycle.getUrlRenderer().renderFullUrl(url);
	}

	public static String getAbsoluteUrl(Class<? extends Page> reference) {
		RequestCycle requestCycle = RequestCycle.get();
		assert requestCycle != null;
		CharSequence resetUrl = requestCycle.urlFor(reference, null);
		assert resetUrl != null;
		String abs = RequestUtils.toAbsolutePath("/", resetUrl.toString());
		Url url = Url.parse(abs);
		return requestCycle.getUrlRenderer().renderFullUrl(url);
	}
}
