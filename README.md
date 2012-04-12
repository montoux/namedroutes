# namedroutes

namedroutes is a proof-of-concept implementation of named routes for [Compojure](https://github.com/weavejester/compojure "Compojure on Github").

## Introduction

Compojure provides an excellent routing mechanism for web applications, routing incoming requests to specific handlers based on the request url, method and parameters.

In your web application, you'll frequently want to provide links to parts of your application. Compojure doesn't provide a mechanism to generate a url for a *named route* such as used in [Ruby on Rails](http://rubyonrails.org "The Ruby on Rails website") or [Noir](http://webnoir.org "The Noir website"), another Clojure web framework.

namedroutes implements named routes for Compojure.

## Usage

	(ns example
	  (:use [compojure.core]
	        [namedroutes.routes]))

	(defnamedroutes users
	  :index  (GET  "/users"          []            "show all users")
	  :new    (GET  "/users/new"      []            "show form for new user")
	  :show   (GET  "/users/:id"      [id]          "show a user")
	  :edit   (GET  "/users/:id/edit" [id]          "show form to edit a user")
	  :create (POST "/users"          [& params]    "create a user from params")
	  :update (PUT  "/users/:id"      [id & params] "update a user from params")
	  :delete (DELETE "/users/:id"    [id]          "delete a user"))

	(defnamedroutes wildcard-routes
	  :foo    (POST "/foo/:bar/:baz/*"  [bar baz * & params] "Hello, world!"))
	
	
	(url-for users :edit 123)  ; => [:get "/users/123/edit"]
	(url-for users :delete 123); => [:delete "/users/123"]
	
	(url-for wildcard-routes :foo "one" "two" "three/four")
	; => [:post "/foo/one/two/three/four"]


## License

Copyright © 2012 Montoux Limited

Distributed under the Eclipse Public License, the same as Clojure.
