;; # 🌿 Playing with genetic algorithms
(ns genetic
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]))

;; Hello gamers today we are playing with genetic algorithms,
;; ## 🐅 Our Genome
;; Our genome is pretty simple, It is a vector of vectors where each vector inside the vector is a symbol
;; the symbol is up down, left or right to signifiy direction

(def allile-generator #(get [:up :down :left :right] (rand-int 4)))

(defn generate-chromosome
  "Generate a vec of length 32"
  []
  (vec (repeatedly 32 allile-generator)))

(def pop-size 100)

(defn generate-genome
  "Generate a genome, takes an pop-size: int and returns a [[symbol]]"
  [pop-size]
  (vec (take pop-size (repeatedly generate-chromosome))))

(def genome (generate-genome pop-size))
;; Each row is an individual.
^{::clerk/visibility {:code :hide}} (clerk/table genome)

;; ## 🏋 Calculating Fitness
;; My solution to grade fitness is three fold, run, grade then do that for everyone.

;; ### 🤓 Grade function

;; This function will take in a position, indiviual and its acumulated points so
;; far. then adapt the grade. Instead of ending a run if an individual goes out
;; of bound I decidied instead to penalise them, The highest score wins, the
;; individual who also has the most moves left over also gets those added to
;; there score becuase why not
(defn grade-indiviual
  "Return grade or false if you need more info
  highest score wins
  "
  [pos individual points]
  (let [out-of-bounds? (let [{x :x y :y} pos]
                         (or (< x 0)
                             (> x 8)
                             (< y 0)
                             (> y 8)))
        points (+ points (if out-of-bounds? 0 5))
        steps-left (count individual)]
    (if (= pos {:x 8 :y 8})             ; we have reached the finish point
      {:continue? false
       :points (+ points steps-left)
       :finished? true}
      {:continue? (not (zero? steps-left))
       :points (- points (if (zero? steps-left) 30 0)) ; if we have no steps left and still did not reach the end, deduct a final 30 points
       :finished? false})))

;; ### 🏃 Fitness function

;; In my fitness function I do one move and then submit the indiviual for
;; regrading, Instead of having a physical board I just track coordinates. The
;; grade function tells us mostly how to do things, if its happy we stop, if not
;; we keep going. NOTE: for non clojurans, `recur` is where we go into the next
;; loop, think of a recursive function. `loop` emulates that
(defn fitness [ind]
  (loop [indiviual ind
         position {:x 0 :y 0}
         points 50]
    ;; each indiviual gets 50 points before they begin
    (let [[action & rest] indiviual
          position (case action
                     :left (update position :x dec)
                     :right (update position :x inc)
                     :up (update position :y inc)
                     :down (update position :y dec))
          {continue? :continue? grade :points finished :finished?} (grade-indiviual position rest points)]
      (if continue?
        (recur rest position grade)
        {:individual ind :grade grade :finished? finished}))))

;; ### 🤝 Bringing it all together

;; Now finally we can take the genome, apply fitness too all of the individuals
;; and then sort by grade, in accending order
(def stage-1 (->> (generate-genome 10)
                  (map fitness)
                  (sort-by :grade >)))
;;  As we can see, our little bots are not doing okay

^{::clerk/visibility {:code :hide}} (clerk/table stage-1)

;; ## 😚 Breeding

;; We have tested them but now we need too actually evolve them

;; ### selection

(defn select [evaluation]
  (let [total-fitness (transduce (map :grade) + evaluation)
        normalised (map (fn [m] (update m :grade #(/ %1 total-fitness))) evaluation)
        cum-fitness (reductions + (map :grade normalised))
        pop-size (count evaluation)]
    [cum-fitness normalised]))

(rationalize 1.0)

^{::clerk/visibility {:code :fold}
  ::clerk/auto-expand-results? false}
(select (->> (generate-genome 10)
             (map fitness)
             (sort-by :grade >)))
;; ### ⚔ Crossover

;; Too cross them over we need too take the top 50%, pair them off, split them
;; in twain and then put the two halfs together

(defn vec-split [vec]
  [(subvec vec 0 16) (subvec vec 16)])

(defn crossover
  [ind1 ind2]
  (let [[ind1-f ind1-l] (vec-split ind1)
        [ind2-f ind2-l] (vec-split ind2)]
    [(vec (concat ind1-f ind2-l)) (vec (concat ind2-f ind1-l))]))

(clerk/table (crossover (vec (repeat 32 :up))
                        (vec (repeat 32 :down))))

;; ### 🤢 Mutation
;; (Nauseated is the closest I have to zombie)
;; Muation takes random indexes and changes them randomly.
;; over the 32 alliels we will select 6 for the mutation
(defn mutate [individual]
  (let [mutation-value 3
        new-vals (repeatedly mutation-value allile-generator)
        ;; horrific, but it works, and works like really well
        indexes (loop [index-set (set (take mutation-value (repeatedly #(rand-int 32))))]
                  (if (= mutation-value (count index-set))
                    index-set
                    (recur (conj index-set (rand-int 32)))))]
    ;; go through
    (reduce (fn [acc [index to-set]]
              (assoc-in acc [index] to-set)) individual
            (map vector indexes new-vals))))

^{::clerk/auto-expand-results? true} (mutate (vec (repeat 32 :example)))

;; ## 💃🏿 The final solution

;; The final solution is just to take all of these and then combine it.
;; something like this, we would probably do this in the loop where we actually
;; do some checking. This is just pseudocode but we will look at the actual implementation now
;; ```clojure
;; (->> genome
;;      (map fitness)
;;      (sort :grade >)
;;      (take top 50%)
;;      (crossover)
;;      (mutate)
;;      (append)
;;      (recur))
;; ```

;; Our actual solution will need to loop over a much larger population and

(defn happy?
  "Check if we are happy the amount f"
  [_population gen]
  (= gen 100))

(defn final-solution []
  (let [pop-size 1000]
    (loop [population (generate-genome pop-size)
           generation-num 0]
      (let [evaluation (->> population
                            (map fitness)
                            (sort-by :grade >))]
        (if (happy? evaluation generation-num)
          {:finished evaluation}
          (let [selected (into [] (map :individual) (take (/ (count evaluation) 2) evaluation))
                crossed-over (->> selected
                                  (partition-all 2)
                                  (map (partial apply crossover))
                                  (apply concat))
                tng (->> (into [] cat [crossed-over selected])
                         (map mutate))]
            (recur tng (inc generation-num))))))))

^{::clerk/visibility {:code :hide}} (clerk/table (:finished (final-solution)))
