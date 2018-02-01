(ns star-realms.core
  (:gen-class)
  (:require [clojure.core.async :as async :refer [>!! <!! go <! close!]]))

(defrecord Card [name cost income damage heal])
(defn scout [] (Card.   "SCOUT"   1 1 0 0))
(defn fighter [] (Card. "FIGHTER" 1 0 1 0))
(defn warrior [] (Card. "WARRIOR" 3 0 2 0))
(defn healer [] (Card.  "HEALER"  2 0 0 1))

(defn card2s
  [card]
  (format "%s (cost: %s, income: %s, damage: %s, heal: %s)" (:name card)
          (:cost card) (:income card) (:damage card) (:heal card)))

(defn print-card [card] (println (card2s card)))

(defn create-deck
  "generates shuffled deck"
  []
  (shuffle (concat
             (take 30 (repeat (scout)))
             (take 30 (repeat (fighter)))
             (take 20 (repeat (warrior)))
             (take 20 (repeat (healer))))))

(defn take-cards
  "takes n cards from the top"
  [n deck]
  (split-at n deck))

(defn game-over?
  [game]
  (<= (count (:deck game)) 0))

(def hand (list (warrior) (scout) (healer) (scout) (fighter)))
(defn take-payment
  [hand price]
  (loop [taken '()
         hand' (reverse (sort-by :income hand))]
    (if (>= (reduce (partial +) (map :income taken)) price)
      [taken, hand']
      (recur (take 1 hand') (drop 1 hand')))))
(comment
  (take-payment hand 1))

(defn disp-trade
  [trader marketplace chan]
  (println "trade stage...")
  (println "MARKETPLACE:")
  (doseq [card marketplace] (print-card card))
  (println "TRADER HAND:")
  (doseq [card (trader :hand)] (print-card card)))

(defn game
  []
  (println "starting new game")
  (let [deck (create-deck)
        [hand-1 deck] (take-cards 5 deck)
        [hand-2 deck] (take-cards 5 deck)
        [marketplace deck] (take-cards 5 deck)
        game-chan (async/chan)]
    (go
      (loop [game {:deck deck
                   :attacker {:hand hand-1
                              :name "P1"
                              :health 50
                              :reserve '()}
                   :defender {:hand hand-2
                              :name "P2"
                              :health 50
                              :reserve '()}
                   :marketplace marketplace
                   :chan game-chan}]
        (println (str "deck size: " (count (:deck game))))
        (if (game-over? game)
          (do
            (println "GAME OVER")
            (close! game-chan))
          (do

            (println (str ((game :attacker) :name) "s turn"))

            (disp-trade (game :attacker) marketplace game-chan)
            (let [buy-command (<! game-chan)
                  card-to-buy (first (filter #(= (:name %) (:name buy-command)) (game :marketplace)))
                  price (:cost card-to-buy)]
              (println (str "BUY: " (card2s card-to-buy)))
              (let [[taken hand'] (take-payment ((game :attacker) :hand) price)]
                (recur (assoc game :attacker (game :defender)
                                   :defender (assoc (game :attacker) :hand hand'
                                                                     :reserve (conj taken card-to-buy))
                                   :deck (second (take-cards 30 (game :deck)))))))))))
      game-chan))

(comment
  (def g (game))
  (>!! g {:name "SCOUT"}))

(defn -main
  []
  (println "ok..."))
