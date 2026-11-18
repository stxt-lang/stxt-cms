/*
 * Book pages (templates/book/): scrolls the left column, the table of contents of
 * the book (some 75 entries, more than a screen), so the current chapter is in
 * view when the page opens. Only the column scrolls, never the page.
 * Self-hosted, no dependencies.
 */
(function () {
	function init() {
		var index = document.querySelector('.book-index');
		if (!index) { return; }
		var column = index.parentNode;
		var current = index.querySelector('.selected');
		if (!current || column.scrollHeight <= column.clientHeight) { return; }
		var top = current.offsetTop - column.offsetTop;
		column.scrollTop = Math.max(0, top - column.clientHeight / 3);
	}
	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	} else {
		init();
	}
})();
