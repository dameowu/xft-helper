/**
 * @auther dameowu@163.com
 * @description nodejs 调用薪福通restAPI
 * 
 * A librairy to call XFT restAPI in nodejs.
 */
const axios = require('axios');
const java = require('./jvm').load();

const XFT_TIME_OUT = 10000;

let _apiHelper = null;
function init(compandId, appId, authSecret) {
  _apiHelper = java.import('com.cmb.xft.util.ApiHelper');
  _apiHelper.initSync(compandId, appId, authSecret);
}

function post(context, url, queryParam, reqBodyJson) {
  if (!reqBodyJson) {
    return Promise.reject('xft post param error');
  }
  const reqBody = JSON.stringify(reqBodyJson);
  const httpInfo = _apiHelper.postInfoSync(url, queryParam, reqBody);
  const xftUrl = httpInfo.getUrlSync();
  const headersJson = httpInfo.getHeadersSync();
  const headers = JSON.parse(headersJson);
  const body = Buffer.from(httpInfo.getBodySync(), 'utf8');
  
  headers['xft-api-call-type'] = 'sdk-java';
  headers['xft-api-sdk-version'] = '1.0.5-RELEASE';
  headers['xft-api-scene'] = 'cust';
  
  let _axiosInstance = axios.create({timeout: XFT_TIME_OUT});
  if (context) {
    _axiosInstance.interceptors.request.use(config => {
      config.localContext = context; 
      return config;
    }, error => {
      const localContext = error.config ? error.config.localContext : null;
      error.localContext = localContext;
      return Promise.reject(error);
    });
  }
  return _axiosInstance.post(xftUrl, body, {headers: headers});
}

function getErrMsg(err) {
  if (err.response.data && err.response.data.message) {
    return err.response.data.message;
  } 
}

function checkResult(res) {
  return (res.data && res.data.returnCode === 'SUC0000' && res.data.errorMsg === null)
}

module.exports = { init, post, checkResult, getErrMsg };