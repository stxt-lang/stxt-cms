/*
 * The theme of the portal, light or dark: the one chosen with the button of the
 * header or, while none is chosen, the one of the system, which the page then
 * follows when it changes. It is the `data-theme` of <html>; the colours are
 * custom properties (scss/_theme.scss).
 *
 * The button stores the theme only while it contradicts the system: back to the
 * one of the system, nothing is stored and the page follows the system again, so
 * the button needs no third "automatic" state. Same behaviour as the playground
 * (stxt-play, src/app/theme.ts), on its own key: the two sites do not share
 * storage.
 *
 * The script in the head of every page (templates/head.vm) makes the same
 * decision before the first paint, from the same key; keep both in step.
 */
(function () {
	var KEY = 'stxt.theme';
	var root = document.documentElement;
	var button = document.getElementById('theme-toggle');
	var system = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null;

	function systemTheme() {
		return system && system.matches ? 'dark' : 'light';
	}

	function chosen() {
		var theme = null;
		try { theme = localStorage.getItem(KEY); } catch (e) { /* storage blocked */ }
		return theme === 'dark' || theme === 'light' ? theme : null;
	}

	function store(theme) {
		try {
			if (theme) {
				localStorage.setItem(KEY, theme);
			} else {
				localStorage.removeItem(KEY);
			}
		} catch (e) { /* storage blocked: the choice lasts until the page is left */ }
	}

	var current = chosen();

	function render() {
		var theme = current || systemTheme();
		root.setAttribute('data-theme', theme);
		var chrome = document.querySelector('meta[name="theme-color"]');
		if (chrome) {
			var colour = getComputedStyle(root).getPropertyValue('--site-chrome').trim();
			if (colour) { chrome.setAttribute('content', colour); }
		}
		if (button) {
			var dark = theme === 'dark';
			button.setAttribute('aria-pressed', String(dark));
			button.title = dark ? button.getAttribute('data-title-light') : button.getAttribute('data-title-dark');
		}
	}

	if (button) {
		button.addEventListener('click', function () {
			var next = (current || systemTheme()) === 'dark' ? 'light' : 'dark';
			current = next === systemTheme() ? null : next;
			store(current);
			render();
		});
	}
	if (system) {
		var follow = function () { render(); };
		if (system.addEventListener) {
			system.addEventListener('change', follow);
		} else if (system.addListener) {
			system.addListener(follow);
		}
	}

	render();
})();
