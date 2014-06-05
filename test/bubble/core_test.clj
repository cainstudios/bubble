(ns bubble.core-test
  (:refer-clojure :exclude [pop])
  (:require [clojure.test :refer :all]
            [clojure.set :as set]
            [bubble.core :refer :all]))

(deftest t-blow-once
  (testing "creates ns accessible with bubble/through"
    (let [bubble (init)]
      (try
        (blow bubble
              '[[(ns bubble.t)
                 (def works :yes)]])
        (is (= :yes
               (some-> (through bubble 'bubble.t/works)
                       deref)))
        (finally
          (pop bubble)))))
  (testing "cleans up"
    (let [bubble (init)
          count-all (comp count all-ns)
          before (count-all)]
      (blow bubble '[[(ns bubble.t)]])
      (is (= (inc before) (count-all)))
      (pop bubble)
      (is (= before (count-all)))))
  (testing "through doesn't work after pop"
    (let [bubble (init)]
      (blow bubble '[[(ns bubble.t)
                      (def x 1)]])
      (is (= 1 (some-> (through bubble 'bubble.t/x)
                       deref)))
      (pop bubble)
      (is (nil? (through bubble 'bubble.t/x))))))

(deftest t-blow-more
  (let [blow-once #(blow %1 [['(ns bubble.t)
                              (list 'def 'x %2)]])]
    (testing "remove old ns"
      (let [bubble (init)
            all (comp set all-ns)
            before (all)
            first-diff (atom ())]
        (try
          (blow-once bubble :x)
          (reset! first-diff (set/difference (all) before))
          (blow-once bubble :y)
          (let [second-diff (set/difference (all) before)]
            (is (= 1 (count second-diff)))
            (is (not= @first-diff second-diff)))
          (finally
            (pop bubble)))))))
