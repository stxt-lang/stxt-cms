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
