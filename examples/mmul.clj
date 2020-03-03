(ns mmul
  (:require [clojure.core.matrix :as m]
            [clojure.pprint :as pp]
            [clj-latex.core :as l]))

(println
 (l/render-latex
  (:documentclass ["12pt"] 'article)
  (:usepackage 'amsmath)
  ('document
   (:noindent)
   (let [a [[1 2]
            [3 4]]
         b [[2 0]
            [0 2]]
         ans-src `(m/mmul ~a ~b)
         ans (m/emap int (eval ans-src))]
     (list
      "The product of the matrices"
      l/$$
      (:mathbf \A) '= (l/matrix a) (:text " and ") (:mathbf \B) '= (l/matrix b)
      l/$$

      "is"

      l/$$
      (:mathbf "AB") '= (l/matrix ans) \.
      l/$$

      "This result can be produced using the following Clojure source code (with clojure.core.matrix):"
      ('verbatim
       (l/esc (prn-str ans-src))))))))
