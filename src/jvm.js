/**
 * @auther dameowu@163.com
 * @description nodejs 调用薪福通Java SDK要加载到JVM里面的jar包
 */
function load() {
  if (!global.java) {
    const java = require('java');
    java.classpath.push('../jar/commons-logging-1.2.jar');
    java.classpath.push('../jar/spring-core-5.3.18.jar');
    java.classpath.push('../jar/spring-web-5.3.18.jar');
    java.classpath.push('../jar/bcprov-jdk15on-1.63.jar');
    java.classpath.push('../jar/open-api-client-1.0.5-RELEASE.jar');
    java.classpath.push('../jar/xft-api-helper.jar');
    java.classpath.push('../jar/fastjson2-2.0.48.jar');
    global.java = java;
  }
  return global.java;
}

module.exports = {load};
