(ns snake.cljs.board-widget
  (:require [snake.board :as b]
            [cljs.core.async :as a]
            [dommy.core :as d]
            [goog.events.KeyCodes :as kc])
  (:require-macros [dommy.macros :refer [node sel1]]
                   [cljs.core.async.macros :refer [go]]))

(defprotocol BoardComponent
  (board->node [_])
  (focus! [_])

  (render-snake! [_ cells color])
  (render-apple! [_ cell])
  (clear-board! [_]))

(def key->command
  {kc/UP :up
   kc/DOWN :down
   kc/LEFT :left
   kc/RIGHT :right})

(defn color-cells! [$canvas cells color]
  (let [ctx (.getContext $canvas "2d")]
    (set! (.-fillStyle ctx) color)
    (doseq [[x y] cells]
      (doto ctx
        (.fillRect (* x b/block-size-px)
                   (* y b/block-size-px)
                   b/block-size-px
                   b/block-size-px)))))

(defn clear-canvas! [$canvas]
  (let [ctx (.getContext $canvas "2d")]
    (doto ctx
      (.clearRect 0 0
                  (* b/board-size b/block-size-px)
                  (* b/board-size b/block-size-px)))))

(defn canvas-board-component []
  (let [canvas-size (* b/block-size-px b/board-size)
        $canvas (node [:canvas {:height canvas-size
                                :width canvas-size
                                :style {:border "1px solid black"}
                                :tabindex 0}])]
    (reify BoardComponent
      (board->node [_]
        (node
         [:div {:style {:margin-top "5em"}}
          $canvas]))
      (focus! [_]
        (go
         (a/<! (a/timeout 200))
         (.focus $canvas)))

      (render-snake! [_ cells color]
        (color-cells! $canvas cells color))

      (render-apple! [_ cell]
        (color-cells! $canvas [cell] "#d00"))

      (clear-board! [_]
        (clear-canvas! $canvas)))))

(defn watch-game! [board !game]
  ;; TODO changes to !game to be reflected on screen
  
  )

(defn bind-commands! [board model-command-ch]
  ;; TODO business-logic commands to be put onto model-command-ch
  
  )

(defn make-board-widget [!game model-command-ch]
  (let [board (doto (canvas-board-component)
                (watch-game! !game)
                (bind-commands! model-command-ch)
                (focus!))]

    (render-snake! board [[4 5] [4 6] [4 7]] "blue")
    (render-apple! board [10 10])
    (clear-board! board)
    
    (board->node board)))
