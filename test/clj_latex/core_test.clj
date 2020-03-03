(ns clj-latex.core-test
  (:require [clojure.test :refer :all]
            [clj-latex.core :refer :all]))

(defmacro gen-test-def [form]
  `(is (= '~(eval form)
          ~form)))

(deftest basic-tests
  (binding [indent-width 2
            indent-unit " "]
    (testing "sym->str"
      (is (= '("123" "quoted" "\\cmd" "str" "c" "single elem coll")
             (map sym->str [123 'quoted :cmd "str" \c '("single elem coll")]))))
    (testing "flatten-strify"
      (is (= '("1" "2" "3" "4" "\\cmd" "quoted" "c" "str")
             (flatten-strify [1 2 [[[3 '(4)]]] :cmd 'quoted \c "str"]))))
    (testing "escaping"
      (is (= '("Not a \\\\LaTeX command!" "\\&\\%\\$\\#\\_\\{\\}\\~\\^abcd1234")
             (esc "Not a \\LaTeX command!" "&%$#_{}~^abcd1234"))))
    (testing "cmd generic names"
      (is (= '("\\cmd-name")
             (cmd :cmd-name)))
      (is (= '("")
             (cmd "")))
      (is (= '("cmd-name")
             (cmd 'cmd-name)))
      (is (= '("\\cmd-name")
             (cmd '(((:cmd-name)))))))
    (testing "cmd optional and required args"
      (is (= '("\\cmd[\\cmd-in-opt, quoted-in-opt, text in opt]{quoted-required}[another-opt]")
             (cmd :cmd
                  [:cmd-in-opt 'quoted-in-opt "text in opt"]
                  'quoted-required
                  ["another-opt"]))))
    (testing "cmd complex args"
      (is (= '("\\cmd{" "  line 1" "  line 2" \})
             (cmd :cmd '("line 1" "line 2")))))
    (testing "cmd nested"
      (is (= '("\\outer{\\inner{inner-arg}}{outer-arg}")
             (cmd :outer (cmd :inner 'inner-arg) 'outer-arg))))
    (testing "env"
      (is (= '("\\begin[required-arg, \\opt-arg]"
               "\\end[required-arg, \\opt-arg]")
             (env ['required-arg [:opt-arg]])))
      (is (= '("\\begin{\\cmd}" "  123" "  quoted" "  c" "  str" "\\end{\\cmd}")
             (env :cmd 123 'quoted \c "str")))
      (is (= '("\\begin{document}" "  line 1" "  line 2" "\\end{document}")
             (env 'document
                  "line 1"
                  "line 2")))
      (is (= '("\\begin{document}" "  \\begin{abstract}" "    Testing" "  \\end{abstract}" "\\end{document}")
             (env 'document
                  (env 'abstract
                       "Testing"))))
      (is (= '("\\begin{document}" "  \\command" "  c" "  str" "  123" "  should be flattened" "\\end{document}")
             (env 'document
                  :command \c "str" 123
                  [[[[["should be flattened"]]]]]))))
    (testing "utilities"
      (is (= '"1 2 3 nested \\cmd quoted"
             (j 1 2 3 [[["nested"] :cmd 'quoted]])))
      ($ "inline" 'math "mode")
      (is (= '("{" "  Block" "  Test" \})
             (blk "Block" "Test")))
      (is (= '("{small block test}")
             (b "small" "block" "test")))
      (is (= '"table & row & test & 1 & 2 & 3 \\\\"
             (tr 'table 'row 'test [[[1 2 3]]])))
      (is (= '"$\\lambda + 1 = 2$"
             ($ :lambda '+ 1 '= 2)))
      (is (= '("\\begin{bmatrix}"
               "  1 & 2 \\\\"
               "  3 & 4 \\\\"
               "\\end{bmatrix}")
             (matrix [[1 2]
                      [3 4]])))
      (is (= '("\\begin{tabular}{|c|c|c|}"
               "  \\hline"
               "  1 & 2 & 3 \\\\"
               "  \\hline"
               "\\end{tabular}")
             (env 'tabular ['|c|c|c|]
                  :hline
                  (tr 1 2 3)
                  :hline))))
    (testing "latex macros"
      (is (= (list
              :testcmd
              'test-quoted
              (clj-latex.core/cmd :documentclass 'article)
              (clj-latex.core/env 'document (clj-latex.core/env 'tabular)))
             (latex
              :testcmd 'test-quoted
              (:documentclass 'article)
              ('document
               ('tabular)))))
      (is (= "\\testcmd\ntest-quoted\n\\documentclass{article}\n\\begin{document}\n  \\begin{tabular}\n  \\end{tabular}\n\\end{document}"
             (render-latex
              :testcmd 'test-quoted
              (:documentclass 'article)
              ('document
               ('tabular)))))
      (is (= '"\\begin{document}\n  This is nested\n\\end{document}\nOuter"
             (render-latex
               (render-latex
                 ('document
                   "This is nested"))
               "Outer"))))
    (testing "comprehensive"
      (is (= "\\documentclass{article}\n\\usepackage[utf8]{inputenc}\n\\begin{document}\n  \\begin{abstract}\n    This paper describes foo, a bar that barzes, bazzing buzz.\n  \\end{abstract}\n  \\maketitle\n  \\section{Introduction}\n  Foo\n  \\cite{foo}\n  is a bar that barzes, bazzing buzz\n  \\cite{buzz}\n  .\n  \\begin{figure}[ht]\n    \\begin{center}\n      \\includegraphics[scale=1.0]{zu.eps}\n    \\end{center}\n    \\caption{Caption}\n    \\ecaption{ECaption}\n    \\label{fig:zu}\n  \\end{figure}\n  \\forall\n  x\n  \\in\n  \\mathbb\n  R\n  $$\n  1 + 1 = \n  2\n  $$\n  \\begin{align}\n    1 + 1 &= 2 \\\\\n    2 + 2 & = 4 ^ {1.000}\n  \\end{align}\n  \\frac{a+b}{2}\n  \\textbf{\\textif{Test}}\n  \\bf{\n    \\begin{center}\n      Test\n    \\end{center}\n  }\n  \\widetilde{\\mathbf{R}}\n  =\n  \\mathbf{R}\n  -\n  \\hat{\\mathbf{R}}\n  \\noindent{This and that}\n  $\\lambda + 1 = 2$\n  \\begin{tabular}{|c|c|c|}\n    \\hline\n    1 & 2 & 3 \\\\\n    \\hline\n    \\a & b & c \\\\\n    \\hline\n    0 & 1 & 2 \\\\\n  \\end{tabular}\n  \\begin{bmatrix}\n    1 & 2 \\\\\n    3 & 4 \\\\\n  \\end{bmatrix}\n  {\n    Block\n    Test\n  }\n\\end{document}"
             (render-latex
              (:documentclass 'article)
              (:usepackage ['utf8] 'inputenc)
              ('document
               ('abstract
                "This paper describes foo, a bar that barzes, bazzing buzz.")
               (:maketitle)
               (:section 'Introduction)
               "Foo" (:cite 'foo) "is a bar that barzes, bazzing buzz" (:cite 'buzz) \.
               ('figure [['ht]]
                        ('center
                         (:includegraphics ['scale=1.0] 'zu.eps))
                        (:caption 'Caption)
                        (:ecaption "ECaption")
                        (:label 'fig:zu))
               :forall \x :in :mathbb \R
               $$ "1 + 1 = " (+ 1 1) $$
               ('align
                (j 1 '+ 1 '&= 2 br)
                (j 2 '+ 2 & '= 4 \^ (b "1.000")))
               (:frac "a+b" 2)
               (:textbf (:textif "Test"))
               (:bf ('center
                     "Test"))
               (:widetilde (:mathbf \R)) '= (:mathbf \R) '- (:hat (:mathbf \R))
               (:noindent '("This and that"))
               ($ :lambda '+ 1 '= 2)
               ('tabular ['|c|c|c|]
                         :hline
                         (tr 1 2 3)
                         :hline
                         (tr :a \b "c")
                         :hline
                         (apply tr (range 3)))
               (matrix [[1 2]
                        [3 4]])
               (blk
                "Block"
                "Test"))))))))
