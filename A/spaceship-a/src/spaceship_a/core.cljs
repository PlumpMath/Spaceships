(ns spaceship-a.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(defn log [& args]
  (.log js/console (apply str args)))

(defn pulse [low high rate]
  (let [diff (- high low)
        half (/ diff 2)
        mid (+ low half)
        s (/ (q/millis) 1000.0)
        x (q/sin (* s (/ 1.0 rate)))]
    (+ mid (* x half))))

(def images (atom {}))

(defn load-image [k name]
  (swap! images (fn [image-atom] (assoc image-atom k (q/load-image name)))))

(defn log-loaded-images []
  (doseq [[k img] @images]
    (log k " " (if (nil? img) "nil" "OK"))))

(defn draw-image [k [x y]]
  (q/image (get @images k) x y))

(defn random-star [speed]
  [(rand-int (* 2 (q/width)))
   (rand-int (q/height))
   (* speed (+ 1.0 (rand 3.0)))])

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :rgb)
  (q/image-mode :center)
  (load-image :body "Body.png")
  (load-image :bg "Bg.jpg")
  (load-image :star "Star.png")
  (load-image :anchor "Anchor.png")
  (load-image :antenna "Antenna.png")
  (load-image :engine "Engine.png")
  (load-image :cupola "Cupola.png")
  (load-image :flag0 "Flag1.png")
  (load-image :flag1 "Flag2.png")
  (doall (map load-image [:fire0 :fire2 :fire1 :fire3] ["Fire1.png" "Fire2.png" "Fire3.png" "Fire4.png"]))
  {:stars  (take 25 (repeatedly #(random-star 6.0)))
   :stars2 (take 25 (repeatedly #(random-star 3.0)))
   :fade 1.2}
  )

(defn wrap [[x y v]]
  (if (< x -500)
    [(q/width) (rand-int (q/height)) v]
    [x y v]))

(defn move-star [[x y v]]
  (wrap [(- x v) y v]))

(defn update [state]
  (-> state
      (update-in [:fade] #(if (> % 0.0) (- % 0.02) %))
      (update-in [:stars] #(map move-star %))
      (update-in [:stars2] #(map move-star %))))

(defn animated-keyword [base-name n speed]
  (let [s (* speed (/ (q/millis) 1000.0))
        x (mod (int s) n)]
    (keyword (str base-name x))))

(defn draw-stars [stars]
  (doseq [[x y v] stars]
    (q/push-matrix)
    (q/translate x y)
    (q/scale (* 0.05 v))
    (draw-image :star [0 0])
    (q/pop-matrix)
    ))

(defn draw [state]
  (let [w (q/width)
        h (q/height)
        hw (/ w 2)
        hh (/ h 2)]
    (draw-image :bg [hw hh])
    (draw-stars (:stars2 state))
    (q/push-matrix)
    (q/translate (+ hw (pulse -20 20 3.0)) (+ hh (pulse -10 10 0.25)))
    (q/rotate (pulse -0.02 0.02 0.5))
    (draw-image (animated-keyword "fire" 4 10.0) [-300 64])
    (q/with-translation [145 20]
      (q/with-rotation [(pulse 0.12 -0.12 1.0)]
        (draw-image :antenna [50 0])))
    (q/with-translation [-160 (pulse 67 74 0.20)]
      (draw-image :cupola [160 -235]))
    (draw-image :body [0 0])
    (q/with-translation [-175 75]
      (q/with-rotation [(pulse 0.1 -0.1 0.25)]
        (draw-image :anchor [0 35])))
    (q/with-translation [-72 4]
      (q/with-rotation [(* 0.01 (q/millis))]
        (draw-image :engine [0 0])))
    (q/with-translation [-72 4]
      (draw-image (animated-keyword "flag" 2 10.0) [-100 -53]))
    (q/pop-matrix)
    (draw-stars (:stars state))
    (q/fill 255 (* 255 (:fade state)))
    (q/rect -1 -1 (+ 2 w) (+ 2 h)))
  )

(q/defsketch spaceship-a
  :host "spaceship-a"
  :size [1100 700]
  :setup setup
  :update update
  :draw draw
  :middleware [m/fun-mode])

