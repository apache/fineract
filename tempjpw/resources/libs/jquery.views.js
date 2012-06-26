/*! JsViews v1.0pre: http://github.com/BorisMoore/jsviews */
/*
 * jQuery plugin providing interactive data-driven views, based on integration between jQuery Templates and jQuery Data Link.
 * Requires jquery.render.js (optimized version of jQuery Templates, for rendering to string)
 *    See JsRender at http://github.com/BorisMoore/jsrender
 * and jquery.observable.js also at http://github.com/BorisMoore/jsviews
 *
 * Copyright 2011, Boris Moore
 * Released under the MIT License.
 */
(function( $, undefined ) {

//===============
// event handlers
//===============

// var TEST_EVENTS = { total:0, change:0, arrayChange:0, propertyChange:0 };
var FALSE = false, TRUE = true,
	topView, viewsNs, decl,
	oldCleanData = $.cleanData,
	fnSetters = {
		value: "val",
		html: "html",
		text: "text"
	},
	jsvData = "_jsvData";

function elemChangeHandler( ev ) {
	var setter, cancel, fromAttr, toPath, linkToInfo, linkContext, sourceValue, link, cnvt, target,
		data = this.target,
		source = ev.target,
		$source = $( source ),
		view = $.view( source ),
		links = this.links,
		context = view.ctx,
		beforeChange = context.beforeChange,
		l = links.length;

	while ( l-- && !cancel ) {
		link = links[ l ];
		if ( !link.filter || (link.filter && $source.is( link.filter ))) {
			fromAttr = link.fromAttr;
			if ( !fromAttr ) {
				// Merge in the default attribute bindings for this source element
				fromAttr = viewsNs.merge[ source.nodeName.toLowerCase() ];
				fromAttr = fromAttr ? fromAttr.from.fromAttr : "text";
			}
			setter = fnSetters[ fromAttr ];
			sourceValue = $.isFunction( fromAttr ) ? fromAttr( source ) : setter ? $source[setter]() : $source.attr( fromAttr );
			if ((!beforeChange || !(cancel = beforeChange.call( view, ev ) === FALSE )) && sourceValue !== undefined ) {
				// Find linkToInfo using link.to, or if not specified, use declarative specification
				// provided by decl.applyLinkToInfo, applied to source element
				linkToInfo = link.to || $.trim( source.getAttribute( viewsNs.linkToAttr ));
				// linkTo does not support multiple targets - use imperative linking for that scenario...
				if ( linkToInfo ) {
					cnvt = link.convert;
					linkToInfo = splitParams( linkToInfo );
					target = linkToInfo[ 0 ].slice( 0, -1 );
					data = (view && view.data) || data;
					// data is the current view data, or the top-level target of linkTo.
					// get the target object
					target = getTargetObject( data, view, target );
					cnvt = linkToInfo[ 3 ];
					toPath = $.trim( linkToInfo[ 1 ].slice( 0, -1 ));
					linkContext = {
						source: source,
						target: target,
						convert: cnvt,
						path: toPath
					};
					if ( cnvt ) {
						sourceValue = getConvertedValue( linkContext, data, view, cnvt, sourceValue );
					}
					if ( sourceValue !== undefined && target ) {
						$.observable( target ).setProperty( toPath, sourceValue );
						if ( context.afterChange ) {  //TODO only call this if the target property changed
							context.afterChange.call( linkContext, ev );
						}
					}
					ev.stopPropagation(); // Stop bubbling
				}
			}
			if ( cancel ) {
				ev.stopImmediatePropagation();
			}
		}
	}
}

function propertyChangeHandler( ev, eventArgs ) {
	var pathInfo, setter, cancel, changed, attr, sourceValue, css,
		link = this,
		target = link.target,
		$target = $( target ),
		source = ev.target,
		sourcePath = (eventArgs && eventArgs.path) || ev.path,
		view = $.view( target ),
		context = view.ctx,
		beforeChange = context.beforeChange,
		pathInfos = getLinkFromDataInfo( target, source ).paths[ sourcePath ],
		l = pathInfos && pathInfos.length;

	while ( l-- ) {
		pathInfo = pathInfos[ l ];
		if ((!beforeChange || !(eventArgs && (cancel = beforeChange.call( this, ev, eventArgs ) === FALSE )))
		&& (!view || view.onDataChanged( eventArgs ) !== FALSE )) {

			attr = pathInfo.attr;

			sourceValue = getConvertedValue( link, link.source, view, pathInfo.expr, eventArgs && eventArgs.value );

			if ( $.isFunction( sourceValue )) {
				sourceValue = sourceValue.call( source );
			}

			if ( !attr ) {
				// Merge in the default attribute bindings for this target element
				attr = viewsNs.merge[ target.nodeName.toLowerCase() ];
				attr = attr ? attr.to.toAttr : "text";
			}

			if ( css = attr.indexOf( "css-" ) === 0 && attr.substr( 4 )) {
				if ( changed = $target.css( css ) !== sourceValue ) {
					$target.css( css, sourceValue );
				}
			} else {
				setter = fnSetters[ attr ];
				if ( setter ) {
					if ( changed = $target[setter]() !== sourceValue ) {
						$target[setter]( sourceValue );
					}
				} else if ( changed = $target.attr( attr ) !== sourceValue ) {
					$target.attr( attr, sourceValue );
				}
			}

			if ( eventArgs && changed && context.afterChange ) {
				context.afterChange.call( link, ev, eventArgs );
			}
		}
	}
}

function arrayChangeHandler( ev, eventArgs ) {
	var cancel,
		context = this.ctx,
		beforeChange = context.beforeChange,
		sourceValue = eventArgs ? eventArgs.change : viewsNs.linkToAttr;  // linkToAttr used as a marker of trigger events

	if ((!beforeChange || !(cancel = beforeChange.call( this, ev, eventArgs ) === FALSE ))
		&& sourceValue !== undefined ) {
		this.onDataChanged( eventArgs );
		if ( context.afterChange ) {
			context.afterChange.call( this, ev, eventArgs );
		}
	}
}

function setArrayChangeLink( view ) {
	var handler,
		data = view.data,
		onArrayChange = view._onArrayChange;

	if ( onArrayChange ) {
		if ( onArrayChange[ 1 ] === data ) {
			return;
		}
		$([ onArrayChange[ 1 ]]).unbind( "arrayChange", onArrayChange[ 0 ]);
//	$( "#console" ).append( --TEST_EVENTS.total + " - arrayChange " + --(TEST_EVENTS.arrayChange) + "<br/>");
	}

	if ( $.isArray( data )) {
		handler = function() {
			arrayChangeHandler.apply( view, arguments );
		};
		$([ data ]).bind( "arrayChange", handler );
		view._onArrayChange = [ handler, data ];
//	$( "#console" ).append( ++TEST_EVENTS.total + " + arrayChange " + ++(TEST_EVENTS.arrayChange) + "<br/>");
	}
}

//===============
// view hierarchy
//===============

function setViewContext( view, context, merge ) {
	var parentContext = view.parent && view.parent.ctx,
		viewContext = view._ctx;
		// Propagate inherited context through children
	view.ctx = merge
		? (viewContext && parentContext ? $.extend( {}, parentContext, viewContext ) : parentContext || viewContext)
		: context && context !== parentContext
			// Set additional context on this view (which will modify the context inherited from the parent, and be inherited by child views)
			? (view._ctx = context, (parentContext ? $.extend( {}, parentContext, context ) : context))
			: parentContext;
}

function View( context, node, path, template, parentView, parentElViews, data ) {
	var views, index, viewCount, tagInfo, ctx, presenter, options,
		self = this;

	$.extend( self, {
		views: [],
		nodes: node ? [] : [ document.body ],
		tmpl: template || (parentView && parentView.tmpl),
		path: path,
		parent: parentView,
		prevNode: node
	});
	if ( parentView ) {
		views = parentView.views;
		parentElViews.push( self );
		data = data || parentView.data;
		if ( $.isArray( parentView.data ))  {
			self.index = index = path;
			views.splice( index, 0, self );
			viewCount = views.length;
			data = data[ index ];
			while ( index++ < viewCount-1 ) {
				$.observable( views[ index ] ).setProperty( "index", index );
			}
			self.tag = parentView.tag;
		} else {
			if ( path ) {
				// TODO getDataAndContext and passing of context and data from tags needs work. 
				// Also, consider more 'codeless' approach, and more consistent syntax with codeless tag markup
				data = getDataAndContext( data, parentView, path );
				context = context || data[ 1 ];
				data = data[ 0 ];
			}
			self.index = views.length;
			views.push( self );
			if ( context.tag ) {
				self.tag = [context.tag];
			} else {
				template = template.split( "=" );
				if ( template.shift() === "tag" ) { 
					self.tag = template;
				}
			}
		}
	}
	self.data = data;
	setViewContext( self, context );
	setArrayChangeLink( self );
	
	if ( (tagInfo = self.tag) && !$.isArray( data )) { 
		// This view is from a registered presenter
		presenter = viewsNs.tags[ tagInfo[ 0 ]];
		if ( presenter && presenter.presenter ) {
			ctx = presenter.ctx || {};
			options = tagInfo[1] 
				? getTargetObject( parentView.data, parentView, "{" + tagInfo[ 1 ] + "}" )
				// Declarative presenter {{foo}}
				: parentView.ctx;
				// Imperative: link( data, foo )
			$.extend( options, presenter.options || {} );
			// attach plugin to content of view
	
			ctx[ tagInfo[ 0 ]] = new presenter.presenter( options, self );
			// Make presenter available off the ctx object
			self.context( ctx );
		}
	}
}

function createNestedViews( node, parent, nextNode, depth, data, context, prevNode, index ) {
	var tokens, parentElViews, view, existing, parentNode, elem, elem2, params, presenter, tagInfo, ctx,
		currentView = parent,
		viewDepth = depth;
	context = context || {};
	index = index || 0;
	node = prevNode || node;

	if ( !prevNode && node.nodeType === 1 ) {
		if ( viewDepth++ === 0 ) {
			// Add top-level element nodes to view.nodes
			currentView.nodes.push( node );
		}
		var linkFromInfo = node.getAttribute( viewsNs.linkFromAttr ),
			getFromInfo = node.getAttribute( viewsNs.getFromAttr );

		if ( linkFromInfo || getFromInfo ) {
			addLinksFromData( data || parent.data, node, getFromInfo, linkFromInfo, TRUE );
		}
		node = node.firstChild;
	} else {
		node = node.nextSibling;
	}

	while ( node && node !== nextNode ) {
		if ( node.nodeType === 1 ) {
			createNestedViews( node, currentView, nextNode, viewDepth, data, context );
		} else if ( node.nodeType === 8 && (tokens = /^(\/?)(?:(item)|(?:(tmpl)(?:\((.*)\))?(?:\s+([^\s]+))?))$/.exec( node.nodeValue ))) {
			// tokens: [ commentText, "/", "item", dataParams, tmplParam ]

			parentNode = node.parentNode;
			if ( tokens[ 1 ]) {
				// <!--/item--> or <!--/tmpl-->
				currentView.nextNode = node;
				if (( tagInfo = currentView.tag ) && ( ctx = currentView.ctx[ tagInfo[ 0 ]]) && ctx.onAfterCreate ) {
					// This view is from a registered presenter which has registered an onAfterCreate callback
					ctx.onAfterCreate();
				}
				if ( currentView.ctx.onAfterCreate ) {
					currentView.ctx.onAfterCreate.call( currentView, currentView );
				}
				if ( tokens[ 2 ]) {
					// An item close tag: <!--/item-->
					currentView = parent;
				} else  {
					// A tmpl close tag: <!--/tmpl-->
					return node;
				}
			} else {
				// <!--item--> or <!--tmpl-->
				parentElViews = parentElViews || jsViewsData( parentNode, "view", TRUE );
				if ( tokens[ 2 ]) {
					// An item open tag: <!--item-->
					currentView = new View( context, node, index++, undefined, currentView, parentElViews );
				} else {
					// A tmpl open tag: <!--tmpl(path) name-->
					view = $.view( node );
					if ( view && view.prevNode === node ) {
						if ( view.data === data ) {
							existing = view.nextNode;
						} else {
							view.data = data;
							view.render( context );
							return view.nextNode;
						}
					} else {
						view = new View( context, node, tokens[ 4 ], tokens[ 5 ], currentView, parentElViews, data );
					}
					// Jump to the nextNode of the tmpl view
					node = existing || createNestedViews( node, view, nextNode, 0 );
				}
			}
		} else if ( viewDepth === 0 ) {
			// Add top-level non-element nodes to view.nodes
			currentView.nodes.push( node );
		}
		node = node.nextSibling;
	}
}

//=======================
// Expression evaluation
//=======================

function getDataAndContext( source, view, paramString ) {
	return Function( "$", "$data", "$view", "$ctx",
		"with($data){return [" + paramString + "];}")( $, source, view, view.ctx );
}

function getConvertedValue( context, source, view, expression, value ) {
	try {
		return Function( "$", "$data", "$view", "$ctx", "$value",
			"with($data){return " + expression + ";}")
			.call( context, $, source, view, view.ctx, value );
	} catch(e) {
		// in debug mode, throw 'bad syntax error';
		throw e.message;
	}
}

function getTargetObject( source, view, expression ) {
	try {
		return expression
			? Function( "$", "$data", "$view", "$ctx",
				"with($data){return " + expression  + ";}")( $, source, view, view.ctx )
			: source;
	} catch(e) {
		// in debug mode, throw 'bad syntax error';
		throw e.message;
	}
}

//===============
// data linking
//===============

function link( from, to, links, context ) {
	var lnk, filter, targetElems, toLinks, fromLinks, linksToData, i;

	if ( links ) {

		links = $.isArray( links ) ? links : [ links ];

		toLinks = [];
		fromLinks = [];
		i = links.length;

		while ( i-- ) {
			lnk = links[ i ];
			if ( lnk.to ) {
				toLinks.push({ to: lnk.to, filter: lnk.filter });
			}
			if ( lnk.from || lnk.getFrom ) {
				fromLinks.push( lnk );
			}
		}
		i = fromLinks.length;
		while ( i-- ) {
			lnk = fromLinks[ i ];
			filter = lnk.filter;
			targetElems = filter ? from.find( filter ).add( $( this ).filter( filter )) : from; // Use future findFilter method in jQuery 1.7?

			targetElems.each( function() {
				// If 'from' path points to a property of a descendant 'leaf object',
				// link not only from leaf object, but also from intermediate objects
				addLinksFromData( to, this, lnk.getFrom, lnk.from );
			});
		}
	}
	if ( !links || toLinks.length ) {
		from.each( function() {
			if ( !links ) {
				// DECLARATIVE DATA LINKING

				// Linking HTML to object or array
				linksToData = jsViewsData( this, "to" );
				i = linksToData.length;
				while ( i-- ) {
					lnk = linksToData[ i ];
					if ( lnk.links === declLinkTo ) {
						if ( lnk.target === to ) {
							// Already declaratively linked to the same object
							return;
						}
						// Already linked to a different object, so unlink from previous object
						removeLinksToData( this, declLinkTo );
						linksToData.splice( i, 1 );
					}
				}
				toLinks = declLinkTo; // For declarative case

				// Linking object or array to HTML
				createNestedViews( this, $.view( this ), undefined, undefined, to, context );
			}
			addLinksToData( this, to, toLinks );
		});
	}
	return from;
}

//function unlink( from, to, links ) { // TODO
//}

function addLinksFromData( source, target, getFrom, linkFrom ) {
	var param, cnvtParam, i, l, lastChar, attr, openParenIndex, get, object, cnvtParams, view,
		triggers = [],
		cnvt = "";

	linkFrom = splitParams( (linkFrom ? linkFrom + "," : "") + (getFrom ? "|," + getFrom + ",": ""), TRUE );
	while ( param = linkFrom.shift() ) {
		l = param.length;
		lastChar = param.charAt( l - 1 );
		param = param.slice( 0, -1 );
		switch ( lastChar ) {
		case ':':
			attr = $.trim( param );
			break;
		case '[':
			cnvt = $.trim( param );
			break;
		case ')':
			cnvt = cnvt || param;
			break;
		case ']':
			triggers = [[ cnvt, param ]];
			cnvt = cnvt ? (cnvt + "." + param) : param;
			cnvt += $.isFunction( source[ param ]) ? "()": "";
			break;
		case '\r':
			openParenIndex = ++param; // Convert to integer and increment
			break;
		case ',':
			if ( param === '|') {
				get = TRUE;
			} else {
				// Apply binding
				if ( openParenIndex ) {
					cnvtParams = cnvt.slice( openParenIndex );
					cnvtParams = splitParams( cnvtParams );
					for ( i = 0, l = cnvtParams.length; i < l; i++ ) {
						cnvtParam = $.trim(cnvtParams[ i ]);
						lastChar = cnvtParam.charAt( cnvtParam.length - 1 );
						cnvtParam = cnvtParam.slice( 0, -1 );
						if ( lastChar === '[') {
							cnvtParams[ i ] = object = cnvtParam;
						} else if ( lastChar === ']') {
							triggers.push([ object, cnvtParam ]);
							cnvtParams[ i ] = object ? ("." + cnvtParam) : cnvtParam;
							cnvtParams[ i ] += $.isFunction( source[ cnvtParam ]) ? "()": "";
							object = "";
						}
					}
					cnvt = cnvt.slice( 0, openParenIndex ) + cnvtParams.join("") + ")";
				}

				cnvt += param;
				view = $.view( target );
				l = triggers.length;
				while ( l-- ) {
					var trigger = triggers[ l ],
						path = trigger[ 1 ],
						fromOb = getTargetObject( source, view, trigger[ 0 ]),
						link = { source: source, target: target },
						innerPath = path.split("."),
						innerOb = fromOb;

					// If 'from' path points to a property of a descendant 'leaf object',
					// link not only from leaf object, but also from intermediate objects
					while ( innerPath.length > 1 ) {
						innerOb = innerOb[ innerPath.shift() ];
						if ( innerOb ) {
							addLinkFromData( innerOb, link, innerPath.join( "." ), cnvt, attr );
						}
					}
					// The last trigger of get bindings will be called on adding the link (to get/initialize the value)
					addLinkFromData( fromOb, link, path, cnvt, attr, !l && get );
				}
				openParenIndex = 0;
				triggers = [];
				attr = cnvt = "";
			}
		}
	}
}

function addLinkFromData( source, link, path, expr, attr, get ) {
	var paths, pathInfos, handler,
		target = link.target,
		linkInfo = getLinkFromDataInfo( target, source ),
		pathInfo = { attr: attr, expr: expr};

	if ( linkInfo ) {
		// Set path info for this path
		pathInfos = linkInfo.paths[ path ] = linkInfo.paths[ path ] || [];
		pathInfos.push( pathInfo );
		// get handler
		handler = linkInfo.handler;
	} else {
		handler = function() {
			propertyChangeHandler.apply( link, arguments );
		};

		// Store handler for unlinking
		if ( target ) {
			paths = {};
			paths[ path ] = [ pathInfo ];
			jsViewsData( target, "from", TRUE ).push({ source: source, paths: paths, handler: handler });
		}
		$( source ).bind( "propertyChange", handler );
	}
//	$( "#console" ).append( ++TEST_EVENTS.total + " + propertyChange " + ++(TEST_EVENTS.propertyChange) + "<br/>");
	if ( get ) {
		handler({ target: source, path: path });
	}
}

function addLinksToData( source, target, links ) {
	var handler = function() {
			elemChangeHandler.apply( { target: target, links: links }, arguments );
		};

	// Store handler for unlinking
	jsViewsData( source, "to", TRUE ).push({ target: target, links: links, handler: handler });
	$( source ).bind( "change", handler );
}

function removeLinksToData( source, links ) {
	var prevLinkInfo,
		prevLinkInfos = jsViewsData( source, "to" ),
		l = prevLinkInfos.length;
	while( l-- ) {
		prevLinkInfo = prevLinkInfos[ l ];
		if ( prevLinkInfo.links === links ) {
			$( source ).unbind( "change", prevLinkInfo.handler);
			prevLinkInfos.splice( l, 1 );
//	$( "#console" ).append( --TEST_EVENTS.total + " - change " + --(TEST_EVENTS.change) + "<br/>");
		}
	}
}

function getLinkFromDataInfo( target, source ) {
	var link,
		links = jsViewsData( target, "from" ),
		l = links.length;
	while( l-- ) {
		link = links[ l ];
		if ( link.source === source ) {
			// Set path info for this path
			return link;
		}
	}
}

//===============
// helpers
//===============

function clean( i, el ) { // TODO optimize for perf
	var link, links , l, views, parentView, view;

	if ( jsViewsData( el, "to" ).length ) {
		$( el ).unbind( "change" );
//	$( "#console" ).append( --TEST_EVENTS.total + " - change " + --(TEST_EVENTS.change) + "<br/>");
	}

	links = jsViewsData( el, "from" );
	l = links.length;

	while( l-- ) {
		link = links[ l ];
		$( link.source ).unbind( "propertyChange", link.handler );
//	$( "#console" ).append( --TEST_EVENTS.total + " - propertyChange " + --(TEST_EVENTS.propertyChange) + "<br/>");
	}

	views = jsViewsData( el, "view" );
	if ( l = views.length ) {
		parentView = $.view( el );
		while( l-- ) {
			view = views[ l ];
			if ( view.parent === parentView ) {
				parentView.removeViews( view.index, 1 );  // NO - ONLY remove view if its top-level nodes are all.. (TODO)
			}
		}
	}
}

function jsViewsData( el, type, create ) {
	var jqData = $.data( el, jsvData ) || (create && $.data( el, jsvData, { "view": [], "from": [], "to": [] }));
	return jqData ? jqData[ type ] : [];
}

function splitParams( paramString, markParen ) {
	// Split into params (or values in an array literal, or keys and values in an object literal)
	// (Achieved by splitting before top-level ':' or ',' chars)
	var openParenIndex,
		startIndex = 0,
		parenDepth = 0,
		quoted = FALSE, // boolean for string content in double qoutes
		aposed = FALSE; // boolean for string content in single qoutes

	paramString = paramString.replace( /\s+/g, " " );

	return paramString
		.replace( /(\))|([\:\,])|(\')|(\")|([\(\[\{])|([\}\]])/g, function( all, cnvt, colon, apos, quot, leftParen, rightParen, index ) {
			if ( aposed ) {
				// within single-quoted string
				aposed = !apos;
				return all;
			}
			if ( quoted ) {
				// within double-quoted string
				quoted = !quot;
				return all;
			}
			if ( cnvt ) {
				// follow top-level ':' or ',' with '\t'
				return --parenDepth
					? all
					: (markParen && paramString.charAt( index + 1 ) === ',' )
						? (openParenIndex -= startIndex, startIndex = index,
							all + "\t" + openParenIndex + "\r\t")
						:all;
			}
			if ( colon ) {
				// follow top-level ':' or ',' with '\t'
				return parenDepth
					? all
					: (startIndex = index+1, all + "\t");
			}
			if ( leftParen ) {
				if ( parenDepth++ ) {
					return all;
				}
				if ( all === '(' ) {
					openParenIndex = index;
				}
				// follow top-level '[' by '\t'
				return all !== '[' ? all : "[\t";
			}
			if ( rightParen ) {
				// follow top-level ']' by '\t'
				return ( --parenDepth || all !== ']' ) ? all : "]\t";
			}
			aposed = apos;
			quoted = quot;
			return all;
		})
		.split( "\t" );
}

function inputAttrib( elem ) {
	return elem.type === "checkbox" ? elem.checked : $( elem ).val();
}

$.extend({

	//=======================
	// jQuery $.view() plugin
	//=======================

	view: function( node, inner ) {
		// $.view() returns top node
		// $.view( node ) returns view that contains node
		var returnView, view, parentElViews, i, finish,
			startTagReg = /^item|^tmpl(\(\$?[\w\.]*\))?(\s+[^\s]+)?$/,
			topNode = document.body,
			startNode = node;

		if ( inner ) {
			// Treat supplied node as a container element, step through content, and return the first view encountered.
			finish = node.nextSibling || node.parentNode; //
			while ( finish !== (node = node.firstChild || node.nextSibling || node.parentNode.nextSibling )) {
				if  ( node.nodeType === 8 && startTagReg.test( node.nodeValue )) {
					view = $.view( node );
					if ( view.prevNode === node ) {
						return view;
					}
				}
			}
			return;
		}

		node = node || topNode;
		if ( topView && !topView.views.length ) {
			returnView = topView; // Perf optimization for common case
		} else {
			// Step up through parents to find an element which is a views container, or if none found, create the top-level view for the page
			while( !(parentElViews = jsViewsData( finish = node.parentNode || topNode, "view" )).length ) {
				if ( !finish || node === topNode ) {
					jsViewsData( topNode.parentNode, "view", TRUE ).push( returnView = topView = new View());
					topView.ctx = {};
					break;
				}
				node = finish;
			}
			if ( !returnView && node === topNode ) {
				returnView = parentElViews[0];
			}
			while ( !returnView && node ) {
				// Step back through the nodes, until we find an item or tmpl open tag - in which case that is the view we want
				if  ( node.nodeType === 8 ) {
					if (  /^\/item|^\/tmpl$/.test( node.nodeValue )) {
						// A tmpl or item close tag: <!--/tmpl--> or <!--/item-->
						i = parentElViews.length;
						while ( i-- ) {
							view = parentElViews[ i ];
							if ( view.nextNode === node ) {
								// If this was the node originally passed in, this is the view we want.
								returnView = (node === startNode && view);
								// If not, jump to the beginning of this item/tmpl and continue from there
								node = view.prevNode;
								break;
							}
						}
					} else if ( startTagReg.test( node.nodeValue )) {
						// A tmpl or item open tag: <!--tmpl--> or <!--item-->
						i = parentElViews.length;
						while ( i-- ) {
							view = parentElViews[ i ];
							if ( view.prevNode === node ) {
								returnView = view;
								break;
							}
						}
					}
				}
				node = node.previousSibling;
			}
			// If not within any of the views in the current parentElViews collection, move up through parent nodes to find next parentElViews collection
			returnView = returnView || $.view( finish );
		}
		return returnView;
	},

	//=======================
	// override cleanData
	//=======================

	cleanData: function( elems ) {
		$( elems ).each( clean );  // TODO - look at perf optimization on this
		oldCleanData.call( $, elems );
	}
});

//=======================
// $.views (namespace)
//=======================

viewsNs = $.views = $.views || {};

$.extend( viewsNs, {
	presenters: {},
	activeViews: true,
	getProperty: function( data, value ) {
		// support for property getter on data
		return $.isFunction( value ) ? value.call(data) : value;
	},
	linkToAttr: "data-to",
	linkFromAttr: "data-from",
	getFromAttr: "data-getfrom",
	merge: {
		input: {
			from: {
				fromAttr: inputAttrib
			},
			to: {
				toAttr: "value"
			}
		}
	},

	//===============
	// registerPresenters
	//===============

	// Register a 'control' - which associates a Presenter object with a template
	// Optionally associate with a tag, for declarative use. (Rendering of template and instantiation/attaching of presenter).
	registerPresenters: registerPresenters = function( name, presenter ) {
		var key, tag;
		
		if ( typeof name === "object" ) {
			for ( key in name ) {
				registerPresenters( key, name[ key ]);
			}
		} else {
			// Simple single property case.
			presenter.ctx = presenter.ctx || {};
			
			tag = (tag = presenter.tag) === undefined ? name : tag;
			// If tag not set to null or empty string, then register a tag for this control 
			if ( tag ) {
				presenter.tag = tag;
				viewsNs.tags[ tag ] = presenter;
			}

			plugin = (plugin = presenter.plugin) === undefined ? name : plugin; 
			// If plugin not set to null or empty string, then register create a generated jQuery plugin for this control 
			if ( plugin ) {
				//Generated jQuery plugin
				$.fn[ plugin ] = $.fn[ plugin ]  || function( data, options ) {
					return this.link( data, presenter, options );
				}
				presenter.plugin = plugin;
			}
			viewsNs.presenters[ name ] = presenter;
		}
		return this;
	},

	//=======================
	// view prototype
	//=======================

	view: {
		onDataChanged: function( eventArgs ) {
			if ( eventArgs ) {
				// This is an observable action (not a trigger/handler call from pushValues, or similar, for which eventArgs will be null)
				var self = this,
					action =  eventArgs.change,
					index = eventArgs.index,
					items = eventArgs.items;
				switch ( action ) {
					case "insert":
						self.addViews( index, items );
					break;
					case "remove":
						self.removeViews( index, items.length );
					break;
					case "move":
						self.render(); // Could optimize this
					break;
					case "refresh":
						self.render();
					// Othercases: (e.g.undefined, for setProperty on observable object) etc. do nothing
				}
			}
			return TRUE;
		},
		render: function() {
			var prevNode = this.prevNode,
				nextNode = this.nextNode,
				parentNode = prevNode.parentNode;

			$( this.nodes ).remove();
			this.removeViews( 0, this.views.length );
			this.nodes = [];
			$( prevNode ).after( $.render( this.data, this.tmpl, this.ctx ) );
			parentNode.removeChild( prevNode.nextSibling );
			parentNode.removeChild( nextNode.previousSibling );
			createNestedViews( parentNode, this, nextNode, 0, undefined, undefined, prevNode, 0 ); //this.index
			setArrayChangeLink( this );
			return this;
		},
		addViews: function( index, dataItems, tmpl ) {
			var parent,
				itemsCount = dataItems.length,
				context = this.ctx,
				views = this.views;

			if ( index && !views[index-1] ) {
				return; // If subview for provided index does not exist, do nothing
			}
			if ( itemsCount ) {
				if ( this.path ) {
					parent = this.parent;
					context = getDataAndContext( parent.data, parent, this.path )[1];
				}
				var html = $.render( dataItems, tmpl || this.tmpl, context, this, TRUE ), 
					// Use passed-in template if provided, since this added view may use a different template than the original one used to render the array.

					prevNode = index ? views[ index-1 ].nextNode : this.prevNode,
					nextNode = prevNode.nextSibling,
					parentNode = prevNode.parentNode;
				$( prevNode ).after( html );
				parentNode.removeChild( prevNode.nextSibling );
				parentNode.removeChild( nextNode.previousSibling );
				createNestedViews( parentNode, this, nextNode, 0, undefined, undefined, prevNode, index );
			}
			return this;
		},
		removeViews: function( index, itemsCount, keepHtml ) {
			if ( itemsCount ) {
				var parentElViews, parentViewsIndex, viewCount,
					views = this.views,
					current = index + itemsCount;

				while ( current-- > index ) {
					var view = views[ current ],
						i = view.views.length,
						node = view.prevNode,
						nextNode = view.nextNode,
						nodes = [ node ];

					if ( i ) {
						view.removeViews( 0, i, keepHtml );
					}

					// Remove this view from the parentElViews collection
					parentElViews = parentElViews || jsViewsData( view.nextNode.parentNode, "view" );
					i = parentElViews.length;
					while ( i-- && parentViewsIndex === undefined ) {
						if ( parentElViews[ i ] === view ) {
							parentViewsIndex = i;
						}
					}
					parentElViews.splice( parentViewsIndex, 1 );

					if ( !keepHtml ) {
						while ( node !== nextNode ) {
							node = node.nextSibling;
							nodes.push( node );
						}
						$( nodes ).remove();
					}
					view.data = undefined;
					setArrayChangeLink( view );
				}
				views.splice( index, itemsCount );
				viewCount = views.length;

				while ( index < viewCount ) {
					$.observable( views[ index ] ).setProperty( "index", index++ );
				}
			}
			return this;
		},
		context: function( context ) {
			var self = this,
				parent = self.parent,
				parentCtx = parent ? parent.ctx : {};
			if ( !context ) {
				// Clear context
				self.each( function( view ) {
					view.ctx = parentCtx;
					view._ctx = undefined;
				});
			} else if ( context !== self.ctx ) {
				self.each( function( view ) {
					setViewContext( view, context, view !== self );
				});
			}
			return this;
		},
		each: function( callback ) {
			callback( this );
			var l = this.views.length;
			while ( l-- ) {
				this.views[ l ].each( callback );
			}
			return this;
		},
		content: function( select ) {
			return select ? $( select, this.nodes ) : $( this.nodes );
		}
	}
});

//=======================
// jQuery plugins
//=======================

$.fn.extend({
	view: function( inner ) {
		return $.view( this[0], inner );
	},
	addLinks: function( data, links, context ) {
		// Explicit Linking
		return link( this, data, links, context );
	},
//	removeLinks: function( data, links, context ) { //TODO
//		return unlink( this, data, links, context );
//	},
	link: function( data, tmpl, context ) {
		// Declarative Linking
		// If context is a function, cb - shorthand for { beforeChange: cb }
		// if tmpl not a map, corresponds to $("#container").html( $.render( data, tmpl )).link( data );
		if ( !this.length ) {
			return this;
		}

		if ( tmpl ) {
			tmpl = tmpl.tag && tmpl === $.views.tags[ tmpl.tag ]
				? (context.tag = tmpl.tag, context.tmpl || tmpl.tmpl)
				// Special case: tmpl is a presenter
				: $.isPlainObject( tmpl )
					? (context = tmpl, FALSE)
					// Linking only. (context was passed in as second parameter, no template parameter passed)
					: tmpl;
					// Linking and rendering (passing a template)
		
			if ( tmpl && (tmpl = $.template( tmpl ))) {
				removeLinksToData( this[0], declLinkTo );
				this.empty();
				if ( data ) {
					this.append( $.render( data, tmpl, context ));
					// Using append, rather than html, as workaround for issues in IE compat mode. (Using innerHTML leads to initial comments being stripped)
				}
			}
		}
		return link( this, data, undefined, context );
	}
});

View.prototype = viewsNs.view;
decl = viewsNs.decl;
declLinkTo = [{ filter: "input[" + viewsNs.linkToAttr + "]" }];
})( jQuery );
