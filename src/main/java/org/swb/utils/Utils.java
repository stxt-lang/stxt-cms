package org.swb.utils;

import java.io.File;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class Utils
{
	/** Directorio raíz de los assets estáticos (relativo al cwd de la generación). */
	private static final String STATIC_DIR = "static";

	/** Caché de hashes por ruta: cada asset se lee y hashea una sola vez por build. */
	private static final Map<String, String> HASH_CACHE = new ConcurrentHashMap<String, String>();

	/**
	 * Devuelve un hash corto (sha1, 10 hex) del contenido de un asset, para
	 * cache-busting: el token sólo cambia cuando el fichero cambia de verdad.
	 * @param path ruta pública del asset, p.ej. "/css/site.css" o "js/copy-code.js";
	 *             se resuelve contra el directorio "static".
	 */
	public String assetHash(String path)
	{
		String key = path.startsWith("/") ? path.substring(1) : path;
		String cached = HASH_CACHE.get(key);
		if (cached != null) return cached;

		String hash;
		try
		{
			byte[] bytes = FileUtils.readFileToByteArray(new File(STATIC_DIR, key));
			byte[] digest = MessageDigest.getInstance("SHA-1").digest(bytes);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 5; i++) sb.append(String.format("%02x", digest[i]));
			hash = sb.toString();
		}
		catch (Exception e)
		{
			System.err.println("assetHash: no se pudo hashear '" + key + "': " + e.getMessage());
			hash = "0";
		}

		HASH_CACHE.put(key, hash);
		return hash;
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
