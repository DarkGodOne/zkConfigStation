///// Creating the tree component
// p_div: ID of the div where the tree will be rendered;
// p_backColor: Background color of the region where the tree is being rendered;
// p_contextMenu: Object containing all the context menus. Set null for no context menu;
function createTree(p_div,p_backColor,p_contextMenu) {
	var clickFlag = 0;
	var timeoutFun;
	var tree = {
		name: 'tree',
		div: p_div,
		ulElement: null,
		childNodes: [],
		backcolor: p_backColor,
		contextMenu: p_contextMenu,
		selectedNode: null,
		nodeCounter: 0,
		contextMenuDiv: null,
		rendered: false,
		///// Creating a new node
		// p_text: Text displayed on the node;
		// p_expanded: True or false, indicating wether the node starts expanded or not;
		// p_icon: Relative path to the icon displayed with the node. Set null if the node has no icon;
		// p_parentNode: Reference to the parent node. Set null to create the node on the root;
		// p_path: node full path;
		// p_tag: Tag is used to store additional information on the node. All node attributes are visible when programming events and context menu actions;
		// p_contextmenu: Name of the context menu, which is one of the attributes of the p_contextMenu object created with the tree;
		createNode: function(p_text,p_expanded, p_icon, p_parentNode,p_path,p_tag,p_contextmenu) {
			v_tree = this;
			node = {
				id: 'node_' + this.nodeCounter,
				text: p_text,
				nodepath: p_path,
				icon: p_icon,
				parent: p_parentNode,
				expanded : p_expanded,
				childNodes : [],
				tag : p_tag,
				contextMenu: p_contextmenu,
				elementLi: null,
				///// Removing the node and all its children
				removeNode: function() { v_tree.removeNode(this); },
				///// Expanding or collapsing the node, depending on the expanded value
				toggleNode: function(p_event) { v_tree.toggleNode(this); },
				///// Expanding the node
				expandNode: function(p_event) { v_tree.expandNode(this); },
				///// Expanding the node and its children recursively
				expandSubtree: function() { v_tree.expandSubtree(this); },
				///// Changing the node text
				// p_text: New text;
				setText: function(p_text) { v_tree.setText(this,p_text); },
				changeicon: function(t_icon) 
				{
					this.elementLi.getElementsByTagName('span')[0].firstChild.src = t_icon;
					this.icon = t_icon;
				},
				///// Collapsing the node
				collapseNode: function() { v_tree.collapseNode(this); },
				///// Collapsing the node and its children recursively
				collapseSubtree: function() { v_tree.collapseSubtree(this); },
				///// Deleting all child nodes
				removeChildNodes: function() { v_tree.removeChildNodes(this); },
				///// Creating a new child node;
				// p_text: Text displayed;
				// p_expanded: True or false, indicating wether the node starts expanded or not;
				// p_icon: Icon;
				// p_tag: Tag;
				// p_contextmenu: Context Menu;
				createChildNode: function(p_text,p_expanded,p_icon,p_path,p_tag,p_contextmenu) { return v_tree.createNode(p_text,p_expanded,p_icon,this,p_path,p_tag,p_contextmenu); }
			}

			this.nodeCounter++;

			if (this.rendered) {
				if (p_parentNode==undefined) {
					this.drawNode(this.ulElement,node);
					this.adjustLines(this.ulElement,false);
				}
				else {
					var v_ul = p_parentNode.elementLi.getElementsByTagName("ul")[0];
					if (p_parentNode.childNodes.length==0) {
						if (p_parentNode.expanded) {
						p_parentNode.elementLi.getElementsByTagName("ul")[0].style.display = 'block';
						v_img = p_parentNode.elementLi.getElementsByTagName("img")[0];
						v_img.style.visibility = "visible";
						v_img.src = 'static/images/collapse.png';
						v_img.id = 'toggle_off';
						}
						else {
							p_parentNode.elementLi.getElementsByTagName("ul")[0].style.display = 'none';
							v_img = p_parentNode.elementLi.getElementsByTagName("img")[0];
							v_img.style.visibility = "visible";
							v_img.src = 'static/images/expand.png';
							v_img.id = 'toggle_on';
						}
					}
					this.drawNode(v_ul,node);
					this.adjustLines(v_ul,false);
				}
			}

			if (p_parentNode==undefined) {
				this.childNodes.push(node);
				node.parent=this;
			}
			else
				p_parentNode.childNodes.push(node);

			return node;
		},
		///// Render the tree;
		drawTree: function() {

			this.rendered = true;
			var div_tree = document.getElementById(this.div);
			div_tree.innerHTML = '';

			ulElement = createSimpleElement('ul',this.name,'tree');
			this.ulElement = ulElement;

			for (var i=0; i<this.childNodes.length; i++) {
				this.drawNode(ulElement,this.childNodes[i]);
			}

			div_tree.appendChild(ulElement);

      this.adjustLines(document.getElementById(this.name),true);

		},
		///// Drawing the node. This function is used when drawing the Tree and should not be called directly;
		// p_ulElement: Reference to the UL tag element where the node should be created;
		// p_node: Reference to the node object;
		drawNode: function(p_ulElement,p_node) {

			v_tree = this;
			v_tree.selectedNode = null;
			var v_icon = null;

			if (p_node.icon!=null)
				v_icon = createImgElement(null,'icon_tree',p_node.icon);

			var v_li = document.createElement('li');
			p_node.elementLi = v_li;

			var v_span = createSimpleElement('span',null,'node');

			var v_exp_col = null;

			if (p_node.childNodes.length == 0) {
				v_exp_col = createImgElement('toggle_off','exp_col','static/images/collapse.png');
				v_exp_col.style.visibility = "hidden";
			}
			else {
				if (p_node.expanded) {
					v_exp_col = createImgElement('toggle_off','exp_col','static/images/collapse.png');
				}
				else {
					v_exp_col = createImgElement('toggle_on','exp_col','static/images/expand.png');
				}
			}

			v_span.ondblclick = function() {
				v_tree.doubleClickNode(p_node);
			};

			v_exp_col.onclick = function() {
				v_tree.toggleNode(p_node);
			};

			v_span.onclick = function() {
				v_tree.selectNode(p_node);
				v_tree.clickNode(p_node);
			};

			v_span.oncontextmenu = function(e) {
				v_tree.selectNode(p_node);
				v_tree.nodeContextMenu(e,p_node);
			};

			if (v_icon!=undefined)
				v_span.appendChild(v_icon);

				v_a = createSimpleElement('a',null,null);
				v_a.innerHTML=p_node.text;
				v_span.appendChild(v_a);
				v_li.appendChild(v_exp_col);
				v_li.appendChild(v_span);

			p_ulElement.appendChild(v_li);

			var v_ul = createSimpleElement('ul','ul_' + p_node.id,null);
			v_li.appendChild(v_ul);

			if (p_node.childNodes.length > 0) {

				if (!p_node.expanded)
					v_ul.style.display = 'none';

				for (var i=0; i<p_node.childNodes.length; i++) {
					this.drawNode(v_ul,p_node.childNodes[i]);
				}
			}
		},
		///// Changing node text
		// p_node: Reference to the node that will have its text updated;
		// p_text: New text;
		setText: function(p_node,p_text) {
			p_node.elementLi.getElementsByTagName('span')[0].lastChild.innerHTML = p_text;
			p_node.text = p_text;
		},
		///// Expanding all tree nodes
		expandTree: function() {
			for (var i=0; i<this.childNodes.length; i++) {
				if (this.childNodes[i].childNodes.length>0) {
					this.expandSubtree(this.childNodes[i]);
				}
			}
		},
		///// Expanding all nodes inside the subtree that have parameter 'p_node' as root
		// p_node: Subtree root;
		expandSubtree: function(p_node) {
			this.expandNode(p_node);
			for (var i=0; i<p_node.childNodes.length; i++) {
				if (p_node.childNodes[i].childNodes.length>0) {
					this.expandSubtree(p_node.childNodes[i]);
				}
			}
		},
		///// Collapsing all tree nodes
		collapseTree: function() {
			for (var i=0; i<this.childNodes.length; i++) {
				if (this.childNodes[i].childNodes.length>0) {
					this.collapseSubtree(this.childNodes[i]);
				}
			}
		},
		///// Collapsing all nodes inside the subtree that have parameter 'p_node' as root
		// p_node: Subtree root;
		collapseSubtree: function(p_node) {
			this.collapseNode(p_node);
			for (var i=0; i<p_node.childNodes.length; i++) {
				if (p_node.childNodes[i].childNodes.length>0) {
					this.collapseSubtree(p_node.childNodes[i]);
				}
			}
		},
		///// Expanding node
		// p_node: Reference to the node;
		expandNode: function(p_node) {
			if (p_node.childNodes.length>0 && p_node.expanded==false) {
				if (this.nodeBeforeOpenEvent!=undefined)
					this.nodeBeforeOpenEvent(p_node);

				var img=p_node.elementLi.getElementsByTagName("img")[0];

				p_node.expanded = true;

				img.id="toggle_off";
				img.src = 'static/images/collapse.png';
				elem_ul = img.parentElement.getElementsByTagName("ul")[0];
				elem_ul.style.display = 'block';

				if (this.nodeAfterOpenEvent!=undefined)
					this.nodeAfterOpenEvent(p_node);
			}
		},
		///// Collapsing node
		// p_node: Reference to the node;
		collapseNode: function(p_node) {
			if (p_node.childNodes.length>0 && p_node.expanded==true) {
				var img=p_node.elementLi.getElementsByTagName("img")[0];

				p_node.expanded = false;
				if (this.nodeBeforeCloseEvent!=undefined)
					this.nodeBeforeCloseEvent(p_node);

				img.id="toggle_on";
				img.src = 'static/images/expand.png';
				elem_ul = img.parentElement.getElementsByTagName("ul")[0];
				elem_ul.style.display = 'none';

			}
		},
		///// Toggling node
		// p_node: Reference to the node;
		toggleNode: function(p_node) {
			clickFlag = 0;
			if (p_node.childNodes.length>0) {
				if (p_node.expanded)
					p_node.collapseNode();
				else
					p_node.expandNode();
			}
		},
		// p_node: get node data
		getData: function(p_node) {
			clickFlag = 0;
			var xmlhttp;
			var b = new Base64();
			if (window.XMLHttpRequest)
			{
				//  IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
				xmlhttp=new XMLHttpRequest();
			}
			else
			{
				// IE6, IE5 浏览器执行代码
				xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
			}
			xmlhttp.onreadystatechange=function()
			{
				if (xmlhttp.readyState==4)
				{
					if(xmlhttp.status==200)
					{
						document.getElementById("div_log").innerHTML=b.decode(xmlhttp.responseText);
						/*var str = xmlhttp.responseText;
						treelist = eval('(' + str + ')'); 
						reloadtree(treelist);*/
					}
					else{
						alert("request is error: "+ xmlhttp.status +"!");  
					}
				}
			}
			try{ 
				xmlhttp.open("GET","/configStation/loadTree.do\?func=getdata&curpath="+b.encode(p_node.nodepath),false);
				xmlhttp.send(null);
			}catch(exception){  
				alert("getdata resource is not accessed!");  
			}
		},
		addNode: function(p_node,nodename,nodedata) {
			var xmlhttp;
			var b = new Base64();
			if (window.XMLHttpRequest)
			{
				//  IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
				xmlhttp=new XMLHttpRequest();
			}
			else
			{
				// IE6, IE5 浏览器执行代码
				xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
			}
			xmlhttp.onreadystatechange=function()
			{
				if (xmlhttp.readyState==4)
				{
					if(xmlhttp.status==200)
					{
						try {
							var str = b.decode(xmlhttp.responseText);
							var obj=JSON.parse(str);
							treelist = eval('(' + str + ')');
							
							var nodepath = nodename;
							if(p_node.nodepath == "/")
							{
								nodepath = p_node.nodepath + nodename;
							}
							else
							{
								nodepath = p_node.nodepath + "/" + nodename;
							}
							p_node.changeicon('static/images/folder.png');
							p_node.createChildNode(nodename, false, 'static/images/leaf.png',nodepath,null,'context1');
						} catch(e) {
							alert(b.decode(xmlhttp.responseText));
						}
						//reloadtree(treelist);
					}
					else{
						alert("request is error: "+ xmlhttp.status +"!");					
					}
				}
			}
			try{  
				xmlhttp.open("POST","/configStation/loadTree.do\?func=addnode\&curpath="+b.encode(p_node.nodepath)+
				"\&nodename="+b.encode(nodename)+"\&nodedata="+b.encode(nodedata),false);
				xmlhttp.send(b.encode(JSON.stringify(treelist)));
			}catch(exception){  
				alert("addNode resource is not accessed!");
			}
		},
		delNode: function(p_node) {
			var xmlhttp;
			var b = new Base64();
			if (window.XMLHttpRequest)
			{
				//  IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
				xmlhttp=new XMLHttpRequest();
			}
			else
			{
				// IE6, IE5 浏览器执行代码
				xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
			}
			xmlhttp.onreadystatechange=function()
			{
				if (xmlhttp.readyState==4)
				{
					if(xmlhttp.status==200)
					{
						if(p_node.childNodes.length != 0)
						{
							loadJsonTree("/");
							loadJson(tree,null, treelist);
							tree.drawTree();
							alert(b.decode(xmlhttp.responseText));
						}
						else
						{
							try {
								var str = b.decode(xmlhttp.responseText);
								var obj = JSON.parse(str);
								treelist = eval('(' + str + ')');
								
								p_node.removeNode();
								if(p_node.parent.childNodes.length==0)
								{
									p_node.parent.changeicon('static/images/leaf.png');
								}
							} catch(e) {
								alert(b.decode(xmlhttp.responseText));
							}
						}
					}
					else{
						alert("request is error: "+ xmlhttp.status +"!");					
					}
					loading(false,"");
				}
			}
			try{  
				xmlhttp.open("POST","/configStation/loadTree.do\?func=delnode\&curpath="+b.encode(p_node.nodepath),false);
				xmlhttp.send(b.encode(JSON.stringify(treelist)));
			}catch(exception){  
				alert("delNode resource is not accessed!");
				loading(false,"");
			}
		},
		setNode: function(nodepath,nodedata) {
			var xmlhttp;
			var b = new Base64();
			if (window.XMLHttpRequest)
			{
				//  IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
				xmlhttp=new XMLHttpRequest();
			}
			else
			{
				// IE6, IE5 浏览器执行代码
				xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
			}
			xmlhttp.onreadystatechange=function()
			{
				if (xmlhttp.readyState==4)
				{
					if(xmlhttp.status==200)
					{
						document.getElementById("div_log").innerHTML=b.decode(xmlhttp.responseText);
					}
					else{
						alert("request is error: "+ xmlhttp.status +"!");					
					}
				}
			}
			try{ 
				xmlhttp.open("POST","/configStation/loadTree.do\?func=setnode\&curpath="+b.encode(nodepath),false);
				xmlhttp.send(b.encode(nodedata));
			}catch(exception){  
				alert("setNode resource is not accessed!");
			}
		},
		// p_node: Reference to the node;
		clickNode: function(p_node) {
			if(clickFlag == 0)
				timeoutFun = setTimeout(function(){tree.getData(p_node);},250);
			clickFlag++;
		},
		///// Double clicking node
		// p_node: Reference to the node;
		doubleClickNode: function(p_node) {
			clearTimeout(timeoutFun);
			this.toggleNode(p_node);
		},
		///// Selecting node
		// p_node: Reference to the node;
		selectNode: function(p_node) {
			var span = p_node.elementLi.getElementsByTagName("span")[0];
			span.className = 'node_selected';
			if (this.selectedNode!=null && this.selectedNode!=p_node)
				this.selectedNode.elementLi.getElementsByTagName("span")[0].className = 'node';
			this.selectedNode = p_node;
		},
		///// Deleting node
		// p_node: Reference to the node;
		removeNode: function(p_node) {
			var index = p_node.parent.childNodes.indexOf(p_node);

			if (p_node.elementLi.className=="last" && index!=0) {
				p_node.parent.childNodes[index-1].elementLi.className += "last";
				p_node.parent.childNodes[index-1].elementLi.style.backgroundColor = this.backcolor;
			}

			p_node.elementLi.parentNode.removeChild(p_node.elementLi);
			p_node.parent.childNodes.splice(index, 1);

			if (p_node.parent.childNodes.length==0) {
				var v_img = p_node.parent.elementLi.getElementsByTagName("img")[0];
				v_img.style.visibility = "hidden";
			}

		},
		///// Deleting all node children
		// p_node: Reference to the node;
		removeChildNodes: function(p_node) {

			if (p_node.childNodes.length>0) {
				var v_ul = p_node.elementLi.getElementsByTagName("ul")[0];

				var v_img = p_node.elementLi.getElementsByTagName("img")[0];
				v_img.style.visibility = "hidden";

				p_node.childNodes = [];
				v_ul.innerHTML = "";
			}
		},
		///// Rendering context menu when mouse right button is pressed over a node. This function should no be called directly
		// p_event: Event triggered when right clicking;
		// p_node: Reference to the node;
		nodeContextMenu: function(p_event,p_node) {
			if (p_event.button==2) {
				p_event.preventDefault();
				p_event.stopPropagation();
				if (p_node.contextMenu!=undefined) {

					v_tree = this;

					var v_menu = this.contextMenu[p_node.contextMenu];

					var v_div;
					if (this.contextMenuDiv==null) {
						v_div = createSimpleElement('ul','ul_cm','menu');
						document.body.appendChild(v_div);
					}
					else
						v_div = this.contextMenuDiv;

					v_div.innerHTML = '';

					var v_left = p_event.pageX-5;
					var v_right = p_event.pageY-5;

					v_div.style.display = 'block';
					v_div.style.position = 'absolute';
					v_div.style.left = v_left + 'px';
					v_div.style.top = v_right + 'px';

					for (var i=0; i<v_menu.elements.length; i++) (function(i){

						var v_li = createSimpleElement('li',null,null);

						var v_span = createSimpleElement('span',null,null);
						v_span.onclick = function () {  v_menu.elements[i].action(p_node) };

						var v_a = createSimpleElement('a',null,null);
						var v_ul = createSimpleElement('ul',null,'sub-menu');

						v_a.appendChild(document.createTextNode(v_menu.elements[i].text));

						v_li.appendChild(v_span);

						if (v_menu.elements[i].icon!=undefined) {
							var v_img = createImgElement('null','null',v_menu.elements[i].icon);
							v_li.appendChild(v_img);
						}

						v_li.appendChild(v_a);
						v_li.appendChild(v_ul);
						v_div.appendChild(v_li);

						if (v_menu.elements[i].submenu!=undefined) {
							var v_span_more = createSimpleElement('div',null,null);
							v_span_more.appendChild(createImgElement(null,'menu_img','images/right.png'));
							v_li.appendChild(v_span_more);
							v_tree.contextMenuLi(v_menu.elements[i].submenu,v_ul,p_node);
						}

					})(i);

					this.contextMenuDiv = v_div;

				}
			}
		},
		///// Recursive function called when rendering context menu submenus. This function should no be called directly
		// p_submenu: Reference to the submenu object;
		// p_ul: Reference to the UL tag;
		// p_node: Reference to the node;
		contextMenuLi : function(p_submenu,p_ul,p_node) {

			v_tree = this;

			for (var i=0; i<p_submenu.elements.length; i++) (function(i){

				var v_li = createSimpleElement('li',null,null);

				var v_span = createSimpleElement('span',null,null);
				v_span.onclick = function () {  p_submenu.elements[i].action(p_node) };

				var v_a = createSimpleElement('a',null,null);
				var v_ul = createSimpleElement('ul',null,'sub-menu');

				v_a.appendChild(document.createTextNode(p_submenu.elements[i].text));

				v_li.appendChild(v_span);

				if (p_submenu.elements[i].icon!=undefined) {
					var v_img = createImgElement('null','null',p_submenu.elements[i].icon);
					v_li.appendChild(v_img);
				}

				v_li.appendChild(v_a);
				v_li.appendChild(v_ul);
				p_ul.appendChild(v_li);

				if (p_submenu.elements[i].p_submenu!=undefined) {
					var v_span_more = createSimpleElement('div',null,null);
					v_span_more.appendChild(createImgElement(null,'menu_img','static/images/right.png'));
					v_li.appendChild(v_span_more);
					v_tree.contextMenuLi(p_submenu.elements[i].p_submenu,v_ul,p_node);
				}

			})(i);
		},
		///// Adjusting tree dotted lines. This function should not be called directly
		// p_node: Reference to the node;
		adjustLines: function(p_ul,p_recursive) {
			var tree = p_ul;

      var lists = [];

			if (tree.childNodes.length>0) {
				lists = [ tree ];

				if (p_recursive) {
		      for (var i = 0; i < tree.getElementsByTagName("ul").length; i++) {
						var check_ul = tree.getElementsByTagName("ul")[i];
						if (check_ul.childNodes.length!=0)
		        	lists[lists.length] = check_ul;
					}
				}

			}

      for (var i = 0; i < lists.length; i++) {
        var item = lists[i].lastChild;

        while (!item.tagName || item.tagName.toLowerCase() != "li") {
     	  item = item.previousSibling;
				}

        item.className += "last";
				item.style.backgroundColor = this.backcolor;

				item = item.previousSibling;

				if (item!=null)
					if (item.tagName.toLowerCase() == "li") {
						item.className = "";
						item.style.backgroundColor = 'transparent';
					}
      }
		}
	}

	window.onclick = function() {
		if (tree.contextMenuDiv!=null)
			tree.contextMenuDiv.style.display = 'none';
	}

	return tree;
}

// Helper Functions

//Create a HTML element specified by parameter 'p_type'
function createSimpleElement(p_type,p_id,p_class) {
	element = document.createElement(p_type);
	if (p_id!=undefined)
		element.id = p_id;
	if (p_class!=undefined)
		element.className = p_class;
	return element;
}

//Create img element
function createImgElement(p_id,p_class,p_src) {
	element = document.createElement('img');
	if (p_id!=undefined)
		element.id = p_id;
	if (p_class!=undefined)
		element.className = p_class;
	if (p_src!=undefined)
		element.src = p_src;
	return element;
}

//tree struct
var treelist = {
		"nodeName":"/",
		"nodePath":"/",
		"hasChild":false,
		"childNodes":[]
}

function trim(s){
return s.replace(/(^\s*)|(\s*$)/g,"");
}

//**************************action*****************************
//create loading 
function loading(onoff,str)
{
	// 获得loading显示层
	var loadingdiv = document.getElementById("loading"); 
	var display = document.getElementById("loadvalue"); 
	var treediv = document.getElementById("div_tree");
	var logdiv = document.getElementById("div_log");
	if(onoff)
	{
		// 显示loading层
		display.innerHTML = str;
		loadingdiv.style.display = ""; 
		treediv.style.display = "none";
		logdiv.style.display = "none";
	}
	else{
		// 数据处理完毕之后，将loading层再隐藏掉
		loadingdiv.style.display = "none"; 
		treediv.style.display = "";
		logdiv.style.display = "";
	}
}

//uploadfile
function uploadFile()
{
	loading(true,"Importing ...");
	var upload = document.getElementById("upload");
	var file = upload.files[0];
	if(file == null)
	{
		loading(false,"");
		return false;
	}
	
	var xmlhttp;
	var b = new Base64();
	if (window.XMLHttpRequest)
	{
		//  IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
		xmlhttp=new XMLHttpRequest();
	}
	else
	{
		// IE6, IE5 浏览器执行代码
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange=function()
	{
		if (xmlhttp.readyState==4)
		{
			if(xmlhttp.status==200)
			{
				loadJsonTree("/");
				loadJson(tree,null, treelist);
				tree.drawTree();
				alert(b.decode(xmlhttp.responseText));
			}
			else{
				alert("request is error: "+ xmlhttp.status +"!");
			}
			loading(false,"");
		}
	}
	try{
		/* FormData 是表单数据类 */
		var fd = new FormData();
		/* 把文件添加到表单里 */
		fd.append("upfile", file);
		xmlhttp.open("POST","/configStation/loadTree.do\?func=uploadfile\&curpath="+b.encode(upload.name),true);
		xmlhttp.send(fd);
	}catch(exception){  
		alert("uploadFile resource is not accessed!");
		loading(false,"");
	}
}

//downloadfile
function downloadFile(nodepath)
{
	loading(true,"Exporting ...");
	var xmlhttp;
	var b = new Base64();
	if (window.XMLHttpRequest)
	{
		//  IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
		xmlhttp=new XMLHttpRequest();
	}
	else
	{
		// IE6, IE5 浏览器执行代码
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange=function()
	{
		if (xmlhttp.readyState==4)
		{
			if(xmlhttp.status==200)
			{
				if(xmlhttp.getResponseHeader("Content-Disposition") != "")
				{
					var aLink = document.createElement('a');
					var blob = new Blob([xmlhttp.responseText]);
					var evt = document.createEvent("HTMLEvents");
					evt.initEvent("click", false, false);//initEvent 不加后两个参数在FF下会报错, 感谢 Barret Lee 的反馈
					aLink.download = "export.conf";
					aLink.href = URL.createObjectURL(blob);
					aLink.dispatchEvent(evt);
					aLink.click();
					alert("export file is success");
				}
				else
				{
					alert(b.decode(xmlhttp.responseText));
				}
			}
			else{
				alert("request is error: "+ xmlhttp.status +"!");
			}
			loading(false,"");
		}
	}
	try{
		xmlhttp.open("GET","/configStation/loadTree.do\?func=downloadfile\&curpath="+b.encode(nodepath),true);
		xmlhttp.send(null);
	}catch(exception){  
		alert("downloadFile resource is not accessed!");
		loading(false,"");
	}
}

//Tree Context Menu Structure
var contex_menu = {
	'context1' : {
		elements : [
			{
				text : 'Create Child Node',
				icon: 'static/images/add1.png',
				action : function(node) {
					if (tree.contextMenuDiv!=null)
						tree.contextMenuDiv.style.display = 'none';
					var nodename = prompt("input node name:","newnode");
					if(nodename == null)
					{
						return;
					}
					nodename = trim(nodename);
					
					var nodedata = prompt("input node data:","");
					if(nodedata == null)
					{
						return;
					}
					nodedata = trim(nodedata);
					tree.addNode(node,nodename,nodedata);
				}
			},
			{
				text : 'Delete Node',
				icon: 'static/images/delete.png',
				action : function(node) {
					if (tree.contextMenuDiv!=null)
						tree.contextMenuDiv.style.display = 'none';
					
					loading(true,"Deleteing ...");
					setTimeout(function(){tree.delNode(node);loading(false,"");},100);
				}
			},
			{
				text : 'Set Node',
				icon: 'static/images/file.png',
				action : function(node) {
					if (tree.contextMenuDiv!=null)
						tree.contextMenuDiv.style.display = 'none';
					var nodedata = prompt("input node data:","");
					if(nodedata == null)
					{
						return;
					}
					nodedata = trim(nodedata);
					tree.setNode(node.nodepath,nodedata);
				}
			},
			{
				text : 'Refresh Tree',
				icon: 'static/images/refresh.png',
				action : function(node) {
					if (tree.contextMenuDiv!=null)
						tree.contextMenuDiv.style.display = 'none';
					
					loading(true,"Rfreshing ...");
					setTimeout(function(){loadJsonTree("/");loadJson(tree,null, treelist);tree.drawTree();loading(false,"");},100);
				}
			},
			{
				text : 'Import Config',
				icon: 'static/images/import.png',
				action : function(node) {
					if (tree.contextMenuDiv!=null)
						tree.contextMenuDiv.style.display = 'none';
					
					var upload = document.getElementById("upload");
					upload.name = node.nodepath;
					upload.click();
				}
			},
			{
				text : 'Export Config',
				icon: 'static/images/export.png',
				action : function(node) {
					if (tree.contextMenuDiv!=null)
						tree.contextMenuDiv.style.display = 'none';
					
					downloadFile(node.nodepath);
				}
			}
			/*{
				text : 'Child Actions',
				icon: 'static/images/blue_key.png',
				action : function(node) {

				},
				submenu: {
					elements : [
						{
							text : 'Create Child Node',
							icon: 'static/images/add1.png',
							action : function(node) {
								node.createChildNode('Created',false,'static/images/folder.png',null,'context1');
							}
						},
						{
							text : 'Create 1000 Child Nodes',
							icon: 'static/images/add1.png',
							action : function(node) {
								for (var i=0; i<1000; i++)
									node.createChildNode('Created -' + i,false,'static/images/folder.png',null,'context1');
							}
						},
						{
							text : 'Delete Child Nodes',
							icon: 'static/images/delete.png',
							action : function(node) {
								node.removeChildNodes();
							}
						}
					]
				}
			}*/
		]
	}
};

//getChildNode
function loadJsonTree(curpath)
{
	var xmlhttp;
	var b = new Base64();
	if (window.XMLHttpRequest)
	{
		//  IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
		xmlhttp=new XMLHttpRequest();
	}
	else
	{
		// IE6, IE5 浏览器执行代码
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange=function()
	{
		if (xmlhttp.readyState==4)
		{
			if(xmlhttp.status==200)
			{
				try{
					var str = b.decode(xmlhttp.responseText);
					var obj=JSON.parse(str);
					treelist = eval('(' + str + ')');
					ret = true;
				} catch(e) {
					alert(b.decode(xmlhttp.responseText));
					ret = false;
				}
			}
			else{
				alert("request is error: "+ xmlhttp.status +"!");  
				ret = false;
			}
		}
	}
	try{
		xmlhttp.open("GET","/configStation/loadTree.do\?func=loadtree\&curpath="+b.encode(curpath),false);
		xmlhttp.send(null);
		return ret;
	}catch(exception){  
		alert("loadtree resource is not accessed!");
		return false;
	}
}

function reloadtree(jsonvar){
	//Initializing Tree
	//Creating the tree
	tree = createTree('div_tree','white',contex_menu);

	//div_log = document.getElementById('div_log');

	//Setting custom events
	/*tree.nodeBeforeOpenEvent = function(node) {
		div_log.innerHTML += node.text + ': Before expand event<br/>';
	}

	tree.nodeAfterOpenEvent = function(node) {
		div_log.innerHTML += node.text + ': After expand event<br/>';
	}

	tree.nodeBeforeCloseEvent = function(node) {
		div_log.innerHTML += node.text + ': Before collapse event<br/>';
	}*/

	loadJson(tree,null, jsonvar);

	//Rendering the tree
	tree.drawTree();

	//Adding node after tree is already rendered
	//tree.createNode('Real Time',false,'static/images/leaf.png',null,null,'context1');

}

function loadJson(tree,node, jsonvar)
{
	if(jsonvar == null || jsonvar == "")
	{
		if(tree.nodeCounter == 0)
		{
			tree.childNodes.splice(0,tree.childNodes.length);
			node = tree.createNode("/",false,'static/images/leaf.png',null,"/",null,'context1');
		}
		return;
	}
	
	if(node == null)
	{
		tree.childNodes.splice(0,tree.childNodes.length);
		if(jsonvar.hasChild == false)
		{
			node = tree.createNode(jsonvar.nodeName,false,'static/images/leaf.png',null,jsonvar.nodePath,null,'context1');
		}
		else
		{
			node = tree.createNode(jsonvar.nodeName,true,'static/images/folder.png',null,jsonvar.nodePath,null,'context1');
		}
		loadJson(tree,node,jsonvar.childNodes);
	}
	else
	{
		//Loop to create test nodes
		for (var i=0; i<jsonvar.length; i++) {
			var node1;
			if(jsonvar[i].hasChild == false)
			{
				node1 = node.createChildNode(jsonvar[i].nodeName, false, 'static/images/leaf.png',jsonvar[i].nodePath,null,'context1');
			}
			else
			{
				node1 = node.createChildNode(jsonvar[i].nodeName, false, 'static/images/folder.png',jsonvar[i].nodePath,null,'context1');
			}
			loadJson(tree,node1,jsonvar[i].childNodes);
		}
	}
}

function login() {
	var xmlhttp;
	var b = new Base64();
	if (window.XMLHttpRequest)
	{
		//  IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码
		xmlhttp=new XMLHttpRequest();
	}
	else
	{
		// IE6, IE5 浏览器执行代码
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange=function()
	{
		if (xmlhttp.readyState==4)
		{
			if(xmlhttp.status==200)
			{
				document.write(xmlhttp.responseText);
				document.close();
			}
			else{
				alert(xmlhttp.responseText);
			}
		}
	}
	try{  
		var user = document.getElementsByName("username")[0].value;
		var pass = document.getElementsByName("password")[0].value;
		xmlhttp.open("POST","/configStation/main.do\?user="+b.encode(user)+"\&pass="+b.encode(pass),false);
		xmlhttp.send(null);
	}catch(exception){  
		alert("login resource is not accessed!");  
	}
}

function Base64() {
 
    // private property
    _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
 
    // public method for encoding
    this.encode = function (input) {
        var output = "";
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
        var i = 0;
        input = _utf8_encode(input);
        while (i < input.length) {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);
            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;
            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }
            output = output +
            _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
            _keyStr.charAt(enc3) + _keyStr.charAt(enc4);
        }
        return output;
    }
 
    // public method for decoding
    this.decode = function (input) {
        var output = "";
        var chr1, chr2, chr3;
        var enc1, enc2, enc3, enc4;
        var i = 0;
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
        while (i < input.length) {
            enc1 = _keyStr.indexOf(input.charAt(i++));
            enc2 = _keyStr.indexOf(input.charAt(i++));
            enc3 = _keyStr.indexOf(input.charAt(i++));
            enc4 = _keyStr.indexOf(input.charAt(i++));
            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;
            output = output + String.fromCharCode(chr1);
            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }
        }
        output = _utf8_decode(output);
        return output;
    }
 
    // private method for UTF-8 encoding
    _utf8_encode = function (string) {
        //string = string.replace(/\r\n/g,"\n");
        var utftext = "";
        for (var n = 0; n < string.length; n++) {
            var c = string.charCodeAt(n);
            if (c < 128) {
                utftext += String.fromCharCode(c);
            } else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            } else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }
 
        }
        return utftext;
    }
 
    // private method for UTF-8 decoding
    _utf8_decode = function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;
        while ( i < utftext.length ) {
            c = utftext.charCodeAt(i);
            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            } else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            } else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }
        }
        return string;
    }
}