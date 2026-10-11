/*
 * Highlights the STXT blocks of the page (<pre class="code-stxt">, emitted by
 * templates/node.vm for the Code nodes) with the static highlighter of
 * @stxt-lang/editor (static/js/stxt-highlight.js, global StxtHighlight), which
 * takes its tokens from the parser of @stxt-lang/core: the portal carries no
 * grammar of the language. The palette is in scss/_panels.scss.
 *
 * These blocks carry no "language-*" class on purpose: Prism, which highlights
 * the other languages, would take them as its own and rewrite them as plain text.
 */
(function () {
	if (window.StxtHighlight) {
		window.StxtHighlight.highlightAll('pre.code-stxt code');
	}
})();
