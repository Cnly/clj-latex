# clj-latex

clj-latex is a library for representing LaTeX code in Clojure, inspired by
[hiccup](https://github.com/weavejester/hiccup/) and
[cl-latex](https://github.com/wadoon/cl-latex).

## Installation

`project.clj` dependency:

```clojure
[clj-latex "0.1.0"]
```

## Usage

The easiest way to produce a LaTeX document with clj-latex is to use the
`render-latex` macro in `clj-latex.core`:

```clojure
(use 'clj-latex.core)
(render-latex
  (:documentclass 'article)
  (:usepackage ['utf8] "inputenc")
  :myshortcmd)
```
```latex
\documentclass{article}
\usepackage[utf8]{inputenc}
\myshortcmd
```

`(:documentclass ...` is a list starting with a keyword, so is transformed into
a LaTeX command by the `render-latex` macro (This means you cannot use the
syntax `(:keyword elem)` inside the macro to get something out from `elem`).
Each element that follows becomes an argument for that command. If the element
is in a vector, it becomes an optional argument for that command (like
`[utf8]`); otherwise it's a required argument (like `{article}`).

During the rendering, elements that are not strings already are converted
automatically:
- `'quoted` => `"quoted"`
- `123` => `"123"`
- `\c` => `"c"`
- `[[[["flattened"]]]]` => `"flattened"` (So your code can produce nested lists,
  etc.)
- `:keywords-become-commands` => `"\keywords"-become-commands` (A shortcut for
  writing LaTeX commands)

```clojure
(render-latex
  (:documentclass 'article)
  ('document
   :noindent
   "Lorem ipsum."
   ('table [['ht]]
           ('tabular ['|c|c|c|]
                     :hline
                     (tr 1 2 3)
                     :hline))))
```
```latex
\documentclass{article}
\begin{document}
  \noindent
  Lorem ipsum.
  \begin{table}[ht]
    \begin{tabular}{|c|c|c|}
      \hline
      1 & 2 & 3 \\
      \hline
    \end{tabular}
  \end{table}
\end{document}
```

`('document ...` is a list starting with a quoted symbol, so is transformed into
an environment block (This means you cannot use the syntax `('kw elem)` to get
something out of `elem`). If the first element in an environment is a vector, it
specifies the arguments for the environment (thus optional arguments will be in
a vector inside that vector):
- `('tabular ['|c|c|c|] ...` => `\begin{tabular}{|c|c|c|} ...`
- `('table [['ht]]` => `\begin{table}[ht] ...`
- `('my-env [['opt-arg1 'opt-arg2] 'arg1 'arg2] ...` => `\begin{my-env}[opt-arg1, opt-arg2]{arg1}{arg2} ...`

Note that all arguments for environments must be passed to them in a vector as
the first element after the environment name, while arguments for commands don't
need to be enclosed in such a vector, because commands don't have bodies.

In the above example, `tr` is a utility provided by clj-latex to ease the
creation of **t**able **r**ows. The library provides a few functions like this:
- `(j 1 "joined" 2)` => `"1 joined 2"`
- `(blk "A" "Block")` => `("{" "  A" "  Block" "}")`
- `(b "smaller" "block")` => `("{smaller block}")`
- `(tr 1 2 3)` => `"1 & 2 & 3 \\\\"`
- `($ "inline" 'math "mode")` => `"$inline math mode$"`
- `(esc "\\escaped" "$trings")` => `("\\\\escaped" "\\$trings")`
- `(matrix [[1 2] [3 4]])` => A `bmatrix` environment with the matrix contents
  (An alternative environment name can also be passed to the function as the
  first argument.)
- The symbols `br`, `$$`, `&` are defined as `"\\\\"`, `"$$"`, `\&`,
  respectively.

For more examples, see the `examples/` and `test/` directories.

## Options

The library has two dynamic definitions for indentation control and can be
adjusted as needed:

```clojure
(def ^:dynamic indent-width 2)
(def ^:dynamic indent-unit " ")
```

For example, to use tab instead of spaces, you may use:

```clojure
(binding [indent-width 1
          indent-unit "\t"]
  (render-latex
    ...))
```

## License

The MIT License.
