/*
 * Adds a "copy to clipboard" button to every highlighted code block
 * (<pre class="language-*">), for both STXT and EBNF blocks. Self-hosted, no
 * dependencies. Runs after Prism; copying uses the <code> element's textContent
 * so the copied text is the clean source, without highlight markup.
 */
(function () {
	var isEs = (document.documentElement.lang || 'en').toLowerCase().indexOf('es') === 0;
	var LABELS = isEs
		? { copy: 'Copiar código', copied: '¡Copiado!' }
		: { copy: 'Copy code', copied: 'Copied!' };

	var COPY_ICON = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="9" y="9" width="13" height="13" rx="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>';
	var CHECK_ICON = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12"></polyline></svg>';

	function copyText(text) {
		if (navigator.clipboard && navigator.clipboard.writeText) {
			return navigator.clipboard.writeText(text);
		}
		return new Promise(function (resolve, reject) {
			var ta = document.createElement('textarea');
			ta.value = text;
			ta.style.position = 'fixed';
			ta.style.opacity = '0';
			document.body.appendChild(ta);
			ta.select();
			var ok = false;
			try { ok = document.execCommand('copy'); } catch (e) { ok = false; }
			document.body.removeChild(ta);
			ok ? resolve() : reject();
		});
	}

	function reset(btn) {
		btn.classList.remove('copied');
		btn.innerHTML = COPY_ICON;
		btn.title = LABELS.copy;
		btn.setAttribute('aria-label', LABELS.copy);
	}

	function addButton(pre) {
		var parent = pre.parentNode;
		if (!parent || (parent.classList && parent.classList.contains('code-wrapper'))) {
			return; // already processed
		}
		var code = pre.querySelector('code');
		if (!code) { return; }

		var wrapper = document.createElement('div');
		wrapper.className = 'code-wrapper';
		parent.insertBefore(wrapper, pre);
		wrapper.appendChild(pre);

		var btn = document.createElement('button');
		btn.type = 'button';
		btn.className = 'copy-code-button';
		reset(btn);

		var timer;
		btn.addEventListener('click', function () {
			copyText(code.textContent).then(function () {
				btn.classList.add('copied');
				btn.innerHTML = CHECK_ICON;
				btn.title = LABELS.copied;
				btn.setAttribute('aria-label', LABELS.copied);
				clearTimeout(timer);
				timer = setTimeout(function () { reset(btn); }, 1600);
			});
		});

		wrapper.appendChild(btn);
	}

	function init() {
		var pres = document.querySelectorAll('pre[class*="language-"]');
		for (var i = 0; i < pres.length; i++) { addButton(pres[i]); }
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}
})();
