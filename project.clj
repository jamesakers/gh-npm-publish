(defproject gh-npm-publish "0.0.1"
  :description "Auto-publish of Node Packages on version changes."
  :url "http://github.com/jamesakers/gh-npm-publish"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring-serve "0.1.2"]
                 [conch "0.2.1"]
                 [net.lstoll/utils "0.3.0"]
                 [org.clojure/data.json "0.2.2"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler gh-npm-publish.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
