# namedroutes

namedroutes is a proof-of-concept implementation of named routes for [Compojure](https://github.com/weavejester/compojure "Compojure on Github").

## Introduction

Compojure provides an excellent routing mechanism for web applications, routing incoming requests to specific handlers based on the request url, method and parameters.

In your web application, you'll frequently want to provide links to parts of your application. Compojure doesn't provide a mechanism to generate a url for a *named route* such as used in [Ruby on Rails](http://rubyonrails.org "The Ruby on Rails website") or [Noir](http://webnoir.org "The Noir website"), another Clojure web framework.

namedroutes implements named routes for Compojure.

## Usage

It's easiest to demonstrate this project's functionality with an example:

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

## API

### defnamedroutes
Like Compojure's `defroutes`, but creates named routes. Example:

	(defnamedroutes users
	  :index  (GET "/users" [] "SHOW INDEX")
	  :new    (GET "/users/new" [] "NEW USER FORM")
	  :show   (GET "/users/:id" [id] "SHOW USER")
	  :edit   (GET "/users/:id/edit" [id] "EDIT USER")
	  :create (POST "/users/" [& params] "CREATE NEW USER")
	  :update (POST "/users/:id" [id & params] "UPDATE USER")
	  :delete (POST "/users/:id" [id] "DELETE USER"))

named urls can be retrieved using the `url-for' function:

	(url-for 'users :show "my-user-id")
	; => [:get "/users/my-user-id"]
	
### namedroutes
Like Compojure's `routes`, but creates named routes. Example:

	(def x (namedroutes :foo (GET "/my/foo/:id" [id] "foo!")))
	;=> ~'user/x

### url-for

Returns a pair *[method url-string]* with the url specified by *routes*, *action*
and any arguments the url needs. *routes* must be defined
with `defnamedroutes`, which stores the url-resolving functions
in it's meta.

*routes* should be a routes datastructure as defined
by `defnamedroutes`, a `var` bound to named routes, or a fully qualified `symbol` that
can be resolved to a var bound to named routes.

 Example:

	(defnamedroutes users
	  :index (GET "/users" [] (...))
	  :edit (GET "/users/:id/edit [id] (...))
	  :update (POST "/users/:id" [id & params] (...)))
	
	(url-for users :edit 3456)   ; => [:get "/users/3456/edit"]
	(url-for users :update 3456) ; => [:post "/users/3456"]

or, if you want to look up named routes without depending on the
namespace that defined it (to avoid circular dependencies):

	(url-for 'my-ns/users :edit 3456) ; => [:get "/users/3456/edit"] ;



## Limitations

Currently, namedroutes does not support Compojure's `context` (i.e. *nested routes*) macro, and it does not support routes combined with compojure's `routes`.

## License

Copyright © 2012 Montoux Limited

Distributed under the Eclipse Public License, the same as Clojure.
