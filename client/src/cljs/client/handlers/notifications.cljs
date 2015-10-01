(ns client.handlers.notifications)

(defn handle [msg state]
  (println msg)
  (swap! state assoc-in [:components :notification] msg))
