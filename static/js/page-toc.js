/*
 * Highlights, in the "On this page" column (toc.vm), the section the reader is
 * currently in, and keeps that link visible inside the column as the page
 * scrolls. The current section is the last heading whose top has passed the
 * reading line (the bottom of the sticky header plus a small gap, the same
 * offset the headings use as scroll-margin-top), so a link clicked in the
 * column lights up as soon as the browser lands on its heading. When the page
 * is scrolled to the very bottom the last section is current even if its
 * heading never reaches the line. Self-hosted, no dependencies; does nothing
 * on pages without the column (fewer than two sections).
 */
(function () {
	var LINE_GAP = 24; // px below the header bottom

	function init() {
		var toc = document.querySelector('.page-toc');
		if (!toc) { return; }
		var column = toc.parentNode;
		var header = document.querySelector('.site-header');

		// Pair each link with its heading (the element the anchor points to; for the
		// legacy index_N anchors, an empty <a> inside the heading, at the same height).
		var entries = [];
		var links = toc.querySelectorAll('a[href^="#"]');
		for (var i = 0; i < links.length; i++) {
			var id;
			try { id = decodeURIComponent(links[i].getAttribute('href').slice(1)); } catch (e) { continue; }
			var target = id && document.getElementById(id);
			if (target) { entries.push({ link: links[i], target: target }); }
		}
		if (!entries.length) { return; }

		var active = null;

		function setActive(entry) {
			if (entry === active) { return; }
			if (active) {
				active.link.classList.remove('active');
				active.link.removeAttribute('aria-current');
			}
			active = entry;
			if (!active) { return; }
			active.link.classList.add('active');
			active.link.setAttribute('aria-current', 'location');
			keepVisible(active.link);
		}

		// Scroll only the column, never the page, so it does not fight the reader's scroll.
		function keepVisible(link) {
			var top = link.offsetTop;
			var bottom = top + link.offsetHeight;
			if (top < column.scrollTop) {
				column.scrollTop = top;
			} else if (bottom > column.scrollTop + column.clientHeight) {
				column.scrollTop = bottom - column.clientHeight;
			}
		}

		function update() {
			var line = (header ? header.getBoundingClientRect().bottom : 0) + LINE_GAP;
			var atBottom = window.innerHeight + window.pageYOffset >= document.documentElement.scrollHeight - 2;
			var current = null;
			if (atBottom) {
				current = entries[entries.length - 1];
			} else {
				for (var i = 0; i < entries.length; i++) {
					if (entries[i].target.getBoundingClientRect().top <= line) {
						current = entries[i];
					} else {
						break;
					}
				}
			}
			setActive(current);
		}

		var pending = false;
		function schedule() {
			if (pending) { return; }
			pending = true;
			window.requestAnimationFrame(function () {
				pending = false;
				update();
			});
		}

		window.addEventListener('scroll', schedule, { passive: true });
		window.addEventListener('resize', schedule);
		window.addEventListener('hashchange', schedule);
		update();
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}
})();
