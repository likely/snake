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
  (add-watch !game ::renderer
             (fn [_ _ _ {:keys [my-id clients apples]}]
               (clear-board! board)
               (doseq [snake (-> clients
                                 (dissoc my-id)
                                 vals
                                 (->> (map :snake)))]
                 (render-snake! board snake "black"))
               
               (render-snake! board (get-in clients [my-id :snake]) "blue")

               (doseq [apple apples]
                 (render-apple! board apple)))))

(defn bind-commands! [board model-command-ch]
  ;; TODO business-logic commands to be put onto model-command-ch
  
  )

(defn make-board-widget [!game model-command-ch]
  (let [board (doto (canvas-board-component)
                (watch-game! !game)
                (bind-commands! model-command-ch)
                (focus!))]

    (reset! !game
            {:clients {"0b9a9cf8-abd2-47e0-b241-4de37312edde"
                       {:snake [[10 4] [10 5] [10 6]],
                        :direction :up},
    
                       "2f594c2a-123e-4352-98a5-7e9621da9ec2"
                       {:snake [[16 26] [17 26]],
                        :direction :up}},

             :my-id "2f594c2a-123e-4352-98a5-7e9621da9ec2"

             :apples (set [[11 22] [24 9] [7 3] [34 0] [0 28] [18 17] [30 34] [13 13] [6 13] [4 13]])})
    
    (board->node board)))
