# CLAUDE.md

Este archivo proporciona orientación a Claude Code (claude.ai/code) cuando se trabaja con el código de este repositorio.

## Normas de trabajo (reglas de la sesión)

- **Este proyecto (`stxt-cms`)** y **`../stxt-web`**: libres para leer y editar.
- **`../stxt-dev`**: solo lectura. **Nunca escribir, generar ni borrar allí** — el usuario lo regenera exclusivamente mediante `./generate.sh` (y los demás scripts). No ejecutar `generate.sh`/`clean.sh`/`compile_sass.sh` para «reconstruir»; proponer cambios y dejar que el usuario genere.
- **Git**: el usuario gestiona todos los commits y pushes en cada repositorio. No ejecutar `git add`/`commit`/`push` salvo que se indique explícitamente.
- **`../stxt-java`**: referencia solo de lectura — el código fuente de `lib/stxt-parser-0.1.0.jar` (el analizador `dev.stxt`: `Parser`, `Node`, …). Consultarlo para ver lo que hacen realmente estas clases al trabajar aquí.

## Qué es esto

`stxt-cms` (paquete interno `org.swb`, «Semantic Web Builder») es un pequeño generador de sitios estáticos en Java que construye la web/libro del lenguaje STXT. Lee archivos de contenido `.stxt`, los renderiza mediante plantillas Velocity a HTML, compila SCSS y escribe el sitio terminado en un directorio de salida hermano. Es una herramienta de compilación sin **ningún componente de servidor propio** — el sitio generado se sirve con `http-server`.

El motor es genérico: lo que hace depende por completo de [processor.properties](processor.properties), un pipeline declarativo de comandos con nombre. Cambiar la compilación significa editar ese archivo, no Java.

## Estructura del repositorio: entradas y salidas

Las rutas son relativas a este proyecto y se definen en la parte superior de [processor.properties](processor.properties):

- **`../stxt-web`** (`$web_pages`) — contenido fuente, un archivo `.stxt` por página, en `en/` y `es/`. Es un repositorio Git aparte con su propio `CLAUDE.md` que describe el lenguaje STXT y las convenciones de contenido. Léelo antes de tocar el contenido o el formato del documento `dev.stxt.website`.
- **`../stxt-dev`** (`$web_out`) — sitio generado (territorio ignorado por Git; producido por la compilación, `es/` bajo una subcarpeta y el inglés en la raíz). Se sirve en el puerto 8080.
- **[static/](static/)** — recursos estáticos (incluido el `static/css/` compilado) copiados literalmente en la salida.
- **[scss/](scss/)** — fuentes Sass compiladas a `static/css/` antes de la generación.
- **[templates/](templates/)** — plantillas Velocity (`.vm`). `page.vm` es la plantilla de entrada.
- **[lang/](lang/)** — archivos de propiedades i18n por idioma (`pages_es.properties`, `pages_en.properties`) con textos de menú/pie y el código `lang`.

## Comandos

Los wrappers `.sh` se vuelven a lanzar en Konsole cuando no se ejecutan desde un terminal; el trabajo real está en la última línea de cada uno. Desde la raíz del proyecto:

```bash
# Compilación completa: compilar SCSS y luego ejecutar el pipeline «main» -> ../stxt-dev
./generate.sh
# de forma equivalente:
sass scss:static/css --style=compressed
java -cp 'bin:lib/*' org.swb.Executor processor.properties main

# Solo compilar SCSS
./compile_sass.sh          # sass scss:static/css --style=compressed

# Borrar el directorio de salida (ejecuta el pipeline «clean»)
./clean.sh                 # java -cp 'bin:lib/*' org.swb.Executor processor.properties clean

# Servir el sitio generado (desde ../stxt-dev en :8080)
./start_server.sh          # http-server . -p 8080 -c-1
```

`Executor` acepta dos argumentos: el archivo de propiedades (por defecto `processor.properties`) y el nombre del pipeline o la lista de comandos a ejecutar (por defecto `main`). La compilación se realiza con Eclipse (JDT) en `bin/` — no existe una build con Maven/Gradle/Ant para Java; `packaging-build.xml` solo empaqueta `bin/` para distribución. Las dependencias son los jars sueltos de [lib/](lib/) (Velocity 1.7, commonmark, jackson, commons-*, y `stxt-parser-0.1.0.jar`). No hay suite de pruebas; varias clases llevan un `main()` para comprobaciones manuales ad hoc (por ejemplo, `WikiRender`, `VelocityUtils`).

## Arquitectura: el pipeline de procesamiento

Todo el sistema es un **ejecutor de comandos genérico** ([Executor.java](src/org/swb/Executor.java)) sobre un contexto compartido implícito:

1. Un pipeline (por ejemplo, `main=`) en `processor.properties` es una lista separada por comas de **nombres de comandos**.
2. Para cada nombre de comando `X`, el valor `X=SomeType` nombra una clase Java en `org.swb.processor`, y todas las claves `X.*` pasan a ser la configuración de esa instancia (`X.dir`, `X.todir`, `X.out`, …). `Executor` instancia reflectivamente `org.swb.processor.<Type>` y llama a `init(name, config)`.
3. A continuación, todos los procesadores se ejecutan en orden, compartiendo un único `Map<String,Object> context`. Los procesadores se comunican únicamente a través de ranuras con nombre en el contexto: un `Read*` escribe su resultado bajo la clave `.out`, y `Velocity` lo lee de nuevo a través de `.in`.

Para añadir un paso de compilación, escribe una clase que implemente [`Processor`](src/org/swb/Processor.java) (`init` + `execute(context)`) y refiérela desde `processor.properties` — sin código de wiring. Los pasos de recorrido de directorios deben extender [`AbstractDirProcessor`](src/org/swb/processor/AbstractDirProcessor.java) (gestionan `dir`/`todir`/`filter` con coincidencia de estilo Ant); los lectores que analizan un directorio de archivos en un mapa `name -> object` deberían extender [`AbstractRead`](src/org/swb/processor/AbstractRead.java).

### El pipeline `main`, en orden

Copiar recursos → copiar páginas en bruto → **`ReadStxt`** analiza cada `.stxt` en un árbol `dev.stxt.Node` (almacenado como `pages_es` / `pages_en`) → `ReadProperties` carga `lang/` → `VelocityInit` inicializa el motor frente a `templates/` → para cada idioma: `InsertProperties` establece `nav_lang`, y luego **`Velocity`** renderiza el árbol de nodos de cada página mediante `page.vm` → `ReplaceText` intercambia el token `@STXT@` por marcado con estilo → `Sitemap` genera `sitemap.xml`.

### Modelo de renderizado

[`Velocity.java`](src/org/swb/processor/Velocity.java) itera el mapa `in` (nombre de página → Node) y combina cada uno mediante la plantilla en `todir/<name>.html`. Cada renderización expone en el contexto de Velocity: `$doc` (el nodo raíz de la página), `$doc_name`, `$index` (el nodo de la página `_index`, es decir, la navegación del sitio), `$nav_lang`/`$langs`, además de los beans auxiliares `$wiki` ([`WikiRender`](src/org/swb/utils/WikiRender.java), commonmark → HTML con tablas GFM; `render` / `renderNoP`) y `$utils` ([`Utils.java`](src/org/swb/utils/Utils.java): `escapeHtml`, `parseInt`, y `assetHash('/path')` — un sha1 corto de un archivo bajo [static/](static/), para cache-busting; véase Notas de edición).

Las plantillas de [templates/](templates/) recorren el árbol de nodos: [main_content.vm](templates/main_content.vm) hace `#foreach` sobre `$doc.children` y delega en [node.vm](templates/node.vm), que comprueba `$node.normalizedName` para generar HTML por tipo de nodo STXT (`header`, `subheader`, `content`, `code`, `assert`, `alert`, `link`, …). El texto del contenido pasa por `$wiki.render`. Así, la salida visual de un tipo de nodo STXT concreto se define en `node.vm`, mientras que la estructura de la página vive en `page.vm` → sus parciales `#parse`.

## Notas de edición

- **El contenido y el propio lenguaje STXT viven en `../stxt-web`** — consulta el `CLAUDE.md` de ese repositorio. Los archivos `.stxt` están indentados con tabulaciones y la indentación *es* la estructura; no reformatearlos.
- Los archivos de propiedades en `lang/` y `processor.properties` se leen con codificación heredada (`Cp1252`); los caracteres no ASCII de los archivos existentes pueden aparecer alterados — conservar los bytes en lugar de «arreglarlos» salvo que se reencodifique intencionalmente.
- Añadir o renombrar una página: dejar el archivo `.stxt` en `../stxt-web/{es,en}`; el pipeline lo descubre mediante escaneo de directorios. Integrarlo en la navegación del sitio significa editar `_index.stxt` allí.
- Añadir un nuevo tipo de nodo STXT a los elementos visuales: manejar su `normalizedName` en [node.vm](templates/node.vm) y estilizarlo en [scss/](scss/).
- Cache-busting de recursos estáticos: referenciar CSS/JS/iconos desde las plantillas como `href="/css/site.css?v=${utils.assetHash('/css/site.css')}"`. `assetHash` devuelve un sha1 corto del archivo resuelto bajo [static/](static/), de modo que el token `?v=` solo cambia cuando cambian los bytes de ese archivo. El paso `copy_resources` copia `static/**` literalmente en la raíz de la salida, por lo que las rutas son absolutas del sitio (`/css/…`, `/favicon.ico`, …). Favicons + `site.webmanifest` viven en `static/` y están conectados en [head.vm](templates/head.vm).
- **El resaltado de sintaxis de los bloques de código** es Prism autohospedado (sin CDN) con dos gramáticas escritas a mano: [static/js/prism-stxt.js](static/js/prism-stxt.js) para bloques `Code >>` (`class="language-stxt"`) y [static/js/prism-ebnf.js](static/js/prism-ebnf.js) para bloques `Grammar >>` (`class="language-ebnf"`); ambas se cargan en [footer.vm](templates/footer.vm) después de `prism-core.min.js`. La paleta es **un esquema semántico compartido por ambos lenguajes**, definido una sola vez como variables `$stxt-*` en [scss/_panels.scss](scss/_panels.scss) y aplicado bajo `pre.language-stxt` / `pre.language-ebnf`: la clave/regla en línea = azul portal en negrita, el encabezado `>>` del bloque = magenta en negrita (token propio `block-node`, reconocido antes que `node`), el valor/literal = terracota, el espacio de nombres = turquesa, los comentarios = verde (recto), y solo los operadores y la puntuación EBNF permanecen en gris pizarra apagado. Los separadores comparten el color de su clave (`:` azul mediante `operator`, `>>` magenta mediante `block-operator`) para que la clave y el separador se lean como una unidad. Editar el aspecto = editar esas variables y reglas, no las gramáticas (las gramáticas solo deciden qué es un token; el orden importa — véase los comentarios de encabezado en `prism-stxt.js`).
