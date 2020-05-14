# lein-replace

Transform your leiningen project with replace expressions.

``` clojure
:plugin [[exoscale/lein-replace "0.1.0"]]
```

## Motivation

Repositories which create several artifacts usually need to
reference the same information several times in a single
project. The most common occurence of this is the project's
version.

`lein-replace` allows avoiding these repetitive calls by
providing a middleware which parses replacement expressions
and applies them to the project.

## Usage

By default, `lein-replace` will replace any occurrence of `:version`
in either `:dependencies` or `:managed-dependencies`.

To disable this behavior, set `:replace-version?` to false in the
project map.

Additional replacements may be provided in the `:replace-expressions`
key. This key is expected to be a collection of maps.

### Replace expression

Each replace expression is map of `:replacements` and `:paths`:

``` clojure
{:replacements {:group1-version [:versions :group1]
                :group2-version [:versions :group2]}
 :paths        [:dependencies :managed-dependencies]}
```

- `:replacements` is a map of keyword to replace to path in the project.
  paths may be either keys or a nested path (as for `get-in`).
- `:paths` instructs where to do the replacement in the project map and
  is expected to be a collection of paths. paths can also be either
  keys or nested paths.

Copyright © 2020 Exoscale
