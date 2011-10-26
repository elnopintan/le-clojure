(ns languageexperiment.feed
   (use hiccup.core)
  (use [ hiccup.page-helpers :only (unordered-list)])
  (use [ hiccup.form-helpers ])
   (use [languageexperiment.data ])
    (use [languageexperiment.templates])
    (use [somnium.congomongo])
    (use [ring.util.response :only (redirect)])

  )


(defn get-comments [scope]
  (with-mongo conn
    (fetch :feed :where { :scope scope})))
  
(defn get-all-comments [user]
  (concat 
    (get-comments user)
    (get-comments "all")))


  (defn show-user-comment [{text :text user :user}]
    [:div {:class (comment-tag (who-is user)) }
     text
     [:div.user user]])
  
(defn form-comment [user ] 
  (form-to [:post "/comment"]
            (text-area "comment")
            (drop-down "scope"  (conj (get-users)  "all"))
            (submit-button "Enviar")
            ))


(defn show-user-comments [user]
  (mainpage
        (let [feeds (get-all-comments user)]
          (conj
            [:div#feeds.paragraph ]
            (map  show-user-comment feeds)
           (form-comment user)))))
   

(defn do-comment [text scope]

  (let [ { user :login  } *my-session*] 
    (do
  (with-mongo conn
    (insert! :feed { :user user :text text :scope scope} ))
  (redirect (str "/user/"  user "/feed")))))
  