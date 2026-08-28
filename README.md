# stxt-cms

The static site generator that builds **<https://stxt.dev>**, the [STXT](https://stxt.dev)
language portal. It reads the portal's pages — one `.stxt` document per page, in English and
Spanish, from the [stxt-lang](https://github.com/stxt-lang/stxt-lang) repository — parses them
with the STXT Java library ([`dev.stxt:stxt-core`](https://central.sonatype.com/artifact/dev.stxt/stxt-core)),
renders them to HTML with Velocity templates, compiles the SCSS, and writes the finished site.
The portal is its own proof: every page you read on `stxt.dev` went through this generator, and
appending `.stxt` to any page's address shows the source it was built from.

> **Status.** This was an internal tool, made public so it can be read and tried. There is still
> a lot to polish — no test suite, and the code comments are in Spanish — but it is a real CMS
> running on STXT, in production for the language's own portal.

## How it works

The engine is a small, generic **pipeline executor** (`org.swb.Executor`). Everything it does is
declared in [`processor.properties`](processor.properties):

- A pipeline (e.g. `main=`) is a comma-separated list of command names.
- Each command `X` names a processor class (`X=CopyFiles`, `X=ReadStxt`, `X=Velocity`…) plus its
  configuration as `X.*` keys (`X.dir`, `X.todir`, `X.out`…). The executor instantiates
  `org.swb.processor.<Type>` reflectively and runs the commands in order.
- Processors share a single context map and communicate only through named slots: a reader writes
  its result under its `.out` key, a renderer picks it up through `.in`.

Changing what the build does means editing `processor.properties`, not Java. Adding a step means
writing one class that implements `Processor` and referencing it from the properties file — no
wiring code.

The `main` pipeline, in order: copy static resources → copy the raw `.stxt` sources next to the
generated pages → parse every page into an STXT tree (`ReadStxt`) → load the i18n properties →
initialize Velocity → render every page per language (`page.vm` walks the document tree and
delegates each node to `node.vm` by its canonical name) → post-process text tokens → generate
`sitemap.xml`.

## Layout

| Path | What it is |
|---|---|
| `processor.properties` | The build, declared: variables, pipelines, commands |
| `src/main/java` | The executor, the processors and the template helper beans |
| `templates/` | Velocity templates: `page.vm` is the entry point, `node.vm` renders each node type |
| `scss/` | Sass sources, compiled to `static/css/` before generating |
| `static/` | Static assets, copied verbatim to the site root (CSS, JS, icons, Prism bundle) |
| `lang/` | Per-language properties (`pages_en`, `pages_es`) for menus, footer and UI texts |

The input and output directories are variables at the top of `processor.properties`; by default
they point to the sibling checkouts `../stxt-lang` (content) and `../stxt-dev` (generated site).

## Building and running

Requirements: Java 11+, Maven, the [`sass`](https://sass-lang.com/install) CLI.

```bash
mvn compile                        # compiles to target/classes
mvn dependency:copy-dependencies   # fills target/dependency/ (the runtime classpath)

./generate.sh                      # compile SCSS + run the "main" pipeline
./compile_sass.sh                  # SCSS only
./clean.sh                         # delete the output directory
./start_server.sh                  # serve the generated site locally on port 8080
```

`generate.sh` boils down to:

```bash
sass scss:static/css --style=compressed
java -cp "target/classes:target/dependency/*" org.swb.Executor processor.properties main
```

To experiment without the STXT portal content, point `$web_pages` and `$web_out` in a copy of
`processor.properties` at your own directories and pass that file as the first argument.

## License

MIT © stxt-lang.
