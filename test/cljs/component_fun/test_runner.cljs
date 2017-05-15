(ns component-fun.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [component-fun.core-test]
   [component-fun.common-test]))

(enable-console-print!)

(doo-tests 'component-fun.core-test
           'component-fun.common-test)
