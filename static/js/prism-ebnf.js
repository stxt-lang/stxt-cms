/*
 * Prism language definition for the informal EBNF notation used in the STXT
 * spec's grammar appendices (Appendix A of stxt-core-ref / stxt-template-ref).
 *
 * That notation is NOT STXT — it must read as a formal grammar, so it gets its
 * own language + palette (see scss/_panels.scss, pre.language-ebnf). Shape:
 *   RuleName = expression        ; comment to end of line
 * with "literals" in double quotes, [optional], {repetition}, (grouping) and
 * | alternation. Plain identifiers on the right are non-terminals (left plain).
 */
(function (Prism) {
	Prism.languages.ebnf = {
		// Terminal literals in double (or single) quotes. First, so a ';' or a
		// bracket that lives inside a literal is never mistaken for syntax.
		'string': {
			pattern: /"[^"\n]*"|'[^'\n]*'/,
			greedy: true
		},
		// Comment: ';' to end of line.
		'comment': {
			pattern: /;.*/,
			greedy: true
		},
		// Rule name being defined: an identifier at line start, before its '='.
		'rule': {
			pattern: /(^[ \t]*)[A-Za-z]\w*(?=[ \t]*=)/m,
			lookbehind: true
		},
		// Definition / alternation operators.
		'operator': /=|\|/,
		// Meta punctuation: [optional] {repetition} (grouping).
		'punctuation': /[[\]{}()]/
	};
}(Prism));
