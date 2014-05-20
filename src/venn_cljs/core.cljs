(ns venn-cljs.core
  (:require [venn-cljs.math :as mc]))

(def link-prefix "/?")

(def circles [{:name "one" :color "#FF0000" :opacity 0.5 :cx 360 :cy 200 :r 200}
              {:name "two" :color "#00FF00" :opacity 0.5 :cx 200 :cy 360 :r 200}
              {:name "three" :color "#0000FF" :opacity 0.5 :cx 520 :cy 360 :r 200}
              {:name "four" :color "#7F7F7F" :opacity 0.7 :cx 360 :cy 520 :r 200}])

(def width 720)
(def height 720)

(defn combinations
  "All possible combinations of the set of input items, choosing
   1, ..., n from the total set"
  [items]
  (apply concat
         (for [n (range 1 (inc (count items)))]
           (mc/combinations items n)
           )))

(defn parse-color
  "Parse hex color string into decimal clj map"
  [color opacity]
  {:r (js/parseInt (subs color 1 3) 16)
   :g (js/parseInt (subs color 3 5) 16)
   :b (js/parseInt (subs color 5 7) 16)
   :opacity opacity})

(defn padded-hex
  "Returns a string of exactly two hexidecimal digits for given decimal value"
  [decimal]
  (if (> decimal 255)
    "ff" ;Naively return ff if the decimal is too large

    (let [hex (.toString (int decimal) 16)]
      (if (< (count hex) 2)
        (str "0" hex)
        hex))))

(defn hex-color
  "Turns a decimal cljs color map back into a hex string"
  [color]
  (str "#"
       (padded-hex (:r color))
       (padded-hex (:g color))
       (padded-hex (:b color))))

(defn color-pair
  "Calculate the color resulting from applying overlay to base"
  [base overlay]
  (let [opacity (:opacity overlay)]
    {:r (+
         (* (- 1 opacity) (:r base))
         (* opacity (:r overlay)))

     :g (+
         (* (- 1 opacity) (:g base))
         (* opacity (:g overlay)))

     :b (+
         (* (- 1 opacity) (:b base))
         (* opacity (:b overlay)))

     :opacity 1
     }))

(defn defcircle!
  "Define a circular clipping path in the svg container specified"
  [container circle]
  (let [{:keys [name color opacity cx cy r]} circle]
    (-> container
        (.append "clipPath")
        (.attr "id" name)
        (.append "circle")
        (.attr "cx" cx)
        (.attr "cy" cy)
        (.attr "r" r))))

(defn add-clipping!
  "Add a circular clipping path to the specified container element"
  [container circle]
  (let [{:keys [name]} circle]
    (-> container
        (.append "g")
        (.attr "clip-path" (str "url(#" name ")"))
        )))

(defn render-shape!
  "Render an independent shape based on the overlaps of the list of circles specified, and attach to container"
  [container circles]
  (let [url (apply str (for [{:keys [name]} circles] (str "field=" name ";")))
        colors (map #(apply parse-color %) (for [{:keys [color opacity]} circles] (list color opacity)))
        link (-> container
                 (.append "a")
                 (.attr "xlink:href" (str link-prefix url)))
        shape (reduce add-clipping! link circles)
        color (hex-color (reduce color-pair (first colors) colors))
        rect (-> shape
                 (.append "rect")
                 (.attr "width" width)
                 (.attr "height" height)
                 (.style "fill" color))]

    (if (= (count circles) 1)
      ;Opacity change on outer shapes
      (-> shape
          (.on "mouseover" #(.style link "opacity" 0.5))
          (.on "mouseout" #(.style link "opacity" 1)))

      ;Absolute change on inner shapes
      (-> shape
          (.on "mouseover" #(.style rect "fill"
                                    (hex-color (color-pair (parse-color "#ffffff" 1) (parse-color color 0.3)))))
          (.on "mouseout" #(.style rect "fill" color))))
    ))

(defn run!
  "Code to run once on page load, applying functions to DOM"
  []
  (let
    [displaysize (* 0.8 (min (.-innerWidth js/window) (.-innerHeight js/window)))

     svg ;Base SVG element, attached to the DOM by ID
     (-> js/d3
         (.select "#venn")
         (.append "svg")
         (.attr "viewBox" (str 0 " " 0 " " width " " height))
         (.attr "width" displaysize)
         (.attr "height" displaysize))

     defs ;Definitions container for the base clipping paths
     (-> svg (.append "defs"))]

    (doseq
      ;Add each circular clipping path to the defs container
      [circle circles]
      (defcircle! defs circle))

    (doseq
      ;Render every possible overlap of the circles as a separate shape
      [circle-combination (combinations circles)]
      (render-shape! svg circle-combination))))

(run!)
