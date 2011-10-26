(ns languageexperiment.wall
  (use hiccup.core)
  (use [ hiccup.page-helpers :only (unordered-list)])
  (use [ hiccup.form-helpers ])
  (use [clojure.string :only (split)])
  (use [languageexperiment.data ])
  (use [ring.util.response :only (redirect)])
  (use [somnium.congomongo])
  (use [languageexperiment.templates])
  (import java.util.UUID )
  )

(defn get-snippets [user] 
 (with-mongo conn
    (fetch :snippets :where {:user user})))
            
(defn get-snippet [id] 
 (with-mongo conn
    (fetch-one :snippets :where {:id id})))
 


(defn comment-snippet [id]
  (form-to {:class "form-comments"} [:post (str "/snippet/" id)]
           (text-area {:cols "70"} "comment" "")[:br ]
           (submit-button "Comentar")))

(defn add-comment [id a-comment]
  (let [ { comments :comments  user :user  :as snippet} (get-snippet id)
              comments (if (not comments) [] comments)
              {a-name  :login } *my-session* ]
  (do
       (with-mongo conn
         (update! :snippets snippet (assoc snippet :comments (conj comments { :user a-name  :comment a-comment}))))
      (redirect (str "/user/" user)))))      


(defn show-comment [ {text :comment user :user}]
  [:div.comment
   [:div.user user]
   [:div.textcomment text]
   ])


(defn show-snippet [{ text :text  tags  :tags  lang :lang id :id comments :comments}]
               [:div.snippet 
                [:div.snippet-code
                [:code.clojure { :class  lang }  text  ]]
                [:div.tag-title "Tags" ]
                [:div.tag (apply str (interpose ", " tags))]
                [:div.comments 
                 [:div.comments-title "Comentarios"]
                (conj [:div]
                (map  show-comment comments)
                (comment-snippet id))]
                ])
        
(defn snippet-form [user]
     [:div.new-snippet 
              (form-to {:class "form-snippets"} [:post (str "/user/" user "/snippet")] 
                       [:div.newsnippet-title "Crear Snippet"]
                       (text-area {:cols "80"} "snippet")[:br]
                       [:div.tag-title "Tags" ]
                       (text-field "tags")[:br]
                       (submit-button "Crear nuevo Snippet de código"))])

  
(defn find-tag [ search  {tags :tags} ]
  (not (empty? (filter #(do-filter search % ) tags))))
  
(defn do-show-wall  [user search ] 
  "Show wall of snippets"  
    (let [ {loged-name :login } *my-session*  ]
      (mainpage 
      (conj 
      [:div.paragraph
       [:h1 (str "Snippets de " user)] ]
     (form-to {:id "filter"} [:get (str "/user/" user) ]
	         [:div.filtro "Filtro: " (text-field :search)]
	         (submit-button "Filtrar")
	         )
        (let  [ snippets (get-snippets user)
                  snippets  (if search (filter #(find-tag search % ) snippets ) snippets)
                  ] 
         (map show-snippet snippets))
       (if (= loged-name user)
         (snippet-form user))
       ))))

(defn show-wall  [user search] 
  "Show wall of snippets"  
 
    (only-friends do-show-wall user search)
    )
                       


(defn save-snippet [user snippet tags]
 (let [tags (set (split tags #" +")) ]
   (do 
    (with-mongo conn 
      (insert!  :snippets {:user user :text snippet :tags tags
                                        :id (str (UUID/randomUUID)) }))
   (redirect (str "/user/" user)))))

   