(ns dptab
  (:require [clojure.core.matrix :as m]
            [clj-latex.core :as l]))

(println
 (l/render-latex
   (:documentclass 'article)
   (:usepackage 'tikz)
   (:usepackage 'amsmath)
   (:newcommand :rn [2]
                (list
                  (:tikz ["remember picture", "baseline=(#1.base)"])
                  (:node ["inner sep=0"]) "(#1) {$#2$};"))
   ('document
     (let [[rows cols] [8 8]
           g (fn [i j] (if (= i j)
                         1
                         -1))
           tab (loop [tab (vec (repeat rows (vec (repeat cols 0))))
                      ijs (drop 1 (for [i (range rows)
                                        j (range cols)]
                                    [i j]))]
                 (if-let [[i j] (first ijs)]
                   (let [get-in-tab #(get-in tab %& ##-Inf)
                         v-top (+ (get-in-tab (dec i) j) -3)
                         v-left (+ (get-in-tab i (dec j)) -3)
                         v-topleft (+ (get-in-tab (dec i) (dec j)) (g i j))]
                     (recur (assoc-in tab [i j] (max v-top v-left v-topleft))
                            (next ijs)))
                   tab))
           tab (m/emap-indexed (fn [[i j] elem]
                                 (format "\\rn{%d%d}{%s}" i j elem)) tab) ]
       (list
         (l/matrix 'matrix tab)
         ('tikzpicture [['overlay, "remember picture"]]
                       (for [i (range (- rows 1))]
                         (list (:draw ['->]) (format "(%d%d)--(%d%d);" i i (inc i) (inc i))))))))))
