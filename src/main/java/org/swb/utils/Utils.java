package org.swb.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	 * Un asset que no se puede leer detiene el build (RuntimeException): una
	 * plantilla que referencia un fichero inexistente es un error, no un "?v=0".
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
			throw new RuntimeException("assetHash: cannot hash '" + key + "': " + e.getMessage(), e);
		}

		HASH_CACHE.put(key, hash);
		return hash;
	}

	public String escapeHtml(String text)
	{
		return StringEscapeUtils.escapeHtml(text);
	}

	/** A heading that starts with a section number: "4.3 Name", "17.1. Name", "2. Name". */
	private static final Pattern SECTION_NUMBER = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)*)\\.?\\s+.*", Pattern.DOTALL);

	/**
	 * Devuelve el ancla estable de un título de sección (Subheader / Subsubheader), la que
	 * usan node.vm y toc.vm como {@code id} del {@code <h2>}/{@code <h3>} y a la que apuntan
	 * los enlaces {@code pagina#ancla} del contenido. A diferencia de {@code index_N}, que
	 * es posicional y cambia al insertar una sección, esta se deriva del propio título:
	 * <ul>
	 * <li>Si el título empieza por un número de sección ("4.3 Nombre canónico", "17.1. Asociación",
	 *     "2. Terminología"), el ancla es ese número con guiones: {@code s4-3}, {@code s17-1},
	 *     {@code s2}. Es lo que ya se cita en prosa como "§4.3", así que renumerar una sección
	 *     obliga a revisar la cita y el enlace en el mismo sitio.</li>
	 * <li>Si no, un slug ASCII del título al estilo de GitHub: sin diacríticos, en minúsculas,
	 *     toda secuencia de caracteres que no sea [a-z0-9] pasa a un guion, sin guiones en los
	 *     extremos: "¿Tabuladores o espacios?" → {@code tabuladores-o-espacios},
	 *     "TypeScript / JavaScript" → {@code typescript-javascript}. El token {@code @STXT@}
	 *     queda como {@code stxt} (sus arrobas se pierden), lo que además impide que la
	 *     sustitución posterior de ReplaceText toque el atributo.</li>
	 * </ul>
	 * Devuelve la cadena vacía si no queda nada (título vacío o sin caracteres ASCII), y
	 * entonces las plantillas no emiten {@code id}. Los títulos de una misma página deben ser
	 * distintos entre sí para que las anclas no choquen; hoy lo son en todo el sitio.
	 * @param text el texto del título tal como está en el nodo.
	 */
	public String headingId(String text)
	{
		if (text == null) return "";
		Matcher m = SECTION_NUMBER.matcher(text);
		if (m.matches()) return "s" + m.group(1).replace('.', '-');
		String s = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		s = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
		return s;
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

	/**
	 * Escapa un texto para incrustarlo dentro de una cadena JSON (el JSON-LD que emite
	 * head.vm): comillas dobles, barra invertida y caracteres de control, y además
	 * {@code <}, {@code >} y {@code &} como {@code \\uXXXX}, para que un
	 * {@code </script>} del contenido no pueda cerrar el bloque {@code <script>}.
	 * @param text el texto a escapar; null devuelve la cadena vacía.
	 */
	public String jsonEscape(String text)
	{
		if (text == null) return "";
		StringBuilder sb = new StringBuilder(text.length() + 16);
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			switch (c)
			{
				case '"': sb.append("\\\""); break;
				case '\\': sb.append("\\\\"); break;
				case '\n': sb.append("\\n"); break;
				case '\r': sb.append("\\r"); break;
				case '\t': sb.append("\\t"); break;
				case '<': sb.append("\\u003C"); break;
				case '>': sb.append("\\u003E"); break;
				case '&': sb.append("\\u0026"); break;
				default:
					if (c < 0x20) sb.append(String.format("\\u%04X", (int) c));
					else sb.append(c);
			}
		}
		return sb.toString();
	}
}
