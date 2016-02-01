#TOC

TOC is a jQuery plugin that will automatically generate a table of contents for your page. You can see an example of it on the left side of the page.

##Features
- Completely customizable
- Click to smooth scroll to that spot on the page
- Automatically highlight the current section
- Extremely lightweight (744 bytes gzipped)
- Can have multiple on a page

##Download

- [Production](https://raw.github.com/jgallen23/toc/master/dist/jquery.toc.min.js)
- [Development](https://raw.github.com/jgallen23/toc/master/dist/jquery.toc.js)
- [Source](http://github.com/jgallen23/toc)

##Usage

	`javascript
	$('#toc').toc();

###Options
Defaults shown below

	`javascript
	$('#toc').toc({
		'selectors': 'h1,h2,h3', //elements to use as headings
		'container': 'body', //element to find all selectors in
		'smoothScrolling': true, //enable or disable smooth scrolling on click
		'prefix': 'toc', //prefix for anchor tags and class names
		'onHighlight': function(el) {}, //called when a new section is highlighted 
		'highlightOnScroll': true, //add class to heading that is currently in focus
		'highlightOffset': 100, //offset to trigger the next headline
		'anchorName': function(i, heading, prefix) { //custom function for anchor name
			return prefix+i;
		},
		'headerText': function(i, heading, $heading) { //custom function building the header-item text
			return $heading.text();
		},
		'itemClass': function(i, heading, $heading, prefix) { // custom function for item class
			return $heading[0].tagName.toLowerCase();
		}
	});

##Example CSS

	`css
	#toc {
		top: 0px;
		left: 0px;
		height: 100%;
		position: fixed;
		background: #333;
		box-shadow: inset -5px 0 5px 0px #000;
		width: 150px;
		padding-top: 20px;
		color: #fff;
	}

	#toc ul {
		margin: 0;
		padding: 0;
		list-style: none;
	}

	#toc li {
		padding: 5px 10px;
	}

	#toc a {
		color: #fff;
		text-decoration: none;
		display: block;
	}

	#toc .toc-h2 {
		padding-left: 10px;
	}

	#toc .toc-h3 {
		padding-left: 20px;
	}

	#toc .toc-active {
		background: #336699;
		box-shadow: inset -5px 0px 10px -5px #000;
	}

##History

[View](https://raw.github.com/jgallen23/toc/master/History.md)

##Future
- Figure out how to handle headlines on bottom of page
- Zepto.js support (should work, need to verify)
- Ender support


##Contributors
- Greg Allen ([@jgaui](http://twitter.com/jgaui)) [jga.me](http://jga.me)
