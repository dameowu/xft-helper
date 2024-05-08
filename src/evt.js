/**
 * @auther dameowu@163.com
 * @description 薪福通事件处理接口
 */

const java = require('./jvm').load();

_evtHelper = null;
_pubKey = null;
function init(pubKey) {
  if (!_evtHelper) {
    _evtHelper = java.import('com.cmb.xft.util.EvtHelper');

    _evtHelper.initSync(pubKey);
    _pubKey = pubKey;
  }
}

// 事件验证，不知道为什么，签名验证失败，暂时未使用
function verify(eventId, prjCod, eventTime, eventCd, signature) {
  return _evtHelper.verifySync(eventId, prjCod, eventTime, eventCd, signature);
}

function decode(body) {
  return JSON.parse(_evtHelper.decodeSync(body));
}

let evtHanders = {}
function handleEvt(evt) {
  try {
    const getEvtHanders = (eventId) => {
      const handlers = evtHanders[eventId];
      return (handlers) ? handlers : [];
    };
    if (evt.info) {
      handlers = getEvtHanders(evt.id)
      if (handlers.length === 0) {
        console.log(`未注册处理事件: ${evt.id}`);
        return;
      }
      handlers.forEach((callback) => {
        callback(evt.info);
      });
    }
  }
  catch (error) {
    console.log(`处理薪福通事件失败: ${error}`);
  }
}

const regEvtHander = (eventId, callback) => {
  if (!evtHanders[eventId])
  {
    evtHanders[eventId] = [];
  }
  evtHanders[eventId].push(callback)
}


module.exports = {init, verify, decode, handleEvt, regEvtHander};
