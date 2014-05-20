(defproject venn-cljs "0.1.0-SNAPSHOT"
  :description "A simple cljs script to create and render interactive Venn diagrams with d3"
  :url "http://sparrho.com/"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [prismatic/dommy "0.1.2"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds {:prod
             {:source-paths ["src"]
              :compiler {
                :output-to "cljs.compiled.js"
                :output-dir "out"
                :externs ["d3_externs.js"]
                :optimizations :advanced}}}})
