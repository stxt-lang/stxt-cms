# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Working norms (session rules)

- **This project (`stxt-cms`)** and **`../stxt-web`**: free to read and edit.
- **`../stxt-dev`**: read-only. **Never write, generate, or delete there** — the user regenerates it exclusively via `./generate.sh` (and the other scripts). Do not run `generate.sh`/`clean.sh`/`compile_sass.sh` to "rebuild"; propose changes and let the user generate.
- **Git**: the user handles all commits and pushes, in every repo. Do not `git add`/`commit`/`push` unless explicitly asked.
- **`../stxt-java`**: read-only reference — the source code of `lib/stxt-parser-0.1.0.jar` (the `dev.stxt` parser: `Parser`, `Node`, …). Consult it to see what those classes actually do when working here.

## What this is

`stxt-cms` (internal package `org.swb`, "Semantic Web Builder") is a small Java static-site generator that builds the STXT language website/book. It reads `.stxt` content files, renders them through Velocity templates into HTML, compiles SCSS, and writes the finished site to a sibling output directory. It is a build tool with **no server component of its own** — the generated site is served with `http-server`.

The engine is generic: what it does is entirely driven by [processor.properties](processor.properties), a declarative pipeline of named commands. Changing the build means editing that file, not the Java.

## Repository layout of inputs/outputs

Paths are relative to this project and defined at the top of [processor.properties](processor.properties):

- **`../stxt-web`** (`$web_pages`) — Source content, one `.stxt` file per page, in `en/` and `es/`. This is a separate git repo with its own `CLAUDE.md` describing the STXT language and content conventions. Read it before touching content or the `dev.stxt.website` document format.
- **`../stxt-dev`** (`$web_out`) — Generated output site (git-ignored territory; produced by the build, `es/` under a subfolder, English at root). Served on port 8080.
- **[static/](static/)** — Static assets (incl. compiled `static/css/`) copied verbatim into the output.
- **[scss/](scss/)** — Sass sources compiled to `static/css/` before generation.
- **[templates/](templates/)** — Velocity templates (`.vm`). `page.vm` is the entry template.
- **[lang/](lang/)** — Per-language i18n property files (`pages_es.properties`, `pages_en.properties`) with menu/footer strings and the `lang` code.

## Commands

The `.sh` wrappers re-launch themselves in Konsole when not run from a terminal; the real work is the last line of each. From the project root:

```bash
# Full build: compile SCSS, then run the "main" pipeline -> ../stxt-dev
./generate.sh
# equivalently:
sass scss:static/css --style=compressed
java -cp 'bin:lib/*' org.swb.Executor processor.properties main

# Compile SCSS only
./compile_sass.sh          # sass scss:static/css --style=compressed

# Delete the output directory (runs the "clean" pipeline)
./clean.sh                 # java -cp 'bin:lib/*' org.swb.Executor processor.properties clean

# Serve the generated site (from ../stxt-dev on :8080)
./start_server.sh          # http-server . -p 8080 -c-1
```

`Executor` takes two args: the properties file (default `processor.properties`) and the pipeline name / command list to run (default `main`). Compilation is done by Eclipse (JDT) into `bin/` — there is no Maven/Gradle/Ant build for the Java itself; `packaging-build.xml` only jars `bin/` for distribution. Dependencies are the loose jars in [lib/](lib/) (Velocity 1.7, commonmark, jackson, commons-*, and `stxt-parser-0.1.0.jar`). There is no test suite; several classes carry a `main()` for ad-hoc manual checks (e.g. `WikiRender`, `VelocityUtils`).

## Architecture: the processor pipeline

The whole system is a **generic command runner** ([Executor.java](src/org/swb/Executor.java)) over an implicit shared context:

1. A pipeline (e.g. `main=`) in `processor.properties` is a comma-separated list of **command names**.
2. For each command name `X`, the value `X=SomeType` names a Java class in `org.swb.processor`, and all `X.*` keys become that instance's config (`X.dir`, `X.todir`, `X.out`, …). `Executor` reflectively instantiates `org.swb.processor.<Type>` and calls `init(name, config)`.
3. All processors then run in order, sharing a single `Map<String,Object> context`. Processors communicate purely through named context slots: a `Read*` writes its result under its `.out` key, and `Velocity` reads it back via `.in`.

To add a build step, write a class implementing [`Processor`](src/org/swb/Processor.java) (`init` + `execute(context)`) and reference it from `processor.properties` — no wiring code. Directory-walking steps should extend [`AbstractDirProcessor`](src/org/swb/processor/AbstractDirProcessor.java) (handles `dir`/`todir`/`filter` with Ant-style glob matching); readers that parse a directory of files into a `name -> object` map should extend [`AbstractRead`](src/org/swb/processor/AbstractRead.java).

### The `main` pipeline, in order

Copy assets → copy raw pages → **`ReadStxt`** parses each `.stxt` into a `dev.stxt.Node` tree (stored as `pages_es` / `pages_en`) → `ReadProperties` loads `lang/` → `VelocityInit` boots the engine against `templates/` → for each language: `InsertProperties` sets `nav_lang`, then **`Velocity`** renders every page's Node tree through `page.vm` → `ReplaceText` swaps the `@STXT@` token for styled markup → `Sitemap` emits `sitemap.xml`.

### Rendering model

[`Velocity.java`](src/org/swb/processor/Velocity.java) iterates the `in` map (page name → Node) and merges each through the `template` into `todir/<name>.html`. Every render exposes in the Velocity context: `$doc` (the page's root Node), `$doc_name`, `$index` (the `_index` page's Node, i.e. site navigation), `$nav_lang`/`$langs`, plus helper beans `$wiki` ([`WikiRender`](src/org/swb/utils/WikiRender.java), commonmark → HTML with GFM tables; `render` / `renderNoP`) and `$utils`.

Templates in [templates/](templates/) walk the Node tree: [main_content.vm](templates/main_content.vm) `#foreach`es `$doc.children` and delegates to [node.vm](templates/node.vm), which switches on `$node.normalizedName` to emit HTML per STXT node type (`header`, `subheader`, `content`, `code`, `assert`, `alert`, `link`, …). Content text is run through `$wiki.render`. So the visual output of a given STXT node type is defined in `node.vm`, while page structure lives in `page.vm` → its `#parse`d partials.

## Editing notes

- **Content and the STXT language itself live in `../stxt-web`** — see that repo's `CLAUDE.md`. `.stxt` files are tab-indented and indentation *is* the structure; do not reformat.
- Property files under `lang/` and `processor.properties` are read with legacy encoding (`Cp1252`); non-ASCII in existing files may appear mangled (`Configuraci�n`) — preserve bytes rather than "fixing" them unless intentionally re-encoding.
- Adding/renaming a page: drop the `.stxt` in `../stxt-web/{es,en}`; the pipeline discovers files by directory scan. Wiring it into site nav means editing `_index.stxt` there.
- Adding a new STXT node type to the visuals: handle its `normalizedName` in [node.vm](templates/node.vm) and style it in [scss/](scss/).
