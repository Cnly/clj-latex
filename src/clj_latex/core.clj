(ns clj-latex.core
  (:require
   [clojure.string :as str]
   [clojure.walk :as w])
  (:gen-class))

(def ^:dynamic indent-width 2)
(def ^:dynamic indent-unit " ")
(defn indented [s]
  (str (str/join (repeat indent-width indent-unit)) s))

(defn sym->str [x]
  (cond
    (keyword? x)
    (str \\ (name x))

    (instance? clojure.lang.Named x)
    (name x)

    (coll? x)
    (if (next x)
      (throw (IllegalArgumentException. "Don't know how to handle coll of count > 1"))
      (sym->str (first x)))

    :else
    (str x)))

(defn flatten-strify [body]
  (->> (flatten body)
       (map sym->str)))

(defn esc [& strs]
  (map #(str/escape % (->> [\& \% \$ \# \_ \{ \} \~ \^ \\] ; LaTeX special chars
                           (mapcat (fn [ch] {ch (str \\ ch)}))
                           (into {})))
       (flatten-strify strs)))

(defn cmd [cmd-name & opts-and-args]
  (loop [opts-and-args opts-and-args
         result (list (sym->str cmd-name))]
    (if (nil? opts-and-args)
      result
      (let [opt-or-arg (first opts-and-args)
            opt-or-arg (if (coll? opt-or-arg)
                         opt-or-arg
                         (list opt-or-arg))
            add-to-last-str #(concat (butlast result) [(apply str (last result) %&)])]
        (if (vector? opt-or-arg) ; Optional args
          (recur (next opts-and-args) (add-to-last-str \[ (str/join ", " (flatten-strify opt-or-arg)) \]))
          (recur (next opts-and-args) (if (next opt-or-arg)
                                        (concat (add-to-last-str \{)
                                              (concat (map indented (flatten-strify opt-or-arg))
                                                      [\}]))
                                        (add-to-last-str \{ (sym->str opt-or-arg) \}))))))))

(defmulti env (fn [_name & opts-args-contents] (vector? (first opts-args-contents))))
(defmethod env true [env-name opts-and-args & contents]
  (concat (apply cmd :begin env-name opts-and-args)
          (map indented (flatten-strify contents))
          (cmd :end env-name)))
(defmethod env false [env-name & contents]
  (apply env env-name [] contents))

(defn j [& body]
  (str/join " " (flatten-strify body)))

(defn blk [& body]
  (cmd "" (flatten-strify body)))

(defn b [& body]
  (blk (j body)))

(defn tr [& row-body]
  (->> (flatten-strify row-body)
       (str/join " & ")
       (#(str % " \\\\"))))

(defn $ [& body]
  (str \$ (apply j body) \$))

(def $$ "$$")
(def br "\\\\")
(def & \&)

(defn matrix
  ([m]
   (matrix 'bmatrix m))
  ([env-name m]
   (env env-name []
        (map (partial apply tr) m))))

(defmacro latex [& body]
  `(list ~@(map (partial w/postwalk
                         (fn [form]
                           (cond
                             (and (seq? form) (seq? (first form)) (= 'quote (first (first form))))
                             `(env ~@form)
                             (and (seq? form) (keyword? (first form)))
                             `(cmd ~@form)
                             :else
                             form)))
                body)))

(defmacro render-latex [& body]
  `(str/join "\n" (flatten-strify (latex ~@body))))
