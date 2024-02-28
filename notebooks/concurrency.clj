(ns concurrency)

;; # Concurrency experiments
;; ## Agents
;; I know of agents from Elixir, I do miss some things but this seems too work nicely
(defonce state (agent {:some "state"}))

state

@state

(send state #(conj %1 {:something "else"}))

@state
;; for IO
(send-off state #(conj % {:another "thing"}))

(. state state)
