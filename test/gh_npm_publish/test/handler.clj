(ns gh-npm-publish.test.handler
  (:use clojure.test
        ring.mock.request  
        gh-npm-publish.handler))

(deftest test-app
  (testing "main route - nothingness"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "<h1>Nothing to see here, move along</h1>"))))
  
  (testing "not-found route"
    (let [response (app (request :get "/publish"))]
      (is (= (:status response) 404))
      (is (= (:body response) "<h1>FOUR-OH-FOUR!</h1>"))))

  (testing "post bad request"
    (let [response (app (request :post "/publish" {:body "app=SOMEAPP&key=SETME"}))]
      (is (= (:status response) 403))
      (is (= (:body response) "DENIED")))))
