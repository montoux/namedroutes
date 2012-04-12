;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; 2012, Montoux Limited
;;;
;;; Author(s): Gert Verhoog, <gert@montoux.com>
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns ^{:author "Montoux Limited, <info@montoux.com>"
      :doc "
A version of Compojure's defroutes for named routes.

Example:

   (defnamedroutes users
     :index (GET \"/users\" [] (...))
     :edit (GET \"/users/:id/edit [id] (...)))

   (url-for users :edit 3456)
   ; => [:get \"/users/3456/edit\"]

or, if you want to look up named routes without depending on the
namespace that defined it (to avoid circular dependencies):

  (url-for #'my-ns/users :edit 3456)
  ; => [:get \"/users/3456/edit\"]
"}
  namedroutes.routes
  (:require [compojure.core :as compojure]
            [clojure.string :as string]
            [clojure.tools.macro :as macro]))


(def ^{:private true}
  symbol->keyword
  {'GET :get 'POST :post 'PUT :put 'DELETE :delete 'ANY :get})

(defn url
  "Returns the url part of a [method url] pair returned by `url-for'"
  [[_ url]] url)
(defn method
  "Returns the method part of a [method url] pair returned by `url-for'"
  [[method _]] method)

(defn named-routes-for
  "Returns all named routes found in the argument. The argument can
  be a routes datastructure as defined by `defnamedroutes', a var
  bound to named routes, or a symbol that will be resolved to a var."
  [routes]
  (let [routes (cond
                (var? routes) (var-get routes)
                (symbol? routes) (var-get (find-var routes))
                :default routes)]
    (:named-route-fns (meta routes))))

(defn url-for
  "Returns a pair [method url-string] with a url for routes and action,
   and any arguments the url needs. The routes must be defined
   with defnamedroutes, which stores the url-resolving functions
   in it's meta. `routes' should be a routes datastructure as defined
   by `defnamedroutes', a var bound to named routes, or a symbol that
   will be resolved to a var.

   Example:

   (defnamedroutes users
     :index (GET \"/users\" [] (...))
     :edit (GET \"/users/:id/edit [id] (...))
     :update (POST \"/users/:id\" [id & params] (...)))

   (url-for users :edit 3456)   ; => [:get \"/users/3456/edit\"]
   (url-for users :update 3456) ; => [:post \"/users/3456\"]

   or, if you want to look up named routes without depending on the
   namespace that defined it (to avoid circular dependencies):

   (url-for #'my-ns/users :edit 3456) ; => [:get \"/users/3456/edit\"] ;
  "
  [routes action & args]
  (when-let [f (get (named-routes-for routes) action)]
    (try
      (apply f args)
      (catch clojure.lang.ArityException e
        (throw (IllegalArgumentException.
                (str "Wrong number of args (" (count args) ") "
                     "passed to url-for. Make sure you supply "
                     "an argument for each parameter in the route.")))))))

(defn- split-url [url]
  (map #(if (or (.startsWith ^String % ":") (= "*" %)) (gensym) %)
       (string/split url #"/")))

(defn url-fn-for
  "Returns a function that accepts as many arguments as there are
  keywords and wildcards in the url, and returns a [method url] pair
  where method is a keyword and url is a string that is the input url
  with every keyword and wildcard replaced by its corresponding argument.

  This function is a low-level function used by `namedroutes'.

  Example:
  (let [f (url-fn-for '(POST \"/foo/:bar/*\" [bar * & params]
                        \"Hello World\"))]
    (f 123 \"baz/shazam\"))
  ; => [:post \"/foo/123/baz/shazam\"]"
  [[method url & _]]
  (let [url-parts (split-url url)
        fn-args (filter symbol? url-parts)
        method (get symbol->keyword method)]
    (eval
     `(fn [~@fn-args]
        [~method (str ~@(interpose "/" url-parts))]))))


(defmacro namedroutes
  "Like compojure's routes, but creates named routes. Example:

  (namedroutes
    :index  (GET \"/users\" [] \"SHOW INDEX\")
    :new    (GET \"/users/new\" [] \"NEW USER FORM\")
    :show   (GET \"/users/:id\" [id] \"SHOW USER\")
    :edit   (GET \"/users/:id/edit\" [id] \"EDIT USER\")
    :create (POST \"/users/\" [& params] \"CREATE NEW USER\")
    :update (POST \"/users/:id\" [id & params] \"UPDATE USER\")
    :delete (POST \"/users/:id\" [id] \"DELETE USER\"))

  named urls can be retrieved using the `url-for' function:
  (url-for my-named-routes :show \"my-user-id\")
  ; => [:get \"/users/my-user-id\"]
  "
  [& keyroutes]
  (let [routemap (apply hash-map keyroutes)
        routes (vals routemap)
        [name routes] (macro/name-with-attributes name routes)
        fs (into {} (map (fn [[k v]] [k (url-fn-for v)]) routemap))]
    `(let [routefns# ~fs]
       (with-meta (compojure/routes ~@routes)
         {:named-route-fns routefns#}))))

(defmacro defnamedroutes
  "Like compojure's defroutes, but creates named routes. Example:

  (defnamedroutes users
    :index  (GET \"/users\" [] \"SHOW INDEX\")
    :new    (GET \"/users/new\" [] \"NEW USER FORM\")
    :show   (GET \"/users/:id\" [id] \"SHOW USER\")
    :edit   (GET \"/users/:id/edit\" [id] \"EDIT USER\")
    :create (POST \"/users/\" [& params] \"CREATE NEW USER\")
    :update (POST \"/users/:id\" [id & params] \"UPDATE USER\")
    :delete (POST \"/users/:id\" [id] \"DELETE USER\"))

  named urls can be retrieved using the `url-for' function:
  (url-for 'users :show \"my-user-id\")
  ; => [:get \"/users/my-user-id\"]
  "
  [name & keyroutes]
  `(def ~name (namedroutes ~@keyroutes)))


