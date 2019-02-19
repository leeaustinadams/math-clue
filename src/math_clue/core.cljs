(ns math-clue.core
  (:require [cljs.core.async :refer [<! chan put!]]
            [math-clue.components :refer [render-game]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce game-channel (chan))

(def container (.getElementById js/document "app"))

(defonce app-state (atom {:game {:questions []}}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (render-game (:game @app-state) container game-channel))

(defn init-game
  "Initialize the game state"
  [state]
  (-> state
      (assoc-in [:game :questions] (vec (repeatedly 10 (fn [] {:a (inc (rand-int 10)) :b (inc (rand-int 10)) :answer nil :correct? nil}))))))

(defn check-answer
  "Checks an answer"
  [{:keys [a b answer] :as question}]
  (assoc-in question [:correct?] (= answer (+ a b))))

(defn check-answers
  "Checks the answers"
  [state {:keys [idx question answer] :as value}]
  (let [a (:a question)
        b (:b question)]
    (-> state
        (assoc-in [:game :questions idx :answer] answer)
        (assoc-in [:game :questions idx :correct?] (= answer (+ a b))))))

(defn check-win
  "Check to see if the player got them all correct"
  [state]
  (println "checking win")
  (every? identity (map :correct? (get-in state [:game :questions]))))

(defonce run-once
  (go
    (swap! app-state init-game)
    (render-game (:game @app-state) container game-channel)
    (while true
      (let [game (:game @app-state)]
        (let [message (<! game-channel)
              new-state (condp = (:type message)
                          :answer (swap! app-state check-answers (:value message))
                          :reset (swap! app-state init-game)
                          app-state)]
          (render-game (:game new-state) container game-channel)
          (when (check-win new-state) (set! (.-location js/document) "win.html")))))))
