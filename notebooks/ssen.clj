(ns ssen
  (:require [nextjournal.clerk :as clerk]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.math :as math]))

;; Here we process the major Bulk supply points for each city checking the amps out from them,
;; we sum up the line currents to get the amps out use the formula
;; $ Watts = \sqrt{3} \cdot V \cdot A$
;; where $ V = 33,000 $ and $ A $ is the summed line current at a time point
;; 
^{::clerk/visibility {:code :hide :result :hide}}
(def auth-key "add ur own innit")

(defn fetch-station
  [station]
  (-> (str  "https://nerda.hackathons.dev/api/ApiNerdaStatic?substation=" station)
      (client/get 
       {:headers {"Authorization" (str "Bearer " auth-key)} 
        :debug true})
      :body
      json/read-str))

(defn fetch-measure [id]
  (-> (str "https://nerda.hackathons.dev/api/ApiNerdaAfter?measurement=" id "&after=2025-11-15T00:00:00.000Z")
      (client/get
       {:headers {"Authorization" (str "Bearer " auth-key)} 
        :throw-exceptions false
        :debug true})
      :body
      json/read-str)
  )

(defn get-data
  "Get the data for a station innit"
  [station]
  (->> (fetch-station station)
       first
       (#(get % "lines"))
       (map #(get-in % ["measurements"]))
       (map #(filter (fn [m] (= "LineCurrent" (get m "measurementType"))) %))
       (filter (comp not empty?))
       (map (comp #(get % "nerda_measurement_id") first))
       (map fetch-measure)))

     
^{::clerk/visibility {:code :hide :result :hide}}
(def ports-bps ["faeb353a-94e5-47e5-94dc-92450071434a", "2d2348b1-54b7-4a97-a404-193815efbf87",])
^{::clerk/visibility {:code :hide :result :hide}}
(def soton-bps ["dd2fd307-e821-419a-88ae-217b1a0df217", "c3048094-3e59-4ce3-8010-254104263ad6", "c9d390e3-a3c6-40f6-a5d2-6d5885a066e7",])

^{::clerk/visibility {:code :hide :result :hide}}
(def ports-fetched  (map get-data ports-bps))
^{::clerk/visibility {:code :hide :result :hide}}
(def soton-fetched (map get-data soton-bps))

^{::clerk/visibility {:code :hide :result :hide}}
(def map2 #(map (fn [l2] (map %1 l2)) %2) )

(defn wattage-time [data]
  (apply
   map
   (fn [& args]
     {:timestamp (get (first args) "__ts")
      :value (* (math/sqrt 3)
                33000
                (apply + (map #(get % "value" ) args)))})
   data))

(defn process-data [lst]
  (->> lst
       (map2 (fn [m] (-> m
                         (get-in ["AnalogValues" 0])
                         (select-keys ["value_history"]))))
       flatten
       (map #(get % "value_history"))
       wattage-time))

^{::clerk/visibility {:code :hide :result :hide}}
(def ports-processed
 (process-data ports-fetched))

^{::clerk/visibility {:code :hide :result :hide}}
(def soton-processed
 (process-data soton-fetched))

(clerk/table ports-processed)
(clerk/table soton-processed)

^{::clerk/visibility {:code :hide :result :show}}
(defn to-plot [processed name]
  {:x (map :timestamp processed)
   :y (map :value processed)
   :type "line"
   :name name})

(clerk/md "# Behold Graph")

^{::clerk/visibility {:code :hide :result :show}}
(clerk/plotly
 {:data [(to-plot ports-processed "Portsmouth")
         (to-plot soton-processed "Scumhampton")]
  :layout {:margin {:l 50 :r 0 :b 50 :t 50}
           :paper_bgcolor "transparent"
           :plot_bgcolor "transparent"}
  :config {:displayModeBar false
           :displayLogo false}})

