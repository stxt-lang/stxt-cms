/*
 * Prism language definition for STXT (Semantic Text).
 *
 * STXT is a bespoke, tab-indented language: each node is `Name : value`
 * (inline) or `Name >>` (a block whose indented children are literal text),
 * a name may carry a namespace in parentheses `(a.b.c)` / `(@stxt.template)`,
 * and `#` starts a full-line comment. See ../stxt-vscode/stxt/src/core for the
 * authoritative parser this grammar mirrors (Constants: SEP_NODE ":",
 * SEP_TEXT_NODE ">>", COMMENT_CHAR "#").
 *
 * Token types are chosen to match the stxt-vscode extension's semantic tokens
 * (see extension/TokenGeneratorObserver.ts) so the portal and the editor share
 * one colour scheme: node names + operators = "property", namespaces =
 * "namespace", inline values = "string", comments = "comment".
 */
(function (Prism) {
	Prism.languages.stxt = {
		// A '>>' block: everything indented deeper than the header line (plus any
		// blank lines) is LITERAL text and must never be tokenised, even when it
		// contains ':' or '>>'. Indentation is the structure, so we anchor to the
		// header's leading whitespace and match children that start with it plus
		// at least one more whitespace char. The header line itself is kept out of
		// the token (lookbehind = group 1) so it still gets node highlighting.
		'block-text': {
			pattern: /((^[ \t]*)[^\n]*?>>[ \t]*\r?\n)(?:\2[ \t]+.*(?:\r?\n|$)|[ \t]*\r?\n)*/m,
			lookbehind: true,
			greedy: true
		},
		// Full-line comment: (indentation) then '#' to end of line.
		'comment': {
			pattern: /(^[ \t]*)#.*/m,
			lookbehind: true,
			greedy: true
		},
		// Node name at the start of a line, up to its ':' or '>>' separator
		// (an optional namespace in parentheses may sit between them).
		'node': {
			pattern: /(^[ \t]*)[^\s:()>][^\n:()>]*?(?=[ \t]*(?:\([^)\n]*\)[ \t]*)?(?::|>>))/m,
			lookbehind: true,
			alias: 'property'
		},
		// Inline value: everything after the first ':' to end of line. Matched
		// BEFORE 'namespace' so anything in the value that merely looks like a
		// namespace — e.g. `Name: text (looks.like.ns)` — or a comment — e.g.
		// `Name: text # not a comment` — stays part of the value, not re-coloured.
		'value': {
			pattern: /(:[ \t]*)[^\n]+/,
			lookbehind: true,
			alias: 'string'
		},
		// Namespace / annotation in parentheses in NAME position: (a.b.c),
		// (@stxt.template), and schema markers such as (?) / (1).
		'namespace': {
			pattern: /\([^)\n]*\)/
		},
		// Node separators.
		'operator': />>|:/
	};
}(Prism));
