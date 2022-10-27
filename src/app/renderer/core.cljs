(ns app.renderer.core
  (:require
   [debux.cs.core :as debux :refer-macros [dbg dbgn break]]
   [cljs.pprint :refer [pprint cl-format]]
   [cljs.repl :as repl]
   [clojure.string :as string]
   [cognitect.transit :as t]
   ["react" :as react]
   ["react-dom/client" :as rdom]))

(def devtools-error-formatter
  "Uses cljs.repl utilities to format ExceptionInfo objects in Chrome devtools console."
  #js{:header
      (fn [object _config]
        (when (instance? ExceptionInfo object)
          (let [message (some->> (repl/error->str object)
                                 (re-find #"[^\n]+"))]
            #js["span" message])))
      :hasBody (constantly true)
      :body (fn [object _config]
              #js["div" (repl/error->str object)])})
(defonce _
  (some-> js/window.devtoolsFormatters
          (.unshift devtools-error-formatter)))

(enable-console-print!)

(debux.cs.core/set-cljs-devtools! true)

(defonce root (rdom/createRoot (js/document.getElementById "app-container")))

(defonce global-app-state (atom (or {}
                                    (->> (.getItem (.-localStorage js/window) "pages")
                                         (t/read (t/reader :json))))))

(defn my-app
  [{:keys [title]}]
  (react/createElement "div" nil
                       title
                       (react/createElement "input"
                                            (clj->js {:type "text"
                                                      :value (or title "")
                                                      :onInput (fn [e]
                                                                 (let [text (.. e -target -value)]
                                                                   (.setTitle js/window.electronAPI text)
                                                                   (swap! global-app-state assoc :title text)))}))))

(.render root
         (react/createElement
          (fn function-component []
            (let [[state set-state] (react/useState @global-app-state)]
              (react/useEffect (fn []
                                 (add-watch global-app-state :app-state-use-state (fn [_ _ _ n] (set-state n)))
                                 (fn [] (remove-watch global-app-state :app-state-use-state)))
                               #js[])
              (->> state
                   (t/write (t/writer :json))
                   (.setItem (.-localStorage js/window) "pages"))
              (case (.-name js/window)
                "debug" (my-app state)
                (my-app state))))
          nil))


(comment (.open js/window "http://localhost:8080" "debug"))
