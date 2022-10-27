(ns app.preload.core
  (:require ["electron" :refer [contextBridge ipcRenderer]]))

(defn main []
  (.exposeInMainWorld contextBridge "electronAPI"
                      (clj->js {:setTitle (fn [title]
                                            (.send ipcRenderer "set-title" title))})))
