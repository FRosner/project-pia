(ns client.state)

(def config {:domain "http://127.0.0.1:8080"})

(def default {:components {:form {:doubleFeature 0}
                           :predictions []
                           :notification nil}
              :predictions {}})
