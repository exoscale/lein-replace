(ns lein-replace.plugin
  "A leiningen plugin which installs a middleware to allow referencing
   project elements in coordinate definitions.")

(def version-replace
  "The default replace expression. To disable this replacement,
   set `:version-replace?` to false in your project. Additional
   replacements may be added to `:replace-expressions` in the project."
  {:replacements {:version :version}
   :paths        [:dependencies :managed-dependencies]})

(defn make-path
  "Return a path valid for `get-in`"
  [dst]
  (if (sequential? dst) dst [dst]))

(defn get-path
  "Get a path, where a path can be a collection or a key."
  [project v]
  (get-in project (make-path v)))

(defn replace-in-coord
  "Given a built replace map and a coordinate description,
   replace any occurence of patterns in `replace-map` in coord,
   preserving its original form."
  [replace-map coord]
  (into (empty coord) (replace replace-map) coord))

(defn replace-in-path
  "Process a single path from a replace expression."
  [replace-map project path]
  (let [coords          (get-path project path)
        transform-coord (partial replace-in-coord replace-map)]
    (update-in project
               (make-path path)
               (partial into (empty coords) (map transform-coord)))))

(defn build-replace-map
  "Creates a replacement map suitable for `replace`."
  [project replacements]
  (reduce-kv (fn [m k v] (assoc m k (get-path project v)))
             {}
             replacements))

(defn replace-with-expr
  "Process a single replacement expression. Each replacement
   expression is a map of two keys:

   - `:replacements`: A map of keyword to path in the project.
     the key will be the expression to replace when found in the
     project, the value, a path to fetch in the project with
     `get-path`.
  - `:paths`: A collection of paths "
  [project {:keys [replacements paths]}]
  (let [replace-map (build-replace-map project replacements)]
    (->> (filter (comp some? (partial get-path project)) paths)
         (reduce (partial replace-in-path replace-map) project))))

(defn middleware
  "A leiningen middleware to allow self-references. By default,
   replaces any occurences of `:version` in the `:dependencies`
   and `:managed-dependencies` keys by the actual project version.

   This default replacement can be disabled by setting `:replace-version?`
   to false, additional replacements can be specified in
   `:replace-expressions`."
  [{:keys [replace-expressions replace-version?]
    :or   {replace-version?    true
           replace-expressions []}
    :as   project}]
  (reduce replace-with-expr project
          (cond-> replace-expressions replace-version? (conj version-replace))))
