(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["about"],{2403:function(t,e,s){t.exports=s.p+"img/cover1.18ec5016.jpg"},"4e82":function(t,e,s){"use strict";var a=s("23e7"),n=s("1c0b"),r=s("7b0b"),i=s("d039"),o=s("b301"),c=[].sort,l=[1,2,3],u=i((function(){l.sort(void 0)})),d=i((function(){l.sort(null)})),h=o("sort"),p=u||!d||h;a({target:"Array",proto:!0,forced:p},{sort:function(t){return void 0===t?c.call(r(this)):c.call(r(this),n(t))}})},"5a34":function(t,e,s){var a=s("44e7");t.exports=function(t){if(a(t))throw TypeError("The method doesn't accept regular expressions");return t}},"7b5b":function(t,e,s){},"8a79":function(t,e,s){"use strict";var a=s("23e7"),n=s("50c4"),r=s("5a34"),i=s("1d80"),o=s("ab13"),c="".endsWith,l=Math.min;a({target:"String",proto:!0,forced:!o("endsWith")},{endsWith:function(t){var e=String(i(this));r(t);var s=arguments.length>1?arguments[1]:void 0,a=n(e.length),o=void 0===s?a:l(n(s),a),u=String(t);return c?c.call(e,u,o):e.slice(o-u.length,o)===u}})},"8e07":function(t,e,s){},a2f1:function(t,e,s){t.exports=s.p+"img/cover2.bd3cbaf9.jpg"},ab13:function(t,e,s){var a=s("b622"),n=a("match");t.exports=function(t){var e=/./;try{"/./"[t](e)}catch(s){try{return e[n]=!1,"/./"[t](e)}catch(a){}}return!1}},cd58:function(t,e,s){"use strict";var a=s("8e07"),n=s.n(a);n.a},d504:function(t,e,s){"use strict";s.r(e);var a=function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"index-wrapper"},[a("div",{staticClass:"navigation-wrapper"},[a("div",{staticClass:"navigation-title"},[t._v(" 阅读 ")]),a("div",{staticClass:"navigation-sub-title"},[t._v(" 清风不识字，何故乱翻书 ")]),a("div",{staticClass:"search-wrapper"},[a("el-input",{staticClass:"search-input",attrs:{size:"mini",placeholder:"搜索书籍"},model:{value:t.search,callback:function(e){t.search=e},expression:"search"}},[a("i",{staticClass:"el-input__icon el-icon-search",attrs:{slot:"prefix"},slot:"prefix"})])],1),a("div",{staticClass:"recent-wrapper"},[a("div",{staticClass:"recent-title"},[t._v(" 最近阅读 ")]),a("div",{staticClass:"reading-recent"},[a("el-tag",{staticClass:"recent-book",class:{"no-point":""==t.readingRecent.url},attrs:{type:"warning"},on:{click:function(e){return t.toDetail(t.readingRecent.url,t.readingRecent.name,t.readingRecent.chapterIndex,t.book.durChapterPos)}}},[t._v(" "+t._s(t.readingRecent.name)+" ")])],1)]),a("div",{staticClass:"setting-wrapper"},[a("div",{staticClass:"setting-title"},[t._v(" 基本设定 ")]),a("div",{staticClass:"setting-item"},[a("el-tag",{staticClass:"setting-connect",class:{"no-point":t.newConnect},attrs:{type:t.connectType},on:{click:t.setIP}},[t._v(" "+t._s(t.connectStatus)+" ")])],1)]),a("div",{staticClass:"bottom-icons"},[a("a",{attrs:{href:"https://github.com/Celeter/web-yuedu3",target:"_blank"}},[a("div",{staticClass:"bottom-icon"},[a("img",{attrs:{src:s("fa39"),alt:""}})])])])]),a("div",{ref:"shelfWrapper",staticClass:"shelf-wrapper"},[a("div",{staticClass:"books-wrapper"},[a("div",{staticClass:"wrapper"},t._l(t.shelf,(function(e){return a("div",{key:e.noteUrl,staticClass:"book",on:{click:function(s){return t.toDetail(e.bookUrl,e.name,e.durChapterIndex,e.durChapterPos)}}},[a("div",{staticClass:"cover-img"},[a("img",{staticClass:"cover",attrs:{src:t.getCover(e),alt:""}})]),a("div",{staticClass:"info",on:{click:function(s){return t.toDetail(e.bookUrl,e.name,e.durChapterIndex,e.durChapterPos)}}},[a("div",{staticClass:"name"},[t._v(t._s(e.name))]),a("div",{staticClass:"sub"},[a("div",{staticClass:"author"},[t._v(" "+t._s(e.author)+" ")]),a("div",{staticClass:"dot"},[t._v("•")]),a("div",{staticClass:"size"},[t._v("共"+t._s(e.totalChapterNum)+"章")])]),a("div",{staticClass:"dur-chapter"},[t._v("已读："+t._s(e.durChapterTitle))]),a("div",{staticClass:"last-chapter"},[t._v(" 最新："+t._s(e.latestChapterTitle)+" ")])])])})),0)])])])},n=[],r=(s("4e82"),s("8a79"),s("7b5b"),s("bc3a")),i=s.n(r),o={data:function(){return{search:"",readingRecent:{name:"尚无阅读记录",url:"",chapterIndex:0}}},mounted:function(){var t=localStorage.getItem("readingRecent");null!=t&&(this.readingRecent=JSON.parse(t),"undefined"==typeof this.readingRecent.chapterIndex&&(this.readingRecent.chapterIndex=0)),this.loading=this.$loading({target:this.$refs.shelfWrapper,lock:!0,text:"正在获取书籍信息",spinner:"el-icon-loading",background:"rgb(247,247,247)"});var e=this;i.a.get("/getBookshelf",{timeout:3e3}).then((function(t){e.loading.close(),e.$store.commit("setConnectType","success"),e.$store.commit("increaseBookNum",t.data.data.length),e.$store.commit("addBooks",t.data.data.sort((function(t,e){var s=t["webDurChapterTime"]||0,a=e["webDurChapterTime"]||0;return a-s}))),e.$store.commit("setConnectStatus","已连接 "),e.$store.commit("setNewConnect",!1)})).catch((function(t){throw e.loading.close(),e.$store.commit("setConnectType","danger"),e.$store.commit("setConnectStatus","连接失败"),e.$message.error("后端连接失败"),e.$store.commit("setNewConnect",!1),t}))},methods:{getCover:function(t){return t.bookUrl.endsWith(".epub")||t.bookUrl.endsWith(".txt")?s("2403"):(t.coverUrl.endsWith(".jpg")||t.coverUrl.endsWith(".jpeg"))&&t.coverUrl||s("a2f1")},setIP:function(){},toDetail:function(t,e,s,a){sessionStorage.setItem("bookUrl",t),sessionStorage.setItem("bookName",e),sessionStorage.setItem("chapterIndex",s),sessionStorage.setItem("chapterPos",a),this.readingRecent={name:e,url:t,chapterIndex:s,chapterPos:s},localStorage.setItem("readingRecent",JSON.stringify(this.readingRecent)),this.$router.push({path:"/chapter"})}},computed:{shelf:function(){return this.$store.state.shelf},connectStatus:function(){return this.$store.state.connectStatus},connectType:function(){return this.$store.state.connectType},newConnect:function(){return this.$store.state.newConnect}}},c=o,l=(s("cd58"),s("2877")),u=Object(l["a"])(c,a,n,!1,null,"82dc8ee6",null);e["default"]=u.exports},f820:function(t,e,s){"use strict";s.r(e);var a=function(){var t=this,e=t.$createElement;t._self._c;return t._m(0)},n=[function(){var t=this,e=t.$createElement,s=t._self._c||e;return s("div",{staticClass:"about"},[s("h1",[t._v("This is an about page")])])}],r=s("2877"),i={},o=Object(r["a"])(i,a,n,!1,null,null,null);e["default"]=o.exports},fa39:function(t,e){t.exports="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAECUlEQVRYR7WXTYhcRRDHq3pY9yKrYBQ8KBsjgvHgwRhiQBTjYZm4Xe8NusawhwS/o9GLoKhgBGPAgJd1NdGIXwtZTbRf9Rqzl6gHTVyDeIkIgnEOghAM6oKHzTJd0sO8Zaa338zb7NjwmJn++Ndv+lVVVyOsoM3Ozl69sLBAiHiDc26NUuoKv9w5d14p9aeI/DI4OMgjIyN/lJXFMhOttQ8BgBaR0TLzEXEGAKzW+lCv+V0BmLmGiLtF5M5eQrFxRPxaRCaI6LOi9YUAzPwGADxxMYYjayaJ6MkoZKyTmU8AwF19Mp7LfElEW0LNZTvAzIcBYFufjedy00T0QLt2B4AxZo9S6qX/yXhT1jn3cpqme3IbSwDM/DgAvNlu3Dm3Uyl1HAA2IOJ2EdleEu5Io9H4EBHPVCqVLSISRsMuInrLazUBpqamhoaGhr4TkRsDgLVpmtbzPmPMLQBwOwD4vvzxw8P5IyJztVrtVL4my7L1iPhTx7Yj/jw/P79pfHx8vgmQZdkLiPhK+O8GBgauqVarv5f819FpxpjLlVJ/hYMi8mKSJHubAMz8KwBcF1EYI6IjqwRIlFImonGWiNZhlmVVRDxWYGTVAMx8HwB8EtMXka1orT0gIo9GJrxNRLH+FW8IMx8EgEeW5QDEgx5gTkQ2Bk7yr9b60hVb6rKAmc8BwJWBne+x4P3XiWhtPwGstV9FzpSzHuBvALgsMHaaiDp2ZbUwWZZNIuKuQOcfD7AAAJeEcaq1Xr9ao+3rmdknnscCzQse4LdWEukYazQaa2q12vl+QTDztwCwOdCr+zA8iYi3RQwREdl+ADDz9QDwIwB0OLaInPJRcEhEHoyEyAmt9d39ALDW2lg1hYjv+lfgC4WJgkTxcJIkPcuqbpC+qgKATwvm7PYAGwDgdBeRZ4notYvZCWPMDqXUe13W3to8C6y10yJyv//u6zj/2R6ziPiRiBwt6xPMrBExFZEdRcYR8WOt9bb8MNoKAJ+3Jvtwed05d4dSKtz+c4h4VGsdrRWttZMici8AXFVix+4homNLBUmWZQcQMc/9x4mommXZ84i4t11MKbV5dHR06bxvH5uZmbnZOfdN6O0RmMNE1CxulgCstdeKyBcAcFPrVTyltZ4wxiSVSuXplkhda72zh9P1rClFZFOSJHMdAP5Hq3rxR6eH+IGIvIOuqFlr94nIc10WdRzxy6riAMJnr2nn3JlcME3TppMWNWvtfhF5pmB8WX0RvZgEEEtaYUUbM2KtfUdE/FUubNHipvBmZIxZp5TaDwBprlQGIHLqzSHiPq01x4B7Xk6Z2d8TfDwPlwFozfd1f90598Hi4uKrY2NjFwrzQVkP81nNi/byAWOMv8gOp2n6fhnt/wDqJrRWLmhIrwAAAABJRU5ErkJggg=="}}]);