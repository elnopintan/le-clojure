(defproject LanguageEx "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.2.0"]
[org.clojure/clojure-contrib "1.2.0"]
[compojure "0.6.5"]
[hiccup "0.3.6"]
[congomongo "0.1.5-SNAPSHOT"]
                 ]
  :dev-dependencies [
        [lein-eclipse "1.0.0"]
        [lein-ring "0.4.6"]
  ]
  
  :ring {:handler languageexperiment.core/app}
  )
