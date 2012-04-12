;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; 2012, Montoux Limited
;;;
;;; Author(s): Gert Verhoog, <gert@montoux.com>
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns ^{:author "Montoux Limited, <info@montoux.com>"}
  namedroutes.routes-test
  (:require [clojure.string :as string])
  (:use clojure.test
        namedroutes.routes
        compojure.core))


(deftest test-url-and-method
  (is (= "/foo/bar" (url [:get "/foo/bar"])))
  (is (= :get (method [:get "/foo/bar"]))))


(deftest test-named-routes-for
  (let [r (namedroutes :foo (POST "/foo" [] "some code")
                       :bar (GET "/bar" [] "some more code"))]
    (is (= #{:foo :bar}
           (into #{} (keys (named-routes-for r)))))))


(deftest test-with-no-params
  (let [r (namedroutes :foo (POST "/foo" [] "some code"))]
    (is (= [:post "/foo"] (url-for r :foo)))))

(deftest test-with-params
  (let [r (namedroutes :foo (POST "/foo/:bar" [bar] "some code"))]
    (is (= [:post "/foo/123"] (url-for r :foo 123)))))

(deftest test-with-params-map-binding
  (let [r (namedroutes :foo (POST "/foo/:bar"
                                  {{bar "bar"} :params}
                                  "some code"))]
    (is (= [:post "/foo/123"] (url-for r :foo 123)))))

(deftest test-with-wildcard
  (let [r (namedroutes :foo (POST "/foo/:bar/*" [bar *] "some code"))]
    (is (= [:post "/foo/123/shaz/am"] (url-for r :foo 123 "shaz/am")))))



(defnamedroutes my-test-routes
  :foo (GET "/foo" [] "hello world")
  :bar (GET "/bar" [] "hello world"))


(deftest test-with-routes-var-and-symbol
  (is (= "/foo" (url (url-for my-test-routes :foo))))
  (is (= "/foo" (url (url-for #'my-test-routes :foo))))
  (is (= "/foo" (url (url-for 'namedroutes.routes-test/my-test-routes :foo)))))