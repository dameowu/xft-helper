# xft-helper 薪福通API nodejs库

## 依赖项
- bcprov-jdk15on-1.63.jar
- commons-logging-1.2.jar
- fastjson2-2.0.48.jar
- spring-core-5.3.18.jar
- spring-web-5.3.18.jar

- open-api-client-1.0.5-RELEASE.jar
  -- 下载地址：https://xft.cmbchina.com/open/#/doc/open-document?id=10672

## 使用方式

### OpenAPI调用
````javascript

const config = {
  "XFT_PUB_KEY": "xxxxxxxxx", // 薪福通事件推送公钥
  "XFT_COMPANY_ID": "yyyy",   // 薪福通企业ID
  "XFT_APPID": "xxxx",        // 薪福通App ID
  "XFT_AUTH_SECRET": "xxxxxxx" // 薪福通AuthoritySecret，与App ID绑定，在创建网页应用或零开应用后的app页面获取
}

const xftClient = require('./client');

// 薪福通通用审批OA查询列表接口
const XFT_OA_QUERY_LIST = "https://api.cmbchina.com/xft-oa/openapi/xft-oaquery/form/query-list"

xftClient.init(config.XFT_COMPANY_ID, config.XFT_APPID, config.XFT_AUTH_SECRET);

xftClient.post({data: "hello meowu"}, XFT_OA_QUERY_LIST, null, {})
 .then((res) => {
  console.log(res.data);
  console.log("saved data: ",res.config.localContext.data)

  process.exit(0)
 });
````