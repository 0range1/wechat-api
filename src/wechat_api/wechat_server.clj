(ns wechat-api.wechat-server
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [ring.adapter.jetty :as jetty]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [clojure.tools.logging :as log]
            [wechat-api.response-util :as res]
            [ring.util.codec :as encoder]
            ))

(def APP_ID "wxbeaae9b8292e97fa")
(def APP_SECRET "3b8428ccc6eb3859c3d61272125e1a97")
(def SCOPE "snsapi_userinfo")
(def STATE "state")
(def TIMEOUT 200)

(defn get-weixin-accesstoken
  "Get access_token by code"
  [code]
  (try
     ;(println "Getting token...")
     (let [result (client/get "https://api.weixin.qq.com/sns/oauth2/access_token"
                              {:query-params {:appid APP_ID
                                              :secret APP_SECRET
                                              :code code
                                              ::grant_type "authorization_code"}
                               :timeout TIMEOUT
                               :as :json})
           weixin-response (:body result)]
       (if (:errcode weixin-response)
           (res/failResponse weixin-response)
          (do
            (println " the token is " (:access_token weixin-response) ", and openid is " (:openid weixin-response))
            (res/succResponse weixin-response))))

     (catch Exception e
         (do
           ;(log/error "get access token error: " (.getMessage e))
         (res/failResponse 50100 (.getMessage e)))
       )))

(defn get-weixin-info
  [access_token openid]
  (try
    (println "``````enter get-weixin-info``````\n")
    (let [result (client/get "https://api.weixin.qq.com/sns/userinfo"
                             {:query-params {:access_token access_token
                                             :openid openid
                                             :lang "zh_CN"}
                              :timeout TIMEOUT
                              :as :json})
          weixin-response (:body result)]
      ;(log/info "userinfo response: " weixin-response)
      (if (:errcode weixin-response)
        (do
          ;(log/error "get open user info failed: " (:errcode weixin-response))
          (res/failResponse weixin-response))
        (do
         (println "!!!!!!get info successfully and openid is " (:openid weixin-response) ", nickname is " (:nickname weixin-response) ", headimgurl is " (:headimgurl weixin-response))
         (res/succResponse weixin-response))))
    (catch Exception e
      (log/error ("get open user info error: " (.getMessage e))))))

(defn weixin-info
  [code state]
  (println "enter weixin-info and the code is " code)
  (let [accessTokenResponse (get-weixin-accesstoken code)
        success (:isSuccess accessTokenResponse)
        result (:result accessTokenResponse)]
    (do
     (println "``````````````in weixin-info, and the token is " (:access_token result))
     (if success
      (do (println "in weixin-info, and the token is " (:access_token result))
      (get-weixin-info (:access_token result) (:openid result)))
      accessTokenResponse))))

(defn weixin-auth-info
  []
  (println "enter weixin-auth-info")
  (let [query-params {:appid APP_ID
                      :redirect_uri (encoder/url-encode "http://loridomain.free.idcfengye.com/api/wechat-info")
                      :response_type "code"
                      :scope SCOPE
                      :state STATE}]
    (format "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=%s&scope=%s&state=%s#wechat_redirect"
            (:appid query-params)
            (:redirect_uri query-params)
            (:response_type query-params)
            (:scope query-params)
            (:state query-params))))
