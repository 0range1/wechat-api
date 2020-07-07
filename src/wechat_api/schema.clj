(ns wechat-api.schema
  (:require [schema.core :as s])
  (:import (javax.xml.ws Response)))

(s/defschema Response
  {:isSuccess s/Bool
   :errcode s/Int
   :errmsg s/Str
   :result {s/Keyword s/Any}
   })