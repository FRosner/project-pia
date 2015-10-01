(ns client.protobuf)

(defn build-protobuf [path f]
  (.loadProtoFile js/dcodeIO.ProtoBuf path f))

(defn encode [obj]
  (.toArrayBuffer obj))

(defn decode [s constructor]
  (->>
   (.fromBinary js/dcodeIO.ByteBuffer s)
   (.decode constructor)))

(declare Observation)
(declare Prediction)
(declare IDs)

;; public API

(defn observation [m]
  (Observation. (clj->js m)))

(defn prediction [m]
  (Prediction. (clj->js m)))

(defn ids [m]
  (IDs. (clj->js m)))

(defn encode-observation [obj]
  (encode (observation obj)))

(defn decode-ids [s]
  (let [ids (aget (decode s IDs) "ids")]
    (mapv #(aget % "uuid") ids)))

(defn decode-prediction [s]
  (aget (decode s Prediction) "score"))

(defn init []
  (build-protobuf
   "/protobuf/Observations.proto"
   (fn [err builder]
     (when (nil? err)
       (def Observation (.build builder "de.frosner.pia.Observation")))))

  (build-protobuf
   "/protobuf/Predictions.proto"
   (fn [err builder]
     (when (nil? err)
       (def Prediction (.build builder "de.frosner.pia.Prediction"))
       (def IDs (.build builder "de.frosner.pia.IDs"))))))
