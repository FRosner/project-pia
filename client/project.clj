(defproject pia-client "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "1.1.0"]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/clojurescript "1.7.48"]
                 [cljs-http "0.1.24"]
                 [prismatic/schema "1.0.1"]
                 [prismatic/plumbing "0.5.0"]
                 [org.omcljs/om "0.9.0"]
                 [sablono "0.3.6"]]
  :cljsbuild
  {:builds {:dev {:source-paths ["src/cljs/"
                                 "target/generated/src/cljs"]
                  :compiler {:output-to "resources/public/js/compiled/dev.js"
                             :output-dir "resources/public/js/compiled/out"
                             :optimizations :none
                             :source-map true}}}})
