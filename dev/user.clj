(ns dev.user
  (:require [nextjournal.clerk :as clerk]))

(comment
  (clerk/serve! {:browse? true
                 :watch-paths ["notebooks"]})
  (clerk/show! "notebooks/genetic.clj")
  (clerk/show! "notebooks/editor.clj")

  (clerk/build! { :paths ["notebooks/genetic.clj"]}))
