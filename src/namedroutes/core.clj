(ns namedroutes.core
  (:use [compojure.core]
        [namedroutes.routes]))


(defnamedroutes users
  :index  (GET  "/users"            []            "show all users")
  :new    (GET  "/users/new"        []            "show empty form for new user")
  :show   (GET  "/users/:id"        [id]          "show a single user")
  :edit   (GET  "/users/:id/edit"   [id]          "edit a single user")
  :create (POST "/users"            [& params]    "create a new user from params")
  :update (PUT  "/users/:id"        [id & params] "update a single user from params")
  :delete (DELETE "/users/:id"      [id]          "delete a single user"))

(defnamedroutes wildcard-routes
  :foo    (POST "/foo/:bar/:baz/*"  [bar baz * & params] "Hello, world!"))


(defn -main [& args]
  (println "Named routes for Compojure - examples:")
  (println "--------- users urls:")
  (println "index:   " (url-for users :index))
  (println "new:     " (url-for users :new))
  (println "show:    " (url-for users :show 123))
  (println "edit:    " (url-for users :edit 123))
  (println "create:  " (url-for users :create))
  (println "update:  " (url-for users :update 123))
  (println "delete:  " (url-for users :delete 123))
  (println "--------- wildcard urls:")
  (println "foo:     " (url-for wildcard-routes :foo "one" "two" "three/four")))