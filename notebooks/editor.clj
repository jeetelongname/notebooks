(ns editor
  {:nextjournal.clerk/visibility {:code :hide}
   :nextjournal.clerk/doc-css-class [:overflow-hidden :p-0]}
  (:require [nextjournal.clerk :as clerk]))

;; Does not work
^{::clerk/visibility {:code :hide :result :hide}}
(clerk/with-viewer
  {:render-fn 'nextjournal.clerk.render.editor
   :transform-fn clerk/mark-presented}
  (slurp "notebooks/rule_30.clj"))

(clerk/notebook  "notebooks/rule_30.clj")
(clerk/code (macroexpand
             '(ns foo "A great ns"
                  (:require [clojure.string :as str])
                  (:import [java.net URL]))))
