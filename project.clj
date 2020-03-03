(defproject clj-latex "0.1.1"
  :description "A library for representing LaTeX code in Clojure"
  :url "https://github.com/cnly/clj-latex"
  :license {:name "The MIT License"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :profiles {:dev {:dependencies [[net.mikera/core.matrix "0.62.0"]]}}
  :deploy-repositories [["clojars" {:sign-releases false :url "https://clojars.org/repo"}]])
