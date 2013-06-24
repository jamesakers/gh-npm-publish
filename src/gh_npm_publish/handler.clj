(ns gh-npm-publish.handler
  (:use compojure.core
        lstoll.utils
        ring.util.serve
        ring.adapter.jetty)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.json :as json] ;; Required for parsing package.json for version checking
            [conch.core])
  (:gen-class))

(defn expand-path [path] (.getCanonicalPath (java.io.File. path)))

;; Run commands via (heroku|server) command line
(defn run [app-path cmd]
  (let [exec ["sh" "-c" cmd :env (merge (into {} (System/getenv))) ]
    proc (apply conch.core/proc exec)]
    (future (conch.core/stream-to :out proc *out*))
    (future (conch.core/stream-to :err proc *out*))
    (conch.core/exit-code proc)))

;; Update application via git because it is already cloned
(defn update-repo [app-path app]
  (log "Starting repo update")
  (run app-path (str "cd " app "/repo && git fetch && git reset --hard origin/master"))) 

;; Clone application via git because does not exist
(defn clone-repo [app-path app]
  (log "Starting repo clone")
  (run app-path (str "cd " app " && git clone " (env (str app "_GITHUB_REPO")) " repo")))

;; Check version of appliction
(defn get-version [identifier app]
  (log "Get" identifier "version number")
  (if (.isDirectory (io/file (str app "/repo")))
    (do (def package-info (json/read-str (slurp (str app "/repo/package.json"))))
      (get-in package-info ["version"]))))

;; Set npm information from environment variables
(defn setup-npm [app-path]
  (log "Setting username")
  (spit (str "/app/.npmrc") (env (str "NPMRC")))
  (log "Setting username")
  (run app-path (str "npm config set username " (env (str "NPM_USERNAME"))))
  (log "Setting email")
  (run app-path (str "npm config set email " (env (str "NPM_EMAIL"))))
  (log "Setting .npmrc")
  (run app-path (str "npm config set userconfig /app/.npmrc")))

;; Where all the -magic- begins 
(defn publish [app]
  (println "Initializing...")
  (let [app-path (expand-path app) previous-version (get-version "pre-update", app)]
    (when-not (.isDirectory (io/file app))
      (.mkdir (io/file app)))
    (if (= nil (if (.isDirectory (io/file (str app "/repo")))
      (do (update-repo app-path app))))(do (clone-repo app-path app)))
    (if-not (= previous-version (get-version "after-update", app))
      (do (setup-npm app-path)
        (log "Publishing ...")
        (run app-path (str "cd " app "/repo && npm publish")))))
  (println "... Finished"))

(defn valid-key? [key] (= key (env "ACCESS_KEY" "SETME")))

(defroutes app-routes
  (GET "/" [] "<h1>Nothing to see here, move along</h1>")
  ;; Uncomment to test locally if you don't want to use an HTTP Client
  ;(GET "/publish" {{app :app key :key} :params} (if (valid-key? key)
                                                  ;(do (future (publish app)) "OK")
                                                  ;{:status 403 :body "DENIED"}))
  (POST "/publish" {{app :app key :key} :params} (if (valid-key? key)
                                                  (do (future (publish app)) "OK")
                                                  {:status 403 :body "DENIED"}))
  (route/not-found "<h1>FOUR-OH-FOUR!</h1>"))

(def app (handler/site app-routes))
(defn -main [] (run-jetty #'app {:port (Integer/parseInt (env "PORT" "5000")) :join true}))
