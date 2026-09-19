# stxt-cms

The static site generator that builds **<https://stxt.dev>**, the portal of the
[STXT](https://stxt.dev) language.

Every page of `stxt.dev` is an STXT document, and goes through this generator. Adding `.stxt`
to the address of any page shows its source, for example <https://stxt.dev/faq.stxt>.

> **Status.** This was an internal tool, and it is now public so it can be read and tried. There is
> still a lot to polish (no test suite, and the code comments are in Spanish), but it is a real
> CMS running on STXT, in production for the portal of the language.

## What it does

1. Reads the pages of the portal: one `.stxt` document per page, in English and Spanish, from
   the [stxt-lang](https://github.com/stxt-lang/stxt-lang) repository.
2. Parses them with the STXT Java library
   ([`dev.stxt:stxt-core`](https://central.sonatype.com/artifact/dev.stxt/stxt-core)).
3. Renders them to HTML with Velocity templates.
4. Compiles the SCSS, and writes the finished site.

## How it works

The engine is a small, generic **pipeline executor** (`org.swb.Executor`). Everything it does is
declared in [`processor.properties`](processor.properties):

- A pipeline (`main=`) is a comma-separated list of command names.
- Each command `X` names a processor class (`X=CopyFiles`, `X=ReadStxt`, `X=Velocity`...), plus its
  configuration as `X.*` keys (`X.dir`, `X.todir`, `X.out`...).
- The executor instantiates `org.swb.processor.<Type>` by reflection, and runs the commands in order.
- Processors share a single context map. A reader writes its result under its `.out` key, and a
  renderer picks it up through `.in`.

Changing what the build does means editing `processor.properties`. Adding a step means writing one
class that implements `Processor`, and naming it in the properties file.

The `main` pipeline, in order:

| Step | What it does |
|---|---|
| Copy static resources | Icons, fonts, `_headers`, `_redirects` |
| `CopyHashed` | Copies CSS and JS with their content hash in the file name: `site.css` is published as `site.<hash>.css`, and the templates reference it through `$utils.assetPath()` |
| Copy sources | The raw `.stxt` files, next to the generated pages |
| `ReadStxt` | Parses every page into an STXT tree |
| i18n | Loads the properties of each language |
| `Velocity` | Renders every page per language: `page.vm` walks the document tree, and delegates each node to `node.vm` by its canonical name |
| Post-process | Replaces text tokens |
| Sitemap | Generates `sitemap.xml` |

## Layout

| Path | What it is |
|---|---|
| `processor.properties` | The build, declared: variables, pipelines, commands |
| `src/main/java` | The executor, the processors and the template helper beans |
| `templates/` | Velocity templates: `page.vm` is the entry point, `node.vm` renders each node type |
| `scss/` | Sass sources, compiled to `static/css/` before generating |
| `static/` | Static assets, copied to the site root (icons as they are, CSS and JS renamed with their content hash) |
| `lang/` | Per-language properties (`pages_en`, `pages_es`) for menus, footer and UI texts |

The input and output directories are variables at the top of `processor.properties`. By default
they point to the sibling checkouts `../stxt-lang` (content) and `../stxt-dev` (generated site).

## Building and running

Requirements: Java 11+, Maven and the [`sass`](https://sass-lang.com/install) CLI.

```bash
mvn compile                        # compiles to target/classes
mvn dependency:copy-dependencies   # fills target/dependency/ (the runtime classpath)

./generate.sh                      # compile SCSS + run the "main" pipeline
./compile_sass.sh                  # SCSS only
./clean.sh                         # delete the output directory
./start_server.sh                  # serve the generated site locally on port 8080
```

`generate.sh` runs these two commands:

```bash
sass scss:static/css --style=compressed
java -cp "target/classes:target/dependency/*" org.swb.Executor processor.properties main
```

To try it with other content, copy `processor.properties`, point `$web_pages` and `$web_out`
at other directories, and pass that file as the first argument.

## License

MIT © stxt-lang.
