	/*
	* Create by uoon (vnjs.net)
	*/
	var Kombai = {
		archive: {
			data: {},
			method: {},
			variable: {}
		},
		K: {
			add: {},
			get: {},
			set: {},
			is: {},
			remove: {},
			create: {},
			insert: {},
			select: {},
			request: {}
		}
	};
	
	var K = function(r) {return document.getElementById(r)}
	
	for (var o in Kombai.K) {
		K[o] = Kombai.K[o];
	}
	
	K.add = function(r) {
		if (!r) return;
		var setting = {
			element: null,
			//add className
			className: null,
			//add event
			event: null,
			listener: null,
			//add attribute
			attribute: null,
			value: null,
			//add method
			method: null,
			source: null,
			//add effect drag drop
			onDrag: null,
			onMove: null,
			onDrop: null,
			//add childNode
			childNode: null
		};
		
		for (var o in setting) {
			if (r[o] != undefined) {
				setting[o] = r[o];
			}	
		}
		
		Add();
		
		function Add() {
			with (setting) {
				if (!element) {
					return;
				}	
				if (className != null) {
					addClassName(element, className);
				}	
				if (attribute != null && value != null) {
					addAttribute(element, attribute, value);
				}
				if (childNode != null) {
					addChildNode(element, childNode);
				}
				if (method != null && source != null) {
					addMethod(element, method, source);
				}
				if (event != null && listener != null) {
					addEvent(element, event, listener);
				} else if ((/dragdrop/i).test(event)) {
					addEffectDragDrop(element, onDrag, onMove, onDrop);
				}
			}
		};
		
		function addClassName(E, K) {
			if (E.className != "") {
				E.className += " " + K;
			} else {
				E.className = K;
			}
		};
		
		function addEvent(O, E, L) {
			if (O.addEventlistenerer) {
	            O.addEventlistenerer(E, L, false);
	        } else if (O.attachEvent) {
	            O.attachEvent("on" + E, L);
	        } else {
				if (O["on" + E] instanceof Function) {
					var oL = O["on" + E];
					O["on" + E] = function(event) {
						oL(event);
						L(event);
					};
				} else {
					O["on" + E] = L;
				}
			}
		};
		
		function addAttribute(E, A, V) {
			E.setAttribute(A, V);
		};
		
		function addMethod(E, M, S) {
			E[M] = S;
		};
		
		function addChildNode(E, N) {
			E.appendChild(N);
		};
		
		function addEffectDragDrop(E, D, M, U) {
			//reference: K.get.X, Y, eventSource;
			E.store = {
				x: 0,
				y: 0,
				X: 0,
				Y: 0
			};
			E.event = {
				mouseDown: function() {},
				mouseMove: function() {},
				mouseUp: function() {}
			};
			E.run = {
				drag: D || function() {},
				move: M || function() {},
				drop: U || function() {}			
			};
			E.drop = function() {
				E.store = null;
				E.event = null;
				E.run = null;
				E.drop = function() {};
				E.onmousedown = ignore;
			};
			E.penetrate = function(enviroment) {
				if (typeof enviroment != "object") {
					return;
				}
				var Xmin = K.get.X(enviroment);
				var Xmax = Xmin + enviroment.offsetWidth;
				var Ymin = K.get.Y(enviroment);
				var Ymax = Ymin + enviroment.offsetHeight;
				var X = E.store.X;
				var Y = E.store.Y;
				if (Xmin < X && X < Xmax && Ymin < Y && Y < Ymax) {
					return true;
				} else {
					return false;
				}
			};
			E.event.mouseDown = function(event) {
				var event = (event || window.event);
				if (!(event.button == 2 || event.which == 3)) {
					if (isNaN(parseInt(E.style.left))) {
						E.style.left = "0px";
					}
					if (isNaN(parseInt(E.style.top))) {
						E.style.top = "0px";
					}
					E.style.position  = "relative";
					E.store.x = event.clientX;
					E.store.y = event.clientY;
					E.event.mouseMove = function(event) {
						var event = (event || window.event);
						E.style.left = parseInt(E.style.left) + (event.clientX - E.store.x) + "px";
						E.style.top = parseInt(E.style.top) + (event.clientY - E.store.y) + "px";
						E.store.x = event.clientX;
						E.store.y = event.clientY;
						E.store.X = K.get.X(event);
						E.store.Y = K.get.Y(event);
						E.run.move.call(E);
						return false;
					};
					E.event.mouseUp = function() {
						document.onmousemove = null;
						E.event.mouseMove = null;
						E.event.mouseUp = null;
						E.onmouseup = null;
						E.run.drop.call(E);
					};
					E.onmouseup = E.event.mouseUp;
					E.run.drag.call(E);
					document.onmousemove = E.event.mouseMove;
					document.onmouseup = function(event) {
						var S = K.get.eventSource(event);
						if (S != E) {
							document.onmousemove = null;
						}
					};
					return false;
				}
			};
			E.onmousedown = E.event.mouseDown;
			function ignore(event) {
				var event = event || window.event;
				event.cancelBubble = true;
			}
		};
	};
	
	K.get = {
		browserName: function() {
			return navigator.appName;
		},
		domain: function() {
			return document.domain;
		},
		text: function(r) {
			if (typeof r == "string") {
				var reject = new RegExp("<(?:.|\s)*?>", "gi");
				return r.replace(reject, " ");
			}
		},
		height: function(r) {
			var element = r || document.getElementById(r);
			if (element.height != undefined) {
				return element.height;
			} else {
				return element.offsetHeight;
			}
		},
		width: function(r) {
			var element = r || document.getElementById(r);
			if (element.width != undefined) {
				return element.width;
			} else {
				return element.offsetWidth;
			}
		},
		screenHeight: function() {
			return screen.availHeight;
		},
		screenWidth: function() {
			return screen.availWidth;
		},
		windowHeight: function() {
			if (window.innerHeight != undefined) {
				return window.innerHeight;
			} else if (document.documentElement) {
				return document.documentElement.clientHeight;
			} else {
				return document.body.clientHeight;
			}
		},
		windowWidth: function() {
			if (window.innerWidth != undefined) {
				return window.innerWidth;
			}
			else if (document.documentElement) {
				return document.documentElement.clientWidth;
			} else {
				return document.body.clientWidth;
			}
		},
		pageHeight: function() {
			return document.body.scrollHeight;
		},
		pageWidth: function() {
			return document.body.scrollWidth;
		},
		currentStyle: function(E, P) {
			if (typeof E == "string") {
				E = document.getElementById(E);
			}
			if (E.currentStyle) {  
			  var A = P.split("-");
			  for (var i = 1; i < ar.length; ++i) {
				 A[i] = A[i].replace(/\w/, A[i].charAt(0).toUpperCase());
			  }
			  P = A.join("");
			  return E.currentStyle[P];
			} else if (document.defaultView && document.defaultView.getComputedStyle) {
			  return document.defaultView.getComputedStyle(E, null).getPropertyValue(P);
			} else {
				return null;
			}
		},
		currentTime: function() {
			return new Date().getTime();
		},
		eventSource: function(event) {
			return (event && event.target) ? event.target : window.event.srcElement;
		},
		topPage: function() {
			if (window.pageYOffset != undefined) {
				return window.pageYOffset;
			} else if (document.documentElement != undefined) {
				return document.documentElement.scrollTop;
			} else {
				return document.body.scrollTop;
			}
		},
		bottomPage: function() {
			if (window.pageYOffset != undefined) {
				return (window.pageYOffset + window.innerHeight);
			} else if (document.documentElement != undefined) {
				return (document.documentElement.scrollTop + document.documentElement.clientHeight);
			} else {
				return (document.body.scrollTop + document.body.clientHeight);
			}
		},
		leftPage: function() {
			if (window.pageXOffset != undefined) {
				return window.pageXOffset;
			} else if (document.documentElement != undefined) {
				return document.documentElement.scrollLeft;
			} else {
				return document.body.scrollLeft;
			}
		},
		rightPage: function() {
			if (window.pageXOffset != undefined) {
				return (window.pageXOffset + window.innerWidth);
			} else if (document.documentElement != undefined) {
				return (document.documentElement.scrollLeft + document.documentElement.clientWidth);
			} else {
				return (document.body.scrollLeft + document.body.clientWidth);
			}
		},
		X: function(r) {
			if (typeof r == "string") {
				r = document.getElementById(r);
			}
			var E = r || window.event;
			if (E.nodeName != undefined) {
				var X = 0;
				while (E) {
					X += E.offsetLeft;
					E = E.offsetParent;
				}
				return X;
			} else if (E.clientX != undefined) {
				return E.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
			}
		},
		Y: function(r) {
			if (typeof r == "string") {
				r = document.getElementById(r);
			}
			var E = r || window.event;
			if (E.nodeName != undefined) {
				var Y = 0;
				while (E) {
					Y += E.offsetTop;
					E = E.offsetParent;
				}
				return Y;
			} else if (E.clientY != undefined) {
				return E.clientY + document.body.scrollTop + document.documentElement.scrollTop;
			}
		}
	};

	K.set = {
		timeout: function(r) {
			var setting = {
				method: function() {},
				delay: 1000,
				param: [],
				until: function() {return false;},
				amount: 4294967295,
				release: function() {}
			}
			var n = 0;
			for (var o in setting) {
				if (r[o]) {
					setting[o] = r[o];
				}
			}
			function repeat() {
				if (setting && !setting.until() && (n < setting.amount)) {
					setting.method.apply(setting.method, setting.param);
					setTimeout(repeat, setting.delay);
					n ++;
				} else {
					setting.release();
					setting = null;
				}
			};
			setTimeout(repeat, setting.delay);
		},
		event: function(r) {
			var setting = {
				element: null,
				type: null,
				listener: function() {}
			}
			for (var o in setting) {
				if (r[o] != undefined) {
					setting[o] = r[o];
				} else {
					return;
				}
			}
			setting.element["on" + setting.type] = setting.listener;
		}
	};
	
	K.is = {
		SA: function() {
			return new RegExp("Safari").test(navigator.userAgent);
		},
		OP: function() {
			return new RegExp("Opera").test(navigator.userAgent);
		},
		FF: function() {
			return new RegExp("Firefox").test(navigator.userAgent);
		},
		IE: function() {
			return new RegExp("MSIE").test(navigator.userAgent);
		},
		IE6: function() {
			return new RegExp("MSIE 6").test(navigator.userAgent);
		},
		IE7: function() {
			return new RegExp("MSIE 7").test(navigator.userAgent);
		},
		IE8: function() {
			return new RegExp("MSIE 8").test(navigator.userAgent);
		}
	};
	
	K.remove = {
		element: function(r) {
			if (typeof r == "string") {
				r = document.getElementById(r);
			}
			if (r.parentNode) {
				r.parentNode.removeChild(r);
			}
		}, 
		timeout: function(r) {
			clearTimeout(r);
			clearInterval(r);
		},
		space: function(r) {
			if (r && r != "" && typeof r == "string") {
				return r.replace(/^\s+|\s+$/, "");
			} else {
				return new String();
			}
		},
		tag: function(r) {
			if (r && r != "" && typeof r == "string") {
				return r.replace(/<\/?[^>]+>/gi, '');
			} else {
				return new String();
			}
		},
		event: function(r) {
			var setting = {
				element: null,
				type: null,
				listener: null
			}
			for (var o in r) {
				if (r[o] != undefined) {
					setting[o] = r[o]
				} else {
					return;
				}
			}
			with (setting) {
				if (element.removeEventListener) { 
					element.removeEventListener(type, listener, false);
				} else if (element.detachEvent) {
					element.detachEvent("on" + type, listener);
				} else {  
					element["on" + type] = null;
				}
			}
		}
	};
	
	K.create = {
		database: function(r) {
			return new function() {
				if (!r || typeof r != "object") {
					alert("Don't create database");
					return;
				}
				//begin config database;
				var generanlId = new Date().getTime() + Math.random().toString().substring(2);
				var reference = "7c4bc080af27ded752b36160dcb1c716";
				var setting = {
					fromCollection: null,
					rowIndex: 0,
					stepJump: 1,
					startRange: 1,
					fieldName: [],
					primaryKey: null,
					autoIncrement: null,
					ignoreError: false
				};

				for (var o in setting) {
					if (r[o]) {
						setting[o] = r[o];
					}
				}
				if (!setting.fromCollection) {
					this.Row = [];
					//check config database
					if (setting.fieldName.constructor != Array || !setting.fieldName.length) {
						alert("fieldName is not null");
						return;
					} else if (setting.primaryKey) {
						var checkPrimaryKey = false;
						var length = setting.fieldName.length;
						for (var o = 0; o < length; o ++) {
							if (setting.primaryKey == setting.fieldName[o]) {
								checkPrimaryKey = true;
								break;
							}
						}
						if (!checkPrimaryKey) {
							if (!setting.ignoreError) alert("please preview primaryKey");
							return;
						}
					}
					//seting for autooIncrement field;
					if (typeof setting.autoIncrement == "object" && setting.autoIncrement != null) {
						setting.startRange = setting.autoIncrement[1].start;
						setting.stepJump = setting.autoIncrement[1].step;
						setting.autoIncrement =  setting.autoIncrement[0];
					}
					//check autoIncrement key;
					if (setting.autoIncrement) {
						var checkAutoIncrement = false;
						for (var element in setting.fieldName) {
							if (setting.autoIncrement == setting.fieldName[element]) {
								checkAutoIncrement = true;
								break;
							}
						}
						if (checkAutoIncrement) {
							setting.fieldName.splice(element, 1);
							setting.fieldName.push(setting.autoIncrement);
						} else {
							if (!setting.ignoreError) {
								alert("please preview autoIncrement key");
							}
							return;
						}
					}
				} else {
					this.Row = setting.fromCollection;
					with (setting) {
						if (fromCollection.length) {
							rowIndex = fromCollection.length - 1;
						} else {
							if (!ignoreError) {
								alert("fromCollection is not empty");
							}
							return;
						}
						stepJump = 1;
						startRange = 1;
						fieldName = null;
						primaryKey = null;
						autoIncrement = null;
					}
					
				}
				// end config database;
				
				this.Drop = function() {
					this.Row = [];
					with (setting) {
						rowIndex = 0;
						startRange = 0;
						stepJump = 1;
					}
				}
				
				this.Count = function() {
					return this.Row.length;
				}
				
				this.Check = function(content) {
					if (!content.Condition) {
						return false;
					} else {
						content.Condition = parseCondition(content.Condition);
					}
					for (var o in this.Row) {
						with (this.Row[o]) {
							if (eval(content.Condition)) {
								return true;
							}
						}
					}
					return false;
				}//end method Check;	
				
				this.Insert = function(data) {
					if (!data || typeof data != "object") {
						if (!setting.ignoreError) alert("Can't insert value into table");
						return;
					}
					// create mask object;
					var length = setting.fieldName.length;
					if (length) {
						var newObject = {};
						for (var o = 0; o < length; o ++) {
							if (!setting.autoIncrement || (setting.fieldName[o] != setting.autoIncrement)) {
								var cellValue = data[o];
								if (cellValue == null) {
									cellValue = data[setting.fieldName[o]];
								}
								newObject[setting.fieldName[o]] = (cellValue != null) ? cellValue : null;
							} else if (setting.fieldName[o] == setting.autoIncrement) {
								var checkAutoValue = data[o];
								if (checkAutoValue == null) {
									checkAutoValue = data[setting.fieldName[o]];
								}
								if ((checkAutoValue != null) && (checkAutoValue != setting.startRange)) {
									if (!setting.ignoreError) alert("cannot insert value = "+ checkAutoValue + " in to autoIncrement field " + setting.autoIncrement);
									return;
								} else {
									newObject[setting.fieldName[o]] = setting.startRange;
									setting.startRange += setting.stepJump;
								}
							} 
						}
						//check unique value;
						length = this.Count();
						for (o = 0; o < length; o ++) {
							if (setting.primaryKey == null || setting.primaryKey == "") {
								break;
							} else if (this.Row[o][setting.primaryKey] == newObject[setting.primaryKey]) {
								if (!setting.ignoreError) {
									alert("value of primary key '" + setting.primaryKey + "' : " + newObject[setting.primaryKey] + " had exits in row : " + o);
								}
								return;
							}
						}//end check;
					} else {
						var newObject = data;
					}
					// create new record;
					this.Row[setting.rowIndex] = newObject;
					setting.rowIndex += 1;
				}//end method Insert;
				
				this.Select = function(condition) {
					var noneCondition = (condition == undefined || typeof condition != "object");
					var config = {
						fieldName : setting.fieldName,
						Where : null,
						orderBy : null,
						Limit: this.Row.length
					};
					config.Begin = 0;
					config.aMask = [];
					var aData = [];
					if (noneCondition) {
						aData = this.Row;
					} else {
						for (var o in config) { 
							if (condition[o]) {
								config[o] = condition[o];
							}
						}
						for (var o in this.Row) {
							config.aMask[o] = this.Row[o]; 
						}
						config.Where = parseCondition(config.Where);
						parseLimitClause();
						parseOrderByClause();
						aData = selectData();
					}
					
					aData.View = function(extendView) {
						if (extendView && extendView instanceof Function) {
							extendView.call(aData);
						} else {
							alert("Don't view this data");
						}
					}
					
					//return result;
					return aData;
					
					/**************/
					//private function
					function parseLimitClause() {
						if (typeof config.Limit == "object") {
							config.Begin = config.Limit[0];
							config.Limit = config.Limit[1];
						}
					}//end function Limit;
					
					function parseOrderByClause() {
						if (config.orderBy == undefined || config.orderBy == null) return;
						var aOrder = [];
						if (config.orderBy.indexOf(",") > 0) {
							aOrder = config.orderBy.split(",");
						} else 	{
							aOrder[0] = config.orderBy;
						}
						/*
						input : orderBy : "a+b+c ASC, d DESC";
						*/
						var length = aOrder.length;
						for (var o = 0; o < length; o ++) {
							aHead = aOrder.slice(0, o);
							aFoot = aOrder.slice(o, aOrder.length);
							if (aOrder[o].indexOf("+") > 0) {
								aBody = aOrder[o].split("+");
								doEnd = aBody[aBody.length - 1];
								var regExp = new RegExp("\\b(\\w+)\\b(.*)\\b","i");
								if (regExp.test(doEnd)) {
									aReg = regExp.exec(doEnd);
									aBody[aBody.length - 1] = aReg[1];
									if (aReg[2] == "") {
										aReg[2] = "asc";
									}
									for (var i = 0; i < aBody.length; i ++) {
										aBody[i] = aBody[i] + " " + aReg[2];
									}
								}
								//remove the last of aFoot;
								aFoot.splice(0, 1);
								//build the array aOrder;
								aOrder = aHead.concat(aBody).concat(aFoot);
							}
						}
						/*
						ouput : a ASC, b ASC, c ASC, d DESC;
						*/
						for (var o = 0; o < length; o ++) {
							var aClause = [];
							var	sClause = new String();
							for (var i = 0; i <= o; i++) {		
								isOrder = new RegExp("\\b(\\w+)\\b(.*)\\b","i");
								if (isOrder.test(aOrder[i])) {
									aReg = isOrder.exec(aOrder[i]);
									if (aReg[2].toLowerCase().indexOf("desc") > -1) {
										aClause.push("(doFront." + aReg[1] + " <= doAfter." + aReg[1] + ")");
									} else if (aReg[2].toLowerCase().indexOf("asc") > -1 || aReg[2] == "") {
										aClause.push("(doFront." + aReg[1] + " >= doAfter." + aReg[1] + ")");
									}
								}
							}
							if (aClause.length > 0) {
								sClause = aClause.join("&&");
							}
							var doMiddle = {};
							var doFront = {};
							var doAfter = {};
							for (var current = 0; current < config.aMask.length; current ++) {
								for (var next = eval(current + 1); next < config.aMask.length; next++) {
									doFront = config.aMask[current];
									doAfter = config.aMask[next];
									if (eval(sClause)) {
										doMiddle = config.aMask[current];
										config.aMask[current] = config.aMask[next];
										config.aMask[next] = doMiddle;
									}
								}
							}
						}//end for order;
					}//end function orderBy;
					
					function selectData() {
						var nIndex = 0;
						var aResult = [];
						for (var element = config.Begin; element < config.aMask.length && config.Begin < config.Limit; element ++) {
							if (!config.aMask[element]) continue;
							with (config.aMask[element]) {
								if (eval(config.Where)) {
									if (setting.fromCollection) {
										aResult[nIndex] = config.aMask[element];
									} else {
										aResult[nIndex] = {};
										for (var column = 0; column < config.fieldName.length; column ++) {
											aResult[nIndex][config.fieldName[column].toString()] = config.aMask[element][config.fieldName[column].toString()];
										}
									}
									config.Begin ++;
									nIndex ++;
								}
							}
						}
						return aResult;
					}// end function selectData;
				}//end method Select;

				this.Delete = function(condition) {
					if (setting.fromCollection) {
						return;
					}
					var config = {Where: null};
					if (condition.Where) {
						config.Where = parseCondition(condition.Where);
					}
					for (var o in this.Row) {
						with (this.Row[o]) {
							if (eval(config.Where)) {
								this.Row.splice(o, 1); 
								setting.rowIndex -= 1;
							}
						}
					}
				}//end method Delete;
				
				this.Update = function(data) {
					var config = {
						rowSet : {},
						Where : false
					};
					for (var o in data) {
						if (o == "Where") {
							config.Where = data.Where;
							config.Where = parseCondition(config.Where);
						} else {
							config.rowSet[o] = data[o];
						}
					}
					//check value of primary key;
					var length = this.Count();
					for (var o = 0; o < length; o ++) {
						for (var delta in config.rowSet) {
							if (delta == setting.primaryKey) {
								if (this.Row[o][delta] == config.rowSet[delta]) {
									if (!setting.ignoreError) {
										alert("value of primary key '" + delta + "' : " + config.rowSet[delta] + ", it has exits in row : " + o);
									}
									return;
								}
							}
						}
					}
					var checkCondition = false;
					for (var o = 0; o < length; o ++) {
						// check condition;
						checkCondition = false;
						with (this.Row[o]) {
							if (eval(config.Where)) {
								checkCondition = true; 
							}
						}
						//update value to record;
						if (checkCondition) {
							for (delta in config.rowSet) {
								if (delta == setting.autoIncrement)	{
									if (config.rowSet[delta] != this.Row[o][delta]) {
										if (!setting.ignoreError) {
											alert("cannot update value = "+ config.rowSet[delta] + " in to autoIncrement field : " + setting.autoIncrement);
										}
										return;
									}
								} else {
									this.Row[o][delta] = config.rowSet[delta];
								}
							}
						}
					}// end the loop;
					return false;
				}//end method Update;
				
				/*******************************/
				// private method.
				function parseCondition(condition) {
					var condition = Like(condition);
						condition = Between(condition);
					if (condition == "" || !condition) {
						return 1;
					} else {
						return condition;
					}
					
					/**************/
					//private function
					function Between(r) {
						var isBetween = new RegExp("^\\s*(.+)\\s+(between)\\s+(.*)\\s+(and)\\s+(.*)\\s*$","i");
						var notBetween = new RegExp("^\\s*(.+)\\s+(not between)\\s+(.*)\\s+(and)\\s+(.*)\\s*$","i");
						if (notBetween.test(r)) {
							var aBetween= notBetween.exec(r);
								aBetween[3] = eval(aBetween[3]);
								aBetween[5] = eval(aBetween[5]);
							if (typeof aBetween[3] == "string") {
								aBetween[3] = "'" + aBetween[3] + "'";
							}
							if (typeof aBetween[5] == "string") {
								aBetween[5] = "'" + aBetween[5] + "'";
							}
							if (aBetween[3] > aBetween[5]) {
								var between = aBetween[3];
								aBetween[3] = aBetween[5];
								aBetween[5] = between;
							}
							return "(" + aBetween[3] + " > " + aBetween[1] + ") || (" + aBetween[1] + " > " + aBetween[5] + ")";
						} else if (isBetween.test(r)) {
							var aBetween = isBetween.exec(r);
								aBetween[3] = eval(aBetween[3]);
								aBetween[5] = eval(aBetween[5]);
							if (typeof aBetween[3] == "string") {
								aBetween[3] = "'" + aBetween[3] + "'";
							}
							if (typeof aBetween[5] == "string") {
								aBetween[5] = "'" + aBetween[5] + "'";
							}
							if (aBetween[3] > aBetween[5]) {
								var between = aBetween[3];
								aBetween[3] = aBetween[5];
								aBetween[5] = between;
							}
							return "(" + aBetween[3] + " < " + aBetween[1] + ") && (" + aBetween[1] + " < " +aBetween[5] + ")";
						} else {
							return r;
						}
					}//end function Between;
					
					function Like(r) {
						var isLike = new RegExp("^\\s*(.+)\\s+(like)\\s*(')\\s*(.*)\\s*(')\\s*$", "i");
						var notLike = new RegExp("^\\s*(.+)\\s+(not like)\\s*(')\\s*(.*)\\s*(')\\s*$", "i");
						var reject = /^\s+|\s+$/ ;
						var like = false;
						if (notLike.test(r)) {
							var aLike = notLike.exec(r);
							isFalse = " == ";
							isTrue = " != ";
							like = true;
						} else if (isLike.test(r)) {
							var aLike = isLike.exec(r);
							isFalse = " != ";
							isTrue = " == ";
							like = true;
						}	
						if (like) {
							aLike[1] = aLike[1].toString();
							aLike[4] = aLike[4].toString();
							aLike[1] = aLike[1].replace(reject, "");
							aLike[4] = aLike[4].replace(reject, "");
							var regFull = /^%(.*)%$/;
							var regLeft = /^%(.*)/;
							var regRight = /(.*)%$/;
							if (regFull.test(aLike[4])) {
								aLike[4] = regFull.exec(aLike[4])[1];
								return (aLike[1] + ".toString().indexOf('" + aLike[4] + "')" + isFalse + "-1");
							} else if (regLeft.test(aLike[4])) {
								aLike[4] = regLeft.exec(aLike[4])[1];
								return (aLike[1] + ".toString().lastIndexOf('" + aLike[4] + "')" + isTrue + "(" + aLike[1] + ".length - " + aLike[4].length + ")");
							} else if (regRight.test(aLike[4])) {
								aLike[4] = regRight.exec(aLike[4])[1];
								return (aLike[1] + ".toString().indexOf('" + aLike[4] + "')" + isTrue + "0");
							} else {
								return (aLike[1] + isTrue + "'" + aLike[4] + "'");
							}
						} else {
							return r;
						}
					}//end function Like;
					/**************/
				}//end method  parseCondition;
				/*******************************/
			}// end class;
		},//end method  at Tam Diep - Ninh Binh;
			
		element: function(r) {
			var setting = {
				tagName: "div",
				innerHTML: "",
				className: ""
			}
			for (var o in setting) {
				if (r[o]) {
					setting[o] = r[o];
				}
			}
			var newNode = document.createElement(setting.tagName);
				newNode.innerHTML = setting.innerHTML;
			if (setting.className) {
				newNode.className = setting.className;
			}
			return newNode;
		}
	};
	
	K.insert = function(r) {
		var setting = {
			element: document.createElement("div"),
			refer: document.body,
			where: "beforeEnd"
		}
		for (var o in setting) {
			if (r[o]) {
				setting[o] = r[o];
			}
		}
		with (setting) { 
			if ((/beforeBegin/i).test(where)) {
				refer.parentNode.insertBefore(element, refer);
			} else if ((/afterBegin/i).test(where)) {
				setting.refer.insertBefore(element, refer.firstChild);
			} else if ((/beforeEnd/i).test(where)) {
				refer.appendChild(element);
			} else if ((/afterEnd/i).test(where)) {
				refer.parentNode.insertBefore(element, refer.nextSibling);
			}
		}
	};
	
	K.select = function(r) {
		var setting = {
			from: document,
			where: null
		}
		for (var o in setting) {
			if (r[o]) {
				setting[o] = r[o];
			}
		}
		if (setting.from && typeof setting.from == "array") {
			var DOM = K.create.database({fromCollection: setting.from})
		} else {
			var DOM = K.create.database({fromCollection: setting.from.getElementsByTagName("*")});
		}
		return DOM.Select({Where: setting.where});
	};
	
	Kombai.archive.data = {
		oldRequest: K.create.database({
			fieldName : ["index", "setting", "compare", "responseText", "responseXML"],
			autoIncrement: "index"
		})
	};
	
	K.request = function(r) {
		var oldRequest = Kombai.archive.data.oldRequest;
		if (!r || typeof r != "object") {
			return;
		}
		return new function() {
			this.request = false;
			var oSelf = this;
			var oAjax = {};
			var	sADN = new String();
			var setting =  {
				address: null,
				method: "POST",
				data: null,
				async: true,
				retry: 0,
				delay: 1000,
				onRequest: null,
				onSuccess: null,
				onFailure: null,
				onAbort: null
			};
			var bCache = r.cache || false;
			for (var i in setting) {
				if (r[i]) {
					setting[i] = r[i];
				}
			}
			var nCount = r.reload ? setting.retry - r.reload : setting.retry;
			for (i in setting) {
				sADN += i + ":" + encodeURIComponent(setting[i]) + "XOXOX"; 
			}
			var aResult = oldRequest.Select({Where: 'compare == "' + sADN + '"'});
			if (bCache && aResult[0]) {
				oAjax.responseText = aResult[0].responseText || null;
				oAjax.responseXML = aResult[0].responseXML || null;
				onSuccess(oAjax);
			} else {
				oAjax = createRequest();
				if (!oAjax) {
					return;
				}
				oSelf.request = true;
				if (isReady(oAjax)) {
					if (setting.method.toUpperCase() == "POST") {
						oAjax.open(setting.method, setting.address, setting.async);
						oAjax.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
						oAjax.setRequestHeader("Connection", "close");
						oAjax.send(setting.data);
					} else {
						oAjax.open(setting.method, setting.address + "?" + setting.data + "&R=" + new Date().getTime(), setting.async);
						oAjax.send(null);
					}
					onRequest(oAjax);
				} 
				oAjax.onreadystatechange = function() {
					if (!isComplete(oAjax) || !oSelf.request) {
						return;
					}
					onStateChange(oAjax);
				}
			}
			
			function createRequest() {		
				if (window.ActiveXObject) {
					return new ActiveXObject("Microsoft.XMLHTTP");
				} else {
					return new XMLHttpRequest() || null;
				}
			}
			
			function onStateChange(XHR) {
				oSelf.request = false;
				if (isSuccess(XHR)) {
					onSuccess(XHR);
					if (bCache) {
						oldRequest.Insert([setting, sADN, oAjax.responseText, oAjax.responseXML]);
					}
				} else {
					if (!nCount) {
						onFailure(XHR);
					} else {
						r.reload = r.reload ? r.reload + 1 : 1;
						oSelf.stop();
						if (oSelf.repeat) {
							clearTimeout(oSelf.repeat);
						}
						oSelf.repeat = setTimeout(
							function() {
								K.request(r);
							},
							setting.delay
						);
					}
				}
				delete oAjax.onreadystatechange;
			}
			
			function isReady(XHR) {
				return (XHR.readyState == 4 || XHR.readyState == 0);
			}
			
			function isRequest(XHR) {
				return XHR.readyState < 4;
			}
			
			function isComplete(XHR) {
				return XHR.readyState == 4;
			}
			
			function isSuccess(XHR) {
				try {
					return XHR.status == 200;
				} catch(e) {
					return false;
				}  
			}
			
			function onRequest(XHR) {
				if (typeof setting.onRequest == "function") {
					setting.onRequest.call(XHR);
				} else {
					eval(setting.onRequest);
				}
			}
			
			function onSuccess(XHR) {
				if (typeof setting.onSuccess == "function") {
					setting.onSuccess.call(XHR);
				} else {
					eval(setting.onSuccess);
				}
			}
			
			function onFailure(XHR) {
				if (typeof setting.onFailure == "function") {
					setting.onFailure.call(XHR);
				} else {
					eval(setting.onFailure);
				}
			}
			
			function onAbort(XHR) {
				if (typeof setting.onAbort == "function") {
					setting.onAbort.call(XHR);
				} else {
					eval(setting.onAbort);
				}
			}
			
			this.stop = function() {
				delete oAjax.onreadystatechange;
				this.request = false; 
				onAbort(oAjax);
				oAjax.abort();
			}
		}
	};
		
	/****************************************/	
	
	K.dragdrop = function(element, environment, ondrop) {
		if (typeof element == "string") {
			element = document.getElementById(element);
		}
		if (typeof environment == "string") {
			environment = document.getElementById(environment);
		}
		if (environment.style.zIndex) {
			element.style.zIndex = environment.style.zIndex + 1;
		} else {
			element.style.zIndex = 1;
		}
		K.add({
			element: element,
			event: "dragdrop",
			onDrop: function() {
				if (this.penetrate(environment)) {
					if (typeof ondrop == "string") {
						eval(ondrop);
					} else if (typeof ondrop == "function") {
						ondrop();
					}
				}
			}
		});
	};
	
	K.move = function(element, content) {
		if (!element) {
			return;
		}
		if (typeof element == "string") {
			element = document.getElementById(element);
		}
		if (content != undefined) {
			if (typeof content == "string") {
				content = document.getElementById(content);
			}
			K.add({
				element: element,
				event: "dragdrop",
				onDrag: function() {
					K.add({
						element: content,
						event: "dragdrop",
						onDrop: function() {
							this.drop();
						}
					});
				}
			});
		} else {
			K.add({
				element: element,
				event: "dragdrop"
			});
		}
	};
	
	K.discover = function(object) {
		remove();
		if (!object) return;
		var root = document.createElement("div");
			root.setAttribute("id" , "k.o.m.b.a.i");
			root.style.position = "absolute";
			root.style.top = "0px";
			root.style.width = "0px";
			root.style.height = "0px";
		var info = document.createElement("div");
			info.style.background = "black" ;
			info.style.top = "0px" ;
			info.style.color = "white" ;
			info.style.padding = "3px" ;
			info.style.width = "600px" ;
			info.style.zIndex = "9999" ;
			info.style.position = "relative" ;
		var closeButton = document.createElement("div");
			closeButton.style.padding = "3px 0px 6px 0px";
			closeButton.innerHTML = "<span style='cursor: pointer;' onclick='K.discover(window);'> {..} window </span>";
			closeButton.innerHTML += "<span style='cursor: pointer;' onclick='K.discover(window.document);'> / document </span>";
		var rightButton = document.createElement("div");
			rightButton.style.textAlign = "right";
			rightButton.style.margin = "-16px 0px 0px 200px";
		var trueClose  = document.createElement("span");
			trueClose.innerHTML = "<span style='color: red; font-weight: bold; cursor: pointer'>[ X ]</span>" ;
		var blockContent = 	document.createElement("div");
			blockContent.style.background = "#848484" ;
			blockContent.style.border = "1px solid green" ;
			blockContent.style.height = "300px" ;
			blockContent.style.overflow = "auto" ;
			blockContent.style.padding = "10px" ;
			
			document.body.appendChild(root);
			root.appendChild(info);
			info.appendChild(closeButton);
			info.appendChild(blockContent);
			closeButton.appendChild(rightButton);
			rightButton.appendChild(trueClose);
			trueClose.onclick = remove;
			K.move(closeButton, info);
			
		function inspect(o) {
			var node = document.createElement("div");
			if (typeof o == "object") {
				var v = {};
				for (var p in o) {
					try {v = o[p];}
					catch(e) {v = "Can't Access !!!";}
					if (typeof v == 'object') {
						var div = document.createElement("div");						
						div.innerHTML = "<span style='margin-right: 2px;'>{..}</span>";
						div.innerHTML += "<span style='color: #9b1a00'>" + p + "</span> : " + v;
						node.appendChild(div);
					} else if (typeof v == "string") {
						var div = document.createElement("div");
						div.innerHTML = "<span style='margin-right: 15px;'>-</span>";
						div.innerHTML += "<span style='color: #9b1a00'>" + p + "</span> : " + v.replace(/</g, "&lt;");
						node.appendChild(div);
					} else {
						var div = document.createElement("div");
						div.innerHTML = "<span style='margin-right: 15px;'>-</span>";
						div.innerHTML += "<span style='color: #9b1a00'>" + p + "</span> : " + v;
						node.appendChild(div);
					}
				}
			} else if (typeof o == "string") {
				node.innerHTML = o.replace(/</g, "&lt;");
			} else {
				node.innerHTML = o;
			}
			return node;
		}
		
		function show(target, data) {
			target.appendChild(data);
		}
		
		function remove() {
			if (document.getElementById("k.o.m.b.a.i")) {
				var root = document.getElementById("k.o.m.b.a.i");
				root.parentNode.removeChild(root);
			}
		}
		show(blockContent, inspect(object));
	} ;
	
	K.addEventOnLoad = function(r) {
		K.add({element: window, event: "load", listener: r});
	};
	
	K.addEventOnResize = function(r) {
		K.add({element: window, event: "resize", listener: r});
	};
