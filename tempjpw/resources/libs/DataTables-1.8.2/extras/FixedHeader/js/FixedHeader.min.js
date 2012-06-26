/*
 * File:        FixedHeader.min.js
 * Version:     2.0.4
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
var FixedHeader=function(b,a){if(typeof this.fnInit!="function"){alert("FixedHeader warning: FixedHeader must be initialised with the 'new' keyword.");
return}var c=this;var d={aoCache:[],oSides:{top:true,bottom:false,left:false,right:false},oZIndexes:{top:104,bottom:103,left:102,right:101},oMes:{iTableWidth:0,iTableHeight:0,iTableLeft:0,iTableRight:0,iTableTop:0,iTableBottom:0},nTable:null,bUseAbsPos:false,bFooter:false};
this.fnGetSettings=function(){return d};this.fnUpdate=function(){this._fnUpdateClones();
this._fnUpdatePositions()};this.fnInit(b,a)};FixedHeader.prototype={fnInit:function(b,a){var c=this.fnGetSettings();
var d=this;this.fnInitSettings(c,a);if(typeof b.fnSettings=="function"){if(typeof b.fnVersionCheck=="functon"&&b.fnVersionCheck("1.6.0")!==true){alert("FixedHeader 2 required DataTables 1.6.0 or later. Please upgrade your DataTables installation");
return}var e=b.fnSettings();if(e.oScroll.sX!=""||e.oScroll.sY!=""){alert("FixedHeader 2 is not supported with DataTables' scrolling mode at this time");
return}c.nTable=e.nTable;e.aoDrawCallback.push({fn:function(){FixedHeader.fnMeasure();
d._fnUpdateClones.call(d);d._fnUpdatePositions.call(d)},sName:"FixedHeader"})}else{c.nTable=b
}c.bFooter=($(">tfoot",c.nTable).length>0)?true:false;c.bUseAbsPos=(jQuery.browser.msie&&(jQuery.browser.version=="6.0"||jQuery.browser.version=="7.0"));
if(c.oSides.top){c.aoCache.push(d._fnCloneTable("fixedHeader","FixedHeader_Header",d._fnCloneThead))
}if(c.oSides.bottom){c.aoCache.push(d._fnCloneTable("fixedFooter","FixedHeader_Footer",d._fnCloneTfoot))
}if(c.oSides.left){c.aoCache.push(d._fnCloneTable("fixedLeft","FixedHeader_Left",d._fnCloneTLeft))
}if(c.oSides.right){c.aoCache.push(d._fnCloneTable("fixedRight","FixedHeader_Right",d._fnCloneTRight))
}FixedHeader.afnScroll.push(function(){d._fnUpdatePositions.call(d)});jQuery(window).resize(function(){FixedHeader.fnMeasure();
d._fnUpdateClones.call(d);d._fnUpdatePositions.call(d)});FixedHeader.fnMeasure();
d._fnUpdateClones();d._fnUpdatePositions()},fnInitSettings:function(b,a){if(typeof a!="undefined"){if(typeof a.top!="undefined"){b.oSides.top=a.top
}if(typeof a.bottom!="undefined"){b.oSides.bottom=a.bottom}if(typeof a.left!="undefined"){b.oSides.left=a.left
}if(typeof a.right!="undefined"){b.oSides.right=a.right}if(typeof a.zTop!="undefined"){b.oZIndexes.top=a.zTop
}if(typeof a.zBottom!="undefined"){b.oZIndexes.bottom=a.zBottom}if(typeof a.zLeft!="undefined"){b.oZIndexes.left=a.zLeft
}if(typeof a.zRight!="undefined"){b.oZIndexes.right=a.zRight}}b.bUseAbsPos=(jQuery.browser.msie&&(jQuery.browser.version=="6.0"||jQuery.browser.version=="7.0"))
},_fnCloneTable:function(f,e,d){var b=this.fnGetSettings();var a;if(jQuery(b.nTable.parentNode).css("position")!="absolute"){b.nTable.parentNode.style.position="relative"
}a=b.nTable.cloneNode(false);var c=document.createElement("div");c.style.position="absolute";
c.className+=" FixedHeader_Cloned "+f+" "+e;if(f=="fixedHeader"){c.style.zIndex=b.oZIndexes.top
}if(f=="fixedFooter"){c.style.zIndex=b.oZIndexes.bottom}if(f=="fixedLeft"){c.style.zIndex=b.oZIndexes.left
}else{if(f=="fixedRight"){c.style.zIndex=b.oZIndexes.right}}c.appendChild(a);document.body.appendChild(c);
return{nNode:a,nWrapper:c,sType:f,sPosition:"",sTop:"",sLeft:"",fnClone:d}},_fnMeasure:function(){var d=this.fnGetSettings(),a=d.oMes,c=jQuery(d.nTable),b=c.offset(),f=this._fnSumScroll(d.nTable.parentNode,"scrollTop"),e=this._fnSumScroll(d.nTable.parentNode,"scrollLeft");
a.iTableWidth=c.outerWidth();a.iTableHeight=c.outerHeight();a.iTableLeft=b.left+d.nTable.parentNode.scrollLeft;
a.iTableTop=b.top+f;a.iTableRight=a.iTableLeft+a.iTableWidth;a.iTableRight=FixedHeader.oDoc.iWidth-a.iTableLeft-a.iTableWidth;
a.iTableBottom=FixedHeader.oDoc.iHeight-a.iTableTop-a.iTableHeight},_fnSumScroll:function(c,b){var a=c[b];
while(c=c.parentNode){if(c.nodeName!="HTML"&&c.nodeName!="BODY"){break}a=c[b]}return a
},_fnUpdatePositions:function(){var c=this.fnGetSettings();this._fnMeasure();for(var b=0,a=c.aoCache.length;
b<a;b++){if(c.aoCache[b].sType=="fixedHeader"){this._fnScrollFixedHeader(c.aoCache[b])
}else{if(c.aoCache[b].sType=="fixedFooter"){this._fnScrollFixedFooter(c.aoCache[b])
}else{if(c.aoCache[b].sType=="fixedLeft"){this._fnScrollHorizontalLeft(c.aoCache[b])
}else{this._fnScrollHorizontalRight(c.aoCache[b])}}}}},_fnUpdateClones:function(){var c=this.fnGetSettings();
for(var b=0,a=c.aoCache.length;b<a;b++){c.aoCache[b].fnClone.call(this,c.aoCache[b])
}},_fnScrollHorizontalRight:function(g){var e=this.fnGetSettings(),f=e.oMes,b=FixedHeader.oWin,a=FixedHeader.oDoc,d=g.nWrapper,c=jQuery(d).outerWidth();
if(b.iScrollRight<f.iTableRight){this._fnUpdateCache(g,"sPosition","absolute","position",d.style);
this._fnUpdateCache(g,"sTop",f.iTableTop+"px","top",d.style);this._fnUpdateCache(g,"sLeft",(f.iTableLeft+f.iTableWidth-c)+"px","left",d.style)
}else{if(f.iTableLeft<a.iWidth-b.iScrollRight-c){if(e.bUseAbsPos){this._fnUpdateCache(g,"sPosition","absolute","position",d.style);
this._fnUpdateCache(g,"sTop",f.iTableTop+"px","top",d.style);this._fnUpdateCache(g,"sLeft",(a.iWidth-b.iScrollRight-c)+"px","left",d.style)
}else{this._fnUpdateCache(g,"sPosition","fixed","position",d.style);this._fnUpdateCache(g,"sTop",(f.iTableTop-b.iScrollTop)+"px","top",d.style);
this._fnUpdateCache(g,"sLeft",(b.iWidth-c)+"px","left",d.style)}}else{this._fnUpdateCache(g,"sPosition","absolute","position",d.style);
this._fnUpdateCache(g,"sTop",f.iTableTop+"px","top",d.style);this._fnUpdateCache(g,"sLeft",f.iTableLeft+"px","left",d.style)
}}},_fnScrollHorizontalLeft:function(g){var e=this.fnGetSettings(),f=e.oMes,b=FixedHeader.oWin,a=FixedHeader.oDoc,c=g.nWrapper,d=jQuery(c).outerWidth();
if(b.iScrollLeft<f.iTableLeft){this._fnUpdateCache(g,"sPosition","absolute","position",c.style);
this._fnUpdateCache(g,"sTop",f.iTableTop+"px","top",c.style);this._fnUpdateCache(g,"sLeft",f.iTableLeft+"px","left",c.style)
}else{if(b.iScrollLeft<f.iTableLeft+f.iTableWidth-d){if(e.bUseAbsPos){this._fnUpdateCache(g,"sPosition","absolute","position",c.style);
this._fnUpdateCache(g,"sTop",f.iTableTop+"px","top",c.style);this._fnUpdateCache(g,"sLeft",b.iScrollLeft+"px","left",c.style)
}else{this._fnUpdateCache(g,"sPosition","fixed","position",c.style);this._fnUpdateCache(g,"sTop",(f.iTableTop-b.iScrollTop)+"px","top",c.style);
this._fnUpdateCache(g,"sLeft","0px","left",c.style)}}else{this._fnUpdateCache(g,"sPosition","absolute","position",c.style);
this._fnUpdateCache(g,"sTop",f.iTableTop+"px","top",c.style);this._fnUpdateCache(g,"sLeft",(f.iTableLeft+f.iTableWidth-d)+"px","left",c.style)
}}},_fnScrollFixedFooter:function(h){var f=this.fnGetSettings(),g=f.oMes,b=FixedHeader.oWin,a=FixedHeader.oDoc,c=h.nWrapper,e=jQuery("thead",f.nTable).outerHeight(),d=jQuery(c).outerHeight();
if(b.iScrollBottom<g.iTableBottom){this._fnUpdateCache(h,"sPosition","absolute","position",c.style);
this._fnUpdateCache(h,"sTop",(g.iTableTop+g.iTableHeight-d)+"px","top",c.style);this._fnUpdateCache(h,"sLeft",g.iTableLeft+"px","left",c.style)
}else{if(b.iScrollBottom<g.iTableBottom+g.iTableHeight-d-e){if(f.bUseAbsPos){this._fnUpdateCache(h,"sPosition","absolute","position",c.style);
this._fnUpdateCache(h,"sTop",(a.iHeight-b.iScrollBottom-d)+"px","top",c.style);this._fnUpdateCache(h,"sLeft",g.iTableLeft+"px","left",c.style)
}else{this._fnUpdateCache(h,"sPosition","fixed","position",c.style);this._fnUpdateCache(h,"sTop",(b.iHeight-d)+"px","top",c.style);
this._fnUpdateCache(h,"sLeft",(g.iTableLeft-b.iScrollLeft)+"px","left",c.style)}}else{this._fnUpdateCache(h,"sPosition","absolute","position",c.style);
this._fnUpdateCache(h,"sTop",(g.iTableTop+d)+"px","top",c.style);this._fnUpdateCache(h,"sLeft",g.iTableLeft+"px","left",c.style)
}}},_fnScrollFixedHeader:function(g){var d=this.fnGetSettings(),f=d.oMes,b=FixedHeader.oWin,a=FixedHeader.oDoc,c=g.nWrapper,e=d.nTable.getElementsByTagName("tbody")[0].offsetHeight;
if(f.iTableTop>b.iScrollTop){this._fnUpdateCache(g,"sPosition","absolute","position",c.style);
this._fnUpdateCache(g,"sTop",f.iTableTop+"px","top",c.style);this._fnUpdateCache(g,"sLeft",f.iTableLeft+"px","left",c.style)
}else{if(b.iScrollTop>f.iTableTop+e){this._fnUpdateCache(g,"sPosition","absolute","position",c.style);
this._fnUpdateCache(g,"sTop",(f.iTableTop+e)+"px","top",c.style);this._fnUpdateCache(g,"sLeft",f.iTableLeft+"px","left",c.style)
}else{if(d.bUseAbsPos){this._fnUpdateCache(g,"sPosition","absolute","position",c.style);
this._fnUpdateCache(g,"sTop",b.iScrollTop+"px","top",c.style);this._fnUpdateCache(g,"sLeft",f.iTableLeft+"px","left",c.style)
}else{this._fnUpdateCache(g,"sPosition","fixed","position",c.style);this._fnUpdateCache(g,"sTop","0px","top",c.style);
this._fnUpdateCache(g,"sLeft",(f.iTableLeft-b.iScrollLeft)+"px","left",c.style)}}}},_fnUpdateCache:function(e,c,b,d,a){if(e[c]!=b){a[d]=b;
e[c]=b}},_fnCloneThead:function(d){var c=this.fnGetSettings();var a=d.nNode;d.nWrapper.style.width=jQuery(c.nTable).outerWidth()+"px";
while(a.childNodes.length>0){jQuery("thead th",a).unbind("click");a.removeChild(a.childNodes[0])
}var b=jQuery("thead",c.nTable).clone(true)[0];a.appendChild(b);jQuery("thead:eq(0)>tr th",c.nTable).each(function(e){jQuery("thead:eq(0)>tr th:eq("+e+")",a).width(jQuery(this).width())
});jQuery("thead:eq(0)>tr td",c.nTable).each(function(e){jQuery("thead:eq(0)>tr th:eq("+e+")",a)[0].style.width(jQuery(this).width())
})},_fnCloneTfoot:function(d){var c=this.fnGetSettings();var a=d.nNode;d.nWrapper.style.width=jQuery(c.nTable).outerWidth()+"px";
while(a.childNodes.length>0){a.removeChild(a.childNodes[0])}var b=jQuery("tfoot",c.nTable).clone(true)[0];
a.appendChild(b);jQuery("tfoot:eq(0)>tr th",c.nTable).each(function(e){jQuery("tfoot:eq(0)>tr th:eq("+e+")",a).width(jQuery(this).width())
});jQuery("tfoot:eq(0)>tr td",c.nTable).each(function(e){jQuery("tfoot:eq(0)>tr th:eq("+e+")",a)[0].style.width(jQuery(this).width())
})},_fnCloneTLeft:function(f){var c=this.fnGetSettings();var b=f.nNode;var e=jQuery("tbody tr:eq(0) td",c.nTable).length;
var a=($.browser.msie&&($.browser.version=="6.0"||$.browser.version=="7.0"));while(b.childNodes.length>0){b.removeChild(b.childNodes[0])
}b.appendChild(jQuery("thead",c.nTable).clone(true)[0]);b.appendChild(jQuery("tbody",c.nTable).clone(true)[0]);
if(c.bFooter){b.appendChild(jQuery("tfoot",c.nTable).clone(true)[0])}jQuery("thead tr th:gt(0)",b).remove();
jQuery("tfoot tr th:gt(0)",b).remove();$("tbody tr",b).each(function(g){$("td:gt(0)",this).remove();
if($.browser.mozilla||$.browser.opera){$("td",this).height($("tbody tr:eq("+g+")",that.dom.body).outerHeight())
}else{$("td",this).height($("tbody tr:eq("+g+")",that.dom.body).outerHeight()-iBoxHack)
}if(!a){$("tbody tr:eq("+g+")",that.dom.body).height($("tbody tr:eq("+g+")",that.dom.body).outerHeight())
}});var d=jQuery("thead tr th:eq(0)",c.nTable).outerWidth();b.style.width=d+"px";
f.nWrapper.style.width=d+"px"},_fnCloneTRight:function(f){var c=this.fnGetSettings();
var b=f.nNode;var e=jQuery("tbody tr:eq(0) td",c.nTable).length;var a=($.browser.msie&&($.browser.version=="6.0"||$.browser.version=="7.0"));
while(b.childNodes.length>0){b.removeChild(b.childNodes[0])}b.appendChild(jQuery("thead",c.nTable).clone(true)[0]);
b.appendChild(jQuery("tbody",c.nTable).clone(true)[0]);if(c.bFooter){b.appendChild(jQuery("tfoot",c.nTable).clone(true)[0])
}jQuery("thead tr th:not(:nth-child("+e+"n))",b).remove();jQuery("tfoot tr th:not(:nth-child("+e+"n))",b).remove();
$("tbody tr",b).each(function(g){$("td:lt("+e-1+")",this).remove();if($.browser.mozilla||$.browser.opera){$("td",this).height($("tbody tr:eq("+g+")",that.dom.body).outerHeight())
}else{$("td",this).height($("tbody tr:eq("+g+")",that.dom.body).outerHeight()-iBoxHack)
}if(!a){$("tbody tr:eq("+g+")",that.dom.body).height($("tbody tr:eq("+g+")",that.dom.body).outerHeight())
}});var d=jQuery("thead tr th:eq("+(e-1)+")",c.nTable).outerWidth();b.style.width=d+"px";
f.nWrapper.style.width=d+"px"}};FixedHeader.oWin={iScrollTop:0,iScrollRight:0,iScrollBottom:0,iScrollLeft:0,iHeight:0,iWidth:0};
FixedHeader.oDoc={iHeight:0,iWidth:0};FixedHeader.afnScroll=[];FixedHeader.fnMeasure=function(){var d=jQuery(window),c=jQuery(document),b=FixedHeader.oWin,a=FixedHeader.oDoc;
a.iHeight=c.height();a.iWidth=c.width();b.iHeight=d.height();b.iWidth=d.width();b.iScrollTop=d.scrollTop();
b.iScrollLeft=d.scrollLeft();b.iScrollRight=a.iWidth-b.iScrollLeft-b.iWidth;b.iScrollBottom=a.iHeight-b.iScrollTop-b.iHeight
};jQuery(window).scroll(function(){FixedHeader.fnMeasure();for(var b=0,a=FixedHeader.afnScroll.length;
b<a;b++){FixedHeader.afnScroll[b]()}});