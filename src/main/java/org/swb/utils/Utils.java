package org.swb.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.TextNode;

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
		return playgroundUrl(text, title, Collections.<String>emptyList());
	}

	/**
	 * Como {@link #playgroundUrl(String, String)}, pero añadiendo las gramáticas que el
	 * documento del bloque necesita para validar y que están en otros bloques Code de la misma
	 * página: un parámetro "&amp;g=&lt;payload&gt;" por gramática, que el playground recibe
	 * como documento aparte (ver README de stxt-play). Un namespace que el propio bloque ya
	 * define no viaja; para los demás rige la misma regla que website.test.ts de stxt-js
	 * aplica al validar los ejemplos del portal: la definición «más cercana» de la página, la
	 * última mostrada antes del bloque o, si ninguna lo precede, la primera mostrada después.
	 * Así el portal puede enseñar documento y gramática por separado y aun así enviarlos
	 * juntos al playground.
	 * @param page  el nodo raíz de la página ($doc en las plantillas).
	 * @param code  el nodo del bloque de código (un TextNode "Code").
	 * @param title el título del documento en el playground, normalmente el de la página.
	 */
	public String playgroundUrl(Node page, Node code, String title)
	{
		String text = code == null ? "" : code.getText();
		return playgroundUrl(text, title, grammarsFor(page, code, text));
	}

	private String playgroundUrl(String text, String title, List<String> grammars)
	{
		StringBuilder url = new StringBuilder(PLAYGROUND_URL);
		url.append("#d=").append(encodeFragmentPayload(text));
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
		for (String grammar : grammars)
		{
			url.append("&g=").append(encodeFragmentPayload(grammar));
		}
		return url.toString();
	}

	/** Base64url (sin relleno) del deflate-raw del texto en UTF-8: el payload de los fragmentos. */
	private static String encodeFragmentPayload(String text)
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
		return Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray());
	}

	/** Una definición de la página: un bloque Code de solo gramáticas, con su posición. */
	private static final class PageDefinition
	{
		final int position;
		final Set<String> namespaces;
		final String text;

		PageDefinition(int position, Set<String> namespaces, String text)
		{
			this.position = position;
			this.namespaces = namespaces;
			this.text = text;
		}
	}

	/** Caché por página (identidad del nodo raíz) de sus definiciones, en orden de lectura. */
	private static final Map<Node, List<PageDefinition>> PAGE_DEFINITIONS_CACHE =
			Collections.synchronizedMap(new IdentityHashMap<Node, List<PageDefinition>>());

	/**
	 * Las gramáticas de la página que el documento de un bloque Code necesita: las de los
	 * namespaces que usa y no define él mismo, cada uno resuelto a su definición más cercana
	 * (la última anterior al bloque o, si no hay, la primera posterior), en orden de aparición
	 * y sin repetir bloque. Un texto que no parsea no necesita nada (el enlace va sin
	 * gramáticas, como hasta ahora).
	 */
	private List<String> grammarsFor(Node page, Node code, String text)
	{
		List<String> grammars = new ArrayList<String>();
		List<Node> roots = parseQuiet(text);
		if (page == null || roots == null) return grammars;

		Set<String> defined = definedNamespaces(roots);
		List<PageDefinition> definitions = pageDefinitions(page);
		int position = positionOf(page, code);
		Set<String> added = new HashSet<String>(); // por texto de bloque: una gramática puede definir varios namespaces

		for (String namespace : usedNamespaces(roots))
		{
			if (defined.contains(namespace)) continue;
			PageDefinition definition = closestDefinition(definitions, namespace, position);
			if (definition != null && added.add(definition.text)) grammars.add(definition.text);
		}
		return grammars;
	}

	/** La posición del bloque entre los Code de la página (por identidad), o -1 si no está. */
	private static int positionOf(Node page, Node code)
	{
		List<TextNode> blocks = codeBlocksOf(page);
		for (int i = 0; i < blocks.size(); i++)
		{
			if (blocks.get(i) == code) return i;
		}
		return -1;
	}

	/**
	 * La definición de un namespace que aplica al bloque de una posición: la última mostrada
	 * antes o, si ninguna lo precede, la primera mostrada después. Es la misma regla con la
	 * que website.test.ts de stxt-js valida los ejemplos del portal.
	 */
	private static PageDefinition closestDefinition(List<PageDefinition> definitions, String namespace, int position)
	{
		PageDefinition before = null;
		PageDefinition after = null;
		for (PageDefinition definition : definitions)
		{
			if (!definition.namespaces.contains(namespace)) continue;
			if (definition.position < position) before = definition;
			else if (after == null && definition.position > position) after = definition;
		}
		return before != null ? before : after;
	}

	/**
	 * Las definiciones de la página: los bloques Code cuyos nodos raíz son todos gramáticas
	 * (un bloque que mezcla documento y gramática se envía entero con su propio enlace y no
	 * sirve de definición para otros), con los namespaces que define cada uno. Se calcula una
	 * vez por página.
	 */
	private List<PageDefinition> pageDefinitions(Node page)
	{
		List<PageDefinition> cached = PAGE_DEFINITIONS_CACHE.get(page);
		if (cached != null) return cached;

		List<PageDefinition> definitions = new ArrayList<PageDefinition>();
		List<TextNode> blocks = codeBlocksOf(page);
		for (int position = 0; position < blocks.size(); position++)
		{
			String text = blocks.get(position).getText();
			List<Node> roots = parseQuiet(text);
			if (roots == null || roots.isEmpty()) continue;

			Set<String> namespaces = definedNamespaces(roots);
			boolean allGrammars = true;
			for (Node root : roots)
			{
				if (!isGrammarRoot(root)) { allGrammars = false; break; }
			}
			if (!allGrammars || namespaces.isEmpty()) continue;

			definitions.add(new PageDefinition(position, namespaces, text));
		}

		PAGE_DEFINITIONS_CACHE.put(page, definitions);
		return definitions;
	}

	/** Los bloques Code de la página, en orden de documento. */
	private static List<TextNode> codeBlocksOf(Node node)
	{
		List<TextNode> blocks = new ArrayList<TextNode>();
		collectCodeBlocks(node, blocks);
		return blocks;
	}

	private static void collectCodeBlocks(Node node, List<TextNode> blocks)
	{
		if (node instanceof TextNode && "code".equals(node.getCanonicalName()))
		{
			blocks.add((TextNode) node);
		}
		if (node instanceof InlineNode)
		{
			for (Node child : ((InlineNode) node).getChildren()) collectCodeBlocks(child, blocks);
		}
	}

	/** Los namespaces reservados de las gramáticas (normativos y estables desde la 1.0). */
	private static final String SCHEMA_NAMESPACE = "@stxt.schema";
	private static final String TEMPLATE_NAMESPACE = "@stxt.template";

	/** true si el nodo raíz define una gramática: un esquema o una plantilla. */
	private static boolean isGrammarRoot(Node root)
	{
		String namespace = root.getNamespace();
		return SCHEMA_NAMESPACE.equals(namespace) || TEMPLATE_NAMESPACE.equals(namespace);
	}

	/** El namespace que define una raíz de gramática (su valor), o "" si no es una gramática inline. */
	private static String definedNamespaceOf(Node root)
	{
		if (!isGrammarRoot(root) || !(root instanceof InlineNode)) return "";
		return ((InlineNode) root).getValue().trim().toLowerCase(Locale.ROOT);
	}

	/** Los namespaces que definen las raíces de gramática de un bloque. */
	private static Set<String> definedNamespaces(List<Node> roots)
	{
		Set<String> defined = new HashSet<String>();
		for (Node root : roots)
		{
			String namespace = definedNamespaceOf(root);
			if (namespace.length() > 0) defined.add(namespace);
		}
		return defined;
	}

	/** Los namespaces efectivos que usa un árbol, en orden de aparición, sin los @stxt.* reservados. */
	private static Set<String> usedNamespaces(List<Node> roots)
	{
		Set<String> used = new LinkedHashSet<String>();
		for (Node root : roots) collectUsedNamespaces(root, used);
		return used;
	}

	private static void collectUsedNamespaces(Node node, Set<String> used)
	{
		String namespace = node.getNamespace();
		if (namespace != null && namespace.length() > 0 && !namespace.startsWith("@")) used.add(namespace);
		if (node instanceof InlineNode)
		{
			for (Node child : ((InlineNode) node).getChildren()) collectUsedNamespaces(child, used);
		}
	}

	/** Parsea un texto STXT, o null si no es válido: un ejemplo de error se enlaza sin gramáticas. */
	private static List<Node> parseQuiet(String text)
	{
		try
		{
			return new Parser().parse(text == null ? "" : text);
		}
		catch (RuntimeException e)
		{
			return null;
		}
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
