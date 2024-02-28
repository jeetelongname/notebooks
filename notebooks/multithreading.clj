;; # Functional Programming and Multithreading
(ns multithreading
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]))
;; This is a follow up to last session, as a reminder. we discussed what
;; functional programming is. how we apply it and what it may be used for
;;
;; some of the tenants that we discussed include:
;; ### Code is made up of functions.

(defn function [x y z]
  [x y z])

(function 1 2 3)
;; ### Data is a separate entity not linked to code
;; These are not classes. where
;; all of the data and operations on that data are bundled into objects

(def example {:x 1, :y 2, :z 3})

^{::clerk/visibility {:code :fold}}
(clerk/example
 (get example :x))

;; ### Data cannot be mutated in place.
;; In clojure this manifests itself through
;;  the fact that there are no operations to mutate things. Instead by default
;;  copies are made

;;  If I engage with the lie, we can see how a java array uses the same ID

^{::clerk/visibility {:code :fold}}
(let [a (make-array Integer/TYPE 3)]
  (clerk/example
   (System/identityHashCode a)
   (aset a 0 1) ;; this does not really matter
   (System/identityHashCode a)))

;; but with a clojure data structure. the processing of updating returns a new
;; value
^{::clerk/visibility {:code :fold}}
(clerk/example
 (System/identityHashCode example)
 (System/identityHashCode (assoc example :x 2)))

;;  ### Side effects are controlled and are not allowed in normal code.
;;
;; The way this will manafest for us is a little disipline and a virtue of the
;; last tennent. In clojure we *can* perform a lot of side effects in code, such
;; as reading files and writing to disk. But the big ones. mutating objects in
;; place, is covered for us and will let our code be "pure" more often than not!

;; # Recap over time for project!!

;; In this project we will be tackling a very simple problem
;; adding up a very big list of numbers

^{::clerk/visibility {:result :hide}}
(def n 1000000000)

^{::clerk/visibility {:code :hide}}
(def range-n (range n))

;; We want to get the sum of this. we will forget that we can do this in
;; constant time using this equation
;;

^{::clerk/visibility {:code :hide}}
(clerk/tex  "\\sum_{r=1}^n r = \\frac{n (n + 1)}{2}")

^{::clerk/visibility {:code :hide}}
(/ (* n (+ n 1)) 2)

;; This will compute in this time
^{::clerk/visibility {:code :hide}}
(with-out-str
  (time
   (/ (* n (+ n 1)) 2)))

;; if we were to do this in a more traditional way

^{::clerk/visibility {:code :hide}}
(clerk/code "(reduce + range-n)")

^{::clerk/visibility {:code :hide :result :hide}}
(def timed (future
             (with-out-str
               (time
                (reduce + range-n)))))

^{::clerk/visibility {:code :hide}}
@timed

;; we see how slow this is.
;; This is where we need multithreading!!

;; ## Functional programming and multithreading
