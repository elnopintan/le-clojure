(ns languageexperiment.data
 (use  somnium.congomongo))


(def conn (make-connection "languageexperiment"  
                                :host "127.0.0.1"  
                                :port 27017))


; Metodos de acceso a usuarios
;------------------------------------------------------------------------------------------
(defn saveuser-db [usuario] 
    (with-mongo conn 
    (insert! :users usuario))
  )

(defn get-user [nick] 
  (with-mongo conn 
    (fetch-one :users :where {:nickname nick})))

(defn get-users [] 
  (map :nickname
  (with-mongo conn 
    (fetch :users ))))


; Metodos de acceso a amigos
;------------------------------------------------------------------------------------------
(defn addfriend-db [usuario amigo]
  (let [{amigos :friends} usuario
	        amigos (if 
	                 amigos 
	                 amigos
	                 #{})
	        ]
		  (with-mongo conn
		    (update! :users usuario (assoc usuario :friends (conj amigos amigo))
		    )
	    )
	  )
  )

(defn removefriend-db [usuario amigo]
  (let [{amigos :friends} usuario
	        amigos (if 
	                 amigos 
	                 (set amigos)
	                 #{})
	        ]
		  (with-mongo conn
		    (update! :users usuario (assoc usuario :friends (disj amigos amigo))
		    )
	    )
	  )
  )

(defn addenemy-db [usuario enemigo]
  (let [{enemigos :enemies} usuario
	        enemigos (if 
	                 enemigos 
	                 enemigos
	                 #{})
	        ]
		  (with-mongo conn
		    (update! :users usuario (assoc usuario :enemies (conj enemigos enemigo))
		    )
	    )
	  )
  )

(defn removeenemy-db [usuario enemigo]
  (let [{enemigos :enemies} usuario
	        enemigos (if 
	                 enemigos 
	                 (set enemigos)
	                 #{})
	        ]
		  (with-mongo conn
		    (update! :users usuario (assoc usuario :enemies (disj enemigos enemigo))
		    )
	    )
	  )
  )


(defn do-filter [filtro cadena]
  (re-matches (re-pattern (str ".*" filtro ".*")) cadena)
  )

; Métodos para tratar con las notificaciones
;------------------------------------------------------------------------------------------
(defn add-notification [user-nickname noti-type noti-content]
    (with-mongo conn 
       (insert! :notifications {:user user-nickname :type noti-type :content noti-content}))
  )

(defn get-notifications [from]
    (with-mongo conn  (fetch :notifications))
  )

