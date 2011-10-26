(ns languageexperiment.core
   (:use compojure.core)
   (:use languageexperiment.usermgm)
   (:use languageexperiment.templates)
   (:use compojure.core somnium.congomongo
        hiccup.core,hiccup.form-helpers, hiccup.page-helpers
        ring.adapter.jetty, ring.middleware.params, ring.middleware.session, ring.middleware.file-info,ring.middleware.file)  
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:use [languageexperiment.wall :only (show-wall save-snippet add-comment)])
  (:use [languageexperiment.feed :only (show-user-comments do-comment)])
  (use [ring.util.response :only (redirect)])
  )

(defmacro show-page [ body]
           `(html [:header ]
                  [:body ~body]))
           


(defmacro with-session [session & body]
  `(binding [*my-session* ~session]
       ~@body ))

(defn auth [session compojuremap]
  (with-session session
	  (if
	    (:login session)
	    (compojuremap)
	    (redirect (str "/login"))
	    )
	  )
  )

(defroutes main-routes
 (GET "/" {session :session, params :params} (showlogin))
 
 ;Wall routes
 (GET "/user/:id" {session :session, {id :id search :search } :params} (auth session #(show-wall id search)))
 (POST"/user/:id/snippet"  {session :session, {id :id snippet :snippet tags :tags } :params }  (auth session #(save-snippet id snippet tags)))
 (POST  "/snippet/:id" {session :session, {id :id comment :comment } :params} (auth session #(add-comment id comment)))
 
 ;Friend mgmt routes
 (GET  "/addfriend" {session :session, params :params} (auth session #(addfriend params)))
 (GET  "/addenemy" {session :session, params :params} (auth session #(addenemy params)))
 (GET  "/leaveenemy" {session :session, params :params} (auth session #(leaveenemy params)))
 (GET  "/leavefriend" {session :session, params :params} (auth session #(leavefriend params)))
 
 ;User mgmt routes 
 (GET  "/logout" {session :session, params :params} (logout))
 (GET  "/login" {session :session, params :params} (showlogin))
 (POST "/login" {session :session, params :params} (registeruser (createlogin params))) 
 (GET "/user" {session :session, params :params} (showuserform))
 (POST "/user" {session :session, params :params} (saveuser (createuser params)))
 (GET "/userlist"  {session :session, params :params} (auth session #(showuserlist (:filtro params) "/userlist" nil)))
 (GET "/friendlist"  {session :session, params :params} (auth session #(showfriends (:filtro params))))
 (GET "/enemylist"  {session :session, params :params} (auth session #(showenemies (:filtro params))))
 ;feeds
 (GET "/user/:id/feed"  {session :session, {id :id} :params} (auth session #(show-user-comments id )))
 (POST "/comment"  {session :session, { text :comment scope :scope } :params}  (auth session #(do-comment text scope)))
 
 
  (route/resources "/")
  (route/not-found "Page not found"))

(wrap! main-routes :session)

(def app
  (handler/site main-routes )
  )


(defonce server (run-jetty app
                           {:join? false
                            :port 8080}))
