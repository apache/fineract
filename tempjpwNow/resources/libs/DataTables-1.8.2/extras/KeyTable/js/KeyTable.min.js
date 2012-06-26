/*
 * File:        KeyTable.min.js
 * Version:     1.1.6
 * Author:      Allan Jardine (www.sprymedia.co.uk)
 * 
 * Copyright 2009-2011 Allan Jardine, all rights reserved.
 *
 * This source file is free software, under either the GPL v2 license or a
 * BSD (3 point) style license, as supplied with this software.
 * 
 * This source file is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the license files for details.
 */
function KeyTable(q){this.block=false;this.event={remove:{}};this.fnGetCurrentPosition=function(){return[c,a]
};this.fnGetCurrentData=function(){return m.innerHTML};this.fnGetCurrentTD=function(){return m
};this.fnSetPosition=function(H,I){if(typeof H=="object"&&H.nodeName){b(H)}else{b(E(H,I))
}};var f=null;var m=null;var c=null;var a=null;var G=null;var i="focus";var w=false;
var r={action:[],esc:[],focus:[],blur:[]};var e=null;var n;var s;var C=false;function B(H){return function(I,L,K){if((I===null||typeof I=="number")&&(L===null||typeof L=="number")&&typeof K=="function"){x(H,I,L,K)
}else{if(typeof I=="object"&&typeof L=="function"){var J=j(I);x(H,J[0],J[1],L)}else{alert("Unhandable event type was added: x"+I+"  y:"+L+"  z:"+K)
}}}}function d(H){return function(I,L,K){if((I===null||typeof arguments[0]=="number")&&(L===null||typeof arguments[1]=="number")){if(typeof arguments[2]=="function"){F(H,I,L,K)
}else{F(H,I,L)}}else{if(typeof arguments[0]=="object"){var J=j(I);if(typeof arguments[1]=="function"){F(H,J[0],J[1],L)
}else{F(H,J[0],J[1])}}else{alert("Unhandable event type was removed: x"+I+"  y:"+L+"  z:"+K)
}}}}for(var o in r){if(o){this.event[o]=B(o);this.event.remove[o]=d(o)}}function x(K,H,J,I){r[K].push({x:H,y:J,fn:I})
}function F(N,I,M,K){var L=0;for(var J=0,H=r[N].length;J<H-L;J++){if(typeof K!="undefined"){if(r[N][J-L].x==I&&r[N][J-L].y==M&&r[N][J-L].fn==K){r[N].splice(J-L,1);
L++}}else{if(r[N][J-L].x==I&&r[N][J-L].y==M){r[N].splice(J,1);return 1}}}return L
}function A(M,H,L){var K=0;var I=r[M];for(var J=0;J<I.length;J++){if((I[J].x==H&&I[J].y==L)||(I[J].x===null&&I[J].y==L)||(I[J].x==H&&I[J].y===null)||(I[J].x===null&&I[J].y===null)){I[J].fn(E(H,L),H,L);
K++}}return K}function b(M,U){if(m==M){return}if(typeof U=="undefined"){U=true}if(m!==null){p(m)
}jQuery(M).addClass(i);var J;if(e){J=e.fnSettings();var P=g(M)[1];var Q=w;while(P>=J.fnDisplayEnd()){if(J._iDisplayLength>=0){if(J._iDisplayStart+J._iDisplayLength<J.fnRecordsDisplay()){J._iDisplayStart+=J._iDisplayLength
}}else{J._iDisplayStart=0}e.oApi._fnCalculateEnd(J)}while(P<J._iDisplayStart){J._iDisplayStart=J._iDisplayLength>=0?J._iDisplayStart-J._iDisplayLength:0;
if(J._iDisplayStart<0){J._iDisplayStart=0}e.oApi._fnCalculateEnd(J)}e.oApi._fnDraw(J);
w=Q}var N=j(M);m=M;c=N[0];a=N[1];var R,T,L,O,H,I,S;if(U){R=document.documentElement.clientHeight;
T=document.documentElement.clientWidth;L=document.body.scrollTop||document.documentElement.scrollTop;
O=document.body.scrollLeft||document.documentElement.scrollLeft;H=M.offsetHeight;
I=M.offsetWidth;S=y(M);if(S[1]+H>L+R){z(S[1]+H-R)}else{if(S[1]<L){z(S[1])}}if(S[0]+I>O+T){t(S[0]+I-T)
}else{if(S[0]<O){t(S[0])}}}if(e&&typeof J.oScroll!="undefined"&&(J.oScroll.sX!==""||J.oScroll.xY!=="")){var K=J.nTable.parentNode;
R=K.clientHeight;T=K.clientWidth;L=K.scrollTop;O=K.scrollLeft;H=M.offsetHeight;I=M.offsetWidth;
if(M.offsetTop+H>R+L){K.scrollTop=(M.offsetTop+H)-R}else{if(M.offsetTop<L){K.scrollTop=M.offsetTop
}}if(M.offsetLeft+I>T+O){K.scrollLeft=(M.offsetLeft+I)-T}else{if(M.offsetLeft<O){K.scrollLeft=M.offsetLeft
}}}A("focus",c,a)}function u(){p(m);c=null;a=null;m=null;k()}function p(H){jQuery(H).removeClass(i);
A("blur",c,a)}function l(H){var I=this;while(I.nodeName!="TD"){I=I.parentNode}b(I);
D()}function h(N){if(G.block||!w){return true}if(N.metaKey||N.altKey||N.ctrlKey){return true
}var I,O,L=f.getElementsByTagName("tr")[0].getElementsByTagName("td").length,H;if(e){var M=e.fnSettings();
H=M.aiDisplay.length;var K=g(m);if(K===null){return}c=K[0];a=K[1]}else{H=f.getElementsByTagName("tr").length
}var J=(N.keyCode==9&&N.shiftKey)?-1:N.keyCode;switch(J){case 13:N.preventDefault();
N.stopPropagation();A("action",c,a);return true;case 27:if(!A("esc",c,a)){u()}break;
case -1:case 37:if(c>0){I=c-1;O=a}else{if(a>0){I=L-1;O=a-1}else{if(J==-1&&n){C=true;
s.focus();setTimeout(function(){C=false},0);w=false;u();return true}else{return false
}}}break;case 38:if(a>0){I=c;O=a-1}else{return false}break;case 9:case 39:if(c<L-1){I=c+1;
O=a}else{if(a<H-1){I=0;O=a+1}else{if(J==9&&n){C=true;s.focus();setTimeout(function(){C=false
},0);w=false;u();return true}else{return false}}}break;case 40:if(a<H-1){I=c;O=a+1
}else{return false}break;default:return true}b(E(I,O));return false}function D(){if(!w){w=true
}}function k(){w=false}function E(H,J){if(e){var I=e.fnSettings();if(typeof I.aoData[I.aiDisplay[J]]!="undefined"){return I.aoData[I.aiDisplay[J]].nTr.getElementsByTagName("td")[H]
}else{return null}}else{return jQuery("tr:eq("+J+")>td:eq("+H+")",f)[0]}}function j(I){if(e){var H=e.fnSettings();
return[jQuery("td",I.parentNode).index(I),jQuery("tr",I.parentNode.parentNode).index(I.parentNode)+H._iDisplayStart]
}else{return[jQuery("td",I.parentNode).index(I),jQuery("tr",I.parentNode.parentNode).index(I.parentNode)]
}}function z(H){document.documentElement.scrollTop=H;document.body.scrollTop=H}function t(H){document.documentElement.scrollLeft=H;
document.body.scrollLeft=H}function y(J){var I=0;var H=0;if(J.offsetParent){I=J.offsetLeft;
H=J.offsetTop;J=J.offsetParent;while(J){I+=J.offsetLeft;H+=J.offsetTop;J=J.offsetParent
}}return[I,H]}function g(O){var M=e.fnSettings();for(var K=0,H=M.aiDisplay.length;
K<H;K++){var N=M.aoData[M.aiDisplay[K]].nTr;var I=N.getElementsByTagName("td");for(var J=0,L=I.length;
J<L;J++){if(I[J]==O){return[J,K]}}}return null}function v(H,J){G=J;if(typeof H=="undefined"){H={}
}if(typeof H.focus=="undefined"){H.focus=[0,0]}if(typeof H.table=="undefined"){H.table=jQuery("table.KeyTable")[0]
}if(typeof H.focusClass!="undefined"){i=H.focusClass}if(typeof H.datatable!="undefined"){e=H.datatable
}if(typeof H.initScroll=="undefined"){H.initScroll=true}if(typeof H.form=="undefined"){H.form=false
}n=H.form;f=H.table.getElementsByTagName("tbody")[0];if(n){var I=document.createElement("div");
s=document.createElement("input");I.style.height="1px";I.style.width="0px";I.style.overflow="hidden";
if(typeof H.tabIndex!="undefined"){s.tabIndex=H.tabIndex}I.appendChild(s);H.table.parentNode.insertBefore(I,H.table.nextSibling);
jQuery(s).focus(function(){if(!C){w=true;C=false;if(typeof H.focus.nodeName!="undefined"){b(H.focus,H.initScroll)
}else{b(E(H.focus[0],H.focus[1]),H.initScroll)}setTimeout(function(){s.blur()},0)
}});w=false}else{if(typeof H.focus.nodeName!="undefined"){b(H.focus,H.initScroll)
}else{b(E(H.focus[0],H.focus[1]),H.initScroll)}D()}if(jQuery.browser.mozilla||jQuery.browser.opera){jQuery(document).bind("keypress",h)
}else{jQuery(document).bind("keydown",h)}if(e){jQuery("td",e.fnGetNodes()).click(l)
}else{jQuery("td",f).click(l)}jQuery(document).click(function(L){var M=L.target;var K=false;
while(M){if(M==H.table){K=true;break}M=M.parentNode}if(!K){u()}})}v(q,this)};