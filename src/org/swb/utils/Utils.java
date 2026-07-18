package org.swb.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;

public class Utils
{
	/** Marca de generación para cache-busting de assets (CSS/JS).
	 *  Se calcula una sola vez por ejecución, así todas las páginas
	 *  del build comparten el mismo valor. */
	private static final String BUILD_VERSION = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

	public String version()
	{
		return BUILD_VERSION;
	}

	public String escapeHtml(String text)
	{
		return StringEscapeUtils.escapeHtml(text);
	}
    public int parseInt(String text)
    {
        return Integer.parseInt(text);
    }
}
