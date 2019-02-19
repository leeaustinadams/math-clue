(ns math-clue.components
  (:require [cljs.core.async :refer [put!]]
            [quiescent.core :as q]
            [quiescent.dom :as d]))

(q/defcomponent Answer [game idx {:keys [a b answer correct?] :as question} game-channel]
  (d/span {}
   (d/input {:className "answer"
             :onInput (fn [e]
                        (.preventDefault e)
                        (let [value (js/Number (.-value (.-target e)))]
                          (put! game-channel {:type :answer :value {:idx idx :question question :answer value}})))
             :defaultValue (or answer "")
             })
   (d/span {} (if correct? "Yay!" (when (not (nil? correct?)) "Try Again!")))))

(q/defcomponent Line [game idx {:keys [a b] :as question} game-channel]
  (d/div {}
         (d/span {} (str (inc idx) ". What is " a " + " b " = ") (Answer game idx question game-channel))))

(q/defcomponent Game [{:keys [questions] :as game} game-channel]
  (d/div {}
         (apply d/div {:className "board"}
                (map-indexed (fn [idx q] (Line game idx q game-channel)) questions))
         (d/div {}
                (d/button {:onClick (fn [e]
                                      (.preventDefault e)
                                      (put! game-channel {:type :reset}))} "Reset"))))

(defn render-game [game container game-channel]
  (q/render (Game game game-channel) container))

