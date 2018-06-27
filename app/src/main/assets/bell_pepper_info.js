; /* for ignore BOM */
function TBellPepperInfo() {
    this.CurDom = null;
    this.TimerId = null;
    this.Remain = null;
    this.Reg = new RegExp('');
    this.Reg.compile('[、。,・（）「」｛｝［］(){}\\s]');
    this.TouchCnt = 0;
    this.TouchMoved = false;
    this.TouchStartX = 0;
    this.TouchStartY = 0;
}
var gBellPepperInfo = new TBellPepperInfo();
gBellPepperInfo.Trim = function (str) { return str.replace(/^\\s+|\\s+$/g, ''); };
gBellPepperInfo.IsBlank = function (s) { return this.Trim(s) == ''; };
gBellPepperInfo.GetLoc = function (dom1) { var rng1, rec1; try { rng1 = document.createRange(); if (dom1.nodeName == '#text') { rng1.setStart(dom1, 0); rng1.setEnd(dom1, (dom1.nodeValue == null ? 0 : dom1.nodeValue.length)); } else { rng1.selectNode(dom1); } rec1 = rng1.getBoundingClientRect(); return rec1; } catch (e) { return null; } };
gBellPepperInfo.IsValid = function (dom1) { var st, rec1, rec2, i1; rec1 = this.GetLoc(dom1);
	if (rec1 != null) { if (window.scrollX + rec1.right < 0 || window.scrollY + rec1.bottom < 0) { return null; } }
	if (false && dom1.nodeName == '#text') {
		var dom2; bell_pepper_info.Log('chk rec1:' + dom1.nodeName + ' ' + rec1.left + ' ' + rec1.right + ' ' + rec1.top + ' ' + rec1.bottom);
		for (dom2 = dom1.parentNode; dom2 != null && dom2.nodeName != 'BODY'; dom2 = dom2.parentNode) {
			rec2 = this.GetLoc(dom2);
			if (rec2 == null) { bell_pepper_info.Log('chk loc NG:' + dom2.nodeName); break; }
			if (rec1.right < rec2.left || rec2.right < rec1.left || rec1.bottom < rec2.top || rec2.bottom < rec1.top) { return null; }
			bell_pepper_info.Log('chk rec2:' + dom2.nodeName + ' ' + rec2.left + ' ' + rec2.right + ' ' + rec2.top + ' ' + rec2.bottom);
		}
	}
	/* patch for zhihu */
	if (dom1.nodeName == 'NOSCRIPT') { return null; }
	if (dom1.nodeName == '#text') { return rec1; }
	/* patch for ifeng */
	try {
		st = document.defaultView.getComputedStyle(dom1, '');
		if (st != null) {
			if (st.display != 'inline') { this.AllInline = false; }
			if (st.display == 'none' || st.visibility == 'hidden') { return null; }
		}
		return rec1;
	}catch(err){
		return null;
	}
};

gBellPepperInfo.GetSelectText = function (dom1) { var str1, idx; idx = dom1.selectedIndex; if (idx != -1) { str1 = dom1.options[idx].textContent; if (str1 != null && !this.IsBlank(str1)) { return str1; } } return null; };
gBellPepperInfo.GetRubyText = function (dom1) { var dom2; for (dom2 = dom1.firstChild; dom2 != null; dom2 = dom2.nextSibling) { if (dom2.nodeName == 'RT') { return dom2.textContent; } } return null; };
gBellPepperInfo.FirstDom = function (dom1) { var str1, rec1; rec1 = this.IsValid(dom1);
	if (rec1 == null) { return this.NextDom(dom1); }
	if (dom1.firstChild != null) { switch (dom1.nodeName) { case 'SELECT': str1 = this.GetSelectText(dom1); if (str1 != null) { return dom1; } return this.NextDom(dom1); case 'RUBY': return dom1; } return this.FirstDom(dom1.firstChild); } else { if (dom1.nodeName == '#text') { str1 = dom1.nodeValue; if (str1 != null && !this.IsBlank(str1)) { return dom1; } } return this.NextDom(dom1); } };

gBellPepperInfo.NextDom = function (dom1) { var dom2, dom3, dom4; dom2 = dom1.nextSibling;
	/* nextSibling cannot cross-origin frame */
	if (dom2 != null) { return this.FirstDom(dom2); }
	for (dom3 = dom1.parentNode; dom3 != null; dom3 = dom3.parentNode) {
		dom4 = dom3.nextSibling;
		if (dom4 != null) { return this.FirstDom(dom4); }
	}
	return null;
};
gBellPepperInfo.LastDom = function (dom1) { var str1, rec1; rec1 = this.IsValid(dom1); if (rec1 == null) { return this.PrevDom(dom1); } if (dom1.lastChild != null) { switch (dom1.nodeName) { case 'SELECT': str1 = this.GetSelectText(dom1); if (str1 != null) { return dom1; } return this.PrevDom(dom1); case 'RUBY': return dom1; } return this.LastDom(dom1.lastChild); } else { if (dom1.nodeName == '#text') { str1 = dom1.nodeValue; if (str1 != null && !this.IsBlank(str1)) { return dom1; } } return this.PrevDom(dom1); } };
gBellPepperInfo.PrevDom = function (dom1) { var dom2, dom3, dom4; dom2 = dom1.previousSibling; if (dom2 != null) { return this.LastDom(dom2); } for (dom3 = dom1.parentNode; dom3 != null; dom3 = dom3.parentNode) { dom4 = dom3.previousSibling; if (dom4 != null) { return this.LastDom(dom4); } } return null; };
gBellPepperInfo.ClickDom = function () { var dom1; for (dom1 = this.CurDom; dom1 != null && dom1.nodeName != 'BODY'; dom1 = dom1.parentNode) { if (dom1.nodeName == 'A') { window.location.href = dom1.href; break; } } };
gBellPepperInfo.GetLink = function (dom1) { var dom2; for (dom2 = dom1; dom2 != null && dom2.nodeName != 'BODY'; dom2 = dom2.parentNode) { if (dom2.nodeName == 'A') { return dom2; } } return null; };
gBellPepperInfo.MoveMark = function (mk1, x1, y1, w1, h1) { mk1.style.left = x1 + 'px'; mk1.style.top = y1 + 'px'; mk1.style.width = w1 + 'px'; mk1.style.height = h1 + 'px'; };
gBellPepperInfo.ShowMark = function (x1, y1, w1, h1) { var mk1, id1, i1, v1, x2, y2; v1 = new Array(); for (i1 = 0; i1 < 4; i1++) { id1 = 'bell_pepper_info_mark' + i1; mk1 = document.getElementById(id1); if (mk1 == null) { mk1 = document.createElement('span'); mk1.id = id1; mk1.style.position = 'absolute'; mk1.style.zIndex = 100; mk1.style.backgroundColor = 'orange'; } else { document.body.removeChild(mk1); } document.body.appendChild(mk1); v1.push(mk1); } x2 = x1 + w1; y2 = y1 + h1; this.MoveMark(v1[0], x1, y1, 4, h1 + 4); this.MoveMark(v1[1], x2, y1, 4, h1 + 4); this.MoveMark(v1[2], x1, y1, w1 + 4, 4); this.MoveMark(v1[3], x1, y2, w1 + 4, 4); };
gBellPepperInfo.SelectDom = function (dom1) {
	var x1, y1; var rec1, mg = 0;
	if (dom1.parentNode.nodeName == 'BODY') {
		bell_pepper_info.Log('select dom: body'); return;
	}
	rec1 = this.GetLoc(dom1);
	this.ShowMark(window.scrollX + rec1.left - 4, window.scrollY + rec1.top - 4, rec1.width + 4, rec1.height + 4);
	x1 = window.scrollX; y1 = window.scrollY;
	if (window.innerWidth < rec1.right + mg) { x1 = Math.max(0, window.scrollX + rec1.right + mg - window.innerWidth); }
	else if (rec1.left < 0) { x1 = Math.max(0, window.scrollX + rec1.left); }
	if (window.innerHeight < rec1.bottom + mg) { y1 = Math.max(0, window.scrollY + rec1.bottom + mg - window.innerHeight); }
	else if (rec1.top < 0) { y1 = Math.max(0, window.scrollY + rec1.top); }
	if (x1 != window.scrollX || y1 != window.scrollY) { window.scroll(x1, y1); } else { } };
gBellPepperInfo.GetDomRect = function (rec1, dom1) { var rec2; if (dom1.parentNode.nodeName == 'BODY') { return null; } rec2 = this.GetLoc(dom1); if (rec2 == null) { return rec1; } if (rec1 == null) { return rec2; } return { left: Math.min(rec1.left, rec2.left), top: Math.min(rec1.top, rec2.top), right: Math.max(rec1.right, rec2.right), bottom: Math.max(rec1.bottom, rec2.bottom) }; };
gBellPepperInfo.ShowMarkScroll = function (rec1) { var x1, y1, mg = 0; this.ShowMark(window.scrollX + rec1.left - 4, window.scrollY + rec1.top - 4, rec1.right - rec1.left + 4, rec1.bottom - rec1.top + 4); x1 = window.scrollX; y1 = window.scrollY; if (window.innerWidth < rec1.right + mg) { x1 = Math.max(0, window.scrollX + rec1.right + mg - window.innerWidth); } else if (rec1.left < 0) { x1 = Math.max(0, window.scrollX + rec1.left); } if (window.innerHeight < rec1.bottom + mg) { y1 = Math.max(0, window.scrollY + rec1.bottom + mg - window.innerHeight); } else if (rec1.top < 0) { y1 = Math.max(0, window.scrollY + rec1.top); } if (x1 != window.scrollX || y1 != window.scrollY) { window.scroll(x1, y1); } };
gBellPepperInfo.SpeakSent = function (str1) { var len1, k1, v1, n; if (bell_pepper_info.ReadPhrase() == 1) { n = 7; len1 = str1.length; if (30 < len1) { v1 = this.Reg.exec(str1.substr(n)); if (v1 != null) { k1 = n + v1.index; if (k1 != n + -1 && n < len1 - k1) { k1++; this.Remain = str1.substr(k1); bell_pepper_info.Speak(0, str1.substr(0, k1)); return; } } } } this.Remain = null; bell_pepper_info.Speak(0, str1); };
gBellPepperInfo.GetSpeakText = function (dom1) { var str1; switch (dom1.nodeName) { case '#text': return this.Trim(dom1.nodeValue); case 'SELECT': return this.Trim(this.GetSelectText(dom1)); case 'RUBY': return this.Trim(this.GetRubyText(dom1)); } return null; };
gBellPepperInfo.SpeakDom2 = function (rem_str, dom1) { var str1, str2, dom2, dom3, k1, rec1, i; this.Remain = null; dom2 = dom1; str1 = null; rec1 = null; for (i = 0; ; i++) { if (i == 0 && rem_str != null) { str2 = rem_str; } else { str2 = this.GetSpeakText(dom2); } if (str2 != null && 0 < str2.length) { rec1 = this.GetDomRect(rec1, dom2); if (str1 == null) { str1 = ''; } else { str1 = str1 + ' '; } k1 = bell_pepper_info.SplitSent(str2); if (k1 != -1) { str1 = str1 + str2.substr(0, k1); this.Remain = str2.substr(k1); break; } str1 = str1 + str2; if (bell_pepper_info.MaxTextLen() < str1.length) { break; } } this.AllInline = true; dom3 = this.NextDom(dom2); if (dom3 == null || str1 != null && this.AllInline != true) { break; } dom2 = dom3; } if (str1 != null) { if (rec1 != null) { this.ShowMarkScroll(rec1); } this.CurDom = dom2; bell_pepper_info.Speak(0, str1); } };
gBellPepperInfo.SpeakDom = function (dom1) {
	var str1;
	switch (dom1.nodeName) {
		case '#text': this.SelectDom(dom1); this.SpeakSent(dom1.nodeValue);break;
		case 'SELECT': this.SelectDom(dom1); str1 = this.GetSelectText(dom1); if (str1 != null) { bell_pepper_info.Speak(0, str1); } break;
		case 'RUBY': this.SelectDom(dom1); str1 = this.GetRubyText(dom1); if (str1 != null) { bell_pepper_info.Speak(0, str1); } break;
	}
};
gBellPepperInfo.ChkDom = function (dom1) {
	var dom2;
	/* douban 会出现部分节点无父节点的情况，导致会从头开始读，原因是被remove了*/
	for (dom2 = dom1; dom2 != null; dom2 = dom2.parentNode) {
		if (dom2 == document.body) { return true; }
	}
	return false;
};
gBellPepperInfo.RewindFnc = function () { try { if (this.CurDom != null) { this.CurDom = this.PrevDom(this.CurDom); } } catch (e) { } if (this.CurDom != null) { this.SelectDom(this.CurDom); } else { this.StopTimer(); } };
gBellPepperInfo.FastForwardFnc = function () { try { if (this.CurDom != null) { this.CurDom = this.NextDom(this.CurDom); } } catch (e) { } if (this.CurDom != null) { this.SelectDom(this.CurDom); } else { this.StopTimer(); } };
gBellPepperInfo.OnTouchStartFnc = function (ev) {
	var touch = ev.touches[0]; this.TouchCnt = ev.touches.length; this.TouchMoved = false; this.TouchStartX = touch.pageX; this.TouchStartY = touch.pageY;
};
gBellPepperInfo.OnTouchMoveFnc = function (ev) { var touch = ev.touches[0]; this.TouchMoved = true; };
gBellPepperInfo.OnTouchEndFnc = function (ev) {
	var dom1, rec1; var rng1, dom2, dom3, x1, y1, x2, y2, time1, children;
	try {
		if (this.TouchCnt == 1 && !this.TouchMoved) {
			dom3 = null; dom1 = ev.target;
			if (dom1.hasChildNodes()) {
				rng1 = document.createRange(); time1 = (new Date()).getTime(); children = dom1.childNodes;
				for (var i = 0; i < children.length; i++) {
					if (i % 20 == 0 && 3000 < (new Date()).getTime() - time1) {
						bell_pepper_info.Log('time out');
						break;
					}
					dom2 = children[i]; rng1.selectNode(dom2); rec1 = rng1.getBoundingClientRect();
					if (rec1 == null) { }
					else { x1 = window.scrollX + rec1.left; y1 = window.scrollY + rec1.top; x2 = x1 + rec1.width; y2 = y1 + rec1.height;
						if (x1 <= this.TouchStartX && this.TouchStartX <= x2 && y1 <= this.TouchStartY && this.TouchStartY <= y2) {
							dom3 = dom2; break;
						}
						else { }
					}
				}
			}
			if (dom3 == null) { dom1 = this.FirstDom(ev.target); }
			else { dom1 = this.FirstDom(dom3); }
			if (dom1 != null) {
				switch (dom1.nodeName) {
					case '#text':
					case 'SELECT':
					case 'RUBY':this.CurDom = dom1; rec1 = this.GetLoc(dom1); this.ShowMark(window.scrollX + rec1.left - 4, window.scrollY + rec1.top - 4, rec1.width + 4, rec1.height + 4); break;
				}
			}
		}
	}
	catch (e) {
		bell_pepper_info.Log('Touch end err:' + e.message);
	}
	this.TouchCnt = 0; this.TouchMoved = false;
};
gBellPepperInfo.PageFinished = function () {
	if (document.m_BellPepperInfo == undefined) {
		document.m_BellPepperInfo = gBellPepperInfo; bell_pepper_info.Log('Page loading completed');
		document.addEventListener('touchstart', function (ev) { gBellPepperInfo.OnTouchStartFnc(ev); }, false);
		document.addEventListener('touchmove', function (ev) { gBellPepperInfo.OnTouchMoveFnc(ev); }, false);
		document.addEventListener('touchend', function (ev) { gBellPepperInfo.OnTouchEndFnc(ev); }, false);
	}
	else { bell_pepper_info.Log('Page initialized'); }
};
gBellPepperInfo.SpeakCur = function () {
	this.StopTimer(); this.PageFinished();
	if (this.CurDom == null || !this.ChkDom(this.CurDom)) {
		this.CurDom = this.FirstDom(document.body.firstChild);
	}
	if (this.CurDom == null) { bell_pepper_info.Speak(0, 'empty document'); }
	else {
		if (bell_pepper_info.ReadPhrase() == 2) { this.SpeakDom2(null, this.CurDom); }
		else { this.SpeakDom(this.CurDom); }
	}
};
gBellPepperInfo.SpeakNext = function () { var k1;
	if (this.Remain != null) { 
		if (bell_pepper_info.ReadPhrase() == 2) { this.SpeakDom2(this.Remain, this.CurDom); }
		else { this.SpeakSent(this.Remain); } 
		return;
	}
	if (this.CurDom == null || !this.ChkDom(this.CurDom)) { this.CurDom = this.FirstDom(document.body); }
	else { this.CurDom = this.NextDom(this.CurDom); }
	if (this.CurDom == null) { bell_pepper_info.Speak(1, (bell_pepper_info.TtsLang() == 'ja' ? ' end of the page' : ' end of the page')); }
	else {
		if (bell_pepper_info.ReadPhrase() == 2) { this.SpeakDom2(null, this.CurDom); }
		else { this.SpeakDom(this.CurDom); }
	}
};

gBellPepperInfo.FastForward = function () { this.StopTimer(); if (this.CurDom == null || !this.ChkDom(this.CurDom)) { this.CurDom = this.FirstDom(document.body); } if (this.CurDom == null) { bell_pepper_info.Speak(0, 'empty document'); } else { this.StopTimer(); this.TimerId = setInterval(function () { gBellPepperInfo.FastForwardFnc(); }, 100); } };
gBellPepperInfo.Rewind = function () { this.StopTimer(); if (this.CurDom == null || !this.ChkDom(this.CurDom)) { this.CurDom = this.LastDom(document.body); } if (this.CurDom == null) { bell_pepper_info.Speak(0, 'empty document'); } else { this.StopTimer(); this.TimerId = setInterval(function () { gBellPepperInfo.RewindFnc(); }, 100); } };
gBellPepperInfo.StopTimer = function () { if (this.TimerId != null) { clearInterval(this.TimerId); this.TimerId = null; } };
gBellPepperInfo.StartPage = function () { this.StopTimer(); this.CurDom = null; this.Remain = null; };
gBellPepperInfo.GetTextAll = function () { try { bell_pepper_info.FileSaveAs(document.body.innerText); } catch (e) { } };
gBellPepperInfo.CurDom = gBellPepperInfo.FirstDom(document.body);