package org.swb.utils;

import org.apache.commons.lang.StringEscapeUtils;

public class Utils
{
	public String escapeHtml(String text)
	{
		return StringEscapeUtils.escapeHtml(text);
	}
	public String escapeHtmlBold(String text)
	{
		return TextReplacer.replaceWithStrong(StringEscapeUtils.escapeHtml(text));
	}
    public int parseInt(String text)
    {
        return Integer.parseInt(text);
    }
}
