(ns languageexperiment.templates
  (:use 
        hiccup.core,hiccup.form-helpers, hiccup.page-helpers
        ring.adapter.jetty, ring.middleware.params, ring.middleware.session, ring.middleware.file-info,ring.middleware.file) 
  (use languageexperiment.data)
   )

(def *my-session* {})

(defn include-all-js []
  (html
   (include-js "/jquery.min.js")
   (include-js "/javascript.js")
   (include-js "/jquery.form.js")
   (include-js "/code_highlighter.js")
   
   (include-css "/stylesheet.css"))
  )

(defn drawmenu []
  (if 
    (*my-session* :login)  
	  [:div.menu
	    [:a.menuitem {:href "/userlist"} "Usuarios"]
	    [:a.menuitem {:href "/friendlist"} "Amigos"]
	    [:a.menuitem {:href "/enemylist"} "Enemigos"]
	    [:a.menuitem {:href (str "/user/" (*my-session* :login)) } "Muro"]
     [:a.menuitem {:href (str "/user/" (*my-session* :login) "/feed") } "Comentarios"]
	    [:a.menuitem {:href (str "/logout") } "Log out"]
	    ]
    [:div.menu
      [:a.menuitem {:href "/login"} "Login"]
	    [:a.menuitem {:href "/user"} "Registrar"]
      ]
  )
  )

(defn mainpage [& innercode]
  (html
    [:head (include-all-js)]
    [:body  
     [:div.header "Good Old Lisp Club"]
      (drawmenu)
     [:div.main innercode]
     ]
    )
  )

(defn showerror [message]
	  (mainpage
	    [:div.errormessage message]
	  )
  )


(defn only-friends [f id & params]
  (let [ 
        login  (*my-session* :login)
        user (get-user id)
           {friends :friends}  user
           friends (set friends)]
    (if (or (= id login) (friends login ))
                   (apply f id params)
                   (showerror "You shall not pass!!"))))

(defprotocol IUser
  (get-nick [this] "returns its nick")
  (comment-tag [this]  "Get tag for comment")
  (smiley [this] "Return a picture or nil if there is none")
  )



(deftype Nobody  [name]
  IUser
  (get-nick [this] name)
  (comment-tag [this] :comment)
  (smiley [this] nil))

(deftype  Friend  [name]
  IUser
  (get-nick [this] name)
  (comment-tag [this] :friend)
  (smiley [this] "/green.png" ))


(deftype  Enemy  [name]
  IUser
  (get-nick [this] name)
  (comment-tag [this] :enemy)
  (smiley [this] "/red.png" ))
  
(defn who-is [user]
  (let [{friends :friends enemies :enemies } (get-user (*my-session* :login))]
    (cond
      ((set friends) user) (Friend. user)
      ((set enemies) user)  (Enemy. user)
      :else (Nobody. user)
      )))
