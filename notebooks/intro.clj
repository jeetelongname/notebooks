;;  # this is a test of clerk
(ns intro
  (:require [nextjournal.clerk :as clerk]))

(map (partial * 10) (range 0 10))

(clerk/table {:column (range 1 10)
              :column2 (range 1 10)})

