(ns app.main.core
  (:require ["electron" :refer [app BrowserWindow crashReporter ipcMain]]
            ["path" :as path]))

(defonce main-window nil)

(defn create-window []
  (set! main-window (BrowserWindow.
                     (clj->js {:width 800
                               :height 600
                               :webPreferences {:nodeIntegration true
                                                ;; :contextIsolation true
                                                ;; :enableRemoteModule false
                                                :preload (path/join js/__dirname "preload.js")}
                               ;; :transparent true
                               :backgroundColor "#dddddd"})))

  (.on ipcMain "set-title"
       (fn [event title]
         (doto
             (.fromWebContents BrowserWindow (.-sender event))
             (.setTitle title))))

  (.loadFile main-window (path/join js/__dirname "public/index.html"))

  (.on BrowserWindow main-window "closed" #(set! main-window nil))
  ;; (node:17988) UnhandledPromiseRejectionWarning: TypeError: shadow.js.shim.module$electron.BrowserWindow.on is not a function
  )

(defn main []

  (-> (.whenReady app)
      (.then (fn []
               (create-window)
               (.on app "activate" #(when (zero? (.length (.getAllWindows BrowserWindow)))
                                      (create-window))))))

  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app))))

;; (ns app.main.core
;;   (:require #_["electron-devtools-installer" :refer [installExtension REACT_DEVELOPER_TOOLS REDUX_DEVTOOLS]]
;;             ["electron" :refer [app BrowserWindow crashReporter]]))
;;
;; (def main-window (atom nil))
;;
;; (defn init-browser []
;;   (reset! main-window (BrowserWindow.
;;                        (clj->js {:width 800
;;                                  :height 600
;;                                  :webPreferences
;;                                  {:nodeIntegration true}})))
;;                                         ; Path is relative to the compiled js file (main.js in our case)
;;   #_(.setBackgroundColor ^js/electron.BrowserWindow @main-window "blueviolet")
;;   (.loadURL ^js/electron.BrowserWindow @main-window (str "file://" js/__dirname "/public/index.html"))
;;   (.on ^js/electron.BrowserWindow @main-window "closed" #(reset! main-window nil))
;;   #_(doto (installExtension REDUX_DEVTOOLS)
;;     (.then #(js/console.log (str "Added Extension: " %)))
;;     (.catch #(js/console.log (str "An error occurred: " %)))))
;;
;; (defn main []
;;   (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
;;                                   (.quit app)))
;;   (.on app "ready" init-browser))
