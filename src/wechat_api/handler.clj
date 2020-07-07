(ns wechat-api.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [ring.adapter.jetty :as jetty]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [wechat-api.wechat-server :as weixin]
    ;[wechat-api.schema :refer :all]
            [schema.core :as s]
            [wechat-api.schema :as schema]
            )
  )

;(s/defschema Response
;  {:isSuccess s/Bool
;   :errcode s/Int
;   :errmsg s/Str
;   :result {s/Keyword s/Any}})

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Wechat-api"
                    :description "Compojure Api example"}
             :tags [{:name "api", :description "some apis"}]}}}

    (context "/api" []
      :tags ["api"]
      (GET "/wechat-info" []
        :return schema/Response
        ;:tag ["wexin"]
        :query-params [code :- String, state :- String]
        :summary "weixin redirect_api"
        (println "in api/wechat-info, code is " code " and state is " state)
        (ok (weixin/weixin-info code state)))


      (GET "/check-echostr"
           {:keys [headers params body] :as request}
        (def echostr (:echostr params))
        (ok echostr))
      ;(GET "/check-echostr" []
      ;  :return String
      ;  :query-params [signature :- String, timestamp :- String, nonce :- String, echostr :- String]
      ;  :summary "weixin redirect_api"
      ;  (println signature timestamp nonce echostr)
      ;  (println (type echostr))
      ;  (ok echostr))

      (GET "/wechat-auth-info" []
        :tags ["weixin"]
        :summary "需要在微信客户端中运行"
        (permanent-redirect (weixin/weixin-auth-info)))
      )))


(defn -main [& args]
  (jetty/run-jetty app {:port 80}))