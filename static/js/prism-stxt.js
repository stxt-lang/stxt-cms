/*
 * Prism language definition for STXT (Semantic Text).
 *
 * STXT is a bespoke, tab-indented language: each node is `Name : value`
 * (inline) or `Name >>` (a block whose indented children are literal text),
 * a name may carry a namespace in parentheses `(a.b.c)` / `(@stxt.template)`,
 * and `#` starts a full-line comment. See ../stxt-vscode/stxt/src/core for the
 * authoritative parser this grammar mirrors (Constants: SEP_NODE ":",
 * SEP_TEXT_NODE ">>", COMMENT_CHAR "#").
 */
(function (Prism) {
	Prism.languages.stxt = {
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
			alias: 'keyword'
		},
		// Namespace / annotation in parentheses: (a.b.c), (@stxt.template), (?), (1).
		'namespace': {
			pattern: /\([^)\n]*\)/,
			inside: {
				'punctuation': /[()]/,
				'important': /@[a-z0-9]+/,
				'operator': /\./,
				'symbol': /[^().]+/
			}
		},
		// Node separators.
		'operator': />>|:/
	};
}(Prism));
