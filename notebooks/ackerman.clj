(ns ackerman
  (:require [clojure.core.match :refer [match]]))

(defn ackerman [x y]
  (match [x y]
    [0 y] (inc y)
    [x 0] (ackerman (dec x) 1)
    [x y] (ackerman (dec x) (ackerman x (dec y)))))

(ackerman 1 2)
(ackerman 2 1)
(ackerman 3 8)
