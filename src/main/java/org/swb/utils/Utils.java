package org.swb.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class Utils
{
	/** Directorio raíz de los assets estáticos (relativo al cwd de la generación). */
	private static final String STATIC_DIR = "static";

	/** URL del playground; el fragmento "#d=" abre un documento allí (ver README de stxt-play). */
	private static final String PLAYGROUND_URL = "https://play.stxt.dev/";

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

	/**
	 * Devuelve la URL que abre un texto STXT como documento nuevo en el playground:
	 * "https://play.stxt.dev/#d=&lt;payload&gt;&amp;t=&lt;título&gt;", donde el payload es el
	 * base64url (sin relleno) del deflate-raw del texto en UTF-8, tal como lo lee
	 * `decodeOpen` de stxt-play (src/workspace/share.ts). El título es opcional (en blanco
	 * o null se omite) y va codificado como formulario, igual que lo genera URLSearchParams.
	 * @param text  el texto STXT del bloque de código.
	 * @param title el título del documento en el playground, normalmente el de la página.
	 */
	public String playgroundUrl(String text, String title)
	{
		byte[] input = (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true); // true = raw deflate
		deflater.setInput(input);
		deflater.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream(input.length);
		byte[] buffer = new byte[4096];
		while (!deflater.finished())
		{
			int n = deflater.deflate(buffer);
			out.write(buffer, 0, n);
		}
		deflater.end();

		StringBuilder url = new StringBuilder(PLAYGROUND_URL);
		url.append("#d=").append(Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray()));
		if (title != null && title.trim().length() > 0)
		{
			try
			{
				url.append("&t=").append(URLEncoder.encode(title.trim(), "UTF-8"));
			}
			catch (Exception e)
			{
				// UTF-8 siempre existe; si algo fallara, el enlace va sin título
			}
		}
		return url.toString();
	}
    public int parseInt(String text)
    {
        return Integer.parseInt(text);
    }
}
