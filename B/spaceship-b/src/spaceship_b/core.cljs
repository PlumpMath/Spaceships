(ns spaceship-b.core
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

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :rgb)
  (q/image-mode :center)
  (load-image :body "Body.png")
  (load-image :bg "Bg.jpg")
  (load-image :star "Star.png")
  (load-image :tail "Tail.png")
  (load-image :head "Head.png")
  (load-image :flag "Flag.png")
  (doall (map load-image [:fire0 :fire2 :fire1 :fire3] ["Fire1.png" "Fire2.png" "Fire3.png" "Fire4.png"]))  
  {:stars  (take 25 (repeatedly #(random-star 6.0)))
   :stars2 (take 25 (repeatedly #(random-star 3.0)))
   :fade 1.2}
  )

(defn draw [state]
  (let [w (q/width)
        h (q/height)
        hw (/ w 2)
        hh (/ h 2)]
    (draw-image :bg [hw hh])

    ;; Background layer of stars
    (draw-stars (:stars2 state))

    ;; Ship
    (q/push-matrix)
    (q/translate (+ hw #_(pulse -20 20 3.0)) (+ hh #_(pulse -10 10 0.25)))
    ;;(q/rotate (pulse -0.02 0.02 0.5))

    (q/with-translation [0 -75]
      (q/with-rotation [(pulse 0.1 -0.01 0.5)]
        (draw-image :tail [-150 80])
        (q/with-translation [-92 -32]
          (q/with-rotation [(pulse 0.15 -0.15 0.5)]
            (draw-image :flag [0 -40])))))

    (draw-image :body [150 0])

    (q/with-translation [(pulse 275.0 280.0 0.2) 96]
      (draw-image :head [0 0]))    
    
    (q/pop-matrix)

    ;; Front layer of stars
    (draw-stars (:stars state))

    ;; Fade
    (q/fill 255 (* 255 (:fade state)))
    (q/rect -1 -1 (+ 2 w) (+ 2 h)))
  )

(q/defsketch spaceship-b
  :host "spaceship-b"
  :size [1100 700]
  :setup setup
  :update update
  :draw draw
  :middleware [m/fun-mode])
