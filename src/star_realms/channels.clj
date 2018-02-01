(ns star_realms.channels
  (:require [clojure.core.async :as async
             :refer [chan >!! <!! go <! close!]]))

(def echo-chan (chan))
(go (println (<! echo-chan)))
(>!! echo-chan "Hello, world!")

(defn game2
  []
  (let [game-chan (chan)]
    (go (loop [game {:state :active}]
          (if (= :active (game :state))
            (do
              (println "WAITING FOR YOUR COMMAND...")
              (let [command (<! game-chan)]
                (println (str "COMMAND: " command))
                (recur (assoc game :state (keyword command)))))
            (do
              (println "GAME OVER")
              (close! game-chan)))))
    game-chan))

(def gc (game2))
(>!! gc "activeq")
