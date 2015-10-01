(ns client.api
  (:require
   [cljs-http.client :as http]
   [cljs.core.async :as async :refer [put! chan alts!]]
   [schema.core :as s :include-macros true]
   [plumbing.core :as p]
   [client.protobuf :as protobuf]
   [client.state :as state])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go go-loop]]))

(def predictions-url (str
                      (p/safe-get state/config :domain)
                      "/predictions"))

(defn get-predictions
  [api-ch]
  (go
    (let [resp (->
                (http/get predictions-url {:with-credentials? false})
                (<!)
                (update :body protobuf/decode-ids))]
      (put! api-ch {:type :get-predictions
                    :response resp}))))

(s/defn get-prediction
  [api-ch
   id :- s/Int]
  (go
    (let [url (str predictions-url "/" id)
          resp (-> (http/get url {:with-credentials? false})
                   (<!)
                   (update :body protobuf/decode-prediction))]
      (put! api-ch {:type :get-prediction
                    :response resp}))))

(s/defn post-prediction
  [api-ch features]
  (go
    (let [body (protobuf/encode-observation features)
          resp (<! (http/post predictions-url
                              {:body body
                               :with-credentials? false}))]
      (put! api-ch {:type :post-prediction
                    :response resp}))))
