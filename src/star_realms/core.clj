(ns star-realms.core
  (:gen-class))

(defrecord Card [name cost income damage heal])
(defn scout [] (Card. "SCOUT" 1 1 0 0))
(defn fighter [] (Card. "FIGHTER" 1 0 1 0))
(defn warrior [] (Card. "WARRIOR" 3 2 0 0))
(defn healer [] (Card. "HEALER" 2 0 0 1))

(defn print-card
  [card]
  (println (format "%s (cost: %s, income: %s, damage: %s, heal: %s)" (:name card)
                   (:cost card) (:income card) (:damage card) (:heal card))))

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

(defn trade
  [trader marketplace]
  (println "trade stage...")
  (println "MARKETPLACE:")
  (doseq [card marketplace] (print-card card)))

(defn turn
  [attacker defender game]
  (println (str attacker "s turn"))
  (trade (game attacker) (game :marketplace)))

(defn game
  []
  (println "starting new game")
  (let [deck (create-deck)
       [player-1 deck] (take-cards 5 deck)
       [player-2 deck] (take-cards 5 deck)
       [marketplace deck] (take-cards 5 deck)]
    (loop [game {:deck deck
                 :player-1 player-1
                 :player-2 player-2
                 :marketplace marketplace}]
      (println (str "deck size: " (count (:deck game))))
      (when (not (game-over? game))
        (turn :player-1 :player-2 game)
        (turn :player-2 :player-1 game)
        (recur (update game :deck #(drop 100 %)))))))

(comment
  (game)
  (take-cards 10 (create-deck)))

(defn -main
  []
  (println "ok..."))
