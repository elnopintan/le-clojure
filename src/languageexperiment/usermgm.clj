(ns languageexperiment.usermgm
  (:use 
        compojure.core somnium.congomongo
        hiccup.core,hiccup.form-helpers, hiccup.page-helpers
        ring.adapter.jetty, ring.middleware.params, ring.middleware.session, ring.middleware.file-info,ring.middleware.file)  
  (use languageexperiment.data )
  (use [ring.util.response :only (redirect)])
  (use  [languageexperiment.templates ])
   (import languageexperiment.templates.Friend)
 (import languageexperiment.templates.Enemy)
 (import languageexperiment.templates.Nobody)

  )


; Metodos logica negocios
;------------------------------------------------------------------------------------------
(defn saveuser [usuario]
  (saveuser-db usuario)
  (mainpage 
         [:div {:class "message"}
      "Usuario creado"
      ]
    )
  )

(defn registeruser [logindata]
	  (let [user  (with-mongo conn (fetch-one :users :where {:nickname (:login logindata)}))]
	    (if 
	      (and user (= (:pass logindata) (:pass user)))
        (assoc (redirect (str "/user/" (:nickname user))) :session (conj *my-session* {:login (:login logindata)}))
	      (showerror "You Shall not pass!!")
	     )
	   )
  )

(defn logout []
  (assoc (redirect (str "/login")) :session (dissoc :login))
  )

(defn addfriend [params]
  (addfriend-db (get-user (*my-session* :login)) (:friend params))
  (html
    "Amigo añadido"
    )
  )

(defn addenemy [params]
  (addenemy-db (get-user (*my-session* :login)) (:enemy params))
  (html
    "Enemigo añadido"
    ) 
  )

(defn leavefriend [params]
  (removefriend-db (get-user (*my-session* :login)) (:friend params))
  (html
    "Amigo borrado"
    )
  )

(defn leaveenemy [params]
  (removeenemy-db (get-user (*my-session* :login)) (:enemy params))
  (html
    "Enemigo borrado"
    )
  )

; Metodos de extraccion de parametros del params
;-----------------------------------------------------------------------------------------
(defn createlogin [params]
  {:login (:nickname params),
   :pass (:pass params)
   }  
  )

(defn createuser [params]
  {:nickname (:nickname params),
   :nombre (:nombre params),
   :apellidos (:apellidos params),
   :email (:email params),
   :pass (:pass params)
   }
  )

; Metodos de renderizado de HTML
;------------------------------------------------------------------------------------------
(defn showlogin []
  (mainpage 
          (form-to {:id "loginform"} [:post "/login" ]
               [:div {:class "paragraphtitle"} "Log in"]
               [:div {:class "paragraph"} 
                "Nombre de usuario" [:br]
                [:div.input (text-field :nickname)[:br]]                
                "Password" [:br]
                [:div.input (password-field :pass)[:br]]
                (submit-button "Log In")
                ]
               )
    )
  )

(defn showuserform [] 
    (mainpage
      (form-to {:id "userform"} [:post "/user" ]
               [:div {:class "paragraphtitle"} "Datos de usuario"]
               [:div {:class "paragraph"} 
                "Nickname" [:br]
                [:div.input (text-field :nickname)[:br]]
                "Nombre" [:br]
                [:div.input (text-field :nombre)[:br]]
                "Apellidos" [:br]
                [:div.input (text-field :apellidos)[:br]]
                "eMail" [:br]
                [:div.input (text-field :email)[:br]]                
                "Password" [:br]
                [:div.input (password-field :pass)[:br]]
                (submit-button "Crear")
                ]
               )
      )
	)


(defprotocol IUserMgmView 
  (show-view [this])
  )

 (extend-type Friend
  IUserMgmView
  (show-view [this]  (html 
        [:img {:id (.name this)  :class "indicadoramistad" :src "dedoarriba.png"}]
        [:div.indicadores 
         [:span.leavefriend {:id (.name this)} "Dejar de ser amigo"]
         [:span.addenemy {:id (.name this)}  "Añadir a mis enemigos"]
         ])))  

(extend-type  Nobody
  IUserMgmView
  (show-view [this]    (html 
          [:img.friend {:id (.name this) :class "indicadoramistad"  :src "dedoabajo.png"}]
          [:div.indicadores
           [:span.addfriend {:id (.name this)} "Añadir a mis amigos"]
           [:span.addenemy{:id (.name this)}  "Añadir a mis enemigos"]
           ]
          )))

(extend-type  Enemy
  IUserMgmView
  (show-view [this]  (html 
          [:img {:id  (.name this) :class "indicadoramistad"  :src "dedoenemigo.png"}]
          [:div.indicadores
           [:span.addfriend {:id  (.name this)} "Añadir a mis amigos"]
           [:span.leaveenemy {:id  (.name this)} "Dejar de ser enemigo"]
           ])))


(defn showuserdata [user] 
  (let [idusuario (:nickname user)
        typed-user (who-is idusuario)
        ]
  (html 
    [:div.nickname [:a {:href (str "/user/" (:nickname user))} (:nickname user)] ]
    [:div.email [:b "eMail: "] (:email user) ]
    [:div.nombre [:b "Nombre: "] (:nombre user) " " (:apellidos user) ]
   (show-view typed-user) 
           )
        )
      )


(defn showuserlist [filtro form-objetivo conjuntonicks]
  (mainpage 
    (html
     [:div.paragraphtitle "Listado de usuarios"]
     
     [:div.paragraph
	     (form-to {:id "filter"} [:get form-objetivo ]
	         [:div.filtro "Filtro: " [:br ] (text-field :filtro)]
	         (submit-button "Filtrar")
	         )
     
	     [:div.userlist
	     (let [usuarios (with-mongo conn  (fetch :users))
	           usuarioactual (get-user (*my-session* :login))
	           ]
	       
	         (map #(html 
	                 [:div {:class "userrow" :id (str "id_" (:nombre %1))} (showuserdata %1  )]
	                 )
	              
	              (filter #(and
	                          (or
		                          (do-filter filtro (str (:nickname %1)))
		                          (do-filter filtro (str (:nombre %1)))
		                          (do-filter filtro (str (:apellidos %1)))
		                          (do-filter filtro (str (:email %1)))
		                         )
                            (not (do-filter (str (:nickname usuarioactual)) (str (:nickname %1))))
                            (if conjuntonicks (conjuntonicks (str (:nickname %1))) true)
                          ) usuarios)
	         ) 
	       )
	     ]
     ]
    )
    )
  )

(defn showfriends [filtro]
  (showuserlist filtro "/friendlist" (set (:friends (get-user (*my-session* :login)))))
  )

(defn showenemies [filtro]
  (showuserlist filtro "/enemylist" (set (:enemies (get-user (*my-session* :login)))))
  )